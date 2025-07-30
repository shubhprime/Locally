package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.NearestDriverRequest;
import com.locally.locally_delivery_partner.dto.NearestDriverResponse;
import com.locally.locally_delivery_partner.dto.UpdateLocationRequest;
import com.locally.locally_delivery_partner.dto.UpdateLocationResponse;
import com.locally.locally_delivery_partner.service.LocationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private RedisTemplate<String, Object> geoRedisTemplate;

    private static final String LOCATION_KEY = "delivery_locations";

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Override
    public UpdateLocationResponse updateDeliveryPartnerLocation(UpdateLocationRequest updateLocationRequest) {

        String freshnessKey = "location_last_updated:" + updateLocationRequest.getDeliveryPartnerId();

        if (updateLocationRequest.getDeliveryPartnerId() == null) {
            throw new IllegalArgumentException("DeliveryPartnerId is null");
        }

        geoRedisTemplate.opsForGeo().add(LOCATION_KEY, new Point(updateLocationRequest.getLongitude(), updateLocationRequest.getLatitude()), updateLocationRequest.getDeliveryPartnerId().toString());

        geoRedisTemplate.opsForValue().set(freshnessKey, "fresh", 2, java.util.concurrent.TimeUnit.MINUTES);

        logger.info("Updated location for deliveryPartnerId: {}", updateLocationRequest.getDeliveryPartnerId());

        return UpdateLocationResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Location updated successfully")
                .build();
    }

    @Override
    public List<NearestDriverResponse> getNearestDeliveryPartners(NearestDriverRequest nearestDriverRequest) {

        logger.info("Searching for delivery partners near ({}, {}) within {} miles. Limit: {}",
                nearestDriverRequest.getLongitude(), nearestDriverRequest.getLatitude(), nearestDriverRequest.getRadiusMile(), nearestDriverRequest.getLimit());

        Circle area = new Circle(new Point(nearestDriverRequest.getLongitude(), nearestDriverRequest.getLatitude()), new Distance(nearestDriverRequest.getRadiusMile(), Metrics.MILES));

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                geoRedisTemplate.opsForGeo().radius(LOCATION_KEY, area,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(nearestDriverRequest.getLimit()));

        logger.info("Found {} nearby delivery partners", results.getContent().size());

        return results.getContent().stream()
                .filter(result -> {
                    String id = result.getContent().getName().toString();
                    String freshnessKey = "location_last_updated:" + id;
                    String statusKey = "delivery_status:" + id;

                    Boolean isFresh = geoRedisTemplate.hasKey(freshnessKey);
                    Object status = geoRedisTemplate.opsForValue().get(statusKey);

                    return Boolean.TRUE.equals(isFresh) && !"ON_DELIVERY".equals(status);
                })
                .map(result -> {
                    String id = result.getContent().getName().toString();
                    double distance = result.getDistance().getValue();

                    return NearestDriverResponse.builder()
                            .responseCode("200")
                            .success(true)
                            .responseMessage("Driver found")
                            .deliveryPartnerId(id)
                            .distanceInMiles(distance)
                            .build();
                })
                .collect(Collectors.toList());

    }

    @Override
    public Point getLastKnownLocation(Long deliveryPartnerId) {

        String redisKey = deliveryPartnerId.toString();

        List<Point> points = geoRedisTemplate.opsForGeo().position(LOCATION_KEY, redisKey);

        if (points == null || points.isEmpty() || points.get(0) == null) {
            throw new RuntimeException("No location found for deliveryPartnerId: " + deliveryPartnerId);
        }

        return points.get(0);
    }
}
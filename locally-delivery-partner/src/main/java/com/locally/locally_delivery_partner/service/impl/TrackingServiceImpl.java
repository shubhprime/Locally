package com.locally.locally_delivery_partner.service.impl;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.model.DeliveryPartner;
import com.locally.locally_delivery_partner.repository.DeliveryPartnerRepository;
import com.locally.locally_delivery_partner.service.TrackingService;
import com.locally.locally_delivery_partner.utils.DeliveryPartnerUtils;
import com.locally.locally_delivery_partner.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrackingServiceImpl implements TrackingService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private RedisUtil redisUtil;

    private static final String DELIVERY_REQUEST_PREFIX = "delivery_request:";
    private static final String DELIVERY_ACCEPTED_PREFIX = "delivery_accepted:";

    /**
     * Checks if a delivery offer is available for a delivery partner.
     * Looks up the delivery ID in Redis based on the partner's ID.
     *
     * @param getDeliveryOfferRequest - contains deliveryPartnerId
     * @return a response indicating if an offer is available or not
     */
    @Override
    public GetDeliveryOfferResponse getDeliveryOffer(GetDeliveryOfferRequest getDeliveryOfferRequest) {

        String deliveryIdStr = redisUtil.get(DELIVERY_REQUEST_PREFIX + getDeliveryOfferRequest.getDeliveryPartnerId());

        if (deliveryIdStr != null) {
            return GetDeliveryOfferResponse.builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Offer available")
                    .deliveryId(Long.valueOf(deliveryIdStr))
                    .build();
        }

        return GetDeliveryOfferResponse.builder()
                .responseCode("204")
                .success(false)
                .responseMessage("No delivery offer available")
                .build();
    }

    /**
     * Sends a delivery offer to a delivery partner.
     * Stores the offer in Redis with a 20-second expiry.
     *
     * @param deliveryOfferRequest - contains deliveryId and deliveryPartnerId
     * @return response confirming the offer was sent
     */
    @Override
    public TrackingResponse sendOffer(DeliveryOfferRequest deliveryOfferRequest) {
        redisUtil.setWithExpiry(
                DELIVERY_REQUEST_PREFIX + deliveryOfferRequest.getDeliveryPartnerId(),
                deliveryOfferRequest.getDeliveryId().toString(),
                20 // expires in 20 seconds
        );

        System.out.println("➡️  Redis SET: Key = " + DELIVERY_REQUEST_PREFIX + deliveryOfferRequest.getDeliveryPartnerId()
                + ", Value = " + deliveryOfferRequest.getDeliveryId());

        return TrackingResponse.builder()
                .responseCode("200")
                .success(true)
                .responseMessage("Offer sent to delivery partner")
                .deliveryId(deliveryOfferRequest.getDeliveryId())
                .deliveryPartnerId(deliveryOfferRequest.getDeliveryPartnerId())
                .build();
    }

    /**
     * Accepts a delivery offer if it was actually sent to this driver.
     * Checks if the delivery offer exists in Redis and matches.
     * If matched, stores the acceptance with a 60-second TTL.
     *
     * @param acceptDeliveryRequest - contains email and deliveryId
     * @return response indicating success/failure of acceptance
     */
    @Override
    public TrackingResponse acceptDelivery(AcceptDeliveryRequest acceptDeliveryRequest) {

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmailAndIsDeletedFalse(acceptDeliveryRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(DeliveryPartnerUtils.DELIVERY_PARTNER_NOT_FOUND));

        acceptDeliveryRequest.setDeliveryPartnerId(deliveryPartner.getId());

        String expectedDeliveryId = redisUtil.get(DELIVERY_REQUEST_PREFIX + deliveryPartner.getId());

        System.out.println("⬅️  Redis GET: Key = " + DELIVERY_REQUEST_PREFIX + deliveryPartner.getId()
                + ", Fetched Value = " + expectedDeliveryId
                + ", Accepting = " + acceptDeliveryRequest.getDeliveryId());

        if (expectedDeliveryId != null && expectedDeliveryId.equals(acceptDeliveryRequest.getDeliveryId().toString())) {
            redisUtil.setWithExpiry(
                    DELIVERY_ACCEPTED_PREFIX + acceptDeliveryRequest.getDeliveryId(),
                    deliveryPartner.getId().toString(),
                    60 // hold acceptance for 60 seconds
            );

            return TrackingResponse.builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Delivery accepted successfully")
                    .deliveryId(acceptDeliveryRequest.getDeliveryId())
                    .deliveryPartnerId(deliveryPartner.getId())
                    .build();
        }

        return TrackingResponse.builder()
                .responseCode("400")
                .success(false)
                .responseMessage("No delivery was offered to this driver")
                .deliveryId(acceptDeliveryRequest.getDeliveryId())
                .deliveryPartnerId(deliveryPartner.getId())
                .build();
    }

    /**
     * Checks if any delivery partner has accepted the given delivery.
     * Looks up the accepted delivery ID in Redis.
     *
     * @param checkAcceptedRequest - contains deliveryId
     * @return response with delivery partner ID if accepted
     */
    @Override
    public CheckAcceptedResponse checkAccepted(CheckAcceptedRequest checkAcceptedRequest) {
        String acceptedDriverId = redisUtil.get(DELIVERY_ACCEPTED_PREFIX + checkAcceptedRequest.getDeliveryId());

        if (acceptedDriverId != null) {
            return CheckAcceptedResponse.builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Delivery accepted")
                    .deliveryId(checkAcceptedRequest.getDeliveryId())
                    .deliveryPartnerId(Long.valueOf(acceptedDriverId))
                    .build();
        }

        return CheckAcceptedResponse.builder()
                .responseCode("404")
                .success(false)
                .responseMessage("No driver has accepted the delivery yet")
                .deliveryId(checkAcceptedRequest.getDeliveryId())
                .build();
    }
}
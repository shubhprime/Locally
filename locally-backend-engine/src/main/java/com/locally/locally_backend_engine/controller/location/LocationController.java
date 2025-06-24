package com.locally.locally_backend_engine.controller.location;

import com.locally.locally_backend_engine.dto.NearestDriverRequest;
import com.locally.locally_backend_engine.dto.UpdateLocationRequest;
import com.locally.locally_backend_engine.dto.UpdateLocationResponse;
import com.locally.locally_backend_engine.dto.NearestDriverResponse;
import com.locally.locally_backend_engine.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engine/v1/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/update")
    public ResponseEntity<UpdateLocationResponse> updateLocation(@Valid @RequestBody UpdateLocationRequest updateLocationRequest) {
        UpdateLocationResponse updateLocationResponse = locationService.updateLocation(updateLocationRequest);

        return ResponseEntity.ok(updateLocationResponse);
    }

    @PostMapping("/nearest")
    public ResponseEntity<List<NearestDriverResponse>> getNearestDrivers(@RequestBody NearestDriverRequest nearestDriverRequest) {
        List<NearestDriverResponse> drivers = locationService.getNearestDrivers(nearestDriverRequest);

        return ResponseEntity.ok(drivers);
    }
}
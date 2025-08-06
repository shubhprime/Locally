package com.locally.locally_delivery_partner.controller.vehicle;

import com.locally.locally_delivery_partner.dto.*;
import com.locally.locally_delivery_partner.service.VehicleService;
import com.locally.locally_delivery_partner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-partner/v1/vehicle")
public class VehicleController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/create-vehicle")
    public ResponseEntity<AppResponse> createVehicle(@RequestBody CreateVehicleRequest createVehicleRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        createVehicleRequest.setEmail(email);

        AppResponse appResponse = vehicleService.createVehicle(createVehicleRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(appResponse);
    }

    @GetMapping("/show-list")
    public ResponseEntity<List<VehicleResponse>> listVehicles(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        VehicleListRequest vehicleListRequest = new VehicleListRequest(email);

        List<VehicleResponse> vehicles = vehicleService.listVehicles(vehicleListRequest);

        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("/update-vehicle/{vehicleId}")
    public ResponseEntity<AppResponse> updateVehicle(@PathVariable Long vehicleId, @RequestBody UpdateVehicleRequest updateVehicleRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        updateVehicleRequest.setEmail(email);
        updateVehicleRequest.setVehicleId(vehicleId);

        AppResponse appResponse = vehicleService.updateVehicle(updateVehicleRequest);

        return ResponseEntity.status(HttpStatus.OK).body(appResponse);
    }

    @DeleteMapping("/delete-vehicle/{vehicleId}")
    public  ResponseEntity<AppResponse> deleteVehicle(@PathVariable Long vehicleId, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        AppResponse appResponse = vehicleService.deleteVehicle(vehicleId, email);

        return ResponseEntity.status(HttpStatus.OK).body(appResponse);
    }
}
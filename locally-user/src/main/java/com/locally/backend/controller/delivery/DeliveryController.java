package com.locally.backend.controller.delivery;

import com.locally.backend.dto.DeliveryRequest;
import com.locally.backend.dto.DeliveryResponse;
import com.locally.backend.dto.GetUserDeliveryResponse;
import com.locally.backend.dto.UpdatedDeliveryRequest;
import com.locally.backend.service.DeliveryService;
import com.locally.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/user")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create-delivery")
    public ResponseEntity<DeliveryResponse> createDelivery(@RequestBody DeliveryRequest deliveryRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        DeliveryResponse response = deliveryService.createDelivery(deliveryRequest, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-user-delivery")
    public ResponseEntity<List<GetUserDeliveryResponse>> getAllUserDeliveries(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        List<GetUserDeliveryResponse> deliveries = deliveryService.getAllDeliveriesForUser(email);
        return ResponseEntity.ok(deliveries);
    }

    @PutMapping("/update-delivery/{id}")
    public ResponseEntity<DeliveryResponse> updateDelivery(@PathVariable Long id, @RequestBody UpdatedDeliveryRequest updatedDeliveryRequest, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        DeliveryResponse response = deliveryService.updateDelivery(id, updatedDeliveryRequest, email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/cancel-delivery/{id}")
    public ResponseEntity<DeliveryResponse> cancelDelivery(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.retrieveSubject(token);

        DeliveryResponse response = deliveryService.cancelDelivery(id, email);
        return ResponseEntity.ok(response);
    }
}
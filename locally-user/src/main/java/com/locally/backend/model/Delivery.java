package com.locally.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String pickUpAddress;

    @Column(nullable = false)
    private String dropOffAddress;

    @Column(nullable = false)
    private String packageDetails;

    @Column(nullable = false)
    private String typeOfDelivery;

    @Column(nullable = false)
    private String deliveryStatus; // PENDING, ASSIGNED, IN_TRANSIT, DELIVERED, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_delivery_partner_id")
    private User assignedDeliveryPartner;

    @Column(nullable = false)
    private double deliveryFee;

    @Column(nullable = false)
    private double deliveryPartnerTravelFee;

    @Column(nullable = false)
    private double totalFee;

    @Column(nullable = false)
    private double distanceInMiles;

    @Column(nullable = false)
    private boolean isPaid;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime modifiedAt;
    private LocalDateTime cancelledAt;
}
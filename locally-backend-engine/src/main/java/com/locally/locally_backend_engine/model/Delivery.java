package com.locally.locally_backend_engine.model;

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
    private Long deliveryId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String pickUpAddress;

    @Column(nullable = false)
    private String dropOffAddress;

    @Column(nullable = false)
    private String packageDetails;

    @Column(nullable = false)
    private String typeOfDelivery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus; // PENDING, ASSIGNED, IN_TRANSIT, DELIVERED, CANCELLED, FAILED

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "assigned_delivery_partner_id")
    private Long assignedDeliveryPartnerId;

    @Column(nullable = false)
    private double deliveryFee;

    @Column(nullable = false)
    private double deliveryPartnerTravelFee;

    @Column(nullable = false)
    private double totalFee;

    @Column(nullable = false)
    private double distanceInMiles;

    @Column(nullable = true)
    private  int rating;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean userHasRated;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deliveryPartnerHasRated;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isPaid;

    @Column(nullable = true)
    private String cancellationReason;

    @Column(nullable = true)
    private String failureReason;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime modifiedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime inTransitAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime failedAt;
}
package com.locally.locally_backend_engine.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery_assignments")
public class DeliveryAssignment {

    @Id
    private Long deliveryId;
    @Column
    private Long deliveryPartnerId;
    @Column
    private LocalDateTime assignedAt;
    @Column
    private boolean completed;
}
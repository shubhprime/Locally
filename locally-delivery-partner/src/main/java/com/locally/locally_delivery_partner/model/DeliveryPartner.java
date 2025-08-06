package com.locally.locally_delivery_partner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = true)
    private String alternatePhoneNumber;

    @Column(nullable = true)
    private String accountHolderName;

    @Column(nullable = true)
    private String bankName;

    @Column(nullable = true)
    private String routingNumber;

    @Column(nullable = true, unique = true)
    private String bankAccountNumber;

    @Column(nullable = true)
    private String accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_of_delivery", nullable = false)
    private ModeOfDelivery modeOfDelivery;

    @JsonIgnore
    @OneToMany(mappedBy = "deliveryPartner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleDetails> vehicleDetails;

    @Column(nullable = false)
    private String governmentIdType;
    @Column(nullable = false, unique = true)
    private String governmentId;

    @Column(nullable = false)
    private double averageRating = 0.0;

    @Column(nullable = false)
    private int totalRatings = 0;

    @Column(columnDefinition = "TEXT")
    private String profilePictureBase64;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "role", nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean isVerified;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime modifiedAt;
}
package com.locally.backend.repository;

import com.locally.backend.model.Address;
import com.locally.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user);

    Optional<Address> findByIdAndUserAndIsDeletedFalse(Long id, User user);

    Optional<Address> findByUserAndIsDefaultTrueAndIsDeletedFalse(User user);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user AND a.isDeleted = false")
    void clearDefaultAddressForUser(@Param("user") User user);

    long countByUserAndIsDeletedFalse(User user);
}
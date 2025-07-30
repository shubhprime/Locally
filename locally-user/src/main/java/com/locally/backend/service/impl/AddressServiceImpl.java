package com.locally.backend.service.impl;

import com.locally.backend.dto.AddressListResponse;
import com.locally.backend.dto.AddressRequest;
import com.locally.backend.dto.AddressResponse;
import com.locally.backend.dto.AppResponse;
import com.locally.backend.model.Address;
import com.locally.backend.model.User;
import com.locally.backend.repository.AddressRepository;
import com.locally.backend.repository.UserRepository;
import com.locally.backend.service.AddressService;
import com.locally.backend.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public AppResponse<Void> createAddress(String email, AddressRequest addressRequest) {
        try {
            User user = userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            // If this is the first address or marked as default, make it default
            boolean shouldBeDefault = addressRequest.getIsDefault() != null && addressRequest.getIsDefault();
            long existingAddressCount = addressRepository.countByUserAndIsDeletedFalse(user);

            if (existingAddressCount == 0) {
                shouldBeDefault = true; // First address is always default
            }

            // If setting as default, clear existing default
            if (shouldBeDefault) {
                addressRepository.clearDefaultAddressForUser(user);
            }

            Address address = Address.builder()
                    .label(addressRequest.getLabel())
                    .fullAddress(addressRequest.getFullAddress())
                    .houseUnit(addressRequest.getHouseUnit())
                    .instructions(addressRequest.getInstructions())
                    .isDefault(shouldBeDefault)
                    .user(user)
                    .isDeleted(false)
                    .build();

            Address savedAddress = addressRepository.save(address);

            return AppResponse.<Void>builder()
                    .responseCode("201")
                    .success(true)
                    .responseMessage("Address created successfully")
                    .build();

        } catch (Exception e) {
            return AppResponse.<Void>builder()
                    .responseCode("500")
                    .success(false)
                    .responseMessage("Failed to create address: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public AppResponse<Void> updateAddress(String email, Long addressId, AddressRequest addressRequest) {
        try {
            User user = userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(addressId, user)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            // Update fields
            if (addressRequest.getLabel() != null) {
                address.setLabel(addressRequest.getLabel());
            }
            if (addressRequest.getFullAddress() != null) {
                address.setFullAddress(addressRequest.getFullAddress());
            }
            if (addressRequest.getHouseUnit() != null) {
                address.setHouseUnit(addressRequest.getHouseUnit());
            }
            if (addressRequest.getInstructions() != null) {
                address.setInstructions(addressRequest.getInstructions());
            }

            // Handle default setting
            if (addressRequest.getIsDefault() != null && addressRequest.getIsDefault()) {
                addressRepository.clearDefaultAddressForUser(user);
                address.setIsDefault(true);
            }

            addressRepository.save(address);

            return AppResponse.<Void>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Address updated successfully")
                    .build();

        } catch (Exception e) {
            return AppResponse.<Void>builder()
                    .responseCode("500")
                    .success(false)
                    .responseMessage("Failed to update address: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public AppResponse<Void> deleteAddress(String email, Long addressId) {
        try {
            User user = userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(addressId, user)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            boolean wasDefault = address.getIsDefault();

            // Soft delete the address
            address.setIsDeleted(true);
            address.setDeletedAt(LocalDateTime.now());
            address.setIsDefault(false);
            addressRepository.save(address);

            // If deleted address was default, set another address as default
            if (wasDefault) {
                List<Address> remainingAddresses = addressRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user);
                if (!remainingAddresses.isEmpty()) {
                    Address newDefault = remainingAddresses.get(0);
                    newDefault.setIsDefault(true);
                    addressRepository.save(newDefault);
                }
            }

            return AppResponse.<Void>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Address deleted successfully")
                    .build();

        } catch (Exception e) {
            return AppResponse.<Void>builder()
                    .responseCode("500")
                    .success(false)
                    .responseMessage("Failed to delete address: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public AppResponse<Void> setDefaultAddress(String email, Long addressId) {
        try {
            User user = userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(addressId, user)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            // Clear existing default
            addressRepository.clearDefaultAddressForUser(user);

            // Set new default
            address.setIsDefault(true);
            addressRepository.save(address);

            return AppResponse.<Void>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Default address updated successfully")
                    .build();

        } catch (Exception e) {
            return AppResponse.<Void>builder()
                    .responseCode("500")
                    .success(false)
                    .responseMessage("Failed to set default address: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AddressListResponse getUserAddresses(String email) {
        try {
            User user = userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            List<Address> addresses = addressRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user);

            List<AddressResponse> addressResponses = addresses.stream()
                    .map(this::convertToAddressResponse)
                    .collect(Collectors.toList());

            return AddressListResponse.builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Addresses retrieved successfully")
                    .addresses(addressResponses)
                    .totalCount(addressResponses.size())
                    .build();

        } catch (Exception e) {
            return AddressListResponse.builder()
                    .responseCode("500")
                    .success(false)
                    .responseMessage("Failed to retrieve addresses: " + e.getMessage())
                    .addresses(List.of())
                    .totalCount(0)
                    .build();
        }
    }

    @Override
    public AppResponse<AddressResponse> getAddressById(String email, Long addressId) {
        try {
            User user = userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new RuntimeException(AccountUtils.USER_NOT_FOUND));

            Address address = addressRepository.findByIdAndUserAndIsDeletedFalse(addressId, user)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            AddressResponse addressResponse = convertToAddressResponse(address);

            return AppResponse.<AddressResponse>builder()
                    .responseCode("200")
                    .success(true)
                    .responseMessage("Address retrieved successfully")
                    .data(addressResponse)
                    .build();

        } catch (Exception e) {
            return AppResponse.<AddressResponse>builder()
                    .responseCode("404")
                    .success(false)
                    .responseMessage("Address not found: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    private AddressResponse convertToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .fullAddress(address.getFullAddress())
                .houseUnit(address.getHouseUnit())
                .instructions(address.getInstructions())
                .isDefault(address.getIsDefault())
                .build();
    }
}
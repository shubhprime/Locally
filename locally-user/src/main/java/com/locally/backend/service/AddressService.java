package com.locally.backend.service;

import com.locally.backend.dto.AddressListResponse;
import com.locally.backend.dto.AddressRequest;
import com.locally.backend.dto.AppResponse;

public interface AddressService {
    AppResponse createAddress(String email, AddressRequest addressRequest);
    AppResponse updateAddress(String email, Long addressId, AddressRequest addressRequest);
    AppResponse deleteAddress(String email, Long addressId);
    AppResponse setDefaultAddress(String email, Long addressId);
    AddressListResponse getUserAddresses(String email);
    AppResponse getAddressById(String email, Long addressId);
}
package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

public interface DeliveryPartnerService {
    public DeliveryPartnerResponse createDeliveryPartner(CreateDeliveryPartnerRequest createDeliveryPartnerRequest);

    public DeliveryPartnerResponse uploadProfilePicture(UploadProfilePictureRequest uploadProfilePictureRequest);

    public DeliveryPartnerResponse updateDeliveryPartnerProfile(UpdateDeliveryPartnerRequest updateDeliveryPartnerRequest);

    public DeliveryPartnerResponse loginDeliveryPartner(LoginRequest loginRequest);

    public DeliveryPartnerResponse logoutDeliveryPartner(RefreshTokenRequest logoutRequest);

    public DeliveryPartnerResponse deleteAccount(DeleteDeliveryPartnerRequest deleteDeliveryPartnerRequest);

    public DeliveryPartnerResponse sendVerificationOtp(SendVerificationOtpRequest sendVerificationOtpRequest);

    public DeliveryPartnerResponse sendVerificationOtp(String email);

    public DeliveryPartnerResponse verifyVerificationOtp(OtpVerificationRequest otpVerificationRequest);
}
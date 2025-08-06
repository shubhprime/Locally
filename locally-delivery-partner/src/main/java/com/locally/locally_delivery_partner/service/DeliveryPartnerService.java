package com.locally.locally_delivery_partner.service;

import com.locally.locally_delivery_partner.dto.*;

public interface DeliveryPartnerService {
    AppResponse<Void> createDeliveryPartner(CreateDeliveryPartnerRequest createDeliveryPartnerRequest);

    AppResponse<Void> uploadProfilePicture(UploadProfilePictureRequest uploadProfilePictureRequest);

    AppResponse<DeliveryPartnerData> updateDeliveryPartnerProfile(UpdateDeliveryPartnerRequest updateDeliveryPartnerRequest);

    AppResponse<DeliveryPartnerData> loginDeliveryPartner(LoginRequest loginRequest);

    AppResponse<Void> logoutDeliveryPartner(RefreshTokenRequest logoutRequest);

    AppResponse<Void> deleteAccount(DeleteDeliveryPartnerRequest deleteDeliveryPartnerRequest);

    AppResponse<Void> sendVerificationOtp(SendVerificationOtpRequest sendVerificationOtpRequest);

    AppResponse<Void> sendVerificationOtp(String email);

    AppResponse<Void> verifyVerificationOtp(OtpVerificationRequest otpVerificationRequest);
}
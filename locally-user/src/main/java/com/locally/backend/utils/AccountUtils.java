package com.locally.backend.utils;

public class AccountUtils {
    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final boolean ACCOUNT_EXISTS_SUCCESS = false;
    public static final String ACCOUNT_EXISTS_MESSAGE = "A User by this email already exists. Please use a different email ID.";

    public static final String USER_NOT_FOUND = "User Not Found. You Need To Sign Up First.";

    public static final String ACCOUNT_CREATION_CODE = "002";
    public static final boolean ACCOUNT_CREATION_SUCCESS = true;
    public static final String ACCOUNT_CREATION_MESSAGE = "User Created Successfully.";

    public static final String USER_ROLE_NOT_FOUND = "USER role not found.";

    public static final String ACCOUNT_ALREADY_VERIFIED_CODE = "003";
    public static final boolean ACCOUNT_ALREADY_VERIFIED_SUCCESS = false;
    public static final String ACCOUNT_ALREADY_VERIFIED_MESSAGE = "User is already verified.";

    public static final String VERIFY_OTP_FAILED_CODE = "004";
    public static final boolean VERIFY_OTP_FAILED_SUCCESS = false;
    public static final String VERIFY_OTP_FAILED_MESSAGE = "Invalid Request Method.";

    public static final String VERIFICATION_OTP_SENT_CODE = "005";
    public static final boolean VERIFICATION_OTP_SENT_SUCCESS = true;
    public static final String VERIFICATION_OTP_SENT_MESSAGE = "Verification OTP sent to the user's email.";

    public static final String ACCOUNT_IS_VERIFIED_FAILED_CODE = "006";
    public static final boolean ACCOUNT_IS_VERIFIED_FAILED_SUCCESS = false;
    public static final String ACCOUNT_IS_VERIFIED_FAILED_MESSAGE = "Email not verified. Please check your inbox.";

    public static final String EMAIL_VERIFICATION_SUCCESS_CODE = "007";
    public static final boolean EMAIL_VERIFICATION_SUCCESS_SUCCESS = true;
    public static final String EMAIL_VERIFICATION_SUCCESS_MESSAGE = "Email verified successfully.";

    public static final String PICTURE_SAVED_CODE = "008";
    public static final boolean PICTURE_SAVED_SUCCESS = true;
    public static final String PICTURE_SAVED_MESSAGE = "Profile picture saved in DB.";

    public static final String PICTURE_FILE_NOT_FOUND = "No picture file provided.";
    public static final String PICTURE_FILE_TOO_LARGE = "File size too large. Max 2MB allowed.";
    public static final String ERROR_READING_FILE = "Error reading file.";

    public static final String USER_PROFILE_UPDATE_CODE = "009";
    public static final boolean USER_PROFILE_UPDATE_SUCCESS = true;
    public static final String USER_PROFILE_UPDATE_MESSAGE = "Profile Updated Successfully.";

    public static final String ACCOUNT_LOGIN_CODE = "010";
    public static final boolean ACCOUNT_LOGIN_SUCCESS = true;
    public static final String ACCOUNT_LOGIN_MESSAGE = "User Successfully Logged In.";

    public static final String ACCOUNT_INCORRECT_PASSWORD_CODE = "011";
    public static final boolean ACCOUNT_INCORRECT_PASSWORD_SUCCESS = false;
    public static final String ACCOUNT_INCORRECT_PASSWORD_MESSAGE = "Incorrect Password.";

    public static final String LOGOUT_USER_CODE = "012";
    public static final boolean LOGOUT_USER_SUCCESS = true;
    public static final String LOGOUT_USER_MESSAGE = "User Logged Out.";

    public static final String LOGOUT_USER_FAILED_CODE = "013";
    public static final boolean LOGOUT_USER_FAILED_SUCCESS = false;
    public static final String LOGOUT_USER_FAILED_MESSAGE = "Refresh token is missing or invalid.";

    public static final String ACCOUNT_SOFT_DELETED_CODE = "014";
    public static final boolean ACCOUNT_SOFT_DELETED_SUCCESS = true;
    public static final String ACCOUNT_SOFT_DELETED_MESSAGE = "Account soft deleted successfully.";

    public static final String REFRESH_TOKEN_EXPIRED_CODE = "015";
    public static final boolean REFRESH_TOKEN_EXPIRED_SUCCESS = false;
    public static final String REFRESH_TOKEN_EXPIRED_MESSAGE = "Refresh Token Expired.";

    public static final String INVALID_REFRESH_TOKEN_CODE = "016";
    public static final boolean INVALID_REFRESH_TOKEN_SUCCESS = false;
    public static final String INVALID_REFRESH_TOKEN_MESSAGE = "Invalid Refresh Token.";

    public static final String REFRESH_TOKEN_BLACKLISTED_CODE = "017";
    public static final boolean REFRESH_TOKEN_BLACKLISTED_SUCCESS = false;
    public static final String REFRESH_TOKEN_BLACKLISTED_MESSAGE = "Your token has expired. Please log in again to continue.";

    public static final String FORGOT_PASSWORD_EMAIL_CODE = "018";
    public static final boolean FORGOT_PASSWORD_EMAIL_SUCCESS = true;
    public static final String FORGOT_PASSWORD_EMAIL_MESSAGE = "OTP has been sent to your email.";

    public static final String FORGOT_PASSWORD_PHONE_NUMBER_CODE = "019";
    public static final boolean FORGOT_PASSWORD_PHONE_NUMBER_SUCCESS = true;
    public static final String FORGOT_PASSWORD_PHONE_NUMBER_MESSAGE = "OTP has been sent to your phone number.";

    public static final String FORGOT_PASSWORD_FAILED_CODE = "020";
    public static final boolean FORGOT_PASSWORD_FAILED_SUCCESS = false;
    public static final String FORGOT_PASSWORD_FAILED_MESSAGE = "Incorrect Credentials.";

    public static final String WRONG_OTP_CODE = "021";
    public static final boolean WRONG_OTP_SUCCESS = false;
    public static final String WRONG_OTP_MESSAGE = "Incorrect or expired OTP.";

    public static final String ERROR_VERIFYING_OTP = "Error verifying OTP.";

    public static final String FORGOT_PASSWORD_SUCCESS_CODE = "022";
    public static final boolean FORGOT_PASSWORD_SUCCESS_SUCCESS = true;
    public static final String FORGOT_PASSWORD_SUCCESS_MESSAGE = "OTP Verified Successfully.";

    public static final String RESET_PASSWORD_FAILED_CODE = "023";
    public static final boolean RESET_PASSWORD_FAILED_SUCCESS = false;
    public static final String RESET_PASSWORD_FAILED_MESSAGE = "Invalid Request Method.";

    public static final String RESET_PASSWORD_SUCCESS_CODE = "024";
    public static final boolean RESET_PASSWORD_SUCCESS_SUCCESS = true;
    public static final String RESET_PASSWORD_SUCCESS_MESSAGE = "New password has been set.";

    public static final String CHANGE_PASSWORD_FAILED_CODE = "025";
    public static final boolean CHANGE_PASSWORD_FAILED_SUCCESS = false;
    public static final String CHANGE_PASSWORD_FAILED_MESSAGE = "Entered Password Is Incorrect.";

    public static final String CHANGE_PASSWORD_SUCCESS_CODE = "026";
    public static final boolean CHANGE_PASSWORD_SUCCESS_SUCCESS = true;
    public static final String CHANGE_PASSWORD_SUCCESS_MESSAGE = "New password has been set.";

    public static final String CREATE_DELIVERY_SUCCESS_CODE = "027";
    public static final boolean CREATE_DELIVERY_SUCCESS_SUCCESS = true;
    public static final String CREATE_DELIVERY_SUCCESS_MESSAGE = "Delivery Order Placed.";

    public static final String UPDATE_DELIVERY_SUCCESS_CODE = "028";
    public static final boolean UPDATE_DELIVERY_SUCCESS_SUCCESS = true;
    public static final String UPDATE_DELIVERY_SUCCESS_MESSAGE = "Delivery Updated Successfully.";

    public static final String CANCEL_DELIVERY_SUCCESS_CODE = "029";
    public static final boolean CANCEL_DELIVERY_SUCCESS_SUCCESS = true;
    public static final String CANCEL_DELIVERY_SUCCESS_MESSAGE = "Delivery Cancelled Successfully.";

    public static final String TYPE_OF_DELIVERY_LARGE_PACKAGE = "LARGE_DELIVERY";
    public static final String TYPE_OF_DELIVERY_FOOD_DELIVERY = "FOOD_DELIVERY";
    public static final String TYPE_OF_DELIVERY_EXPRESS = "EXPRESS";

    public static final double RATE_PER_MILE_NORMAL = 3.0;
    public static final double RATE_PER_MILE_LARGE_PACKAGE = 4.0;
    public static final double RATE_PER_MILE_FOOD_DELIVERY = 3.5;
    public static final double RATE_PER_MILE_EXPRESS = 5.5;

    public static final String DELIVERY_STATUS_PENDING = "PENDING";
    public static final String DELIVERY_STATUS_ASSIGNED = "ASSIGNED";
    public static final String DELIVERY_STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERY_STATUS_DELIVERED = "DELIVERED";
    public static final String DELIVERY_STATUS_CANCELLED = "CANCELLED";
    public static final String DELIVERY_STATUS_FAILED = "FAILED";
}
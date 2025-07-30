package com.locally.locally_backend_engine.utils;

public class EngineUtils {

    public static final String CREATE_DELIVERY_SUCCESS_CODE = "001";
    public static final boolean CREATE_DELIVERY_SUCCESS_SUCCESS = true;
    public static final String CREATE_DELIVERY_SUCCESS_MESSAGE = "Delivery Order Placed.";

    public static final String UPDATE_DELIVERY_SUCCESS_CODE = "002";
    public static final boolean UPDATE_DELIVERY_SUCCESS_SUCCESS = true;
    public static final String UPDATE_DELIVERY_SUCCESS_MESSAGE = "Delivery Updated Successfully.";

    public static final String CANCEL_DELIVERY_SUCCESS_CODE = "003";
    public static final boolean CANCEL_DELIVERY_SUCCESS_SUCCESS = true;
    public static final String CANCEL_DELIVERY_SUCCESS_MESSAGE = "Delivery Cancelled Successfully.";

    public static final String TYPE_OF_DELIVERY_NORMAL = "NORMAL";
    public static final String TYPE_OF_DELIVERY_LARGE_PACKAGE = "LARGE_DELIVERY";
    public static final String TYPE_OF_DELIVERY_FOOD_DELIVERY = "FOOD_DELIVERY";
    public static final String TYPE_OF_DELIVERY_EXPRESS = "EXPRESS";

    public static final double RATE_PER_MILE_NORMAL = 3.0;
    public static final double RATE_PER_MILE_LARGE_PACKAGE = 4.0;
    public static final double RATE_PER_MILE_FOOD_DELIVERY = 3.5;
    public static final double RATE_PER_MILE_EXPRESS = 5.5;

    public static final double SHARE_FOR_NORMAL = 0.78;
    public static final double SHARE_FOR_LARGE_PACKAGE = 0.82;
    public static final double SHARE_FOR_FOOD_DELIVERY = 0.80;
    public static final double SHARE_FOR_EXPRESS = 0.85;

    public static final String DELIVERY_STATUS_PENDING = "PENDING";
    public static final String DELIVERY_STATUS_ASSIGNED = "ASSIGNED";
    public static final String DELIVERY_STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERY_STATUS_DELIVERED = "DELIVERED";
    public static final String DELIVERY_STATUS_CANCELLED = "CANCELLED";
    public static final String DELIVERY_STATUS_FAILED = "FAILED";
}
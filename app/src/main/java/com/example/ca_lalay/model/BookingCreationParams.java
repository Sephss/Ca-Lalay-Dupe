package com.example.ca_lalay.model;

/**
 * Groups all the "static" booking detail fields that used to be passed as a long
 * parameter list into FirebaseBookingManager.createBooking(...). providerId, the
 * capacity-checked date/start/end, bookingId, bookingStatus and timestamps are
 * handled separately by FirebaseCapacityManager, so they are NOT included here.
 */
public class BookingCreationParams {
    public String serviceId;
    public String bookerId;
    public String bookerImage;
    public String serviceImage;
    public String serviceProviderImage;
    public String bookerName;
    public String serviceName;
    public String serviceProviderName;
    public String serviceProviderLocation;
    public String bookerContactNum;
    public String bookerEmail;
    public String bookerVehicle;
    public String bookerAdditionalNotes;
    public String bookerRating;
    public String bookerFeedback;
    public String serviceType;
    public String servicePrice;
    public String serviceDuration;   // hours, as a String, e.g. "1", "1.5"
    public String serviceDate;       // display/storage string, "yyyy-MM-dd"
    public String serviceTime;       // display/storage string, "hh:mm a" (start time)
    public String createdAt;
    public String bookerVehicleSize;
    public String bookerNumber;
    public String timestamp;
    public String bookerPaymentMethod;
    public String isBookerPaid;
    public String additionalFeeVehicleSize;
    public String baseFee;
    public String overallPriceOfService;
    public String serviceAdded;
    public String serviceAddedTotalPrice;
    public String serviceAddedRemarks;
    public String shopSubmitServiceAddRequest;
    public String customerApprovesServiceChanges;
    public String customerRespondedToServiceChanges;
    public String billingNumber;
    public String rejectedBookingReason;
    public String bookerReceiptImage;
}
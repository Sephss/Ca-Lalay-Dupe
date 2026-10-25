package com.example.ca_lalay.model;

public class CustomerBookingModel {
    String bookingId;
    String serviceId;
    String ProviderId;
    String bookerId;
    String bookerImage;
    String serviceImage;
    String serviceProviderImage;
    String bookerName;
    String serviceName;
    String serviceProviderName;
    String serviceProviderLocation;
    String bookerContactNum;
    String bookerEmail;
    String bookerVehicle;
    String bookerAdditionalNotes;
    String bookerRating;
    String bookerFeedback;
    String serviceType;
    String servicePrice;
    String serviceDuration;
    String serviceDate;
    String serviceTime;
    String bookingStatus;
    String createdAt;
    String starRatings;
    String ratingID;
    String ratingComment;
    String dateRated;
    String isServiceRated;
    String bookerVehicleSize;
    String bookerNumber;
    String timestamp;

    // NEW: epoch-millisecond start/end of the booked slot, derived from
    // serviceDate + serviceTime + serviceDuration. Used for fast, reliable
    // overlap/capacity checks instead of re-parsing the display strings.
    Long startTimestamp;
    Long endTimestamp;
    String bookerPaymentMethod;
     String isBookerPaid;
     String additionalFeeVehicleSize;
     String overallPriceOfService;
     String serviceAdded;
     String serviceAddedTotalPrice;
     String serviceAddedRemarks;
     String shopSubmitServiceAddRequest;
     String customerApprovesServiceChanges;
     String customerRespondedToServiceChanges;
     String billingNumber;
     String rejectedBookingReason;
     String bookerReceiptImage;

    public CustomerBookingModel() {
        // Required empty constructor for Firebase deserialization
    }

    /** Legacy-compatible constructor (no timestamps) - defaults them to 0. */
    public CustomerBookingModel(String bookingId, String serviceId, String providerId, String bookerId,
                                String bookerImage, String serviceImage, String serviceProviderImage,
                                String bookerName, String serviceName, String serviceProviderName,
                                String serviceProviderLocation, String bookerContactNum, String bookerEmail,
                                String bookerVehicle, String bookerAdditionalNotes, String bookerRating,
                                String bookerFeedback, String serviceType, String servicePrice,
                                String serviceDuration, String serviceDate, String serviceTime,
                                String bookingStatus, String createdAt, String starRatings, String ratingID,
                                String ratingComment, String dateRated, String isServiceRated,
                                String bookerVehicleSize, String bookerNumber, String timestamp,
                                String bookerPaymentMethod, String isBookerPaid, String additionalFeeVehicleSize, String overallPriceOfService, String serviceAdded, String serviceAddedTotalPrice, String serviceAddedRemarks, String shopSubmitServiceAddRequest, String customerApprovesServiceChanges, String customerRespondedToServiceChanges, String billingNumber, String rejectedBookingReason, String bookerReceiptImage) {
        this(bookingId, serviceId, providerId, bookerId, bookerImage, serviceImage, serviceProviderImage,
                bookerName, serviceName, serviceProviderName, serviceProviderLocation, bookerContactNum,
                bookerEmail, bookerVehicle, bookerAdditionalNotes, bookerRating, bookerFeedback, serviceType,
                servicePrice, serviceDuration, serviceDate, serviceTime, bookingStatus, createdAt, starRatings,
                ratingID, ratingComment, dateRated, isServiceRated, bookerVehicleSize, bookerNumber, timestamp,
                0L, 0L, bookerPaymentMethod, isBookerPaid, additionalFeeVehicleSize, overallPriceOfService, serviceAdded, serviceAddedTotalPrice, serviceAddedRemarks, shopSubmitServiceAddRequest, customerApprovesServiceChanges, customerRespondedToServiceChanges, billingNumber, rejectedBookingReason, bookerReceiptImage);
    }

    public String getIsBookerPaid() {
        return isBookerPaid;
    }

    public void setIsBookerPaid(String isBookerPaid) {
        this.isBookerPaid = isBookerPaid;
    }

    public String getAdditionalFeeVehicleSize() {
        return additionalFeeVehicleSize;
    }

    public void setAdditionalFeeVehicleSize(String additionalFeeVehicleSize) {
        this.additionalFeeVehicleSize = additionalFeeVehicleSize;
    }

    public String getOverallPriceOfService() {
        return overallPriceOfService;
    }

    public void setOverallPriceOfService(String overallPriceOfService) {
        this.overallPriceOfService = overallPriceOfService;
    }

    public String getServiceAdded() {
        return serviceAdded;
    }

    public void setServiceAdded(String serviceAdded) {
        this.serviceAdded = serviceAdded;
    }

    public String getServiceAddedTotalPrice() {
        return serviceAddedTotalPrice;
    }

    public void setServiceAddedTotalPrice(String serviceAddedTotalPrice) {
        this.serviceAddedTotalPrice = serviceAddedTotalPrice;
    }

    public String getServiceAddedRemarks() {
        return serviceAddedRemarks;
    }

    public void setServiceAddedRemarks(String serviceAddedRemarks) {
        this.serviceAddedRemarks = serviceAddedRemarks;
    }

    public String getShopSubmitServiceAddRequest() {
        return shopSubmitServiceAddRequest;
    }

    public void setShopSubmitServiceAddRequest(String shopSubmitServiceAddRequest) {
        this.shopSubmitServiceAddRequest = shopSubmitServiceAddRequest;
    }

    public String getCustomerApprovesServiceChanges() {
        return customerApprovesServiceChanges;
    }

    public void setCustomerApprovesServiceChanges(String customerApprovesServiceChanges) {
        this.customerApprovesServiceChanges = customerApprovesServiceChanges;
    }

    public String getCustomerRespondedToServiceChanges() {
        return customerRespondedToServiceChanges;
    }

    public void setCustomerRespondedToServiceChanges(String customerRespondedToServiceChanges) {
        this.customerRespondedToServiceChanges = customerRespondedToServiceChanges;
    }

    public String getBillingNumber() {
        return billingNumber;
    }

    public void setBillingNumber(String billingNumber) {
        this.billingNumber = billingNumber;
    }

    public String getRejectedBookingReason() {
        return rejectedBookingReason;
    }

    public void setRejectedBookingReason(String rejectedBookingReason) {
        this.rejectedBookingReason = rejectedBookingReason;
    }

    public String getBookerReceiptImage() {
        return bookerReceiptImage;
    }

    public void setBookerReceiptImage(String bookerReceiptImage) {
        this.bookerReceiptImage = bookerReceiptImage;
    }

    public CustomerBookingModel(String bookingId, String serviceId, String providerId, String bookerId,
                                String bookerImage, String serviceImage, String serviceProviderImage,
                                String bookerName, String serviceName, String serviceProviderName,
                                String serviceProviderLocation, String bookerContactNum, String bookerEmail,
                                String bookerVehicle, String bookerAdditionalNotes, String bookerRating,
                                String bookerFeedback, String serviceType, String servicePrice,
                                String serviceDuration, String serviceDate, String serviceTime,
                                String bookingStatus, String createdAt, String starRatings, String ratingID,
                                String ratingComment, String dateRated, String isServiceRated,
                                String bookerVehicleSize, String bookerNumber, String timestamp,
                                Long startTimestamp, Long endTimestamp, String bookerPaymentMethod, String isBookerPaid, String additionalFeeVehicleSize, String overallPriceOfService, String serviceAdded, String serviceAddedTotalPrice, String serviceAddedRemarks, String shopSubmitServiceAddRequest, String customerApprovesServiceChanges, String customerRespondedToServiceChanges, String billingNumber, String rejectedBookingReason, String bookerReceiptImage) {
        this.bookingId = bookingId;
        this.serviceId = serviceId;
        this.ProviderId = providerId;
        this.bookerId = bookerId;
        this.bookerImage = bookerImage;
        this.serviceImage = serviceImage;
        this.serviceProviderImage = serviceProviderImage;
        this.bookerName = bookerName;
        this.serviceName = serviceName;
        this.serviceProviderName = serviceProviderName;
        this.serviceProviderLocation = serviceProviderLocation;
        this.bookerContactNum = bookerContactNum;
        this.bookerEmail = bookerEmail;
        this.bookerVehicle = bookerVehicle;
        this.bookerAdditionalNotes = bookerAdditionalNotes;
        this.bookerRating = bookerRating;
        this.bookerFeedback = bookerFeedback;
        this.serviceType = serviceType;
        this.servicePrice = servicePrice;
        this.serviceDuration = serviceDuration;
        this.serviceDate = serviceDate;
        this.serviceTime = serviceTime;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
        this.starRatings = starRatings;
        this.ratingID = ratingID;
        this.ratingComment = ratingComment;
        this.dateRated = dateRated;
        this.isServiceRated = isServiceRated;
        this.bookerVehicleSize = bookerVehicleSize;
        this.bookerNumber = bookerNumber;
        this.timestamp = timestamp;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.bookerPaymentMethod = bookerPaymentMethod;
        this.isBookerPaid = isBookerPaid;
        this.additionalFeeVehicleSize = additionalFeeVehicleSize;
        this.overallPriceOfService = overallPriceOfService;
        this.serviceAdded = serviceAdded;
        this.serviceAddedTotalPrice = serviceAddedTotalPrice;
        this.serviceAddedRemarks = serviceAddedRemarks;
        this.shopSubmitServiceAddRequest = shopSubmitServiceAddRequest;
        this.customerApprovesServiceChanges = customerApprovesServiceChanges;
        this.customerRespondedToServiceChanges = customerRespondedToServiceChanges;
        this.billingNumber = billingNumber;
        this.rejectedBookingReason = rejectedBookingReason;
        this.bookerReceiptImage = bookerReceiptImage;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getProviderId() { return ProviderId; }
    public void setProviderId(String providerId) { this.ProviderId = providerId; }

    public String getBookerId() { return bookerId; }
    public void setBookerId(String bookerId) { this.bookerId = bookerId; }

    public String getBookerImage() { return bookerImage; }
    public void setBookerImage(String bookerImage) { this.bookerImage = bookerImage; }

    public String getServiceImage() { return serviceImage; }
    public void setServiceImage(String serviceImage) { this.serviceImage = serviceImage; }

    public String getServiceProviderImage() { return serviceProviderImage; }
    public void setServiceProviderImage(String serviceProviderImage) { this.serviceProviderImage = serviceProviderImage; }

    public String getBookerName() { return bookerName; }
    public void setBookerName(String bookerName) { this.bookerName = bookerName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceProviderName() { return serviceProviderName; }
    public void setServiceProviderName(String serviceProviderName) { this.serviceProviderName = serviceProviderName; }

    public String getServiceProviderLocation() { return serviceProviderLocation; }
    public void setServiceProviderLocation(String serviceProviderLocation) { this.serviceProviderLocation = serviceProviderLocation; }

    public String getBookerContactNum() { return bookerContactNum; }
    public void setBookerContactNum(String bookerContactNum) { this.bookerContactNum = bookerContactNum; }

    public String getBookerEmail() { return bookerEmail; }
    public void setBookerEmail(String bookerEmail) { this.bookerEmail = bookerEmail; }

    public String getBookerVehicle() { return bookerVehicle; }
    public void setBookerVehicle(String bookerVehicle) { this.bookerVehicle = bookerVehicle; }

    public String getBookerAdditionalNotes() { return bookerAdditionalNotes; }
    public void setBookerAdditionalNotes(String bookerAdditionalNotes) { this.bookerAdditionalNotes = bookerAdditionalNotes; }

    public String getBookerRating() { return bookerRating; }
    public void setBookerRating(String bookerRating) { this.bookerRating = bookerRating; }

    public String getBookerFeedback() { return bookerFeedback; }
    public void setBookerFeedback(String bookerFeedback) { this.bookerFeedback = bookerFeedback; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getServicePrice() { return servicePrice; }
    public void setServicePrice(String servicePrice) { this.servicePrice = servicePrice; }

    public String getServiceDuration() { return serviceDuration; }
    public void setServiceDuration(String serviceDuration) { this.serviceDuration = serviceDuration; }

    public String getServiceDate() { return serviceDate; }
    public void setServiceDate(String serviceDate) { this.serviceDate = serviceDate; }

    public String getServiceTime() { return serviceTime; }
    public void setServiceTime(String serviceTime) { this.serviceTime = serviceTime; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStarRatings() { return starRatings; }
    public void setStarRatings(String starRatings) { this.starRatings = starRatings; }

    public String getRatingID() { return ratingID; }
    public void setRatingID(String ratingID) { this.ratingID = ratingID; }

    public String getRatingComment() { return ratingComment; }
    public void setRatingComment(String ratingComment) { this.ratingComment = ratingComment; }

    public String getDateRated() { return dateRated; }
    public void setDateRated(String dateRated) { this.dateRated = dateRated; }

    public String getIsServiceRated() { return isServiceRated; }
    public void setIsServiceRated(String isServiceRated) { this.isServiceRated = isServiceRated; }

    public String getBookerVehicleSize() { return bookerVehicleSize; }
    public void setBookerVehicleSize(String bookerVehicleSize) { this.bookerVehicleSize = bookerVehicleSize; }

    public String getBookerNumber() { return bookerNumber; }
    public void setBookerNumber(String bookerNumber) { this.bookerNumber = bookerNumber; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Long getStartTimestamp() { return startTimestamp; }
    public void setStartTimestamp(Long startTimestamp) { this.startTimestamp = startTimestamp; }

    public Long getEndTimestamp() { return endTimestamp; }
    public void setEndTimestamp(Long endTimestamp) { this.endTimestamp = endTimestamp; }

    public String getBookerPaymentMethod() { return bookerPaymentMethod; }
    public void setBookerPaymentMethod(String bookerPaymentMethod) { this.bookerPaymentMethod = bookerPaymentMethod; }
}
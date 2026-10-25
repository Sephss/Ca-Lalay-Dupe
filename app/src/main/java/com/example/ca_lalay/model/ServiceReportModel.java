package com.example.ca_lalay.model;



public class ServiceReportModel {

    private String serviceId;

    private String serviceName;

    private int totalBookings;

    public ServiceReportModel() {
    }

    public ServiceReportModel(String serviceId,
                              String serviceName,
                              int totalBookings) {

        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.totalBookings = totalBookings;

    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

}
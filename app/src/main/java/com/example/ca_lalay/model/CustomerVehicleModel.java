package com.example.ca_lalay.model;

public class CustomerVehicleModel {
    String vehicleImage, vehicleName, vehiclePlateNumber, vehicleType, vehicleSize, vehicleWheels, vehicleId, vehicleOwnerId;

    public CustomerVehicleModel() {

    }

    public String getVehicleOwnerId() {
        return vehicleOwnerId;
    }

    public void setVehicleOwnerId(String vehicleOwnerId) {
        this.vehicleOwnerId = vehicleOwnerId;
    }

    public String getVehicleImage() {
        return vehicleImage;
    }

    public void setVehicleImage(String vehicleImage) {
        this.vehicleImage = vehicleImage;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleWheels() {
        return vehicleWheels;
    }

    public void setVehicleWheels(String vehicleWheels) {
        this.vehicleWheels = vehicleWheels;
    }

    public String getVehicleSize() {
        return vehicleSize;
    }

    public void setVehicleSize(String vehicleSize) {
        this.vehicleSize = vehicleSize;
    }

    public String getVehiclePlateNumber() {
        return vehiclePlateNumber;
    }

    public void setVehiclePlateNumber(String vehiclePlateNumber) {
        this.vehiclePlateNumber = vehiclePlateNumber;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public CustomerVehicleModel(String vehicleImage, String vehicleName, String vehiclePlateNumber, String vehicleType, String vehicleSize, String vehicleWheels, String vehicleId, String vehicleOwnerId) {
        this.vehicleImage = vehicleImage;
        this.vehicleName = vehicleName;
        this.vehiclePlateNumber = vehiclePlateNumber;
        this.vehicleType = vehicleType;
        this.vehicleSize = vehicleSize;
        this.vehicleWheels = vehicleWheels;
        this.vehicleId = vehicleId;
        this.vehicleOwnerId = vehicleOwnerId;

    }

}

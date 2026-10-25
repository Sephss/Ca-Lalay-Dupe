package com.example.ca_lalay.model;

public class ReportAccountModel {
    String reportId, reporterId, reporterName, reporterImage, reportedId, reportedName, reportedImage, reportTitle, reportDescription, reportCategory, reportImage, createdAt, status;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterImage() {
        return reporterImage;
    }

    public void setReporterImage(String reporterImage) {
        this.reporterImage = reporterImage;
    }

    public String getReportedId() {
        return reportedId;
    }

    public void setReportedId(String reportedId) {
        this.reportedId = reportedId;
    }

    public String getReportedName() {
        return reportedName;
    }

    public void setReportedName(String reportedName) {
        this.reportedName = reportedName;
    }

    public String getReportedImage() {
        return reportedImage;
    }

    public void setReportedImage(String reportedImage) {
        this.reportedImage = reportedImage;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        this.reportCategory = reportCategory;
    }

    public String getReportImage() {
        return reportImage;
    }

    public void setReportImage(String reportImage) {
        this.reportImage = reportImage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ReportAccountModel(String reportId, String reporterId, String reporterName, String reporterImage, String reportedId, String reportedName, String reportedImage, String reportTitle, String reportDescription, String reportCategory, String reportImage, String createdAt, String status) {
       this.reportId = reportId;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reporterImage = reporterImage;
        this.reportedId = reportedId;
        this.reportedName = reportedName;
        this.reportedImage = reportedImage;
        this.reportTitle = reportTitle;
        this.reportDescription = reportDescription;
        this.reportCategory = reportCategory;
        this.reportImage = reportImage;
        this.createdAt = createdAt;
        this.status = status;
    }

    public ReportAccountModel() {

    }
}

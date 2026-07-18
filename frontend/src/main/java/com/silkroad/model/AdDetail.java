package com.silkroad.model;

public class AdDetail extends Ad {
    private String sellerFullName;
    private String sellerPhone;
    private String rejectionReason;
    private boolean submitter; // true if the logged-in user owns this ad
    private Double averageRating;

    public String getSellerFullName() { return sellerFullName; }
    public String getSellerPhone() { return sellerPhone; }
    public String getRejectionReason() { return rejectionReason; }
    public boolean isSubmitter() { return submitter; }
    public Double getAverageRating() { return averageRating; }

    public void setSellerFullName(String sellerFullName) { this.sellerFullName = sellerFullName; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setSubmitter(boolean submitter) { this.submitter = submitter; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
}

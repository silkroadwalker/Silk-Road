package com.silkroad.market.dto.admin;

public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;

    private long totalAdvertisements;
    private long pendingAdvertisements;
    private long approvedAdvertisements;
    private long rejectedAdvertisements;
    private long soldAdvertisements;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(
            long totalUsers,
            long activeUsers,
            long blockedUsers,
            long totalAdvertisements,
            long pendingAdvertisements,
            long approvedAdvertisements,
            long rejectedAdvertisements,
            long soldAdvertisements) {

        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.blockedUsers = blockedUsers;

        this.totalAdvertisements = totalAdvertisements;
        this.pendingAdvertisements = pendingAdvertisements;
        this.approvedAdvertisements = approvedAdvertisements;
        this.rejectedAdvertisements = rejectedAdvertisements;
        this.soldAdvertisements = soldAdvertisements;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(long blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public long getTotalAdvertisements() {
        return totalAdvertisements;
    }

    public void setTotalAdvertisements(long totalAdvertisements) {
        this.totalAdvertisements = totalAdvertisements;
    }

    public long getPendingAdvertisements() {
        return pendingAdvertisements;
    }

    public void setPendingAdvertisements(long pendingAdvertisements) {
        this.pendingAdvertisements = pendingAdvertisements;
    }

    public long getApprovedAdvertisements() {
        return approvedAdvertisements;
    }

    public void setApprovedAdvertisements(long approvedAdvertisements) {
        this.approvedAdvertisements = approvedAdvertisements;
    }

    public long getRejectedAdvertisements() {
        return rejectedAdvertisements;
    }

    public void setRejectedAdvertisements(long rejectedAdvertisements) {
        this.rejectedAdvertisements = rejectedAdvertisements;
    }

    public long getSoldAdvertisements() {
        return soldAdvertisements;
    }

    public void setSoldAdvertisements(long soldAdvertisements) {
        this.soldAdvertisements = soldAdvertisements;
    }
}
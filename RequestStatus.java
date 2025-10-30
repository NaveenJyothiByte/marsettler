public enum RequestStatus {
    SUBMITTED("Submitted"),
    APPROVED("Approved"),
    DELIVERED("Delivered"),
    DECLINED("Declined"),
    CANCELLED("Cancelled");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
    
    @Override
    public String toString() {
        return displayName;
    }
}
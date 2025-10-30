public enum AccountStatus {
    ACTIVE,
    EXPIRED,
    LOCKED;
    
    public boolean equalsIgnoreCase(String status) {
        return this.toString().equalsIgnoreCase(status);
    }
}
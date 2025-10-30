import java.time.Instant;

public class SupplyRequest {
    public final String requestId;
    public final String username;
    public final SupplyItem item;
    public final int quantity;
    public final Instant timestamp;
    public RequestStatus status;
    public String estimatedDelivery;

    public SupplyRequest(String requestId, String username, SupplyItem item, 
                        int quantity, RequestStatus status) {
        this.requestId = requestId;
        this.username = username;
        this.item = item;
        this.quantity = quantity;
        this.timestamp = Instant.now();
        this.status = status;
        this.estimatedDelivery = calculateDelivery();
    }

    private String calculateDelivery() {
        // Simple delivery estimation logic
        return Instant.now().plusSeconds(3600).toString(); // 1 hour later
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %d %s - %s", 
            requestId, item.getDisplayName(), quantity, item.getUnit(), status);
    }
}
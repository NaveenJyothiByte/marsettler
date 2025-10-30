import java.time.Instant;

public class Alert {
    private final String id;
    private final String message;
    private final String severity;
    private final String metric;
    private final Instant timestamp;

    public Alert(String message, String severity, String metric) {
        this.id = "ALERT-" + System.currentTimeMillis();
        this.message = message;
        this.severity = severity;
        this.metric = metric;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getMetric() { return metric; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", severity, metric, message);
    }
}
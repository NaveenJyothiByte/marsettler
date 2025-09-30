/**
 * UptimeMonitor: simple arithmetic model for uptime %.
 * Simulate totalMillis and downtimeMillis, then query uptimePercent().
 */
public class UptimeMonitor {
    private long totalMillis;
    private long downtimeMillis;

    public void simulate(long totalMs, long injectedDowntimeMs) {
        this.totalMillis = totalMs;
        this.downtimeMillis = injectedDowntimeMs;
    }

    public double uptimePercent() {
        if (totalMillis <= 0) return 0.0;
        return 100.0 * (totalMillis - downtimeMillis) / totalMillis;
    }
}

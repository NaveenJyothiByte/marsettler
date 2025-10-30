import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

/**
 * Provides real-time environmental monitoring and alert management
 * Handles UAT-S2-09 through UAT-S2-11
 */
public class OperatorDashboardService {
    private final Map<String, EnvironmentalMetric> metrics = new ConcurrentHashMap<>();
    private final List<Alert> activeAlerts = new ArrayList<>();
    private final List<Alert> alertHistory = new ArrayList<>();
    private Instant lastUpdate = Instant.now();
    
    // Threshold configurations
    private final Map<String, Double> thresholds = new HashMap<>();

    public OperatorDashboardService() {
        initializeMetrics();
        initializeThresholds();
    }

    private void initializeMetrics() {
        metrics.put("O2_LEVEL", new EnvironmentalMetric("O2 Level", 20.9, "%"));
        metrics.put("TEMPERATURE", new EnvironmentalMetric("Temperature", 22.0, "°C"));
        metrics.put("PRESSURE", new EnvironmentalMetric("Pressure", 101.3, "kPa"));
        metrics.put("HUMIDITY", new EnvironmentalMetric("Humidity", 45.0, "%"));
        metrics.put("CO2_LEVEL", new EnvironmentalMetric("CO2 Level", 0.04, "%"));
    }

    private void initializeThresholds() {
        thresholds.put("O2_LEVEL", 19.5); // Minimum O2 level
        thresholds.put("TEMPERATURE", 25.0); // Maximum temperature
        thresholds.put("CO2_LEVEL", 1.0); // Maximum CO2 level
    }

    /**
     * Refreshes environmental metrics and checks thresholds
     * UAT-S2-09: Dashboard refreshes live metrics
     */
    public void refreshMetrics() {
        // Simulate sensor data updates with some random variation
        Random rand = new Random();
        
        metrics.get("O2_LEVEL").setValue(20.5 + rand.nextDouble() * 0.8); // 20.5-21.3%
        metrics.get("TEMPERATURE").setValue(21.0 + rand.nextDouble() * 4.0); // 21-25°C
        metrics.get("PRESSURE").setValue(101.0 + rand.nextDouble() * 1.0); // 101-102 kPa
        metrics.get("HUMIDITY").setValue(40.0 + rand.nextDouble() * 15.0); // 40-55%
        metrics.get("CO2_LEVEL").setValue(0.03 + rand.nextDouble() * 0.1); // 0.03-0.13%
        
        lastUpdate = Instant.now();
        
        // Check for threshold breaches
        checkThresholds();
    }

    /**
     * UAT-S2-10: Threshold alert triggers and acknowledgment
     */
    private void checkThresholds() {
        for (Map.Entry<String, Double> threshold : thresholds.entrySet()) {
            String metricKey = threshold.getKey();
            double thresholdValue = threshold.getValue();
            double currentValue = metrics.get(metricKey).getValue();
            
            boolean isBreach = false;
            String severity = "WARNING";
            
            switch (metricKey) {
                case "O2_LEVEL":
                    isBreach = currentValue < thresholdValue;
                    severity = currentValue < 18.0 ? "CRITICAL" : "WARNING";
                    break;
                case "TEMPERATURE":
                    isBreach = currentValue > thresholdValue;
                    severity = currentValue > 28.0 ? "CRITICAL" : "WARNING";
                    break;
                case "CO2_LEVEL":
                    isBreach = currentValue > thresholdValue;
                    severity = currentValue > 2.0 ? "CRITICAL" : "WARNING";
                    break;
            }
            
            if (isBreach) {
                String alertMessage = String.format("%s breach: %.2f%s (threshold: %.1f%s)",
                    metrics.get(metricKey).getName(), currentValue, 
                    metrics.get(metricKey).getUnit(), thresholdValue,
                    metrics.get(metricKey).getUnit());
                
                // Check if alert already exists
                if (activeAlerts.stream().noneMatch(alert -> alert.getMessage().equals(alertMessage))) {
                    Alert alert = new Alert(alertMessage, severity, metricKey);
                    activeAlerts.add(alert);
                    alertHistory.add(alert);
                }
            }
        }
    }

    public boolean acknowledgeAlert(String alertId) {
        return activeAlerts.removeIf(alert -> alert.getId().equals(alertId));
    }

    public List<EnvironmentalMetric> getCurrentMetrics() {
        return new ArrayList<>(metrics.values());
    }

    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts);
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    // Method to simulate threshold breach for testing
    public void simulateThresholdBreach(String metricKey, double value) {
        if (metrics.containsKey(metricKey)) {
            metrics.get(metricKey).setValue(value);
            checkThresholds();
        }
    }
}

class EnvironmentalMetric {
    private final String name;
    private double value;
    private final String unit;

    public EnvironmentalMetric(String name, double value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public void setValue(double value) { this.value = value; }

    @Override
    public String toString() {
        return String.format("%s: %.1f%s", name, value, unit);
    }
}

class Alert {
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
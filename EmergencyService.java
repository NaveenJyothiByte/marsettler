import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles emergency alerts and broadcasting to users
 * Sprint 3 - OPERATOR3: Manage emergency alerts and evacuation protocols
 */
public class EmergencyService {
    private final Map<String, List<Alert>> userAlerts = new ConcurrentHashMap<>();
    private final List<Alert> activeAlerts = new CopyOnWriteArrayList<>();
    private final Map<String, Set<String>> alertAcknowledgments = new ConcurrentHashMap<>();
    
    public EmergencyService() {}
    
    /**
     * Broadcast emergency alert to all users
     * @param alertType Type of emergency
     * @param message Alert message
     * @param severity Alert severity level
     * @param broadcastBy User ID of operator broadcasting alert
     * @return List of user IDs who received the alert
     */
    public List<String> broadcastEmergencyAlert(AlertType alertType, String message, 
                                               Severity severity, String broadcastBy) {
        List<String> deliveredUsers = new ArrayList<>();
        Alert alert = new Alert(generateAlertId(), alertType, message, severity, broadcastBy, Instant.now());
        
        activeAlerts.add(alert);
        
        // Broadcast to all users in the system
        for (String username : userAlerts.keySet()) {
            addAlertToUser(username, alert);
            deliveredUsers.add(username);
        }
        
        // Initialize acknowledgment tracking
        alertAcknowledgments.put(alert.getAlertId(), new HashSet<>());
        
        return deliveredUsers;
    }
    
    /**
     * Add alert to specific user's alert list
     */
    public void addAlertToUser(String username, Alert alert) {
        userAlerts.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(alert);
    }
    
    /**
     * Get active alerts for a user
     */
    public List<Alert> getUserAlerts(String username) {
        return userAlerts.getOrDefault(username, new ArrayList<>());
    }
    
    /**
     * Acknowledge an alert
     */
    public boolean acknowledgeAlert(String username, String alertId) {
        Set<String> acknowledgments = alertAcknowledgments.get(alertId);
        if (acknowledgments != null) {
            return acknowledgments.add(username);
        }
        return false;
    }
    
    /**
     * Check if user has acknowledged alert
     */
    public boolean hasAcknowledgedAlert(String username, String alertId) {
        Set<String> acknowledgments = alertAcknowledgments.get(alertId);
        return acknowledgments != null && acknowledgments.contains(username);
    }
    
    /**
     * Get all active emergency alerts
     */
    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts);
    }
    
    /**
     * Resolve an emergency alert
     */
    public boolean resolveAlert(String alertId) {
        return activeAlerts.removeIf(alert -> alert.getAlertId().equals(alertId));
    }
    
    private String generateAlertId() {
        return "EMR-" + System.currentTimeMillis();
    }
}

/**
 * Alert entity for emergency notifications
 */
class Alert {
    private final String alertId;
    private final AlertType alertType;
    private final String message;
    private final Severity severity;
    private final String broadcastBy;
    private final Instant createdAt;
    
    public Alert(String alertId, AlertType alertType, String message, 
                Severity severity, String broadcastBy, Instant createdAt) {
        this.alertId = alertId;
        this.alertType = alertType;
        this.message = message;
        this.severity = severity;
        this.broadcastBy = broadcastBy;
        this.createdAt = createdAt;
    }
    
    // Getters
    public String getAlertId() { return alertId; }
    public AlertType getAlertType() { return alertType; }
    public String getMessage() { return message; }
    public Severity getSeverity() { return severity; }
    public String getBroadcastBy() { return broadcastBy; }
    public Instant getCreatedAt() { return createdAt; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Severity: %s)", 
            alertId, alertType, message, severity);
    }
}

enum AlertType {
    HABITAT_BREACH, RADIATION_STORM, FIRE, LIFE_SUPPORT_FAILURE, COMMUNICATION_OUTAGE, CUSTOM
}

enum Severity {
    CRITICAL, HIGH, MEDIUM, LOW
}
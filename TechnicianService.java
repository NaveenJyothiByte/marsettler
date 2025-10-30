import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles technician-specific functionality
 * Sprint 3 - TECH1, TECH2, TECH3: Alert management, diagnostics, and maintenance
 */
public class TechnicianService {
    private final Map<String, List<MaintenanceTask>> maintenanceSchedule = new ConcurrentHashMap<>();
    private final Map<String, DiagnosticReport> diagnosticReports = new ConcurrentHashMap<>();
    private final Map<String, List<String>> technicianAssignments = new ConcurrentHashMap<>();
    
    public TechnicianService() {}
    
    // TECH1: Receive and manage alerts
    /**
     * Get alerts assigned to a technician
     */
    public List<Alert> getAssignedAlerts(String technicianUsername) {
        // In a real system, this would filter alerts by assignment
        // For now, return all active alerts for demonstration
        EmergencyService emergencyService = new EmergencyService(); // Would be injected
        return emergencyService.getActiveAlerts();
    }
    
    /**
     * Acknowledge an alert as a technician
     */
    public boolean acknowledgeTechnicianAlert(String technicianUsername, String alertId) {
        EmergencyService emergencyService = new EmergencyService(); // Would be injected
        return emergencyService.acknowledgeAlert(technicianUsername, alertId);
    }
    
    // TECH2: Diagnostic tools
    /**
     * Run diagnostics on a system component
     */
    public DiagnosticReport runSystemDiagnostics(String technicianUsername, SystemComponent component) {
        String reportId = generateReportId();
        DiagnosticReport report = new DiagnosticReport(reportId, technicianUsername, component, Instant.now());
        
        // Simulate diagnostic tests
        Map<String, String> testResults = performDiagnosticTests(component);
        report.setTestResults(testResults);
        report.setStatus(DiagnosticStatus.COMPLETED);
        
        diagnosticReports.put(reportId, report);
        return report;
    }
    
    /**
     * Get diagnostic history for a technician
     */
    public List<DiagnosticReport> getDiagnosticHistory(String technicianUsername) {
        List<DiagnosticReport> history = new ArrayList<>();
        for (DiagnosticReport report : diagnosticReports.values()) {
            if (report.getTechnicianId().equals(technicianUsername)) {
                history.add(report);
            }
        }
        return history;
    }
    
    // TECH3: Maintenance scheduling
    /**
     * Schedule a maintenance task
     */
    public MaintenanceTask scheduleMaintenance(String technicianUsername, SystemComponent component,
                                              String description, MaintenanceType type,
                                              LocalDate scheduledDate, LocalTime startTime, 
                                              int durationHours) {
        
        // Check for scheduling conflicts
        if (hasSchedulingConflict(technicianUsername, scheduledDate, startTime, durationHours)) {
            throw new IllegalArgumentException("Scheduling conflict: Technician already has maintenance scheduled at this time");
        }
        
        String taskId = generateMaintenanceId();
        MaintenanceTask task = new MaintenanceTask(taskId, technicianUsername, component, 
                                                  description, type, scheduledDate, startTime, 
                                                  durationHours, MaintenanceStatus.SCHEDULED);
        
        maintenanceSchedule.computeIfAbsent(technicianUsername, k -> new ArrayList<>()).add(task);
        return task;
    }
    
    /**
     * Get maintenance schedule for a technician
     */
    public List<MaintenanceTask> getMaintenanceSchedule(String technicianUsername) {
        return maintenanceSchedule.getOrDefault(technicianUsername, new ArrayList<>());
    }
    
    /**
     * Update maintenance task status
     */
    public boolean updateMaintenanceStatus(String taskId, MaintenanceStatus newStatus) {
        for (List<MaintenanceTask> tasks : maintenanceSchedule.values()) {
            for (MaintenanceTask task : tasks) {
                if (task.getTaskId().equals(taskId)) {
                    task.setStatus(newStatus);
                    return true;
                }
            }
        }
        return false;
    }
    
    // Private helper methods
    private boolean hasSchedulingConflict(String technicianUsername, LocalDate date, 
                                        LocalTime startTime, int durationHours) {
        List<MaintenanceTask> scheduled = maintenanceSchedule.getOrDefault(technicianUsername, new ArrayList<>());
        LocalTime endTime = startTime.plusHours(durationHours);
        
        for (MaintenanceTask task : scheduled) {
            if (task.getScheduledDate().equals(date)) {
                LocalTime taskEnd = task.getStartTime().plusHours(task.getDurationHours());
                // Check for time overlap
                if (startTime.isBefore(taskEnd) && endTime.isAfter(task.getStartTime())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Map<String, String> performDiagnosticTests(SystemComponent component) {
        Map<String, String> results = new HashMap<>();
        Random random = new Random();
        
        switch (component) {
            case LIFE_SUPPORT:
                results.put("O2_Generator", random.nextBoolean() ? "NORMAL" : "WARNING");
                results.put("CO2_Scrubbers", random.nextBoolean() ? "NORMAL" : "EFFICIENCY 85%");
                results.put("Air_Circulation", "NORMAL");
                results.put("Pressure_Regulators", "NORMAL");
                break;
            case POWER_DISTRIBUTION:
                results.put("Solar_Panels", random.nextBoolean() ? "NORMAL" : "OUTPUT_REDUCED");
                results.put("Battery_Health", "92%");
                results.put("Power_Flow", "NORMAL");
                break;
            case COMMUNICATION_ARRAY:
                results.put("Signal_Strength", "EXCELLENT");
                results.put("Data_Throughput", "NORMAL");
                results.put("Antenna_Alignment", random.nextBoolean() ? "NORMAL" : "REALIGNMENT_NEEDED");
                break;
            default:
                results.put("Basic_Check", "NORMAL");
        }
        
        return results;
    }
    
    private String generateReportId() { return "DIA-" + System.currentTimeMillis(); }
    private String generateMaintenanceId() { return "MT-" + System.currentTimeMillis(); }
}

// Enums and supporting classes
enum SystemComponent {
    LIFE_SUPPORT, POWER_DISTRIBUTION, COMMUNICATION_ARRAY, WATER_RECLAMATION, THERMAL_CONTROL
}

enum MaintenanceType {
    PREVENTIVE, CORRECTIVE, EMERGENCY
}

enum MaintenanceStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
}

enum DiagnosticStatus {
    IN_PROGRESS, COMPLETED, FAILED
}

class DiagnosticReport {
    private final String reportId;
    private final String technicianId;
    private final SystemComponent component;
    private final Instant generatedAt;
    private Map<String, String> testResults;
    private DiagnosticStatus status;
    private String recommendations;
    
    public DiagnosticReport(String reportId, String technicianId, SystemComponent component, Instant generatedAt) {
        this.reportId = reportId;
        this.technicianId = technicianId;
        this.component = component;
        this.generatedAt = generatedAt;
        this.status = DiagnosticStatus.IN_PROGRESS;
        this.testResults = new HashMap<>();
    }
    
    // Getters and setters
    public String getReportId() { return reportId; }
    public String getTechnicianId() { return technicianId; }
    public SystemComponent getComponent() { return component; }
    public Instant getGeneratedAt() { return generatedAt; }
    public Map<String, String> getTestResults() { return testResults; }
    public void setTestResults(Map<String, String> testResults) { this.testResults = testResults; }
    public DiagnosticStatus getStatus() { return status; }
    public void setStatus(DiagnosticStatus status) { this.status = status; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}

class MaintenanceTask {
    private final String taskId;
    private final String technicianId;
    private final SystemComponent component;
    private final String description;
    private final MaintenanceType type;
    private final LocalDate scheduledDate;
    private final LocalTime startTime;
    private final int durationHours;
    private MaintenanceStatus status;
    private String notes;
    
    public MaintenanceTask(String taskId, String technicianId, SystemComponent component,
                          String description, MaintenanceType type, LocalDate scheduledDate,
                          LocalTime startTime, int durationHours, MaintenanceStatus status) {
        this.taskId = taskId;
        this.technicianId = technicianId;
        this.component = component;
        this.description = description;
        this.type = type;
        this.scheduledDate = scheduledDate;
        this.startTime = startTime;
        this.durationHours = durationHours;
        this.status = status;
    }
    
    // Getters and setters
    public String getTaskId() { return taskId; }
    public String getTechnicianId() { return technicianId; }
    public SystemComponent getComponent() { return component; }
    public String getDescription() { return description; }
    public MaintenanceType getType() { return type; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public LocalTime getStartTime() { return startTime; }
    public int getDurationHours() { return durationHours; }
    public MaintenanceStatus getStatus() { return status; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
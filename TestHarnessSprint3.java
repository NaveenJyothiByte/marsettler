import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Test harness for Sprint 3 functionality
 */
public class TestHarnessSprint3 {
    
    private static void assertTrue(String testId, boolean condition, String successMsg, String failureMsg) {
        System.out.println(testId + " :: " + (condition ? "PASS" : "FAIL") + " :: " + 
                          (condition ? successMsg : failureMsg));
    }
    
    public static void main(String[] args) {
        System.out.println("=== Sprint 3 Test Harness ===");
        
        // Test UAT-S3-01: Operator assigns task to Resident
        testTaskAssignment();
        
        // Test UAT-S3-04: Operator broadcasts emergency alert
        testEmergencyAlert();
        
        // Test UAT-S3-06: Technician acknowledges assigned alerts
        testTechnicianAlertAcknowledgment();
        
        // Test UAT-S3-12: Technician accesses system diagnostics
        testSystemDiagnostics();
        
        // Test UAT-S3-13: Technician schedules maintenance activities
        testMaintenanceScheduling();
        
        System.out.println("\n=== Sprint 3 Testing Complete ===");
    }
    
    private static void testTaskAssignment() {
        TaskAssignmentService service = new TaskAssignmentService();
        
        AssignmentResult result = service.assignTask(
            "OP001", "resident.valid@mars.local", 
            "Inspect Solar Panels", 2, 
            LocalDate.now().plusDays(1), LocalTime.of(14, 0)
        );
        
        assertTrue("UAT-S3-01", 
            result.isSuccess() && result.getTaskId() != null,
            "Operator successfully assigned task to resident",
            "Task assignment failed"
        );
    }
    
    private static void testEmergencyAlert() {
        EmergencyService service = new EmergencyService();
        
        // Add test user to receive alerts
        service.addAlertToUser("testuser", new Alert("TEST", AlertType.RADIATION_STORM, 
            "Test alert", Severity.HIGH, "OP001", Instant.now()));
        
        List<String> delivered = service.broadcastEmergencyAlert(
            AlertType.RADIATION_STORM, "Radiation storm detected", 
            Severity.HIGH, "OP001"
        );
        
        assertTrue("UAT-S3-04",
            !delivered.isEmpty(),
            "Emergency alert successfully broadcast to users",
            "Emergency alert broadcast failed"
        );
    }
    
    private static void testTechnicianAlertAcknowledgment() {
        EmergencyService emergencyService = new EmergencyService();
        TechnicianService techService = new TechnicianService();
        
        // Create test alert
        Alert testAlert = new Alert("TEST-ALERT", AlertType.FIRE, 
            "Test fire alert", Severity.CRITICAL, "OP001", Instant.now());
        emergencyService.addAlertToUser("technician", testAlert);
        
        boolean acknowledged = techService.acknowledgeTechnicianAlert("technician", "TEST-ALERT");
        
        assertTrue("UAT-S3-06",
            acknowledged,
            "Technician successfully acknowledged alert",
            "Technician alert acknowledgment failed"
        );
    }
    
    private static void testSystemDiagnostics() {
        TechnicianService service = new TechnicianService();
        
        DiagnosticReport report = service.runSystemDiagnostics("technician", SystemComponent.LIFE_SUPPORT);
        
        assertTrue("UAT-S3-12",
            report != null && report.getTestResults() != null && !report.getTestResults().isEmpty(),
            "System diagnostics completed successfully",
            "System diagnostics failed"
        );
    }
    
    private static void testMaintenanceScheduling() {
        TechnicianService service = new TechnicianService();
        
        try {
            MaintenanceTask task = service.scheduleMaintenance(
                "technician", SystemComponent.LIFE_SUPPORT,
                "Routine filter replacement", MaintenanceType.PREVENTIVE,
                LocalDate.now().plusDays(1), LocalTime.of(8, 0), 4
            );
            
            assertTrue("UAT-S3-13",
                task != null && task.getTaskId() != null,
                "Maintenance successfully scheduled",
                "Maintenance scheduling failed"
            );
        } catch (Exception e) {
            assertTrue("UAT-S3-13", false, "Maintenance successfully scheduled", "Maintenance scheduling failed: " + e.getMessage());
        }
    }
}
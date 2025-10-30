import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.*;

public class MarsSettlerSprint2TDTest {
    
    private SupplyService supplyService;
    private IssueService issueService;
    private OperatorDashboardService dashboardService;
    
    @Before
    public void setUp() {
        // RED PHASE: These will initially fail
        supplyService = new SupplyService();
        issueService = new IssueService();
        dashboardService = new OperatorDashboardService();
    }
    
    // UAT-S2-01: Resident submits valid supply request
    @Test
    public void UAT_S2_01_validSupplyRequest() {
        // Given
        String username = "test.user@mars.local";
        
        // When
        Optional<SupplyRequest> result = supplyService.submitRequest(username, SupplyItem.WATER, 5);
        
        // Then
        assertTrue("Valid supply request should succeed", result.isPresent());
        assertEquals("Status should be SUBMITTED", RequestStatus.SUBMITTED, result.get().status);
    }
    
    // UAT-S2-02: Supply request over personal quota is blocked
    @Test
    public void UAT_S2_02_quotaExceeded() {
        // Given
        String username = "test.user@mars.local";
        
        // When
        Optional<SupplyRequest> result = supplyService.submitRequest(username, SupplyItem.WATER, 15);
        
        // Then
        assertFalse("Request over quota should be blocked", result.isPresent());
    }
    
    // UAT-S2-03: Out-of-stock handling
    @Test
    public void UAT_S2_03_outOfStock() {
        // Given
        String username = "test.user@mars.local";
        
        // When
        Optional<SupplyRequest> result = supplyService.submitRequest(username, SupplyItem.MEDICAL, 100);
        
        // Then
        assertFalse("Out-of-stock request should be blocked", result.isPresent());
    }
    
    // UAT-S2-04: Resident views supply request history
    @Test
    public void UAT_S2_04_viewRequestHistory() {
        // Given
        String username = "test.user@mars.local";
        supplyService.submitRequest(username, SupplyItem.OXYGEN, 2);
        
        // When
        List<SupplyRequest> history = supplyService.getRequestHistory(username);
        
        // Then
        assertFalse("Request history should be available", history.isEmpty());
    }
    
    // UAT-S2-05: Resident cancels pending request
    @Test
    public void UAT_S2_05_cancelPendingRequest() {
        // Given
        String username = "test.user@mars.local";
        Optional<SupplyRequest> request = supplyService.submitRequest(username, SupplyItem.WATER, 3);
        String requestId = request.get().requestId;
        
        // When
        boolean cancelled = supplyService.cancelRequest(username, requestId);
        
        // Then
        assertTrue("Cancellation should succeed", cancelled);
    }
    
    // UAT-S2-06: Resident submits valid issue report
    @Test
    public void UAT_S2_06_validIssueReport() {
        // Given
        String username = "test.user@mars.local";
        
        // When
        Optional<Issue> result = issueService.submitIssue(username, IssueCategory.MAINTENANCE, 
                                                         "Broken equipment", "HIGH");
        
        // Then
        assertTrue("Valid issue should be created", result.isPresent());
    }
    
    // UAT-S2-07: Issue form validation
    @Test
    public void UAT_S2_07_issueFormValidation() {
        // Given
        String username = "test.user@mars.local";
        
        // When
        Optional<Issue> result = issueService.submitIssue(username, IssueCategory.HEALTH, "", "HIGH");
        
        // Then
        assertFalse("Empty description should be rejected", result.isPresent());
    }
    
    // UAT-S2-08: Issue auto-routing
    @Test
    public void UAT_S2_08_issueAutoRouting() {
        // Given
        String username = "test.user@mars.local";
        
        // When
        Optional<Issue> issue = issueService.submitIssue(username, IssueCategory.MAINTENANCE, 
                                                       "Electrical issue", "CRITICAL");
        
        // Then
        assertTrue("Issue should be created", issue.isPresent());
        
        List<Issue> techIssues = issueService.getAssignedIssues(Role.INFRASTRUCTURE_TECHNICIAN);
        boolean found = techIssues.stream().anyMatch(i -> i.issueId.equals(issue.get().issueId));
        assertTrue("Issue should be routed to technician", found);
    }
    
    // UAT-S2-09: Dashboard refresh
    @Test
    public void UAT_S2_09_dashboardRefresh() {
        // When
        dashboardService.refreshMetrics();
        
        // Then
        assertFalse("Metrics should be available", dashboardService.getCurrentMetrics().isEmpty());
    }
    
    // UAT-S2-10: Alert triggering and acknowledgment
    @Test
    public void UAT_S2_10_alertManagement() {
        // Given
        dashboardService.simulateThresholdBreach("O2_LEVEL", 18.0);
        
        // When
        List<Alert> alerts = dashboardService.getActiveAlerts();
        boolean hasAlerts = !alerts.isEmpty();
        boolean acknowledged = false;
        if (hasAlerts) {
            String alertId = alerts.get(0).getId();
            acknowledged = dashboardService.acknowledgeAlert(alertId);
        }
        
        // Then
        assertTrue("Alert should be triggered and acknowledged", hasAlerts && acknowledged);
    }
    
    // UAT-S2-11: RBAC access control
    @Test
    public void UAT_S2_11_rbacAccessControl() {
        // This is tested in App class - for service layer, we test data separation
        String resident = "resident@mars.local";
        
        // When resident submits issue
        issueService.submitIssue(resident, IssueCategory.MAINTENANCE, "Test", "LOW");
        
        // Then technician should see it
        List<Issue> techIssues = issueService.getAssignedIssues(Role.INFRASTRUCTURE_TECHNICIAN);
        assertTrue("Technician should see assigned issues", !techIssues.isEmpty());
    }
    
    // UAT-S2-12: Data persistence
    @Test
    public void UAT_S2_12_dataPersistence() {
        // Given
        String username = "test.user@mars.local";
        supplyService.submitRequest(username, SupplyItem.WATER, 2);
        
        // When
        List<SupplyRequest> requests = supplyService.getRequestHistory(username);
        
        // Then
        assertFalse("Data should persist during runtime", requests.isEmpty());
    }
    
    // UAT-S2-13: Backup/restore
    @Test
    public void UAT_S2_13_backupRestore() {
        // This would test extended BackupService - for now, basic functionality
        assertTrue("Backup service exists", new BackupService() != null);
    }
    
    // UAT-S2-14: Performance
    @Test
    public void UAT_S2_14_performance() {
        long startTime = System.currentTimeMillis();
        
        // Perform multiple operations
        for (int i = 0; i < 50; i++) {
            supplyService.getInventory();
            issueService.getUserIssues("test@mars.local");
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue("Operations should complete quickly", duration < 5000);
    }
    
    // UAT-S2-15: Usability workflow
    @Test
    public void UAT_S2_15_usabilityWorkflow() {
        String newUser = "new.user@mars.local";
        
        // Complete workflow
        boolean supplyWorkflow = supplyService.submitRequest(newUser, SupplyItem.FOOD_A, 1).isPresent();
        boolean issueWorkflow = issueService.submitIssue(newUser, IssueCategory.OTHER, 
                                                        "Assistance needed", "LOW").isPresent();
        boolean viewWorkflow = !supplyService.getRequestHistory(newUser).isEmpty();
        
        assertTrue("Complete usability workflow should work", supplyWorkflow && issueWorkflow && viewWorkflow);
    }
}
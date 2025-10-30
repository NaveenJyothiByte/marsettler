import java.util.*;

public class Sprint2IntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("=== Sprint 2 Integration Test ===\n");
        
        // Create a test instance of App
        App app = new App();
        
        // We'll test the integrated functionality by simulating user interactions
        // Note: This is a simplified test that doesn't actually run the full App loop
        
        System.out.println("Testing integrated Sprint 2 features...");
        
        // Test data setup
        UserStore store = new UserStore();
        store.seedSamples();
        
        SupplyService supply = new SupplyService();
        IssueService issue = new IssueService();
        OperatorDashboardService dashboard = new OperatorDashboardService();
        
        // Test 1: Resident workflow integration
        System.out.println("\n1. Testing Resident Workflow:");
        String resident = "resident.valid@mars.local";
        
        // Supply request
        Optional<SupplyRequest> supplyReq = supply.submitRequest(resident, SupplyItem.WATER, 3);
        System.out.println("   Supply Request: " + (supplyReq.isPresent() ? "SUCCESS" : "FAILED"));
        
        // Issue report
        Optional<Issue> issueReport = issue.submitIssue(resident, IssueCategory.MAINTENANCE, 
                                                      "Integrated test issue", "HIGH");
        System.out.println("   Issue Report: " + (issueReport.isPresent() ? "SUCCESS" : "FAILED"));
        
        // View history
        List<SupplyRequest> requests = supply.getRequestHistory(resident);
        List<Issue> issues = issue.getUserIssues(resident);
        System.out.println("   View History: " + requests.size() + " requests, " + issues.size() + " issues");
        
        // Test 2: Operator workflow integration
        System.out.println("\n2. Testing Operator Workflow:");
        dashboard.refreshMetrics();
        List<EnvironmentalMetric> metrics = dashboard.getCurrentMetrics();
        System.out.println("   Dashboard Metrics: " + metrics.size() + " metrics loaded");
        
        // Test 3: Technician workflow integration  
        System.out.println("\n3. Testing Technician Workflow:");
        List<Issue> assignedIssues = issue.getAssignedIssues(Role.INFRASTRUCTURE_TECHNICIAN);
        System.out.println("   Assigned Issues: " + assignedIssues.size() + " issues");
        
        if (!assignedIssues.isEmpty()) {
            boolean acknowledged = issue.acknowledgeIssue(assignedIssues.get(0).issueId, Role.INFRASTRUCTURE_TECHNICIAN);
            System.out.println("   Issue Acknowledgment: " + (acknowledged ? "SUCCESS" : "FAILED"));
        }
        
        // Test 4: Error handling integration
        System.out.println("\n4. Testing Error Handling:");
        
        // Invalid supply request (over quota)
        Optional<SupplyRequest> invalidReq = supply.submitRequest(resident, SupplyItem.WATER, 20);
        System.out.println("   Invalid Request Handling: " + (!invalidReq.isPresent() ? "CORRECTLY BLOCKED" : "SHOULD HAVE BEEN BLOCKED"));
        
        // Invalid issue report (empty description)
        Optional<Issue> invalidIssue = issue.submitIssue(resident, IssueCategory.HEALTH, "", "HIGH");
        System.out.println("   Invalid Issue Handling: " + (!invalidIssue.isPresent() ? "CORRECTLY BLOCKED" : "SHOULD HAVE BEEN BLOCKED"));
        
        System.out.println("\n=== Integration Test Complete ===");
    }
}
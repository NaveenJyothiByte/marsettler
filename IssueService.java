import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages issue reporting and automatic routing to appropriate technicians/operators
 * Handles UAT-S2-06 through UAT-S2-08
 */
public class IssueService {
    private final Map<String, List<Issue>> userIssues = new ConcurrentHashMap<>();
    private final Map<String, List<Issue>> assignedIssues = new ConcurrentHashMap<>();
    private int nextIssueId = 2000;

    /**
     * Submits a new issue report with validation
     * UAT-S2-06: Resident submits valid issue report
     * UAT-S2-07: Form validation for missing fields
     */
    public Optional<Issue> submitIssue(String username, IssueCategory category, 
                                     String description, String priority) {
        // Validation (UAT-S2-07)
        if (description == null || description.trim().isEmpty()) {
            return Optional.empty();
        }
        if (priority == null || priority.trim().isEmpty()) {
            return Optional.empty();
        }

        // Create issue
        String issueId = "ISS-" + (nextIssueId++);
        Issue issue = new Issue(issueId, username, category, description.trim(), priority.trim());
        
        // Store in user's issue history
        userIssues.computeIfAbsent(username, k -> new ArrayList<>()).add(issue);
        
        // Auto-route to appropriate role (UAT-S2-08)
        String assigneeRole = category.getAssignedRole().name();
        assignedIssues.computeIfAbsent(assigneeRole, k -> new ArrayList<>()).add(issue);
        
        return Optional.of(issue); // UAT-S2-06
    }

    public List<Issue> getUserIssues(String username) {
        return userIssues.getOrDefault(username, new ArrayList<>())
                .stream()
                .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
                .collect(Collectors.toList());
    }

    public List<Issue> getAssignedIssues(Role role) {
        return assignedIssues.getOrDefault(role.name(), new ArrayList<>())
                .stream()
                .filter(issue -> "NEW".equals(issue.status) || "ACKNOWLEDGED".equals(issue.status))
                .collect(Collectors.toList());
    }

    public boolean acknowledgeIssue(String issueId, Role role) {
        List<Issue> issues = assignedIssues.get(role.name());
        if (issues != null) {
            for (Issue issue : issues) {
                if (issue.issueId.equals(issueId)) {
                    issue.status = "ACKNOWLEDGED";
                    return true;
                }
            }
        }
        return false;
    }
}
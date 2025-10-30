import java.time.Instant;

public class Issue {
    public final String issueId;
    public final String username;
    public final IssueCategory category;
    public final String description;
    public final String priority;
    public final Instant timestamp;
    public String status;
    public String assignedTo;

    public Issue(String issueId, String username, IssueCategory category, 
                String description, String priority) {
        this.issueId = issueId;
        this.username = username;
        this.category = category;
        this.description = description;
        this.priority = priority;
        this.timestamp = Instant.now();
        this.status = "NEW";
        this.assignedTo = category.getAssignedRole().name();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s - %s - %s", 
            issueId, category.getDisplayName(), priority, status, description);
    }
}
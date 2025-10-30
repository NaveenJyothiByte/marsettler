public enum IssueCategory {
    MAINTENANCE("Maintenance", Role.INFRASTRUCTURE_TECHNICIAN),
    HEALTH("Health Concern", Role.MISSION_CONTROL_OPERATOR),
    ENVIRONMENTAL("Environmental", Role.MISSION_CONTROL_OPERATOR),
    SAFETY("Safety Hazard", Role.MISSION_CONTROL_OPERATOR),
    OTHER("Other", Role.MISSION_CONTROL_OPERATOR);

    private final String displayName;
    private final Role assignedRole;

    IssueCategory(String displayName, Role assignedRole) {
        this.displayName = displayName;
        this.assignedRole = assignedRole;
    }

    public String getDisplayName() { return displayName; }
    public Role getAssignedRole() { return assignedRole; }
}
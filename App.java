// Add these instance variables to the existing App class
private EmergencyService emergencyService;
private TaskAssignmentService taskAssignmentService;
private TechnicianService technicianService;

// Update the bootstrap method
private void bootstrap() {
    store.seedSamples();
    auth = new AuthService(store.backingMap(), 5, Duration.ofMinutes(15));
    schedule = new ScheduleService();
    backup = new BackupService();
    uptime = new UptimeMonitor();
    
    // Sprint 3 services
    emergencyService = new EmergencyService();
    taskAssignmentService = new TaskAssignmentService();
    technicianService = new TechnicianService();
    
    schedule.seedResidentTasks("resident.valid@mars.local");
}

// Add these new methods to the App class for Sprint 3 features

/**
 * Emergency alert broadcasting - OPERATOR3
 */
private void doEmergencyAlert() {
    if (!ensureActiveSession()) return;
    if (currentUser.role != Role.MISSION_CONTROL_OPERATOR) {
        System.out.println("Access denied: Only Mission Control Operators can broadcast emergency alerts.");
        Input.pause();
        return;
    }
    
    System.out.println("\n=== EMERGENCY ALERT CONSOLE ===");
    System.out.println("Emergency Types:");
    System.out.println("1) HABITAT BREACH - Immediate evacuation");
    System.out.println("2) RADIATION STORM - Seek shelter");
    System.out.println("3) FIRE - Deploy suppression");
    System.out.println("4) LIFE SUPPORT FAILURE - Emergency protocols");
    System.out.println("5) CUSTOM ALERT");
    
    int alertTypeChoice = Input.intRange("Select emergency type (1-5): ", 1, 5);
    AlertType alertType = getAlertTypeFromChoice(alertTypeChoice);
    
    String message = Input.line("Enter alert message: ");
    
    System.out.println("Severity Levels:");
    System.out.println("1) CRITICAL - Immediate life-threatening");
    System.out.println("2) HIGH - Serious threat");
    System.out.println("3) MEDIUM - Moderate risk");
    System.out.println("4) LOW - Minor issue");
    
    int severityChoice = Input.intRange("Select severity (1-4): ", 1, 4);
    Severity severity = getSeverityFromChoice(severityChoice);
    
    // Broadcast alert
    List<String> deliveredUsers = emergencyService.broadcastEmergencyAlert(
        alertType, message, severity, currentUser.userId);
    
    System.out.println("\n🚨 EMERGENCY ALERT BROADCAST!");
    System.out.println("Alert sent to " + deliveredUsers.size() + " users.");
    System.out.println("All users have been notified.");
    Input.pause();
}

/**
 * Task assignment - OPERATOR2
 */
private void doAssignTask() {
    if (!ensureActiveSession()) return;
    if (currentUser.role != Role.MISSION_CONTROL_OPERATOR) {
        System.out.println("Access denied: Only Mission Control Operators can assign tasks.");
        Input.pause();
        return;
    }
    
    System.out.println("\n=== TASK ASSIGNMENT ===");
    
    // Get available residents (simplified - in real system would query user store)
    System.out.println("Available Residents:");
    System.out.println("1) resident.valid@mars.local");
    System.out.println("2) resident2@mars.local");
    System.out.println("3) resident3@mars.local");
    
    int residentChoice = Input.intRange("Select resident (1-3): ", 1, 3);
    String assigneeUsername = getResidentUsername(residentChoice);
    
    String taskTitle = Input.line("Enter task title: ");
    int priority = Input.intRange("Enter priority (1=High, 5=Low): ", 1, 5);
    LocalDate date = Input.dateReq("Enter due date (YYYY-MM-DD): ");
    LocalTime time = Input.timeReq("Enter due time (HH:MM): ");
    
    AssignmentResult result = taskAssignmentService.assignTask(
        currentUser.userId, assigneeUsername, taskTitle, priority, date, time);
    
    if (result.isSuccess()) {
        System.out.println("\n✅ Task assigned successfully!");
        System.out.println("Task ID: " + result.getTaskId());
        System.out.println("Assigned to: " + assigneeUsername);
    } else {
        System.out.println("\n❌ Task assignment failed: " + result.getMessage());
    }
    Input.pause();
}

/**
 * Technician alert management - TECH1
 */
private void doViewAssignedAlerts() {
    if (!ensureActiveSession()) return;
    if (currentUser.role != Role.INFRASTRUCTURE_TECHNICIAN) {
        System.out.println("Access denied: Only Infrastructure Technicians can view assigned alerts.");
        Input.pause();
        return;
    }
    
    System.out.println("\n=== ASSIGNED ALERTS ===");
    List<Alert> alerts = technicianService.getAssignedAlerts(currentUser.username);
    
    if (alerts.isEmpty()) {
        System.out.println("No active alerts assigned to you.");
    } else {
        for (int i = 0; i < alerts.size(); i++) {
            Alert alert = alerts.get(i);
            System.out.println((i + 1) + ") " + alert);
            boolean acknowledged = emergencyService.hasAcknowledgedAlert(currentUser.username, alert.getAlertId());
            System.out.println("   Status: " + (acknowledged ? "ACKNOWLEDGED" : "PENDING ACKNOWLEDGMENT"));
        }
        
        // Option to acknowledge
        System.out.println("\nEnter alert number to acknowledge (0 to cancel): ");
        int choice = Input.intVal("");
        if (choice > 0 && choice <= alerts.size()) {
            String alertId = alerts.get(choice - 1).getAlertId();
            if (technicianService.acknowledgeTechnicianAlert(currentUser.username, alertId)) {
                System.out.println("✅ Alert acknowledged!");
            } else {
                System.out.println("❌ Failed to acknowledge alert.");
            }
        }
    }
    Input.pause();
}

/**
 * System diagnostics - TECH2
 */
private void doRunDiagnostics() {
    if (!ensureActiveSession()) return;
    if (currentUser.role != Role.INFRASTRUCTURE_TECHNICIAN) {
        System.out.println("Access denied: Only Infrastructure Technicians can run diagnostics.");
        Input.pause();
        return;
    }
    
    System.out.println("\n=== SYSTEM DIAGNOSTICS ===");
    System.out.println("Available Systems:");
    System.out.println("1) Life Support Systems");
    System.out.println("2) Power Distribution");
    System.out.println("3) Communication Array");
    System.out.println("4) Water Reclamation");
    System.out.println("5) Thermal Control");
    
    int systemChoice = Input.intRange("Select system (1-5): ", 1, 5);
    SystemComponent component = getSystemComponentFromChoice(systemChoice);
    
    System.out.println("Running diagnostics on " + component + "...");
    
    // Simulate diagnostic processing time
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    
    DiagnosticReport report = technicianService.runSystemDiagnostics(currentUser.username, component);
    
    System.out.println("\n=== DIAGNOSTIC REPORT ===");
    System.out.println("Report ID: " + report.getReportId());
    System.out.println("System: " + report.getComponent());
    System.out.println("Technician: " + report.getTechnicianId());
    System.out.println("Generated: " + report.getGeneratedAt());
    System.out.println("\nTest Results:");
    
    for (Map.Entry<String, String> entry : report.getTestResults().entrySet()) {
        String status = entry.getValue();
        String icon = status.equals("NORMAL") ? "✅" : status.startsWith("WARNING") ? "⚠️" : "❌";
        System.out.println(icon + " " + entry.getKey() + ": " + entry.getValue());
    }
    
    Input.pause();
}

/**
 * Maintenance scheduling - TECH3
 */
private void doScheduleMaintenance() {
    if (!ensureActiveSession()) return;
    if (currentUser.role != Role.INFRASTRUCTURE_TECHNICIAN) {
        System.out.println("Access denied: Only Infrastructure Technicians can schedule maintenance.");
        Input.pause();
        return;
    }
    
    System.out.println("\n=== MAINTENANCE SCHEDULING ===");
    System.out.println("Maintenance Types:");
    System.out.println("1) Preventive - Routine check");
    System.out.println("2) Corrective - Repair issue");
    System.out.println("3) Emergency - Critical fix");
    
    int typeChoice = Input.intRange("Select type (1-3): ", 1, 3);
    MaintenanceType maintenanceType = getMaintenanceTypeFromChoice(typeChoice);
    
    System.out.println("Systems:");
    System.out.println("1) Life Support");
    System.out.println("2) Power Distribution");
    System.out.println("3) Communication");
    System.out.println("4) Water Reclamation");
    System.out.println("5) Thermal Control");
    
    int systemChoice = Input.intRange("Select system (1-5): ", 1, 5);
    SystemComponent component = getSystemComponentFromChoice(systemChoice);
    
    String description = Input.line("Enter maintenance description: ");
    LocalDate scheduledDate = Input.dateReq("Enter scheduled date (YYYY-MM-DD): ");
    LocalTime startTime = Input.timeReq("Enter start time (HH:MM): ");
    int durationHours = Input.intRange("Enter duration in hours: ", 1, 24);
    
    try {
        MaintenanceTask task = technicianService.scheduleMaintenance(
            currentUser.username, component, description, maintenanceType,
            scheduledDate, startTime, durationHours);
        
        System.out.println("\n✅ Maintenance scheduled successfully!");
        System.out.println("Work Order: " + task.getTaskId());
        System.out.println("Scheduled: " + scheduledDate + " at " + startTime);
        System.out.println("Duration: " + durationHours + " hours");
        
    } catch (IllegalArgumentException e) {
        System.out.println("\n❌ Scheduling failed: " + e.getMessage());
    }
    
    Input.pause();
}

// Helper methods for enum conversions
private AlertType getAlertTypeFromChoice(int choice) {
    switch (choice) {
        case 1: return AlertType.HABITAT_BREACH;
        case 2: return AlertType.RADIATION_STORM;
        case 3: return AlertType.FIRE;
        case 4: return AlertType.LIFE_SUPPORT_FAILURE;
        case 5: return AlertType.CUSTOM;
        default: return AlertType.CUSTOM;
    }
}

private Severity getSeverityFromChoice(int choice) {
    switch (choice) {
        case 1: return Severity.CRITICAL;
        case 2: return Severity.HIGH;
        case 3: return Severity.MEDIUM;
        case 4: return Severity.LOW;
        default: return Severity.MEDIUM;
    }
}

private String getResidentUsername(int choice) {
    switch (choice) {
        case 1: return "resident.valid@mars.local";
        case 2: return "resident2@mars.local";
        case 3: return "resident3@mars.local";
        default: return "resident.valid@mars.local";
    }
}

private SystemComponent getSystemComponentFromChoice(int choice) {
    switch (choice) {
        case 1: return SystemComponent.LIFE_SUPPORT;
        case 2: return SystemComponent.POWER_DISTRIBUTION;
        case 3: return SystemComponent.COMMUNICATION_ARRAY;
        case 4: return SystemComponent.WATER_RECLAMATION;
        case 5: return SystemComponent.THERMAL_CONTROL;
        default: return SystemComponent.LIFE_SUPPORT;
    }
}

private MaintenanceType getMaintenanceTypeFromChoice(int choice) {
    switch (choice) {
        case 1: return MaintenanceType.PREVENTIVE;
        case 2: return MaintenanceType.CORRECTIVE;
        case 3: return MaintenanceType.EMERGENCY;
        default: return MaintenanceType.PREVENTIVE;
    }
}

// Update the operator menu in the loop() method to include Sprint 3 options
private void loop() {
    while (true) {
        printHeader();
        
        if (!isLoggedIn()) {
            // Existing login menu...
        } else {
            // Base menu options for all roles
            System.out.println("2) View schedule by date");
            System.out.println("3) Add/Update a task");
            // ... other existing options ...
            System.out.println("9) View alerts");
            
            // Role-specific Sprint 3 options
            if (currentUser.role == Role.MISSION_CONTROL_OPERATOR) {
                System.out.println("12) Assign Tasks (OPERATOR2)");
                System.out.println("13) Emergency Console (OPERATOR3)");
                System.out.println("14) Performance Metrics");
            }
            
            if (currentUser.role == Role.INFRASTRUCTURE_TECHNICIAN) {
                System.out.println("15) View Assigned Alerts (TECH1)");
                System.out.println("16) Run Diagnostics (TECH2)");
                System.out.println("17) Schedule Maintenance (TECH3)");
            }
            
            System.out.println("10) Logout");
            System.out.println("11) Exit");
            
            int c = Input.intVal("Choose an option: ");
            
            switch (c) {
                // Existing cases...
                case 12: if (currentUser.role == Role.MISSION_CONTROL_OPERATOR) doAssignTask(); break;
                case 13: if (currentUser.role == Role.MISSION_CONTROL_OPERATOR) doEmergencyAlert(); break;
                case 14: if (currentUser.role == Role.MISSION_CONTROL_OPERATOR) doPerformanceMetrics(); break;
                case 15: if (currentUser.role == Role.INFRASTRUCTURE_TECHNICIAN) doViewAssignedAlerts(); break;
                case 16: if (currentUser.role == Role.INFRASTRUCTURE_TECHNICIAN) doRunDiagnostics(); break;
                case 17: if (currentUser.role == Role.INFRASTRUCTURE_TECHNICIAN) doScheduleMaintenance(); break;
                // ... rest of existing cases
            }
        }
    }
}

/**
 * Performance metrics display - OPERATOR2
 */
private void doPerformanceMetrics() {
    if (!ensureActiveSession()) return;
    if (currentUser.role != Role.MISSION_CONTROL_OPERATOR) {
        System.out.println("Access denied: Only Mission Control Operators can view performance metrics.");
        Input.pause();
        return;
    }
    
    System.out.println("\n=== PERFORMANCE METRICS ===");
    Map<String, TaskCompletionStats> allStats = taskAssignmentService.getAllUserStats();
    
    if (allStats.isEmpty()) {
        System.out.println("No task completion data available.");
    } else {
        System.out.printf("%-25s %-8s %-8s %-8s %-12s%n", 
            "User", "Assigned", "Completed", "Pending", "Completion Rate");
        System.out.println("------------------------------------------------------------------------");
        
        for (TaskCompletionStats stats : allStats.values()) {
            System.out.printf("%-25s %-8d %-8d %-8d %-11.1f%%%n",
                stats.getUsername(), stats.getTotalAssigned(), stats.getTotalCompleted(),
                stats.getPending(), stats.getCompletionRate());
        }
    }
    
    Input.pause();
}
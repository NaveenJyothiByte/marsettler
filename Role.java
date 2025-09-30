public enum Role {
    COLONY_RESIDENT,
    MISSION_CONTROL_OPERATOR,
    INFRASTRUCTURE_TECHNICIAN;

    public static String pretty(Role r) {
        switch (r) {
            case COLONY_RESIDENT: return "Colony Resident";
            case MISSION_CONTROL_OPERATOR: return "Mission Control Operator";
            case INFRASTRUCTURE_TECHNICIAN: return "Infrastructure Technician";
            default: return r.name();
        }
    }
}

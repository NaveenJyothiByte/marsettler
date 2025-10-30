public enum SupplyItem {
    WATER("Water", "L"),
    OXYGEN("Oxygen", "kg"),
    FOOD_A("Food Pack A", "units"),
    FOOD_B("Food Pack B", "units"),
    MEDICAL("Medical Kit", "units");

    private final String displayName;
    private final String unit;

    SupplyItem(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() { return displayName; }
    public String getUnit() { return unit; }

    public static SupplyItem fromString(String text) {
        for (SupplyItem item : SupplyItem.values()) {
            if (item.displayName.equalsIgnoreCase(text)) {
                return item;
            }
        }
        return null;
    }
}
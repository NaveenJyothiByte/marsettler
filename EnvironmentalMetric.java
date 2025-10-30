public class EnvironmentalMetric {
    private final String name;
    private double value;
    private final String unit;

    public EnvironmentalMetric(String name, double value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public void setValue(double value) { this.value = value; }

    @Override
    public String toString() {
        return String.format("%s: %.1f%s", name, value, unit);
    }
}
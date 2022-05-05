package Lab2.homeautomation.shared;

public final class Temperature {
    double value;
    String unit;

    public Temperature(double value, String unit) {
        this.unit = unit;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return "Temperature: " + value + " " + unit;
    }
}

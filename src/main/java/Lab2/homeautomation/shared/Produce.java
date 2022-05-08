package Lab2.homeautomation.shared;

import java.util.Locale;

public class Produce {

    private String name;
    private int weight;
    private double price;


    public static Produce create(String name) {
        Produce produce = null;
        name = name.toLowerCase();
        switch (name) {
            case "milk":
                produce = new Produce(name, 1000, 0.99);
                break;
            case "cheese":
                produce = new Produce(name, 500, 4.99);
                break;
            case "curryking":
                produce = new Produce(name, 150, 2.99);
                break;
            case "eggs":
                produce = new Produce(name, 100, 2.99);
                break;
            case "beer":
                produce = new Produce(name, 700, 1.49);
                break;
        }
        return produce;
    }

    public Produce(String name, int weight, double price) {
        this.name = name;
        this.weight = weight;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public double getPrice() {
        return price;
    }
}

package Lab2.homeautomation.shared;

import java.util.Random;

public enum Weather {
    SUNNY,
    RAINY;

    private static Random random = new Random();

    public static Weather random() {
        Weather result = null;
        if(random.nextBoolean()){
            result = SUNNY;
        }
        else{
            result = RAINY;
        }
        return result;
    }
}

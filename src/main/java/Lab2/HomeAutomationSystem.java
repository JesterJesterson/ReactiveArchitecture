package Lab2;

import Lab2.homeautomation.HomeAutomationController;
import Lab2.homeautomation.outside.TemperatureEnvironment;
import akka.actor.typed.ActorSystem;

public class HomeAutomationSystem {
    public static void main(String[] args) {
        ActorSystem<Void> home = ActorSystem.create(HomeAutomationController.create(), "HomeAutomation");
    }
}

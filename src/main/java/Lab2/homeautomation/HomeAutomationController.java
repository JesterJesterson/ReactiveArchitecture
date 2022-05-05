package Lab2.homeautomation;

import Lab2.homeautomation.devices.AirCondition;
import Lab2.homeautomation.devices.TemperatureSensor;
import Lab2.homeautomation.outside.TemperatureEnvironment;
import Lab2.homeautomation.ui.UI;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;

public class HomeAutomationController extends AbstractBehavior<Void>{

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);
        //create devices
        ActorRef<AirCondition.AirConditionCommand> airCondition = getContext().spawn(AirCondition.create("2", "1"), "airCondition");
        ActorRef<TemperatureSensor.TemperatureCommand> tempSensor = getContext().spawn(TemperatureSensor.create(airCondition, "1", "1"), "temperatureSensor");

        //create environments
        ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnvironment = getContext().spawn(TemperatureEnvironment.create(16, tempSensor), "temperatureEnvironment");


        ActorRef<Void> ui = getContext().spawn(UI.create(tempSensor, airCondition), "UI");
        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}

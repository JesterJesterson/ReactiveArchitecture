package Lab2.homeautomation;

import Lab2.homeautomation.devices.*;
import Lab2.homeautomation.fridge.Fridge;
import Lab2.homeautomation.outside.TemperatureEnvironment;
import Lab2.homeautomation.outside.WeatherEnvironment;
import Lab2.homeautomation.shared.Weather;
import Lab2.homeautomation.ui.UI;
import akka.actor.Actor;
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
        ActorRef<Blinds.BlindsCommand> blinds = getContext().spawn(Blinds.create("2","2"), "blinds");
        ActorRef<MediaStation.MediaStationCommand> mediaStation = getContext().spawn(MediaStation.create(blinds, "2", "3"), "mediaStation");
        ActorRef<Fridge.FridgeCommand> fridge = getContext().spawn(Fridge.create("2","4"), "fridge");

        ActorRef<TemperatureSensor.TemperatureCommand> tempSensor = getContext().spawn(TemperatureSensor.create(airCondition, "1", "1"), "temperatureSensor");
        ActorRef<WeatherSensor.WeatherSensorCommand> weatherSensor = getContext().spawn(WeatherSensor.create(blinds,"1","2"),"weatherSensor");

        //create environments
        ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnvironment = getContext().spawn(TemperatureEnvironment.create(16, tempSensor), "temperatureEnvironment");
        ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnvironment = getContext().spawn(WeatherEnvironment.create(Weather.SUNNY, weatherSensor), "weatherEnvironment");


        ActorRef<Void> ui = getContext().spawn(UI.create(tempEnvironment, airCondition, weatherEnvironment, mediaStation, fridge), "UI");
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

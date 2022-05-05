package Lab2.homeautomation.ui;

import Lab2.homeautomation.devices.AirCondition;
import Lab2.homeautomation.devices.MediaStation;
import Lab2.homeautomation.outside.TemperatureEnvironment;
import Lab2.homeautomation.outside.WeatherEnvironment;
import Lab2.homeautomation.shared.Movie;
import Lab2.homeautomation.shared.Weather;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import Lab2.homeautomation.devices.TemperatureSensor;

import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnv;
    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnv;
    private ActorRef<MediaStation.MediaStationCommand> mediaStation;

    public static Behavior<Void> create(ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnv,
                                        ActorRef<AirCondition.AirConditionCommand> airCondition,
                                        ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnv,
                                        ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        return Behaviors.setup(context -> new UI(context, tempEnv, airCondition, weatherEnv, mediaStation));
    }

    private  UI(ActorContext<Void> context,
                ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnv,
                ActorRef<AirCondition.AirConditionCommand> airCondition,
                ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnv,
                ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        super(context);
        this.airCondition = airCondition;
        this.tempEnv = tempEnv;
        this.weatherEnv = weatherEnv;
        this.mediaStation = mediaStation;
        new Thread(() -> { this.runCommandLine(); }).start();

        getContext().getLog().info("[UI] started");
    }

    public void runCommandLine() {

        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            String[] command = reader.split(" ");
            if(command[0].equals("t")) {
                this.tempEnv.tell(new TemperatureEnvironment.TemperatureUpdate(Optional.of(Double.valueOf(command[1]))));
            }
            if(command[0].equals("ac")) {
                this.airCondition.tell(new AirCondition.PowerAirCondition(Optional.of(Boolean.valueOf(command[1]))));
            }
            if(command[0].equals("w")){
                this.weatherEnv.tell((new WeatherEnvironment.WeatherUpdate(Optional.of(Weather.valueOf(command[1].toUpperCase())))));
            }
            if(command[0].equals("ms")){
                if (command[1].equals("play")){
                    String movieTitle = "";
                    for (int i = 2; i < command.length; i++) {
                        movieTitle += command[i];
                        movieTitle += " ";
                    }
                    this.mediaStation.tell(new MediaStation.ChangedStatus(Optional.of(new Movie(movieTitle))));
                } else if(command[1].equals("stop")){
                    this.mediaStation.tell(new MediaStation.ChangedStatus(Optional.empty()));
                }

            }
        }
        getContext().getLog().info("UI done");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }
}

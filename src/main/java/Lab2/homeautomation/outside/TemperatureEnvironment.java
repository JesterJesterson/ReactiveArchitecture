package Lab2.homeautomation.outside;

import Lab2.homeautomation.devices.TemperatureSensor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public class TemperatureEnvironment extends AbstractBehavior<TemperatureEnvironment.TemperatureEnvironmentCommand> {

    public interface TemperatureEnvironmentCommand{}

    private double currentTemperature = 15.0;
    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private final TimerScheduler<TemperatureEnvironmentCommand> temperatureTimeScheduler;

    public static final class TemperatureUpdate implements TemperatureEnvironmentCommand{
        double currentTemp;

        public TemperatureUpdate(double currentTemp) {
            this.currentTemp = currentTemp;
        }
    }

    public TemperatureEnvironment(ActorContext<TemperatureEnvironmentCommand> context, double initTemp, TimerScheduler<TemperatureEnvironmentCommand> temperatureTimeSchedule, ActorRef<TemperatureSensor.TemperatureCommand> sensor) {
        super(context);
        this.currentTemperature = initTemp;
        this.temperatureTimeScheduler = temperatureTimeSchedule;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureUpdate(currentTemperature), Duration.ofSeconds(5));
        this.tempSensor = sensor;
    }

    //Aus der Fragestunde übernommen. Weg um ein neuen Wert mit Hilfe eines Timers abzufragen.
    public static Behavior<TemperatureEnvironmentCommand> create(double initTemp, ActorRef<TemperatureSensor.TemperatureCommand> sensor){
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new TemperatureEnvironment(context, initTemp, timers, sensor)));
    }

    @Override
    public Receive<TemperatureEnvironment.TemperatureEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureUpdate.class, this::onTemperatureUpdate)
                .build();
    }

    private Behavior<TemperatureEnvironmentCommand> onTemperatureUpdate(TemperatureUpdate t) {

        Random random = new Random();
        double temperatureChange = (random.nextInt(30+30) -30)/ (double) 10;


        currentTemperature = currentTemperature+temperatureChange;
        tempSensor.tell(new TemperatureSensor.ReadTemperature(currentTemperature));
        getContext().getLog().info("Temperature changed by " + temperatureChange+ " to "+ currentTemperature);
        return this;
    }
}

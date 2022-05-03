package Lab2.homeautomation.outside;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Random;

public class TemperatureEnvironment extends AbstractBehavior<TemperatureEnvironment.TemperatureUpdateCommand> {

    public interface TemperatureUpdateCommand{}

    private double currentTemperature = 15.0;
    private boolean setHighTemp = false;
    private boolean setLowTemp = true;
    private final TimerScheduler<TemperatureUpdateCommand> temperatureTimeScheduler;

    public static final class TemperatureUpdate implements TemperatureUpdateCommand{
        double currentTemp;

        public TemperatureUpdate(double currentTemp) {
            this.currentTemp = currentTemp;
        }
    }

    public TemperatureEnvironment(ActorContext<TemperatureUpdateCommand> context, double initTemp, TimerScheduler<TemperatureUpdateCommand> temperatureTimeSchedule) {
        super(context);
        this.currentTemperature = initTemp;
        this.temperatureTimeScheduler = temperatureTimeSchedule;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureUpdate(currentTemperature), Duration.ofSeconds(5));
    }

    //Aus der Fragestunde übernommen. Weg um ein neuen Wert mit Hilfe eines Timers abzufragen.
    public static Behavior<TemperatureUpdateCommand> create(double initTemp){
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new TemperatureEnvironment(context, initTemp, timers)));
    }

    @Override
    public Receive<TemperatureEnvironment.TemperatureUpdateCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureUpdate.class, this::onTemperatureUpdate)
                .build();
    }

    private Behavior<TemperatureUpdateCommand> onTemperatureUpdate(TemperatureUpdate t) {

        Random random = new Random();
        double temperatureChange = (random.nextInt(62) - 31) / (double) 10;


        currentTemperature = currentTemperature+temperatureChange;
        getContext().getLog().info("Temperature changed by " + temperatureChange+ " to "+ currentTemperature);
        return this;
    }
}

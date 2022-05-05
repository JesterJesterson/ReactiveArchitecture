package Lab2.homeautomation.outside;

import Lab2.homeautomation.devices.TemperatureSensor;
import Lab2.homeautomation.devices.WeatherSensor;
import Lab2.homeautomation.shared.Weather;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public class WeatherEnvironment extends AbstractBehavior<WeatherEnvironment.WeatherEnvironmentCommand> {

    public interface WeatherEnvironmentCommand {}

    private Weather currentWeather = null;
    private ActorRef<WeatherSensor.WeatherSensorCommand> weatherSensor;
    private final TimerScheduler<WeatherEnvironment.WeatherEnvironmentCommand> weatherTimeScheduler;

    public static final class WeatherUpdate implements WeatherEnvironment.WeatherEnvironmentCommand {
        Optional<Weather> newWeather;

        public WeatherUpdate(Optional<Weather> newWeather) {
            this.newWeather = newWeather;
        }
    }

    public WeatherEnvironment(ActorContext<WeatherEnvironment.WeatherEnvironmentCommand> context, Weather initWeather, TimerScheduler<WeatherEnvironment.WeatherEnvironmentCommand> weatherTimeScheduler, ActorRef<WeatherSensor.WeatherSensorCommand> sensor) {
        super(context);
        this.currentWeather = initWeather;
        this.weatherTimeScheduler = weatherTimeScheduler;
        this.weatherTimeScheduler.startTimerAtFixedRate(new WeatherEnvironment.WeatherUpdate(Optional.empty()), Duration.ofSeconds(30));
        this.weatherSensor = sensor;
    }

    //Aus der Fragestunde übernommen. Weg um ein neuen Wert mit Hilfe eines Timers abzufragen.
    public static Behavior<WeatherEnvironmentCommand> create(Weather initWeather, ActorRef<WeatherSensor.WeatherSensorCommand> sensor){
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new WeatherEnvironment(context, initWeather, timers, sensor)));
    }

    @Override
    public Receive<WeatherEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeatherUpdate.class, this::onWeatherUpdate)
                .build();
    }

    private Behavior<WeatherEnvironmentCommand> onWeatherUpdate(WeatherUpdate t) {

        if (t.newWeather.isPresent()){
            this.currentWeather = t.newWeather.get();
        }else{
            this.currentWeather = Weather.random();
        }

        getContext().getLog().info("[WeatherEnv] Changed weather to "+ currentWeather);
        weatherSensor.tell(new WeatherSensor.ReadWeather(currentWeather));

        return this;
    }
}

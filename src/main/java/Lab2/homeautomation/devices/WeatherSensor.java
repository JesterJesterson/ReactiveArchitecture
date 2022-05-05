package Lab2.homeautomation.devices;

import Lab2.homeautomation.shared.Weather;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherSensorCommand> {
    public interface WeatherSensorCommand {}

    public static final class ReadWeather implements WeatherSensor.WeatherSensorCommand {
        final Weather weather;

        public ReadWeather(Weather weather) {
            this.weather = weather;
        }
    }

    public static Behavior<WeatherSensorCommand> create(ActorRef<Blinds.BlindsCommand> blinds,String groupId, String deviceId) {
        return Behaviors.setup(context -> new WeatherSensor(context, blinds, groupId, deviceId));
    }

    private final String groupId;
    private final String deviceId;
    private final ActorRef<Blinds.BlindsCommand> blinds;

    public WeatherSensor(ActorContext<WeatherSensorCommand> context, ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.blinds = blinds;
        getContext().getLog().info("[WeatherSens] started");
    }

    @Override
    public Receive<WeatherSensor.WeatherSensorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadWeather.class, this::onReadWeather)
                .build();
    }

    private Behavior<WeatherSensor.WeatherSensorCommand> onReadWeather(WeatherSensor.ReadWeather r) {
        getContext().getLog().info("[WeatherSens] received {}", r.weather);
        this.blinds.tell(new Blinds.ReceivedWeather(r.weather));
        return this;
    }
}

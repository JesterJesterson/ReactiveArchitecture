package Lab2.homeautomation.devices;

import Lab2.homeautomation.shared.Temperature;
import Lab2.homeautomation.shared.Weather;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Optional;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {
    public interface BlindsCommand {}

    public static final class ReceivedWeather implements Blinds.BlindsCommand {
        Weather weather;

        public ReceivedWeather(Weather weather) {
            this.weather = weather;
        }
    }

    public static final class MoviePlayingStatus implements BlindsCommand {
        Boolean status;

        public MoviePlayingStatus(Boolean status) {
            this.status = status;
        }
    }

    private final String groupId;
    private final String deviceId;
    private boolean blindsClosed = true;
    private boolean isMoviePlaying = false;

    public Blinds(ActorContext<Blinds.BlindsCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("[BLINDS] Started");
    }

    public static Behavior<Blinds.BlindsCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Blinds(context, groupId, deviceId));
    }

    @Override
    public Receive<Blinds.BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceivedWeather.class, this::onReadWeather)
                .onMessage(MoviePlayingStatus.class, this::onMoviePlayingStatus)
                .build();
    }

    private Behavior<Blinds.BlindsCommand> onReadWeather(ReceivedWeather r) {
        if (!isMoviePlaying){
            if(r.weather == Weather.RAINY) {
                getContext().getLog().info("[BLINDS] Received weather {} - opened", r.weather);
                this.blindsClosed =  false;
            } else {
                getContext().getLog().info("[BLINDS] Received weather {} - closed", r.weather);
                this.blindsClosed = true;
            }
        }else {
            getContext().getLog().info("[BLINDS] Ignoring weather command - movie is playing");
        }


        return Behaviors.same();
    }

    private Behavior<Blinds.BlindsCommand> onMoviePlayingStatus(MoviePlayingStatus r) {
        if(r.status) {
            getContext().getLog().info("[BLINDS] Movie starting - Blinds closed");
            this.isMoviePlaying = true;
            this.blindsClosed = true;
        }
        else {
            getContext().getLog().info("[BLINDS] Movie stopped - Blinds opened");
            this.isMoviePlaying =  false;
            this.blindsClosed = false;
        }

        return Behaviors.same();
    }
}

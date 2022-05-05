package Lab2.homeautomation.devices;

import Lab2.homeautomation.shared.Movie;
import Lab2.homeautomation.shared.Weather;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Optional;

public class MediaStation extends AbstractBehavior<MediaStation.MediaStationCommand> {
    public interface MediaStationCommand {}

    public static final class ChangedStatus implements MediaStationCommand {
        Optional<Movie> movie;

        public ChangedStatus(Optional<Movie> movie) {
            this.movie = movie;
        }
    }

    private ActorRef<Blinds.BlindsCommand> blinds;
    private Movie currentMovie;
    private String groupId;
    private String deviceId;

    public MediaStation(ActorContext<MediaStationCommand> context, ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.blinds = blinds;
        getContext().getLog().info("[MS] Started");
    }

    public static Behavior<MediaStationCommand> create(ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId) {
        return Behaviors.setup(context -> new MediaStation(context, blinds, groupId, deviceId));
    }

    @Override
    public Receive<MediaStation.MediaStationCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ChangedStatus.class, this::onChangedStatus)
                .build();
    }

    private Behavior<MediaStationCommand> onChangedStatus(MediaStation.ChangedStatus r) {
        if(r.movie.isPresent()) {
            if(currentMovie == null){
                getContext().getLog().info("[MS] Starting movie " + r.movie.get().getTitle());
                this.currentMovie = r.movie.get();
                this.blinds.tell(new Blinds.MoviePlayingStatus(true));
            }else{
                getContext().getLog().info("[MS] Movie " + currentMovie.getTitle() +" already playing");
            }

        }
        else {
            getContext().getLog().info("[MS] Stopped movie");
            this.currentMovie = null;
            this.blinds.tell(new Blinds.MoviePlayingStatus(false));
        }

        return Behaviors.same();
    }
}

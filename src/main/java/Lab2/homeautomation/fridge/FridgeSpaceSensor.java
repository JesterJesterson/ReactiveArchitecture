package Lab2.homeautomation.fridge;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.FridgeSpaceSensorCommand> {

    public interface FridgeSpaceSensorCommand { }

    public static final class AddSpaceCommand implements FridgeSpaceSensorCommand {
        public AddSpaceCommand() {
        }
    }

    public static final class RemoveSpaceCommand implements FridgeSpaceSensorCommand {
        public RemoveSpaceCommand() {

        }
    }

    public static Behavior<FridgeSpaceSensorCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new FridgeSpaceSensor(context, groupId, deviceId));
    }

    private final int maxSpace = 10;
    private int currentSpace = 0;
    private String groupId;
    private String deviceId;

    public FridgeSpaceSensor(ActorContext<FridgeSpaceSensorCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("[FSS] Started");
    }

    @Override
    public Receive<FridgeSpaceSensorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddSpaceCommand.class, this::onItemAdd)
                .onMessage(RemoveSpaceCommand.class, this::onItemRemove)
                .build();
    }

    private Behavior<FridgeSpaceSensorCommand> onItemAdd(AddSpaceCommand command){
        currentSpace += 1;
        getContext().getLog().info("[FWS] Item was added to the fridge - current itemcount " + currentSpace + " (max " + maxSpace + ")");
        return this;
    }

    private Behavior<FridgeSpaceSensorCommand> onItemRemove(RemoveSpaceCommand command){
        currentSpace -= 1;
        getContext().getLog().info("[FWS] Item was removed from fridge - current itemcount " + currentSpace + " (max " + maxSpace + ")");
        return this;
    }
}

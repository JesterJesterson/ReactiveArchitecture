package Lab2.homeautomation.fridge;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.FridgeWeightSensorCommand> {

    public interface FridgeWeightSensorCommand { }

    public static final class AddWeightCommand implements FridgeWeightSensorCommand {
        final int weightToAdd;
        public AddWeightCommand(int weightToAdd) {
            this.weightToAdd = weightToAdd;
        }
    }

    public static final class RemoveWeightCommand implements FridgeWeightSensorCommand {
        final int weightToRemove;
        public RemoveWeightCommand(int weightToAdd) {
            this.weightToRemove = weightToAdd;
        }
    }

    public static Behavior<FridgeWeightSensor.FridgeWeightSensorCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new FridgeWeightSensor(context, groupId, deviceId));
    }

    private final int maxWeight = 1000;
    private int currentWeight = 0;
    private String groupId;
    private String deviceId;

    public FridgeWeightSensor(ActorContext<FridgeWeightSensor.FridgeWeightSensorCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("[FWS] Started");
    }

    @Override
    public Receive<FridgeWeightSensorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddWeightCommand.class, this::onWeightAdd)
                .onMessage(RemoveWeightCommand.class, this::onWeightRemove)
                .build();
    }

    private Behavior<FridgeWeightSensorCommand> onWeightAdd(AddWeightCommand command){
        currentWeight += command.weightToAdd;
        getContext().getLog().info("[FWS] Item with weight " + command.weightToAdd + "was added to the fridge - current total weight " + currentWeight + " (max " + maxWeight + ")");
        return this;
    }

    private Behavior<FridgeWeightSensorCommand> onWeightRemove(RemoveWeightCommand command){
        currentWeight -= command.weightToRemove;
        getContext().getLog().info("[FWS] Item with weight " + command.weightToRemove + "was removed from fridge - current total weight " + currentWeight + " (max " + maxWeight + ")");
        return this;
    }
}

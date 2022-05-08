package Lab2.homeautomation.shared;

import Lab2.homeautomation.fridge.Fridge;
import Lab2.homeautomation.fridge.FridgeSpaceSensor;
import Lab2.homeautomation.fridge.FridgeWeightSensor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class OrderPreparation extends AbstractBehavior<OrderPreparation.PrepareOrderCommand> {
    public interface PrepareOrderCommand { }

    public static final class WeightFeedbackCommand implements PrepareOrderCommand{
        final int weight;
        public WeightFeedbackCommand(int weight) {
            this.weight = weight;
        }
    }

    public static final class SpaceFeedbackCommand implements PrepareOrderCommand{
        final int space;
        public SpaceFeedbackCommand(int space) {
            this.space = space;
        }
    }

    public static Behavior<PrepareOrderCommand> create(
            Produce produce,
            ActorRef<Fridge.FridgeCommand> fridge,
            ActorRef<FridgeWeightSensor.FridgeWeightSensorCommand> weightSensor,
            ActorRef<FridgeSpaceSensor.FridgeSpaceSensorCommand> spaceSensor) {
        return Behaviors.setup(context -> new OrderPreparation(context, produce, fridge, weightSensor, spaceSensor));
    }


    private final Produce produce;
    private final ActorRef<Fridge.FridgeCommand> fridge;

    private int availableWeight = -1;
    private int availableSpace = -1;

    public OrderPreparation(
            ActorContext<PrepareOrderCommand> context,
            Produce produce,
            ActorRef<Fridge.FridgeCommand> fridge,
            ActorRef<FridgeWeightSensor.FridgeWeightSensorCommand> weightSensor,
            ActorRef<FridgeSpaceSensor.FridgeSpaceSensorCommand> spaceSensor) {
        super(context);
        this.produce = produce;
        this.fridge = fridge;
        weightSensor.tell(new FridgeWeightSensor.RequestWeightCommand(getContext().getSelf()));
        spaceSensor.tell(new FridgeSpaceSensor.RequestSpaceCommand(getContext().getSelf()));
    }


    @Override
    public Receive<PrepareOrderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeightFeedbackCommand.class, this::onWeight)
                .onMessage(SpaceFeedbackCommand.class, this::onSpace)
                .build();
    }

    private Behavior<PrepareOrderCommand> onWeight(WeightFeedbackCommand command) {
        this.availableWeight = command.weight;
        finishOrder();
        return this;
    }

    private Behavior<PrepareOrderCommand> onSpace(SpaceFeedbackCommand command) {
        this.availableSpace = command.space;
        finishOrder();
        return this;
    }

    private Behavior<PrepareOrderCommand> finishOrder() {
        if (availableWeight != -1 && availableSpace != -1) {
            if (availableWeight >= produce.getWeight() && availableSpace > 0) {
                fridge.tell(new Fridge.FridgeProcessOrderCommand(produce));
            } else {
                getContext().getLog().warn("[F] Order couldnt be completed - not enough weight or space capacity" +
                        "(free space: "+availableSpace+" items | available weight: "+availableWeight+" - needed weight: "+ produce.getWeight()+")");
            }
            return Behaviors.stopped();
        }
        return this;
    }
}

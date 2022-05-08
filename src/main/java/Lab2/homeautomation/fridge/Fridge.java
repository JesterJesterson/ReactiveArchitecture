package Lab2.homeautomation.fridge;

import Lab2.homeautomation.shared.Produce;
import Lab2.homeautomation.shared.OrderPreparation;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.LocalDateTime;
import java.util.*;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {
    public interface FridgeCommand { }

    public static final class FridgeListContentCommand implements FridgeCommand { }

    public static final class FridgeOrderHistoryCommand implements FridgeCommand { }

    public static final class FridgeRequestOrderCommand implements FridgeCommand {
        final Produce produce;
        public FridgeRequestOrderCommand(Produce produce) {
            this.produce = produce;
        }
    }

    public static final class FridgeProcessOrderCommand implements FridgeCommand {
        final Produce produce;
        public FridgeProcessOrderCommand(Produce produce) {
            this.produce = produce;
        }
    }

    public static final class FridgeEatCommand implements FridgeCommand {
        final Produce produce;
        public FridgeEatCommand(Produce produce) {
            this.produce = produce;
        }

    }

    public static Behavior<FridgeCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Fridge(context, groupId, deviceId));
    }

    private String groupId;
    private String deviceId;
    private ActorRef<FridgeWeightSensor.FridgeWeightSensorCommand> weightSensor;
    private ActorRef<FridgeSpaceSensor.FridgeSpaceSensorCommand> spaceSensor;

    private final Map<LocalDateTime, Produce> orderHistory = new HashMap<>();
    private final List<Produce> itemsInFridge = new LinkedList<>();

    public Fridge(ActorContext<Fridge.FridgeCommand> context, String groupId, String deviceId) {
        super(context);
        getContext().getLog().info("[F] Started");
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.itemsInFridge.add(Produce.create("curryking"));
        this.weightSensor = getContext().spawn(FridgeWeightSensor.create(groupId,deviceId+".1"), "FridgeWeightSensor");
        this.spaceSensor = getContext().spawn(FridgeSpaceSensor.create(groupId,deviceId+".2"), "FridgeSpaceSensor");
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(FridgeRequestOrderCommand.class, this::onRequestOrder)
                .onMessage(FridgeProcessOrderCommand.class, this::onProcessOrder)
                .onMessage(FridgeListContentCommand.class, this::onListContent)
                .onMessage(FridgeOrderHistoryCommand.class, this::onOrderHistory)
                .onMessage(FridgeEatCommand.class, this::onEat)
                .build();
    }

    public Behavior<FridgeCommand> onRequestOrder(FridgeRequestOrderCommand command) {
        getContext().getLog().info("[F] requested order of " + command.produce.getName());
        getContext().spawn(OrderPreparation.create(command.produce, getContext().getSelf(), weightSensor, spaceSensor), "Order_" + UUID.randomUUID());
        return this;
    }

    public Behavior<FridgeCommand> onProcessOrder(FridgeProcessOrderCommand command) {
        getContext().getLog().info("[F] processing order of " + command.produce.getName());
        orderHistory.put(LocalDateTime.now(),command.produce);
        weightSensor.tell(new FridgeWeightSensor.AddWeightCommand(command.produce.getWeight()));
        spaceSensor.tell(new FridgeSpaceSensor.AddSpaceCommand());
        itemsInFridge.add(command.produce);
        return this;
    }

    public Behavior<FridgeCommand> onListContent(FridgeListContentCommand command) {
        var builder = new StringBuilder("[F] Current Content: \n");
        for(Produce produce : itemsInFridge) {
            builder.append(" - " + produce.getName() +" [" + produce.getWeight() + " | " + produce.getPrice() + "€] \n");
        }
        getContext().getLog().info(builder.toString());
        return this;
    }

    public Behavior<FridgeCommand> onOrderHistory(FridgeOrderHistoryCommand command) {
        var builder = new StringBuilder("[F] Order History: \n");
        for(var entry : orderHistory.entrySet()) {
            builder.append(" - "+entry.getKey() + " | " + entry.getValue().getName() + " [" + entry.getValue().getWeight() + " | " + entry.getValue().getPrice() + "€]\n");
        }
        getContext().getLog().info(builder.toString());
        return this;
    }

    public Behavior<FridgeCommand> onEat(FridgeEatCommand command) {
        Produce itemToEat = null;
        int numberOfItems = 0;
        for (Produce item: itemsInFridge) {
            if (item.getName().equals(command.produce.getName())){
                itemToEat = item;
                numberOfItems++;
            }
        }
        if (itemToEat==null){
            getContext().getLog().error("[F] couldn't eat "+command.produce.getName()+" - not in fridge");
            return this;
        }

        itemsInFridge.remove(itemToEat);
        spaceSensor.tell(new FridgeSpaceSensor.RemoveSpaceCommand());
        weightSensor.tell(new FridgeWeightSensor.RemoveWeightCommand(itemToEat.getWeight()));
        getContext().getLog().error("[F] ate "+command.produce.getName());

        if (numberOfItems-1 <= 0){
            getContext().getLog().info("[FRIDGE] no " + command.produce.getName() + " left in fridge - automatic order initiated");
            getContext().getSelf().tell(new FridgeRequestOrderCommand(command.produce));
        }
        return this;
    }
}

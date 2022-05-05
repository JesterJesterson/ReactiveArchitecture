package Lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


import java.util.Optional;

public class AirCondition extends AbstractBehavior<AirCondition.AirConditionCommand> {
    public interface AirConditionCommand {}

    public static final class PowerAirCondition implements AirConditionCommand {
        final Optional<Boolean> value;

        public PowerAirCondition(Optional<Boolean> value) {
            this.value = value;
        }
    }

    public static final class ReceivedTemperature implements AirConditionCommand {
        double value;

        public ReceivedTemperature(double value) {
            this.value = value;
        }
    }

    private final String groupId;
    private final String deviceId;
    private boolean active = false;
    private boolean poweredOn = true;

    public AirCondition(ActorContext<AirConditionCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("AirCondition started");
    }

    public static Behavior<AirConditionCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new AirCondition(context, groupId, deviceId));
    }

    @Override
    public Receive<AirConditionCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceivedTemperature.class, this::onReadTemperature)
                .onMessage(PowerAirCondition.class, this::onPowerAirConditionOff)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<AirConditionCommand> onReadTemperature(ReceivedTemperature r) {
        if(r.value >= 15) {
            getContext().getLog().info("Aircondition reading {} - activated", r.value);
            this.active = true;
        }
        else {
            getContext().getLog().info("Aircondition reading {} - deactivated", r.value);
            this.active =  false;
        }

        return Behaviors.same();
    }

    private Behavior<AirConditionCommand> onPowerAirConditionOff(PowerAirCondition r) {
        getContext().getLog().info("Turning Aircondition to {}", r.value);

        if(r.value.get() == false) {
            return this.powerOff();
        }
        return this;
    }

    private Behavior<AirConditionCommand> onPowerAirConditionOn(PowerAirCondition r) {
        getContext().getLog().info("Turning Aircondition to {}", r.value);

        if(r.value.get() == true) {
            return Behaviors.receive(AirConditionCommand.class)
                    .onMessage(ReceivedTemperature.class, this::onReadTemperature)
                    .onMessage(PowerAirCondition.class, this::onPowerAirConditionOff)
                    .onSignal(PostStop.class, signal -> onPostStop())
                    .build();
        }
        return this;
    }

    private Behavior<AirConditionCommand> powerOff() {
        this.poweredOn = false;
        return Behaviors.receive(AirConditionCommand.class)
                .onMessage(PowerAirCondition.class, this::onPowerAirConditionOn)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private AirCondition onPostStop() {
        getContext().getLog().info("TemperatureSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}

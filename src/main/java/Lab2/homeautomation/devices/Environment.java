package Lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class Environment {

    private double temperature = 15.0;
    private boolean isSunny = false;
    private boolean setHighTemp = false;
    private boolean setLowTempp = true;

    private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;
    private final TimerScheduler<EnvironmentCommand> TemperatureTimeScheduler;

    //Aus der Fragestunde übernommen. Weg um ein neuen Wert mit Hilfe eines Timers abzufragen.//
    public static void Behavior<EnvironmentCommand> create(){
        return Behaviours.setup(context -> Behaviors.withTimers(timers -> new Environment(context, timers, timers)));
    }

    public Behavior<EnvironmentCommand> create
}

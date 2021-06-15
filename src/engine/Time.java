package engine;


import static java.lang.System.nanoTime;

public class Time {


    private static long lastLoopTime = nanoTime();

    private static double delta;


    public static void calculateDelta() {
        long time = nanoTime();
        delta = (time - lastLoopTime) / 1000000d;
        lastLoopTime = time;
    }

    public static double getDelta() {
        return(delta);
    }
}

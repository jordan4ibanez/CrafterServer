package engine;


import static java.lang.System.nanoTime;

public class Time {


    private static long lastLoopTime = nanoTime();

    private static float delta;


    public static void calculateDelta() {
        long time = nanoTime();
        delta = (float) (time - lastLoopTime) / 1000000f;
        lastLoopTime = time;
    }

    public static float getDelta() {
        return(delta);
    }
}

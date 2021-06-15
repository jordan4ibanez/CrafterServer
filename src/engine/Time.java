package engine;


import static java.lang.System.nanoTime;

public class Time {


    private static long lastLoopTime = nanoTime();

    private static float delta;


    public static void calculateDelta() {
        long time = nanoTime();
        delta = (float)(time - lastLoopTime) / 1_000_000f;
        lastLoopTime = time;

        //this is crash recursion protection
        if (delta > 0.05){
            delta = 0.05f;
        }
    }

    public static float getDelta() {
        return(delta);
    }
}

package engine;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Random;

public class FancyMath {
    private static final Random random = new Random();
    private static final int[] dirArray = new int[]{-1,1};

    public static float randomDirFloat(){
        return dirArray[random.nextInt(2)];
    }

    public static float randomNumber(float x){
        return (float)Math.random() * x;
    }

    public static float randomForceValue(float x){
        return randomNumber(x) * randomDirFloat();
    }

    public static double getDistance(Vector3d pos1, Vector3d pos2){
        return Math.hypot((pos1.x - pos2.x), Math.hypot((pos1.y - pos2.y), (pos1.z - pos2.z)));
    }

    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.hypot((x1 - x2), Math.hypot((y1 - y2),(z1 - z2)));
    }

    public static double getDistance2D(double x1, double z1, double x2, double z2){
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(z1-z2, 2));
    }

    public static double getDistance2D(Vector2d pos1, Vector2d pos2){
        return Math.sqrt(Math.pow(pos1.x-pos2.x, 2) + Math.pow(pos1.y-pos2.y, 2));
    }

    public static Vector3f getCameraRotationVector(Vector3f camRot){
        Vector3f rotationVector = new Vector3f();
        float xzLen = (float)Math.cos(Math.toRadians(camRot.x + 180f));
        rotationVector.z = xzLen * (float)Math.cos(Math.toRadians(camRot.y));
        rotationVector.y = (float)Math.sin(Math.toRadians(camRot.x + 180f));
        rotationVector.x = xzLen * (float)Math.sin(Math.toRadians(-camRot.y));
        return rotationVector;
    }

}

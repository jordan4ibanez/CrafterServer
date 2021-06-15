package game.tnt;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomForceValue;
import static engine.Time.getDelta;
import static game.collision.Collision.applyInertia;
import static game.tnt.Explosion.boom;

public class TNTEntity {
    private final static float tntSize = 0.5f;
    private final static int MAX_ID_AMOUNT = 126_000;
    private static int totalTNT = 0;
    private static final Vector3d[] tntPos = new Vector3d[MAX_ID_AMOUNT];
    private static final Vector3d[] tntScale = new Vector3d[MAX_ID_AMOUNT];
    private static final float[] tntTimer =    new float[MAX_ID_AMOUNT];
    private static final boolean[] tntExists =    new boolean[MAX_ID_AMOUNT];
    private static final Vector3f[] tntInertia = new Vector3f[MAX_ID_AMOUNT];

    public static int getTotalTNT(){
        return totalTNT;
    }

    public static void createTNT(Vector3d pos){
        pos.x += 0.5f;
        //pos.y += 0.5f;
        pos.z += 0.5f;
        tntPos[totalTNT] = new Vector3d(pos);
        tntInertia[totalTNT] = new Vector3f(randomForceValue(3),(float)Math.random()*7f,randomForceValue(3f));
        tntExists[totalTNT] = true;
        tntTimer[totalTNT] = 0f;
        tntScale[totalTNT] = new Vector3d(1,1,1);
        totalTNT++;
        System.out.println("Created new TNT. Total TNT: " + totalTNT);
    }

    public static void createTNT(Vector3d pos, float timer, boolean punched) {
        pos.x += 0.5f;
        //pos.y += 0.5f;
        pos.z += 0.5f;
        tntPos[totalTNT] = new Vector3d(pos);
        float tntJump;
        if (punched){
            tntJump = (float)Math.random()*10f;
        } else {
            tntJump = 0f;
        }
        tntInertia[totalTNT] = new Vector3f(randomForceValue(3f),tntJump,randomForceValue(3f));
        tntExists[totalTNT] = true;
        tntTimer[totalTNT] = timer;
        tntScale[totalTNT] = new Vector3d(1d,1d,1d);
        totalTNT++;
    }

    public static void onTNTStep() throws Exception {
        double delta = getDelta();
        for (int i = 0; i < totalTNT; i++){
            tntTimer[i] += delta;
            applyInertia(tntPos[i], tntInertia[i], true, tntSize, tntSize * 2, true, false, true, false, false);

            if(tntTimer[i]>2.23f){
                tntScale[i].x += delta;
                tntScale[i].y += delta/2f;
                tntScale[i].z += delta;
            }

            if (tntTimer[i] > 2.6f){

                boom(tntPos[i], 5);

                deleteTNT(i);

                continue;
            }

            if (tntPos[i].y < 0){
                deleteTNT(i);
            }
        }
    }


    private static void deleteTNT(int ID){
        tntPos[ID] = null;
        tntInertia[ID] = null;
        tntExists[ID] = false;
        tntScale[ID] = null;
        tntTimer[ID] = 0;

        for (int i = ID; i < totalTNT; i ++){
            tntPos[i] = tntPos[i+1];
            tntInertia[i] = tntInertia[i+1];
            tntExists[i] = tntExists[i+1];
            tntScale[i] = tntScale[i+1];
            tntTimer[i] = tntTimer[i+1];
        }

        tntPos[totalTNT - 1] = null;
        tntInertia[totalTNT - 1] = null;
        tntExists[totalTNT - 1] = false;
        tntScale[totalTNT - 1] = null;
        tntTimer[totalTNT - 1] = 0;

        totalTNT -= 1;
//        System.out.println("A TNT was Deleted. Remaining: " + totalTNT);
    }

    public static Vector3d getTNTScale(int ID){
        return tntScale[ID];
    }

    public static boolean tntExists(int ID){
        return tntExists[ID];
    }

    public static Vector3d getTNTPosition(int ID){
        return tntPos[ID];
    }
}
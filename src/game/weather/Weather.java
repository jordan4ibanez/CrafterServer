package game.weather;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

import static game.collision.ParticleCollision.applyParticleInertia;

public class Weather {
    private final static Map<Integer, RainDropEntity> rainDrops = new HashMap<>();
    private final static Deque<Integer> deletionQueue = new ArrayDeque<>();
    private static int currentID = 0;

    public static void rainDropsOnTick(){
        for (RainDropEntity thisParticle : rainDrops.values()){
            boolean onGround = applyParticleInertia(thisParticle.pos, thisParticle.inertia, true,true,true);
            thisParticle.timer += 0.01f;
            if (thisParticle.timer > 10f || onGround){
                deletionQueue.add(thisParticle.key);
            }
        }

        while (!deletionQueue.isEmpty()){
            rainDrops.remove(deletionQueue.pop());
        }
    }

    public static void createRainDrop(Vector3d pos, Vector3f inertia){
        rainDrops.put(currentID, new RainDropEntity(pos, inertia, currentID));
        currentID++;
    }


    public static Collection<RainDropEntity> getRainDrops(){
        return rainDrops.values();
    }
}

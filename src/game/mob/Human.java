package game.mob;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomDirFloat;
import static engine.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.Mob.registerMob;

public class Human {
    private static final float accelerationMultiplier  = 0.03f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;

    private final static MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(MobObject thisObject) {

            float delta = getDelta();

            thisObject.timer += delta;

            if (thisObject.timer > 1.5f) {
                thisObject.stand = !thisObject.stand;
                thisObject.timer = (float)Math.random() * -2f;
                thisObject.rotation = (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
            }

            float bodyYaw = (float) Math.toRadians(thisObject.rotation) + (float) Math.PI;

            thisObject.inertia.x += (float) (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisObject.inertia.z += (float) (Math.cos(bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;

            Vector3f inertia2D = new Vector3f(thisObject.inertia.x, 0, thisObject.inertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisObject.health <= 0){
                maxSpeed = 0.01f;
            }

            if (thisObject.animationTimer >= 1f) {
                thisObject.animationTimer = 0f;
            }

            if (inertia2D.length() > maxSpeed) {
                inertia2D = inertia2D.normalize().mul(maxSpeed);
                thisObject.inertia.x = inertia2D.x;
                thisObject.inertia.z = inertia2D.z;
            }

            boolean onGround = applyInertia(thisObject.pos, thisObject.inertia, false, thisObject.width, thisObject.height, true, false, true, false, false);

            thisObject.onGround = onGround;


            if (thisObject.health > 0) {
                //check for block in front
                if (onGround) {
                    double x = Math.sin(-bodyYaw);
                    double z = Math.cos(bodyYaw);

                    if (getBlock((int) Math.floor(x + thisObject.pos.x), (int) Math.floor(thisObject.pos.y), (int) Math.floor(z + thisObject.pos.z)) > 0) {
                        thisObject.inertia.y += 8.75f;
                    }
                }
            }

            thisObject.lastPos = new Vector3d(thisObject.pos);

        }
    };

    public static void registerHumanMob(){
        registerMob(new MobDefinition("human",  7, 1.9f, 0.25f, mobInterface));
    }

}

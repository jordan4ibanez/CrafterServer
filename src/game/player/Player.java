package game.player;

import game.blocks.BlockDefinition;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static engine.Time.getDelta;
import static engine.disk.Disk.loadPlayerPos;
import static engine.gui.GUILogic.calculateHealthBarElements;
import static engine.gui.GUILogic.makeHeartsJiggle;
import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.blocks.BlockDefinition.isBlockLiquid;
import static game.chunk.Chunk.*;
import static game.collision.Collision.applyInertia;
import static game.crafting.Inventory.getItemInInventorySlot;
import static game.crafting.Inventory.updateWieldInventory;
import static game.ray.Ray.playerRayCast;


public class Player {

    private static final List<Player> players = new ArrayList<Player>();

    public static List getAllPlayers(){
        return players;
    }

    public int health = 20;
    public int renderDistance = 5;
    public Vector3d pos                  = loadPlayerPos();
    public final float eyeHeight         = 1.5f;
    public final float collectionHeight  = 0.7f;
    public final Vector3f inertia        = new Vector3f(0,0,0);
    public final float height            = 1.9f;
    public final float width             = 0.3f;
    public boolean mining                = false;
    public boolean placing               = false;
    public float placeTimer              = 0;
    public String name;
    public int currentInventorySelection = 0;
    public int oldInventorySelection = 0;
    public Vector3i oldWorldSelectionPos = new Vector3i();
    public Vector3i worldSelectionPos    = new Vector3i();
    public final Vector3i currentChunk = new Vector3i((int)Math.floor(pos.x / 16f),0,(int)Math.floor(pos.z / 16f));
    public static int oldY = 0;
    public final float reach = 3.575f;
    public boolean sneaking              = false;
    public boolean running               = false;
    public float lightCheckTimer = 0f;
    public byte lightLevel = 0;
    public Vector3i oldPos = new Vector3i(0,0,0);
    public Vector3d oldRealPos = new Vector3d(0,0,0);


    public Vector3d camPos = new Vector3d();

    //block hardness cache
    public float stoneHardness = 0f;
    public float dirtHardness = 0f;
    public float woodHardness = 0f;
    public float leafHardness = 0f;

    //tool mining level cache
    public float stoneMiningLevel = 0.3f;
    public float dirtMiningLevel = 1f;
    public float woodMiningLevel = 1f;
    public float leafMiningLevel = 1f;



    public Vector3d getPlayerPos() {
        return new Vector3d(pos);
    }


    public Vector3d getPlayerPosWithCollectionHeight(){
        return new Vector3d(pos.x, pos.y + collectionHeight, pos.z);
    }

    public void setPlayerPos(Vector3d newPos) {
        pos = newPos;
    }

    public Vector3f getPlayerInertia(){
        return inertia;
    }




    public static void playersOnTick() {

        for (Player thisPlayer : players) {
            float delta = getDelta();

            //camera underwater effect trigger

            camPos.y -= 0.02f;
            int cameraCheckBlock = getBlock((int) Math.floor(camPos.x), (int) Math.floor(camPos.y), (int) Math.floor(camPos.z));

            cameraSubmerged = cameraCheckBlock > 0 && isBlockLiquid(cameraCheckBlock);

            //the player comes to equilibrium with the water's surface
            //if this is not implemented like this
            if (wasInWaterTimer > 0.f) {
                //System.out.println(wasInWaterTimer);
                waterLockout = true;
                wasInWaterTimer -= delta;
                if (wasInWaterTimer <= 0) {
                    //System.out.println("turned off lockout");
                    waterLockout = false;
                }
            }

            if (playerIsJumping && isPlayerOnGround()) {
                playerIsJumping = false;
            }

            float camRot = getCameraRotation().y + 180f;

            if (camRot >= 315f || camRot < 45f) {
//            System.out.println(2);
                currentRotDir = 2;
            } else if (camRot >= 45f && camRot < 135f) {
//            System.out.println(3);
                currentRotDir = 3;
            } else if (camRot >= 135f && camRot < 225f) {
//            System.out.println(0);
                currentRotDir = 0;
            } else if (camRot >= 225f && camRot < 315f) {
//            System.out.println(1);
                currentRotDir = 1;
            }

            //this is a minor hack which allows it to "rain"
        /*
        rainBuffer += delta;
        if (rainBuffer >= 0.1f) {
            rainBuffer = 0f;

            double eyeHeightY = getPlayerPosWithEyeHeight().y;
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    int heightMap = getHeightMap((int) Math.floor(pos.x)+x, (int) Math.floor(pos.z)+z);
                    if (127 - heightMap > 0) {
                        for (int y = heightMap + 1; y < 127; y++) {
                            if (Math.abs(eyeHeightY - y) <= 8f) {
                                if (Math.random() > 0.8f) {
                                    createRainDrop(
                                            new Vector3f((int) Math.floor(pos.x) + x, y, (int) Math.floor(pos.z) + z)
                                                    .add((float) Math.random(), (float) Math.random(), (float) Math.random()),
                                            new Vector3f(0, -30f, 0)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

         */


            //mining timer
            hasDug = false;
            //reset mining timer
            if ((mining && worldSelectionPos != null && !worldSelectionPos.equals(oldWorldSelectionPos)) || (currentInventorySelection != oldInventorySelection)) {
                diggingFrame = -1;
                diggingProgress = 0f;
                rebuildMiningMesh(diggingFrame);
            }
            if (mining && worldSelectionPos != null) {
                float progress = 0;
                //don't let players even attempt to dig undiggable blocks
                if (leafHardness > -1 && dirtHardness > -1 && woodHardness > -1 && stoneHardness > -1) {
                    //scan through max quickness for current tool
                    if (leafHardness > 0 && leafMiningLevel / leafHardness > progress) {
                        progress = leafMiningLevel / leafHardness;
                    }
                    if (dirtHardness > 0 && dirtMiningLevel / dirtHardness > progress) {
                        progress = dirtMiningLevel / dirtHardness;
                    }
                    if (stoneHardness > 0 && stoneMiningLevel / stoneHardness > progress) {
                        progress = stoneMiningLevel / stoneHardness;
                    }
                    if (woodHardness > 0 && woodMiningLevel / woodHardness > progress) {
                        progress = woodMiningLevel / woodHardness;
                    }
                }

                //System.out.println(progress);

                diggingProgress += delta * progress;
                if (diggingProgress >= 0.1f) {
                    diggingFrame++;
                    if (diggingFrame > 8) {
                        diggingFrame = 0;
                        hasDug = true;
                    }
                    diggingProgress = 0;
                    rebuildMiningMesh(diggingFrame);
                }
            } else if (diggingFrame != -1) {
                diggingFrame = -1;
                rebuildMiningMesh(0);
            }

            //place timers
            if (placeTimer > 0) {
                placeTimer -= delta;
                if (placeTimer < 0) {
                    placeTimer = 0;
                }
            }

            //values for application of inertia
            applyPlayerInertiaBuffer();

            onGround = applyInertia(pos, inertia, true, width, height, true, sneaking, true, true, true);


            //play sound when player lands on the ground
            if (onGround && !wasOnGround) {
                playSound("dirt_" + (int) (Math.ceil(Math.random() * 3)));
            }


            //falldamage
            if (onGround) {

                int currentY = (int) Math.floor(pos.y);

                if (currentY < oldY) {
                    if (oldY - currentY > 6) {
                        hurtPlayer(oldY - currentY - 6);
                    }
                }
                oldY = currentY;
            }

            //body animation scope
            {
                Vector3f inertia2D = new Vector3f(inertia.x, 0, inertia.z);

                animationTimer += delta * (inertia2D.length() / maxWalkSpeed) * 2f;

                if (animationTimer >= 1f) {
                    animationTimer -= 1f;
                }

                bodyRotations[2] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * 2f)), 0, 0);
                bodyRotations[3] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * -2f)), 0, 0);

                bodyRotations[4] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * -2f)), 0, 0);
                bodyRotations[5] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * 2f)), 0, 0);
            }

            updatePlayerHandInertia();

            if (onGround && playerIsMoving() && !sneaking && !inWater) {
                applyViewBobbing();
            } else {
                returnPlayerViewBobbing();
            }

            //sneaking offset
            if (sneaking) {
                if (sneakOffset > -100.f) {
                    sneakOffset -= delta * 1000f;
                    if (sneakOffset <= -100.f) {
                        sneakOffset = -100.f;
                    }
                }
            } else {
                if (sneakOffset < 0.f) {
                    sneakOffset += delta * 1000f;
                    if (sneakOffset > 0.f) {
                        sneakOffset = 0.f;
                    }
                }
            }

            if (mining && worldSelectionPos != null) {
                particleBufferTimer += delta;
                if (particleBufferTimer > 0.01f) {
                    int randomDir = (int) Math.floor(Math.random() * 6f);
                    int block;
                    int miningBlock = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z);
                    switch (randomDir) {
                        case 0 -> {
                            block = getBlock(worldSelectionPos.x + 1, worldSelectionPos.y, worldSelectionPos.z);
                            if (block == 0) {
                                Vector3d particlePos = new Vector3d(worldSelectionPos);
                                particlePos.x += 1.1f;
                                particlePos.z += Math.random();
                                particlePos.y += Math.random();

                                Vector3f particleInertia = new Vector3f();
                                particleInertia.x = (float) Math.random() * 2f;
                                particleInertia.y = (float) Math.random() * 2f;
                                particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                                createParticle(particlePos, particleInertia, miningBlock);
                            }
                        }
                        case 1 -> {
                            block = getBlock(worldSelectionPos.x - 1, worldSelectionPos.y, worldSelectionPos.z);
                            if (block == 0) {
                                Vector3d particlePos = new Vector3d(worldSelectionPos);
                                particlePos.x -= 0.1f;
                                particlePos.z += Math.random();
                                particlePos.y += Math.random();

                                Vector3f particleInertia = new Vector3f();
                                particleInertia.x = (float) Math.random() * -2f;
                                particleInertia.y = (float) Math.random() * 2f;
                                particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                                createParticle(particlePos, particleInertia, miningBlock);
                            }
                        }
                        case 2 -> {
                            block = getBlock(worldSelectionPos.x, worldSelectionPos.y + 1, worldSelectionPos.z);
                            if (block == 0) {
                                Vector3d particlePos = new Vector3d(worldSelectionPos);
                                particlePos.y += 1.1f;
                                particlePos.z += Math.random();
                                particlePos.x += Math.random();

                                Vector3f particleInertia = new Vector3f();
                                particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                                particleInertia.y = (float) Math.random() * 2f;
                                particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                                createParticle(particlePos, particleInertia, miningBlock);
                            }
                        }
                        case 3 -> {
                            block = getBlock(worldSelectionPos.x, worldSelectionPos.y - 1, worldSelectionPos.z);
                            if (block == 0) {
                                Vector3d particlePos = new Vector3d(worldSelectionPos);
                                particlePos.y -= 0.1f;
                                particlePos.z += Math.random();
                                particlePos.x += Math.random();

                                Vector3f particleInertia = new Vector3f();
                                particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                                particleInertia.y = (float) Math.random() * -1f;
                                particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                                createParticle(particlePos, particleInertia, miningBlock);
                            }
                        }
                        case 4 -> {
                            block = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z + 1);
                            if (block == 0) {
                                Vector3d particlePos = new Vector3d(worldSelectionPos);
                                particlePos.z += 1.1f;
                                particlePos.x += Math.random();
                                particlePos.y += Math.random();

                                Vector3f particleInertia = new Vector3f();
                                particleInertia.z = (float) Math.random() * 2f;
                                particleInertia.y = (float) Math.random() * 2f;
                                particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                                createParticle(particlePos, particleInertia, miningBlock);
                            }
                        }
                        case 5 -> {
                            block = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z - 1);
                            if (block == 0) {
                                Vector3d particlePos = new Vector3d(worldSelectionPos);
                                particlePos.z -= 0.1f;
                                particlePos.x += Math.random();
                                particlePos.y += Math.random();

                                Vector3f particleInertia = new Vector3f();
                                particleInertia.z = (float) Math.random() * -2f;
                                particleInertia.y = (float) Math.random() * 2f;
                                particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                                createParticle(particlePos, particleInertia, miningBlock);
                            }
                        }
                    }
                    particleBufferTimer = 0f;
                }
            }

            calculateRunningFOV();

            if (getCameraPerspective() < 2) {
                if (mining && hasDug) {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, true, false, true);
                } else if (mining) {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, true, false, false);
                } else if (placing && placeTimer <= 0) {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, false, true, false);
                    placeTimer = 0.25f; // every quarter second you can place
                } else {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, false, false, false);
                }
            } else {
                if (mining && hasDug) {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, true, false, true);
                } else if (mining) {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, true, false, false);
                } else if (placing && placeTimer <= 0) {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, false, true, false);
                    placeTimer = 0.25f; // every quarter second you can place
                } else {
                    playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, false, false, false);
                }
            }


            if (health <= 6) {
                makeHeartsJiggle();
            }

            //camera z axis hurt rotation thing

        /*
        if (doHurtRotation){
            if (hurtRotationUp) {
                hurtCameraRotation += delta * 150f;
                if (hurtCameraRotation >= 7f) {
                    hurtRotationUp = false;
                }
            } else {
                hurtCameraRotation -= delta * 150f;
                if (hurtCameraRotation <= 0f){
                    hurtCameraRotation = 0f;
                    hurtRotationUp = true;
                    doHurtRotation = false;
                }
            }
        }

         */

            //update light level for the wield item
            lightCheckTimer += delta;
            Vector3i newFlooredPos = new Vector3i((int) Math.floor(camPos.x), (int) Math.floor(camPos.y), (int) Math.floor(camPos.z));

            //System.out.println(lightCheckTimer);
            if (lightCheckTimer >= 0.5f || !newFlooredPos.equals(oldPos)) {
                lightCheckTimer = 0f;

                byte newLightLevel = getLight(newFlooredPos.x, newFlooredPos.y, newFlooredPos.z);

                if (newLightLevel != lightLevel) {
                    lightLevel = newLightLevel;
                    rebuildWieldHandMesh(lightLevel);

                }
            }

            //do the same for the literal wield inventory
            updateWieldInventory(lightLevel);

            oldPos = newFlooredPos;
            oldRealPos = new Vector3d(pos);
            wasOnGround = onGround;
            oldInventorySelection = currentInventorySelection;
        }
    }

    public void updateWorldChunkLoader(Player thisPlayer){
        int newChunkX = (int)Math.floor(thisPlayer.pos.x / 16f);
        int newChunkZ = (int)Math.floor(thisPlayer.pos.z / 16f);

        if (newChunkX != thisPlayer.currentChunk.x || newChunkZ != thisPlayer.currentChunk.z) {
            thisPlayer.currentChunk.x = newChunkX;
            thisPlayer.currentChunk.z = newChunkZ;
            generateNewChunks(thisPlayer);
        }
    }


    public int getCurrentInventorySelection(){
        return currentInventorySelection;
    }

    public int getPlayerHealth(){
        return health;
    }

    private float healthTimer = 0;

    private void doHealthTest(){
        float delta = getDelta();

        healthTimer += delta;

        if (healthTimer >= 1f){
            healthTimer = 0;

            health -= 1;

            if (health < 0){
                health = 20;
            }
        }
    }
}

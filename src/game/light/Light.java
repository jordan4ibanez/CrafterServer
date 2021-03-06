package game.light;

import java.util.ArrayDeque;
import java.util.Deque;

import static engine.time.Time.getDelta;
import static game.chunk.Chunk.*;

public class Light {

    private static final byte maxLightLevel = 15;
    private static final byte maxTorchLightLevel = 12;
    private static final byte blockIndicator = 127;
    private static final byte lightDistance = 15;
    private static final byte max = (lightDistance * 2) + 1;

    private static byte currentLightLevel = 4;

    public static byte getCurrentGlobalLightLevel(){
        return currentLightLevel;
    }

    public static void setCurrentLightLevel(byte newLightLevel){
        currentLightLevel = newLightLevel;
    }



    public static byte getImmediateLight(int x, int y, int z){
        int theBlock = getBlock(x, y, z);
        if (theBlock == 0 && underSunLight(x, y, z)){
            return maxLightLevel;
        }

        byte maxLight = 0;

        if (getBlock(x + 1, y, z) == 0) {
            byte gottenLight = getNaturalLight(x + 1, y, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x - 1, y, z) == 0) {
            byte gottenLight = getNaturalLight(x - 1, y, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y + 1, z) == 0) {
            byte gottenLight = getNaturalLight(x, y + 1, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y - 1, z) == 0) {
            byte gottenLight = getNaturalLight(x, y - 1, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y, z + 1) == 0) {
            byte gottenLight = getNaturalLight(x, y, z + 1);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y, z - 1) == 0) {
            byte gottenLight = getNaturalLight(x, y, z - 1);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }

        return maxLight;
    }

    public static void lightFloodFill(int posX, int posY, int posZ) {
        new Thread(() -> {
            final Deque<LightUpdate> lightSources = new ArrayDeque<>();
            final byte[][][] memoryMap = new byte[(lightDistance * 2) + 1][(lightDistance * 2) + 1][(lightDistance * 2) + 1];
            for (int x = posX - lightDistance; x <= posX + lightDistance; x++) {
                for (int y = posY - lightDistance; y <= posY + lightDistance; y++) {
                    for (int z = posZ - lightDistance; z <= posZ + lightDistance; z++) {
                        int theBlock = getBlock(x, y, z);
                        if (theBlock == 0 && underSunLight(x, y, z)) {
                            int skipCheck = 0;
                            if (getBlock(x + 1, y, z) == 0 && underSunLight(x + 1, y, z) && getNaturalLight(x + 1, y, z) == currentLightLevel) {
                                skipCheck++;
                            }
                            if (getBlock(x - 1, y, z) == 0 && underSunLight(x - 1, y, z) && getNaturalLight(x - 1, y, z) == currentLightLevel) {
                                skipCheck++;
                            }
                            if (getBlock(x, y + 1, z) == 0 && underSunLight(x, y + 1, z) && getNaturalLight(x, y + 1, z) == currentLightLevel) {
                                skipCheck++;
                            }
                            if (getBlock(x, y - 1, z) == 0 && underSunLight(x, y - 1, z) && getNaturalLight(x, y - 1, z) == currentLightLevel) {
                                skipCheck++;
                            }
                            if (getBlock(x, y, z + 1) == 0 && underSunLight(x, y, z + 1) && getNaturalLight(x, y, z + 1) == currentLightLevel) {
                                skipCheck++;
                            }
                            if (getBlock(x, y, z - 1) == 0 && underSunLight(x, y, z - 1) && getNaturalLight(x, y, z - 1) == currentLightLevel) {
                                skipCheck++;
                            }
                            if (skipCheck < 6) {
                                lightSources.add(new LightUpdate(x - posX + lightDistance, y - posY + lightDistance, z - posZ + lightDistance));
                            }
                            memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = currentLightLevel;
                        } else if (theBlock == 0) {
                            memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = 0;
                        } else {
                            memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = blockIndicator;
                        }
                    }
                }
            }

            int[] crawlerPos;

            while (!lightSources.isEmpty()) {
                LightUpdate thisUpdate = lightSources.pop();

                Deque<LightUpdate> lightSteps = new ArrayDeque<>();

                lightSteps.push(new LightUpdate(thisUpdate.x, thisUpdate.y, thisUpdate.z, maxLightLevel));

                while (!lightSteps.isEmpty()) {
                    LightUpdate newUpdate = lightSteps.pop();

                    if (newUpdate.level <= 1) {
                        continue;
                    }
                    if (newUpdate.x < 0 || newUpdate.x > max || newUpdate.y < 0 || newUpdate.y > max || newUpdate.z < 0 || newUpdate.z > max) {
                        continue;
                    }

                    crawlerPos = new int[]{newUpdate.x, newUpdate.y, newUpdate.z};

                    //+x
                    {
                        if (crawlerPos[0] + 1 < max && memoryMap[crawlerPos[0] + 1][crawlerPos[1]][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0] + 1][crawlerPos[1]][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0] + 1, crawlerPos[1], crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }

                    //-x
                    {
                        if (crawlerPos[0] - 1 >= 0 && memoryMap[crawlerPos[0] - 1][crawlerPos[1]][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0] - 1][crawlerPos[1]][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0] - 1, crawlerPos[1], crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }

                    //+z
                    {
                        if (crawlerPos[2] + 1 < max && memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] + 1] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] + 1] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1], crawlerPos[2] + 1, (byte) (newUpdate.level - 1)));
                        }
                    }

                    //-z
                    {
                        if (crawlerPos[2] - 1 >= 0 && memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] - 1] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] - 1] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1], crawlerPos[2] - 1, (byte) (newUpdate.level - 1)));
                        }
                    }

                    //+y
                    {
                        if (crawlerPos[1] + 1 < max && memoryMap[crawlerPos[0]][crawlerPos[1] + 1][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1] + 1][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1] + 1, crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }

                    //-y
                    {
                        if (crawlerPos[1] - 1 >= 0 && memoryMap[crawlerPos[0]][crawlerPos[1] - 1][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1] - 1][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1] - 1, crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }
                }
            }

            for (int x = posX - lightDistance; x <= posX + lightDistance; x++) {
                for (int y = posY - lightDistance; y <= posY + lightDistance; y++) {
                    for (int z = posZ - lightDistance; z <= posZ + lightDistance; z++) {
                        if (memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] != blockIndicator) {
                            setNaturalLight(x, y, z, memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance]);
                        }
                    }
                }
            }
            lightSources.clear();
            //}
        }).start();
    }



    public static void torchFloodFill(int posX, int posY, int posZ) {
        new Thread(() -> {
            final Deque<LightUpdate> lightSources = new ArrayDeque<>();
            final byte[][][] memoryMap = new byte[(lightDistance * 2) + 1][(lightDistance * 2) + 1][(lightDistance * 2) + 1];

            //lightSources.add(new LightUpdate(lightDistance, lightDistance, lightDistance, getTorchLight(posX,posY,posZ)));

            int minX = posX - lightDistance;
            int maxX = posX + lightDistance;
            int minY = posY - lightDistance;
            int maxY = posY + lightDistance;
            int minZ = posZ - lightDistance;
            int maxZ = posZ + lightDistance;

            for (int x = posX - lightDistance; x <= posX + lightDistance; x++) {
                for (int y = posY - lightDistance; y <= posY + lightDistance; y++) {
                    for (int z = posZ - lightDistance; z <= posZ + lightDistance; z++) {
                        int theBlock = getBlock(x, y, z);
                        if (theBlock == 29){
                            lightSources.add(new LightUpdate( x - posX + lightDistance, y - posY + lightDistance, z - posZ + lightDistance, maxTorchLightLevel));
                        } else if (theBlock == 0 && (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ)) {
                            memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = getTorchLight(x, y, z);
                        } else if (theBlock != 0){
                            memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = blockIndicator;
                        } else { //everything else is zeroed out
                            memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = 0;
                        }
                    }
                }
            }

            int[] crawlerPos;

            while (!lightSources.isEmpty()) {
                LightUpdate thisUpdate = lightSources.pop();

                Deque<LightUpdate> lightSteps = new ArrayDeque<>();

                lightSteps.push(new LightUpdate(thisUpdate.x, thisUpdate.y, thisUpdate.z, maxLightLevel));

                while (!lightSteps.isEmpty()) {

                    LightUpdate newUpdate = lightSteps.pop();

                    if (newUpdate.level <= 1) {
                        continue;
                    }
                    if (newUpdate.x < 0 || newUpdate.x > max || newUpdate.y < 0 || newUpdate.y > max || newUpdate.z < 0 || newUpdate.z > max) {
                        continue;
                    }

                    crawlerPos = new int[]{newUpdate.x, newUpdate.y, newUpdate.z};

                    //+x
                    {
                        if (crawlerPos[0] + 1 < max && memoryMap[crawlerPos[0] + 1][crawlerPos[1]][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0] + 1][crawlerPos[1]][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0] + 1, crawlerPos[1], crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }

                    //-x
                    {
                        if (crawlerPos[0] - 1 >= 0 && memoryMap[crawlerPos[0] - 1][crawlerPos[1]][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0] - 1][crawlerPos[1]][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0] - 1, crawlerPos[1], crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }

                    //+z
                    {
                        if (crawlerPos[2] + 1 < max && memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] + 1] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] + 1] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1], crawlerPos[2] + 1, (byte) (newUpdate.level - 1)));
                        }
                    }

                    //-z
                    {
                        if (crawlerPos[2] - 1 >= 0 && memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] - 1] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] - 1] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1], crawlerPos[2] - 1, (byte) (newUpdate.level - 1)));
                        }
                    }

                    //+y
                    {
                        if (crawlerPos[1] + 1 < max && memoryMap[crawlerPos[0]][crawlerPos[1] + 1][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1] + 1][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1] + 1, crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }

                    //-y
                    {
                        if (crawlerPos[1] - 1 >= 0 && memoryMap[crawlerPos[0]][crawlerPos[1] - 1][crawlerPos[2]] < newUpdate.level - 1) {
                            memoryMap[crawlerPos[0]][crawlerPos[1] - 1][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                            lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1] - 1, crawlerPos[2], (byte) (newUpdate.level - 1)));
                        }
                    }
                }
            }

            for (int x = posX - lightDistance; x <= posX + lightDistance; x++) {
                for (int y = posY - lightDistance; y <= posY + lightDistance; y++) {
                    for (int z = posZ - lightDistance; z <= posZ + lightDistance; z++) {
                        if (memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] != blockIndicator) {
                            setTorchLight(x, y, z, memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance]);
                        }
                    }
                }
            }
        }).start();
    }
}
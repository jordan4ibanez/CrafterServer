package game.chunk;

import engine.FastNoise;
import engine.network.BlockBreakingReceiver;
import engine.network.BlockPlacingReceiver;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

import static engine.FancyMath.getDistance;
import static engine.Time.getDelta;
import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.instantSave;
import static engine.disk.SaveQueue.saveChunk;
import static game.blocks.BlockDefinition.onDigCall;
import static game.blocks.BlockDefinition.onPlaceCall;
import static game.chunk.ChunkMath.posToIndex;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;
import static game.light.Light.*;
import static game.player.Player.*;

public class Chunk {

    private static final ConcurrentHashMap<String, ChunkObject> map = new ConcurrentHashMap<>();

    public static Collection<ChunkObject> getMap(){
        return map.values();
    }

    public static ChunkObject getChunk(int x, int z){
        return map.get(x + " " + z);
    }

    private static double getChunkDistanceFromPlayer(Player thisPlayer, int x, int z){
        Vector3i currentChunk = new Vector3i(thisPlayer.currentChunk.x, 0, thisPlayer.currentChunk.z);
        return Math.max(getDistance(0,0,currentChunk.z, 0, 0, z), getDistance(currentChunk.x,0,0, x, 0, 0));
    }


    private static float saveTimer = 0f;
    public static void globalChunkSaveToDisk(){
        saveTimer += getDelta();
        //System.out.println(saveTimer);
        //save interval is 3 seconds
        if (saveTimer >= 3f){
            for (Player thisPlayer : getAllPlayers()){
                savePlayerPos(thisPlayer.name, thisPlayer.pos);
            }
            updateWorldsPathToAvoidCrash();
            for (ChunkObject thisChunk : map.values()){
                if (thisChunk.modified) {
                    saveChunk(thisChunk);
                    thisChunk.modified = false;
                }
            }

            saveTimer = 0f;
        }
    }

    //this is for testing the day/night cycle
    public static void testLightCycleFlood(){
        byte currentLightLevel = getCurrentGlobalLightLevel();
        for (ChunkObject thisChunk : map.values()){
            if (thisChunk.lightLevel != currentLightLevel){
                floodChunkWithNewGlobalLight(thisChunk, thisChunk.lightLevel, currentLightLevel);
                thisChunk.lightLevel = currentLightLevel;
            }
        }
    }

    //this is for testing the day/night cycle
    private static void floodChunkWithNewGlobalLight(ChunkObject thisChunk, byte oldLight, byte newLight){

        /* this causes SERIOUS lag
        Vector3i thisPos = indexToPos(i);

        int blockX = (int)(thisPos.x + (16d*thisChunk.x));
        int blockZ = (int)(thisPos.z + (16d*thisChunk.z));

        if (thisChunk.block[i] == 0 && !underSunLight(blockX, thisPos.y, blockZ)) {
            lightFloodFill(blockX, thisPos.y, blockZ);
        }

        if (thisChunk.light[i] == oldLight) {
          System.out.println("something broken");
        }
         */
        Arrays.fill(thisChunk.light, newLight);

    }

    public static void globalFinalChunkSaveToDisk(){
        updateWorldsPathToAvoidCrash();
        for (ChunkObject thisChunk : map.values()){
            instantSave(thisChunk);
            thisChunk.modified = false;
        }
        map.clear();
    }

    public static boolean chunkStackContainsBlock(int chunkX, int chunkZ, int yHeight){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null || thisChunk.block == null){
            return false;
        }
        for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++){
                for (int y = yHeight * 16; y < (yHeight + 1) * 16; y++){
                    if (thisChunk.block[posToIndex(x,y,z)] != 0){
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public static int getHeightMap(int x, int z){
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 555;
        }
        if (thisChunk.block == null){
            return 555;
        }

        return thisChunk.heightMap[blockX][blockZ];
    }

    public static boolean underSunLight(int x, int y, int z){
        if (y > 127 || y < 0){
            return false;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return false;
        }
        if (thisChunk.block == null){
            return false;
        }
        return thisChunk.heightMap[blockX][blockZ] < y + 1;
    }

    public static int getBlock(int x,int y,int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return -1;
        }
        if (thisChunk.block == null){
            return -1;
        }
        return thisChunk.block[posToIndex(blockX, y, blockZ)];
    }

    public static byte getBlockRotation(int x, int y, int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.block == null){
            return 0;
        }
        return thisChunk.rotation[posToIndex(blockX, y, blockZ)];
    }

    public static void setBlock(int x,int y,int z, int newBlock, int rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[posToIndex(blockX, y, blockZ)] = newBlock;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] = (byte)rot;
        if (newBlock == 0){
            if (thisChunk.heightMap[blockX][blockZ] == y){
                for (int yCheck = thisChunk.heightMap[blockX][blockZ]; yCheck > 0; yCheck--){
                    if (thisChunk.block[posToIndex(blockX, yCheck, blockZ)] != 0){
                        thisChunk.heightMap[blockX][blockZ] = (byte) yCheck;
                        break;
                    }
                }
            }
        } else {
            if (thisChunk.heightMap[blockX][blockZ] < y){
                thisChunk.heightMap[blockX][blockZ] = (byte) y;
            }
        }
        thisChunk.modified = true;
        chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }

    public static void setLight(int x,int y,int z, byte newLight){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.light[posToIndex(blockX, y, blockZ)] = newLight;
        chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }


    public static void digBlock(int x,int y,int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        int oldBlock = thisChunk.block[posToIndex(blockX, y, blockZ)];
        thisChunk.block[posToIndex(blockX, y, blockZ)] = 0;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] = 0;
        if (thisChunk.heightMap[blockX][blockZ] == y){
            for (int yCheck = thisChunk.heightMap[blockX][blockZ]; yCheck > 0; yCheck--){
                if (thisChunk.block[posToIndex(blockX, yCheck, blockZ)] != 0){
                    thisChunk.heightMap[blockX][blockZ] = (byte) yCheck;
                    break;
                }
            }
        }
        lightFloodFill(x, y, z);
        thisChunk.modified = true;
        thisChunk.light[posToIndex(blockX, y, blockZ)] = getImmediateLight(x,y,z);

        addBrokenBlockToPlayerQueue(chunkX,chunkZ, x,y,z);

        onDigCall(oldBlock, new Vector3d(x,y,z));
    }


    private static void addBrokenBlockToPlayerQueue(int chunkX, int chunkZ, int x, int y, int z){
        for (Player thisPlayer : getAllPlayers()){
            if (getChunkDistanceFromPlayer(thisPlayer, chunkX,chunkZ) < thisPlayer.renderDistance){
                thisPlayer.blockBreakingQueue.put(x + " " + y + " " + z, new BlockBreakingReceiver(new Vector3i(x,y,z)));
            }
        }
    }

    public static void placeBlock(int x,int y,int z, int ID, int rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (x - (16*chunkX));
        int blockZ = (z - (16*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[posToIndex(blockX, y, blockZ)] = ID;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] = (byte) rot;
        if (thisChunk.heightMap[blockX][blockZ] < y){
            thisChunk.heightMap[blockX][blockZ] = (byte) y;
        }
        lightFloodFill(x, y, z);
        thisChunk.modified = true;

        onPlaceCall(ID, new Vector3d(x,y,z));

        addPlacedBlockToPlayersQueue(chunkX,chunkZ,x,y,z, ID, (byte)rot);
    }

    private static void addPlacedBlockToPlayersQueue(int chunkX, int chunkZ, int x, int y, int z, int ID, byte rotation){
        for (Player thisPlayer : getAllPlayers()){
            if (getChunkDistanceFromPlayer(thisPlayer, chunkX,chunkZ) < thisPlayer.renderDistance){
                thisPlayer.blockPlacingQueue.put(x + " " + y + " " + z, new BlockPlacingReceiver(new Vector3i(x,y,z), ID, rotation));
            }
        }
    }

    public static byte getLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.light == null){
            return 0;
        }
        return thisChunk.light[posToIndex(blockX, y, blockZ)];
    }

    private static void updateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        switch (y) {
            case 112, 96, 80, 64, 48, 32, 16 -> chunkUpdate(chunkX, chunkZ, yPillar - 1);
            case 111, 95, 79, 63, 47, 31, 15 -> chunkUpdate(chunkX, chunkZ, yPillar + 1);
        }
        if (x == 15){ //update neighbor
            chunkUpdate(chunkX+1, chunkZ, yPillar);
        }
        if (x == 0){
            chunkUpdate(chunkX-1, chunkZ, yPillar);
        }
        if (z == 15){
            chunkUpdate(chunkX, chunkZ+1, yPillar);
        }
        if (z == 0){
            chunkUpdate(chunkX, chunkZ-1, yPillar);
        }
    }

    private static void fullNeighborUpdate(int chunkX, int chunkZ){
        if (map.get(chunkX+1 + " " + chunkZ) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX+1, chunkZ, y);
            }
        }
        if (map.get(chunkX-1 + " " + chunkZ) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX-1, chunkZ, y);
            }
        }
        if (map.get(chunkX + " " + (chunkZ+1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ+1, y);
            }
        }
        if (map.get(chunkX + " " + (chunkZ-1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ-1, y);
            }
        }
    }


    public static void generateNewChunks(Player thisPlayer){
        //create the initial map in memory
        int chunkRenderDistance = thisPlayer.renderDistance;
        Vector3i currentChunk = new Vector3i(thisPlayer.currentChunk.x,0, thisPlayer.currentChunk.z);
        String currChunk;
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(thisPlayer,x,z) <= chunkRenderDistance){
                    currChunk = x + " " + z;
                    if (map.get(currChunk) == null){
                        genBiome(x,z);
                        for (int y = 0; y < 8; y++) {
                            chunkUpdate(x, z, y);
                        }
                        fullNeighborUpdate(x, z);
                    }
                }
            }
        }
        //scan map for out of range chunks
        for (ChunkObject thisChunk : map.values()){
            if (getChunkDistanceFromPlayer(thisPlayer, thisChunk.x,thisChunk.z) > chunkRenderDistance){
                addChunkToDeletionQueue(thisChunk.x,thisChunk.z);
            }
        }
    }

    private static final Deque<String> deletionQueue = new ArrayDeque<>();

    private static void addChunkToDeletionQueue(int chunkX, int chunkZ) {
        deletionQueue.add(chunkX + " " + chunkZ);
    }


    public static void processOldChunks() {

        while (!deletionQueue.isEmpty()) {
            String key = deletionQueue.pop();
            ChunkObject thisChunk = map.get(key);
            if (thisChunk == null) {
                return;
            }
            if (thisChunk.modified) {
                saveChunk(thisChunk);
            }
            map.remove(key);
        }
    }

    private final static int seed = 532_444_432;

    public static void genBiome(int chunkX, int chunkZ) {

        new Thread(() -> {
            final double heightAdder = 70;
            final byte dirtHeight = 4;
            final byte waterHeight = 50;
            final FastNoise noise = new FastNoise();
            final int noiseMultiplier = 50;

            noise.SetSeed(seed);

            double dirtHeightRandom;
            boolean gennedSand;
            boolean gennedWater;
            boolean gennedGrass;
            byte generationX;
            byte generationY;
            byte generationZ;
            int currBlock;
            byte height;

            ChunkObject loadedChunk = null;
            try {
                loadedChunk = loadChunkFromDisk(chunkX, chunkZ);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (loadedChunk != null) {
                map.put(chunkX + " " + chunkZ, loadedChunk);

                //dump everything into the chunk updater
                for (int i = 0; i < 8; i++) {
                    chunkUpdate(loadedChunk.x, loadedChunk.z, i); //delayed
                }
            } else {
                ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);

                if (thisChunk == null) {
                    thisChunk = new ChunkObject(chunkX,chunkZ);
                    //do not put chunk in yet, or else main thread worker for networking
                    //will send unfinished "corrupted" chunks
                }

                thisChunk.modified = true;
                //biome max 128 trees
                Vector3i[] treePosArray = new Vector3i[128];
                byte treeIndex = 0;
                //standard generation
                for (generationX = 0; generationX < 16; generationX++) {
                    for (generationZ = 0; generationZ < 16; generationZ++) {
                        gennedSand = false;
                        gennedWater = false;
                        gennedGrass = false;
                        dirtHeightRandom = Math.floor(Math.random() * 2d);

                        float realPosX = (float)((chunkX * 16d) + (double) generationX);
                        float realPosZ = (float)((chunkZ * 16d) + (double) generationZ);

                        height = (byte) (Math.abs(noise.GetPerlin(realPosX, realPosZ) * noiseMultiplier + heightAdder));

                        //catch ultra deep oceans
                        if (height < 6) {
                            height = 6;
                        }

                        //y column
                        for (generationY = 127; generationY >= 0; generationY--) {

                            //don't overwrite
                            currBlock = thisChunk.block[posToIndex(generationX, generationY, generationZ)];

                            //bedrock
                            if (generationY <= 0 + dirtHeightRandom) {
                                currBlock = 5;
                                //grass gen
                            } else if (generationY == height && generationY >= waterHeight) {

                                if (generationY <= waterHeight + 1) {
                                    currBlock = 20;
                                    gennedSand = true;
                                } else {
                                    currBlock = 2;
                                    gennedGrass = true;
                                }
                                //tree gen
                            }else if (generationY == height + 1 && generationY > waterHeight + 1){

                                float noiseTest2 = Math.abs(noise.GetWhiteNoise(realPosX, generationY,realPosZ));

                                //add tree to queue
                                if (noiseTest2 > 0.98f){
                                    treePosArray[treeIndex] = new Vector3i(generationX, generationY, generationZ);
                                    treeIndex++;
                                }
                                //dirt/sand gen
                            } else if (generationY < height && generationY >= height - dirtHeight - dirtHeightRandom) {
                                if (gennedSand || gennedWater) {
                                    gennedSand = true;
                                    currBlock = 20;
                                } else {
                                    currBlock = 1;
                                }

                                //stone gen
                            } else if (generationY < height - dirtHeight) {
                                if (generationY <= 30 && generationY > 0) {
                                    if (Math.random() > 0.95) {
                                        currBlock = (short) Math.floor(8 + (Math.random() * 8));
                                    } else {
                                        currBlock = 3;
                                    }
                                } else {
                                    currBlock = 3;
                                }
                                //water gen
                            } else {
                                if (generationY <= waterHeight) {
                                    currBlock = 7;
                                    gennedWater = true;
                                }
                            }

                            thisChunk.block[posToIndex(generationX, generationY, generationZ)] = currBlock;

                            if (height >= waterHeight) {
                                thisChunk.heightMap[generationX][generationZ] = height;
                            } else {
                                thisChunk.heightMap[generationX][generationZ] = waterHeight;
                            }

                            if (gennedSand || gennedGrass) {
                                thisChunk.light[posToIndex(generationX, generationY, generationZ)] = 0;
                            } else {
                                thisChunk.light[posToIndex(generationX, generationY, generationZ)] = getCurrentGlobalLightLevel();
                            }
                        }
                    }
                }

                //check for trees outside chunk borders (simulated chunk generation)
                for (generationX = -3; generationX < 16 + 3; generationX++) {
                    for (generationZ = -3; generationZ < 16 + 3; generationZ++) {

                        //only check outside
                        if (generationX < 0 || generationX > 15 || generationZ < 0 || generationZ > 15) {

                            float realPosX = (float) ((chunkX * 16d) + (double) generationX);
                            float realPosZ = (float) ((chunkZ * 16d) + (double) generationZ);

                            height = (byte) (Math.abs(noise.GetPerlin(realPosX, realPosZ) * noiseMultiplier + heightAdder) + (byte) 1);

                            if (height > waterHeight + 1) {

                                float noiseTest2 = Math.abs(noise.GetWhiteNoise(realPosX, height, realPosZ));

                                //add tree to queue
                                if (noiseTest2 > 0.98f){
                                    treePosArray[treeIndex] = new Vector3i(generationX, height, generationZ);
                                    treeIndex++;
                                }

                            }
                        }
                    }
                }

                //generate tree cores
                for (int i = 0; i < treeIndex; i++){
                    Vector3i basePos = treePosArray[i];
                    //generate stumps
                    for (int y = 0; y < 4; y++){
                        //stay within borders
                        if (y + treePosArray[i].y < 127 && basePos.x >= 0 && basePos.x <= 15 && basePos.z >= 0 && basePos.z <= 15){
                            thisChunk.block[posToIndex(basePos.x,basePos.y + y, basePos.z)] = 25;
                        }
                    }
                }

                //generate tree leaves
                for (int i = 0; i < treeIndex; i++){
                    Vector3i basePos = treePosArray[i];
                    byte treeWidth = 0;
                    for (int y = 5; y > 1; y--){
                        for (int x = -treeWidth; x <= treeWidth; x++){
                            for (int z = -treeWidth; z <= treeWidth; z++) {

                                if (    basePos.x + x >= 0 && basePos.x + x <= 15 &&
                                        basePos.y + y >= 0 && basePos.y + y <= 127 &&
                                        basePos.z + z >= 0 && basePos.z + z <= 15) {

                                    int index = posToIndex(basePos.x + x, basePos.y + y, basePos.z + z);

                                    if (thisChunk.block[index] == 0) {
                                        thisChunk.block[index] = 26;
                                    }
                                }
                            }
                        }
                        if (treeWidth < 2) {
                            treeWidth++;
                        }
                    }

                }

                //this chunk is finished, save it to main thread memory pool
                map.put(chunkX + " " + chunkZ, thisChunk);

                //dump everything into the chunk updater
                for (int i = 0; i < 8; i++) {
                    //generateChunkMesh(thisChunk.x, thisChunk.z, i); //instant
                    chunkUpdate(thisChunk.x, thisChunk.z, i); //delayed
                }

                instantSave(thisChunk);
            }
        }).start();
    }
}

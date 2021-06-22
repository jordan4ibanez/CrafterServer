package game.chunk;

import engine.network.BlockBreakUpdate;
import engine.network.BlockPlaceUpdate;
import game.player.Player;
import org.joml.Vector2i;
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
import static game.chunk.BiomeGenerator.addChunkToBiomeGeneration;
import static game.chunk.ChunkMath.posToIndex;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;
import static game.light.Light.*;
import static game.player.Player.getAllPlayers;

public class Chunk {

    private static final ConcurrentHashMap<Vector2i, ChunkObject> map = new ConcurrentHashMap<>();

    public static Collection<ChunkObject> getMap(){
        return map.values();
    }

    public static ChunkObject getChunk(int x, int z){
        return map.get(new Vector2i(x,z));
    }

    private static double getChunkDistanceFromPlayer(Player thisPlayer, int x, int z){
        Vector3i currentChunk = new Vector3i(thisPlayer.currentChunk.x, 0, thisPlayer.currentChunk.z);
        return Math.max(getDistance(0,0,currentChunk.z, 0, 0, z), getDistance(currentChunk.x,0,0, x, 0, 0));
    }


    public static void setChunk(int x, int z, ChunkObject newChunk){
        if (map.get(new Vector2i(x,z)) != null){
            map.remove(new Vector2i(x,z));
        }

        map.put(new Vector2i(x,z), newChunk);

        for (int y = 0; y < 8; y++) {
            chunkUpdate(x, z, y);
        }

        fullNeighborUpdate(x,z);
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
            floodChunkWithNewGlobalLight(thisChunk, currentLightLevel);
        }
    }

    //this is for testing the day/night cycle
    private static void floodChunkWithNewGlobalLight(ChunkObject thisChunk, byte newLight){

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
        Arrays.fill(thisChunk.naturalLight, newLight);
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX,chunkZ));
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
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
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
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
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
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
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
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
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
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
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

    public static void setNaturalLight(int x, int y, int z, byte newLight){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.naturalLight[posToIndex(blockX, y, blockZ)] = newLight;
        chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }

    public static void setTorchLight(int x,int y,int z, byte newLight){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.torchLight[posToIndex(blockX, y, blockZ)] = newLight;
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
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
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
        torchFloodFill(x, y, z);

        thisChunk.modified = true;
        thisChunk.naturalLight[posToIndex(blockX, y, blockZ)] = getImmediateLight(x,y,z);

        addBrokenBlockToPlayerQueue(chunkX,chunkZ, x,y,z);

        onDigCall(oldBlock, new Vector3d(x,y,z));
    }


    private static void addBrokenBlockToPlayerQueue(int chunkX, int chunkZ, int x, int y, int z){
        for (Player thisPlayer : getAllPlayers()){
            if (getChunkDistanceFromPlayer(thisPlayer, chunkX,chunkZ) < thisPlayer.renderDistance){
                thisPlayer.blockBreakingQueue.put(x + " " + y + " " + z, new BlockBreakUpdate(new Vector3i(x,y,z)));
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
        ChunkObject thisChunk = map.get(new Vector2i(x,z));
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
        torchFloodFill(x, y, z);
        thisChunk.modified = true;

        addPlacedBlockToPlayersQueue(chunkX,chunkZ,x,y,z, ID, (byte)rot);

        onPlaceCall(ID, new Vector3d(x,y,z));
    }

    private static void addPlacedBlockToPlayersQueue(int chunkX, int chunkZ, int x, int y, int z, int ID, byte rotation){
        for (Player thisPlayer : getAllPlayers()){
            if (getChunkDistanceFromPlayer(thisPlayer, chunkX,chunkZ) < thisPlayer.renderDistance){
                thisPlayer.blockPlacingQueue.put(x + " " + y + " " + z, new BlockPlaceUpdate(new Vector3i(x,y,z), ID, rotation));
            }
        }
    }

    public static byte getNaturalLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        ChunkObject thisChunk = map.get(new Vector2i(x,z));

        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.naturalLight == null){
            return 0;
        }

        return thisChunk.naturalLight[posToIndex(blockX, y, blockZ)];
    }

    public static byte getTorchLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        ChunkObject thisChunk = map.get(new Vector2i(x,z));

        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.torchLight == null){
            return 0;
        }


        int index = posToIndex(blockX, y, blockZ);

        return thisChunk.torchLight[index];
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

        if (map.get(new Vector2i(chunkX+1,chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX+1, chunkZ, y);
            }
        }

        if (map.get(new Vector2i(chunkX-1,chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX-1, chunkZ, y);
            }
        }

        if (map.get(new Vector2i(chunkX,chunkZ+1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ+1, y);
            }
        }

        if (map.get(new Vector2i(chunkX,chunkZ-1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ-1, y);
            }
        }
    }


    public static void generateNewChunks(Player thisPlayer){
        //create the initial map in memory
        int chunkRenderDistance = thisPlayer.renderDistance;
        Vector3i currentChunk = new Vector3i(thisPlayer.currentChunk.x,0, thisPlayer.currentChunk.z);
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(thisPlayer,x,z) <= chunkRenderDistance){
                    if (map.get(new Vector2i(x,z)) == null){
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

    private static final Deque<Vector2i> deletionQueue = new ArrayDeque<>();

    private static void addChunkToDeletionQueue(int chunkX, int chunkZ) {
        deletionQueue.add(new Vector2i(chunkX, chunkZ));
    }


    public static void processOldChunks() {

        while (!deletionQueue.isEmpty()) {
            Vector2i key = deletionQueue.pop();
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

    public static void genBiome(int chunkX, int chunkZ) {
        ChunkObject loadedChunk = null;
        try {
            loadedChunk = loadChunkFromDisk(chunkX, chunkZ);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (loadedChunk != null) {
            map.put(new Vector2i(chunkX, chunkZ), loadedChunk);

            //dump everything into the chunk updater
            for (int i = 0; i < 8; i++) {
                chunkUpdate(loadedChunk.x, loadedChunk.z, i); //delayed
            }
        } else {
            addChunkToBiomeGeneration(chunkX,chunkZ);
        }
    }
}

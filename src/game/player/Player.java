package game.player;
import engine.network.*;

import game.chunk.ChunkObject;
import game.crafting.InventoryObject;
import game.item.Item;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.FancyMath.getDistance;
import static engine.time.Time.getDelta;
import static engine.disk.Disk.loadPlayerPos;
import static engine.network.Networking.*;
import static engine.time.TimeOfDay.getTimeOfDay;
import static game.chunk.Chunk.*;
import static game.item.ItemEntity.getAllItemEntities;


public class Player {

    private static final ConcurrentHashMap<Integer,Player> players = new ConcurrentHashMap<>();

    public static List<Player> getAllPlayers(){
        return new ArrayList<>(players.values());
    }

    public InventoryObject mainInventory = null; //try to load this eventually
    public int health = 20;
    public int ID;
    public int renderDistance = 5;
    public int itemRenderDistance = 15;
    public Vector3d pos                  = new Vector3d(0,100,0);
    public int hotBarSlot = 0;
    public final float eyeHeight         = 1.5f;
    public final float collectionHeight  = 0.7f;
    public final Vector3f inertia        = new Vector3f(0,0,0);
    public boolean mining                = false;
    public boolean placing               = false;
    public String name;
    public Vector3i oldWorldSelectionPos = new Vector3i();
    public Vector3i worldSelectionPos    = new Vector3i();
    public final Vector3i currentChunk = new Vector3i((int)Math.floor(pos.x / 16f),0,(int)Math.floor(pos.z / 16f));
    public static int oldY = 0;
    public boolean sneaking              = false;
    public boolean running               = false;

    public ConcurrentLinkedDeque<Vector2i> chunkLoadingQueue = new ConcurrentLinkedDeque<>();
    public ConcurrentLinkedDeque<Vector3i> blockBreakingQueue = new ConcurrentLinkedDeque<>();
    public ConcurrentLinkedDeque<BlockPlaceUpdate> blockPlacingQueue = new ConcurrentLinkedDeque<>();

    public Vector3d camPos = new Vector3d();
    public Vector3f camRot = new Vector3f();




    public void updatePlayerInventory(InventoryObject newInventory){
        this.mainInventory = null; //throw it to the GC
        this.mainInventory = newInventory;
    }


    public static void addPlayer(String name, int ID){
        Player thisPlayer = new Player();
        thisPlayer.name = name;
        thisPlayer.ID = ID;
        thisPlayer.pos = loadPlayerPos(name);
        players.put(ID,thisPlayer);

        sendPlayerNetworkMovePositionDemand(ID, new NetworkMovePositionDemand(thisPlayer.pos));
    }

    public static Player getPlayerByID(int ID){
        for (Player thisPlayer : players.values()){
            if (thisPlayer.ID == ID){
                return thisPlayer;
            }
        }
        return null;
    }

    public static Player getPlayerByName(String name){
        for (Player thisPlayer : players.values()){
            if (thisPlayer.name.equals(name)){
                return thisPlayer;
            }
        }
        return null;
    }

    public static void removePlayer(int ID){
        players.remove(ID);
    }

    public static void indexAndLoadQueuedChunksForEachPlayer() throws IOException {
        for (Player thisPlayer : players.values()){
            if (thisPlayer.chunkLoadingQueue.size() > 0){

                Vector2i thisQueue = thisPlayer.chunkLoadingQueue.pop();

                if (thisQueue != null) {

                    ChunkObject thisChunk = getChunk(thisQueue.x, thisQueue.y);

                    if (thisChunk != null) {
                        sendPlayerChunkData(thisPlayer.ID, thisChunk);

                        //System.out.println("sending player: " + thisChunk.x + " " + thisChunk.z);

                        //remove all equal clones
                        thisPlayer.chunkLoadingQueue.removeIf(thisData -> thisData.equals(thisQueue.x, thisQueue.y));
                    } else {
                        genBiome(thisQueue.x, thisQueue.y);
                        //don't addFirst, it's a linked deque
                        thisPlayer.chunkLoadingQueue.add(new Vector2i(thisQueue.x, thisQueue.y));
                    }
                }
            }
        }
    }

    public static void sendThisPlayerOtherPlayerPos(int playerID){
        for (Player thisPlayer : players.values()){
            if (thisPlayer.ID != playerID){
                PlayerPosObject thisPlayerPosObject = new PlayerPosObject();
                thisPlayerPosObject.ID = thisPlayer.ID;
                thisPlayerPosObject.pos = thisPlayer.pos;
                thisPlayerPosObject.cameraRot = new Vector3f(thisPlayer.camRot);
                thisPlayerPosObject.name = thisPlayer.name;
                sendPlayerPosition(playerID,thisPlayerPosObject);
            }
        }

    }





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

    public static boolean playerExists(String playerName){
        for (Player player : players.values()){
            if (player.name.equals(playerName)){
                return true;
            }
        }
        return false;
    }


    private static double playerDataTimerTicker = 0d;

    public static void playersOnTick() {

        double delta = getDelta();

        playerDataTimerTicker += delta;

        //every 0.05 seconds
        if (playerDataTimerTicker >= 0.05f){
            playerDataTimerTicker = 0f;


            for (Player thisPlayer : players.values()){

                //update their world chunk loader to discard far chunks if not within
                //an overlapping box of another player
                updateWorldChunkLoader(thisPlayer);

                //send players other player positions
                sendThisPlayerOtherPlayerPos(thisPlayer.ID);

                //send players items within their item render distance
                sendThisPlayerItemEntities(thisPlayer);

                //send player the updated time
                sendPlayerTime(thisPlayer.ID, getTimeOfDay());

            }

        }

        //do this every tick as it's very light
        //mining will appear to lag very slightly if not
        for (Player thisPlayer : players.values()) {
            sendThisPlayerBrokenBlocks(thisPlayer);
            sendThisPlayerPlacedBlocks(thisPlayer);
        }
    }

    private static void sendThisPlayerItemEntities(Player thisPlayer){
        Collection<Item> allItemEntities = getAllItemEntities();

        //we are creating new Vector3d objects here as to not cause
        //a memory leak - as in mutability leaking
        Vector3d pos1 = new Vector3d(thisPlayer.pos);

        double acceptableDistance = thisPlayer.itemRenderDistance;

        int ID = thisPlayer.ID;

        for (Item thisItem : allItemEntities){

            Vector3d pos2 = new Vector3d(thisItem.pos);

            if (getDistance(pos1,pos2) <= acceptableDistance){

                ItemSendingObject itemSendingObject = new ItemSendingObject(thisItem.pos, thisItem.ID, thisItem.name);

                sendPlayerItemData(ID, itemSendingObject);
            }
        }
    }


    private static void sendThisPlayerBrokenBlocks(Player thisPlayer){
        if (thisPlayer.blockBreakingQueue.size() > 0) {
            Vector3i thisUpdate = thisPlayer.blockBreakingQueue.pop();

            if (thisUpdate != null) {
                sendPlayerBrokenBlockData(thisPlayer.ID, thisUpdate);

                //remove duplicates
                thisPlayer.blockBreakingQueue.removeIf(dupePos -> thisUpdate.equals(dupePos.x, dupePos.y, dupePos.z));
            }
        }
    }

    private static void sendThisPlayerPlacedBlocks(Player thisPlayer){
        if (thisPlayer.blockPlacingQueue.size() > 0){
            BlockPlaceUpdate thisUpdate = thisPlayer.blockPlacingQueue.pop();

            if (thisUpdate != null) {
                sendPlayerPlacedBlockData(thisPlayer.ID, thisUpdate);

                //remove duplicates
                thisPlayer.blockPlacingQueue.removeIf(thisUpdate::equals);
            }
        }
    }

    private static void updateWorldChunkLoader(Player thisPlayer){
        int newChunkX = (int)Math.floor(thisPlayer.pos.x / 16f);
        int newChunkZ = (int)Math.floor(thisPlayer.pos.z / 16f);

        if (newChunkX != thisPlayer.currentChunk.x || newChunkZ != thisPlayer.currentChunk.z) {
            thisPlayer.currentChunk.x = newChunkX;
            thisPlayer.currentChunk.z = newChunkZ;
        }
    }
}

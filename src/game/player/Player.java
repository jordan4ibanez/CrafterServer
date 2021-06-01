package game.player;
import game.chunk.Chunk;
import game.chunk.ChunkObject;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static engine.Time.getDelta;
import static engine.disk.Disk.loadPlayerPos;
import static engine.network.NetworkOutput.sendPlayerChunkData;
import static game.chunk.Chunk.*;


public class Player {

    private static final List<Player> players = new ArrayList<>();

    public static List<Player> getAllPlayers(){
        return players;
    }

    public static void addPlayer(String name, InetAddress inetAddress){
        Player thisPlayer = new Player();
        thisPlayer.name = name;
        thisPlayer.inetAddress = inetAddress;
        players.add(thisPlayer);
    }

    public static Player getPlayerByInet(InetAddress inetAddress){
        for (Player thisPlayer : players){
            if (thisPlayer.inetAddress.equals(inetAddress)){
                return thisPlayer;
            }
        }
        return null;
    }

    public static Player getPlayerByName(String name){
        for (Player thisPlayer : players){
            if (thisPlayer.name.equals(name)){
                return thisPlayer;
            }
        }
        return null;
    }

    public static void indexAndLoadQueuedChunksForEachPlayer(){
        for (Player thisPlayer : players){
            if (thisPlayer.chunkLoadingQueue.size() > 0){
                String thisQueue = thisPlayer.chunkLoadingQueue.get(0);

                thisPlayer.chunkLoadingQueue.add(thisQueue);
                String backupString = new String(thisQueue);
                String xString = thisQueue.split(" ")[0];
                int x = Integer.parseInt(xString);
                String zString = backupString.split(" ")[1];
                int z = Integer.parseInt(zString);

                ChunkObject thisChunk = getChunk(x,z);
                if (thisChunk != null){
                    sendPlayerChunkData(thisPlayer.inetAddress, thisChunk);
                    thisPlayer.chunkLoadingQueue.remove(0);
                } else {
                    genBiome(x, z);
                }
            }
        }
    }

    public int health = 20;
    InetAddress inetAddress;
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

    public List<String> chunkLoadingQueue = new ArrayList<>();

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



    public static boolean playerExists(String playerName){
        for (Player player : players){
            if (player.name.equals(playerName)){
                return true;
            }
        }
        return false;
    }
    public static Player getPlayer(String name){
        for (Player player : players){
            if (player.name.equals(name)){
                return player;
            }
        }
        return null;
    }


    public static void playersOnTick() {

        float delta = getDelta();

        for (Player thisPlayer : players) {


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


}

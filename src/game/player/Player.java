package game.player;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static engine.Time.getDelta;
import static engine.disk.Disk.loadPlayerPos;
import static game.chunk.Chunk.*;


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

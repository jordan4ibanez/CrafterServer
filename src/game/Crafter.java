package game;

import game.item.ItemEntity;
import game.player.Player;
import game.tnt.TNTEntity;

import static engine.Time.calculateDelta;
import static engine.disk.Disk.createWorldsDir;
import static engine.disk.Disk.savePlayerPos;
import static engine.disk.SaveQueue.startSaveThread;
import static engine.settings.Settings.loadSettings;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkUpdateHandler.chunkUpdater;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.mob.Mob.mobsOnTick;
import static game.player.Player.getAllPlayers;
import static game.player.Player.playersOnTick;

public class Crafter {

    //fields
    private static final String versionName = "Crafter 0.04a Survival Test";

    public static String getVersionName(){
        return versionName;
    }

    public static boolean isGameShouldClose(){
        return false;
    }

    //core game engine elements
    //load everything
    public static void main(String[] args){
        try{
            loadSettings();
            initGame();
            createWorldsDir();
            startSaveThread();

            System.out.println("REMEMBER TO ADD IN CONTROLS IN THE TERMINAL!");
            while (true) {
                gameLoop();
            }

        } catch ( Exception excp ){
            excp.printStackTrace();
            System.exit(-1);
        } finally {
            globalFinalChunkSaveToDisk();
            for (Object thisPlayer : getAllPlayers()){
                Player player = (Player)thisPlayer;
                savePlayerPos(player.name, player.pos);
            }
        }
    }

    //main game loop
    private static void gameLoop() throws Exception {
        calculateDelta();
        chunkUpdater();
        globalChunkSaveToDisk();
        gameUpdate();
        processOldChunks();
    }

    private static void gameUpdate() throws Exception {
        playersOnTick();
        ItemEntity.onStep();
        TNTEntity.onTNTStep();
        fallingEntityOnStep();
        mobsOnTick();
    }

    //the game engine elements
    public static void initGame() {
        //this initializes the block definitions
        initializeBlocks();
    }
}

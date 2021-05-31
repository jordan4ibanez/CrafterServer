package game;

import game.chunk.Chunk;
import game.item.ItemDefinition;
import game.player.Player;

import static engine.disk.Disk.createWorldsDir;
import static engine.disk.Disk.savePlayerPos;
import static engine.disk.SaveQueue.startSaveThread;
import static engine.scene.SceneHandler.handleSceneLogic;
import static engine.settings.Settings.loadSettings;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.chunk.Chunk.globalFinalChunkSaveToDisk;
import static game.player.Player.getAllPlayers;

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
            //this is the scene controller
            handleSceneLogic();

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

    //the game engine elements
    public static void initGame() {
        //this initializes the block definitions
        initializeBlocks();
    }
}

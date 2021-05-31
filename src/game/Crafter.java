package game;

import game.chunk.Chunk;
import game.item.ItemDefinition;

import static engine.disk.Disk.createWorldsDir;
import static engine.disk.Disk.savePlayerPos;
import static engine.disk.SaveQueue.startSaveThread;
import static engine.scene.SceneHandler.handleSceneLogic;
import static engine.settings.Settings.loadSettings;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.chunk.Chunk.globalFinalChunkSaveToDisk;
import static game.crafting.CraftRecipes.registerCraftRecipes;
import static game.player.Player.getPlayerPos;
import static game.tnt.TNTEntity.createTNTEntityMesh;

public class Crafter {

    //fields
    private static final String versionName = "Crafter 0.04a Survival Test";

    public static String getVersionName(){
        return versionName;
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
            savePlayerPos(getPlayerPos());
            cleanup();
        }
    }

    //the game engine elements
    public static void initGame() throws Exception{
        //this initializes the block definitions
        initializeBlocks();
        //this creates a TNT mesh (here for now)
        createTNTEntityMesh();
        registerCraftRecipes();
    }


    private static void cleanup(){
        Chunk.cleanUp();
        ItemDefinition.cleanUp();
    }
}
package game;

import game.chunk.BiomeGenerator;
import game.player.Player;

import static engine.Time.calculateDelta;
import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.startSaveThread;
import static engine.network.Networking.*;
import static engine.settings.Settings.loadSettings;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.chunk.Chunk.*;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.item.ItemEntity.itemsOnTick;
import static game.mob.Mob.mobsOnTick;
import static game.player.Player.*;
import static game.tnt.TNTEntity.onTNTStep;

public class CrafterServer {



    boolean done = false;

    //fields
    private static final String versionName = "Crafter 0.04c Survival Test";

    public static String getVersionName(){
        return versionName;
    }

    public static boolean gameShouldClose(){
        return false;
    }

    //core game engine elements
    //load everything
    public static void main(String[] args){

        //System.out.println("the args are: " + Arrays.toString(args));
        if (args != null && args.length > 0 && !args[0].equals("")){
            try {
                int newPort = Integer.parseInt(args[0]);
                setPort(newPort);
                //System.out.println("the new port is: " + tryingPort);
            } catch (Exception exception){
                System.out.println(args[0] + " IS NOT A VALID PORT! EXITING WITH CODE -1!");
                return;
            }
        }

        try{
            loadSettings();
            initGame();
            createWorldsDir();
            updateWorldsPathToAvoidCrash();
            initializeNetworking();

            //server successfully initialized, output info
            outputServerText();
            startSaveThread();


            //this is the biome generator thread
            BiomeGenerator biomeGenerator = new BiomeGenerator();
            Thread biomeThread = new Thread(biomeGenerator);

            biomeThread.start();

            System.out.println("SERVER IS RUNNING ON PORT: " + getGamePort());

            while (!gameShouldClose()) {
                gameLoop();
            }

        } catch ( Exception excp ){
            excp.printStackTrace();
            System.exit(-1);
        } finally {
            globalFinalChunkSaveToDisk();
            for (Player thisPlayer : getAllPlayers()){
                savePlayerPos(thisPlayer.name, thisPlayer.pos);
            }
            System.out.println("SERVER HAS STOPPED!");
        }
    }

    //main game loop
    private static void gameLoop() throws Exception {
        calculateDelta();
        globalChunkSaveToDisk();
        gameUpdate();
        processOldChunks();
        indexAndLoadQueuedChunksForEachPlayer();
        playersOnTick();
    }

    private static void gameUpdate() throws Exception {
        itemsOnTick();
        onTNTStep();
        fallingEntityOnStep();
        mobsOnTick();
    }

    //the game engine elements
    public static void initGame() {
        //this initializes the block definitions
        initializeBlocks();
    }


    private static void outputServerText(){
        System.out.println("   _____ _____            ______ _______ ______ _____  ");
        System.out.println("  / ____|  __ \\     /\\   |  ____|__   __|  ____|  __ \\ ");
        System.out.println(" | |    | |__) |   /  \\  | |__     | |  | |__  | |__) |");
        System.out.println(" | |    |  _  /   / /\\ \\ |  __|    | |  |  __| |  _  / ");
        System.out.println(" | |____| | \\ \\  / ____ \\| |       | |  | |____| | \\ \\ ");
        System.out.println("  \\_____|_|  \\_\\/_/    \\_\\_|       |_|  |______|_|  \\_\\");
        System.out.println("");
    }
}

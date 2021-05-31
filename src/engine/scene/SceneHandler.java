package engine.scene;

import game.item.ItemEntity;
import game.tnt.TNTEntity;

import static engine.Time.calculateDelta;
import static game.chunk.Chunk.globalChunkSaveToDisk;
import static game.chunk.Chunk.processOldChunks;
import static game.chunk.ChunkUpdateHandler.chunkUpdater;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.mob.Mob.mobsOnTick;
import static game.player.Player.*;

public class SceneHandler {

    public static void handleSceneLogic() throws Exception {

        System.out.println("REMEMBER TO ADD IN CONTROLS IN THE TERMINAL!");
        while (true) {
            gameLoop();
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
}

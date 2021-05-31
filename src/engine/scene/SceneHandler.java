package engine.scene;

import game.item.ItemEntity;
import game.tnt.TNTEntity;

import static engine.Time.calculateDelta;
import static engine.Window.windowShouldClose;
import static engine.gui.GUILogic.pauseMenuOnTick;
import static game.chunk.Chunk.globalChunkSaveToDisk;
import static game.chunk.Chunk.processOldChunks;
import static game.chunk.ChunkMesh.popChunkMeshQueue;
import static game.chunk.ChunkUpdateHandler.chunkUpdater;
import static game.crafting.InventoryLogic.inventoryMenuOnTick;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.mob.Mob.mobsOnTick;
import static game.particle.Particle.particlesOnStep;
import static game.player.Player.*;

public class SceneHandler {

    public static void handleSceneLogic() throws Exception {

        while (!windowShouldClose()) {
            gameLoop();
        }

    }

    //main game loop
    private static void gameLoop() throws Exception {
        calculateDelta();
        updateWorldChunkLoader();
        popChunkMeshQueue(); //this actually transmits the data from the other threads into main thread
        chunkUpdater();
        globalChunkSaveToDisk();
        gameUpdate();
        processOldChunks();
    }

    private static void gameUpdate() throws Exception {
        testPlayerDiggingAnimation();
        playerOnTick();
        ItemEntity.onStep();
        TNTEntity.onTNTStep();
        pauseMenuOnTick();
        inventoryMenuOnTick();
        particlesOnStep();
        fallingEntityOnStep();
        mobsOnTick();
    }
}

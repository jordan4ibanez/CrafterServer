package game.chunk;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static engine.Time.getDelta;
import static engine.settings.Settings.getSettingsChunkLoad;
import static game.chunk.Chunk.chunkStackContainsBlock;
import static game.chunk.Chunk.updateChunkUnloadingSpeed;
import static game.chunk.ChunkMesh.generateChunkMesh;
import static game.chunk.ChunkMesh.updateChunkMeshLoadingSpeed;

public class ChunkUpdateHandler {

    private static final ConcurrentHashMap<String, ChunkUpdate> queue = new ConcurrentHashMap<>();

    public static void chunkUpdate( int x, int z , int y){
        String keyName = x + " " + z + " " + y;
        if (queue.get(keyName) == null) {
            queue.put(keyName, new ChunkUpdate(x, z, y));
        }
    }

    private static final Random random = new Random();



    public static void chunkUpdater() {
        while (!queue.isEmpty()) {
            String key = "";

            Object[] queueAsArray = queue.keySet().toArray();
            String thisKey = (String)queueAsArray[random.nextInt(queueAsArray.length)];

            ChunkUpdate thisUpdate = queue.get(thisKey);

            if (!chunkStackContainsBlock(thisUpdate.x, thisUpdate.z, thisUpdate.y)) {
                key = thisUpdate.key;
            } else {
                generateChunkMesh(thisUpdate.x, thisUpdate.z, thisUpdate.y);
                queue.remove(thisUpdate.key);
            }
            if (!key.equals("")) {
                queue.remove(key);
            }
        }

    }
}

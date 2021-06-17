package engine.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.chunk.ChunkObject;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.zip.GZIPOutputStream;

import static game.CrafterServer.isGameShouldClose;

public class SaveQueue {

    private static byte currentActiveWorld = 1; //failsafe

    public static void updateSaveQueueCurrentActiveWorld(byte newWorld){
        currentActiveWorld = newWorld;
    }

    public static ConcurrentLinkedDeque<ChunkObject> chunkSaveQueueDisk = new ConcurrentLinkedDeque<>();

    public static void startSaveThread(){
        new Thread(() -> {
            final ObjectMapper mapper = new ObjectMapper();

            ChunkSavingObject savingObject;

            ChunkObject thisChunk;
            while(!isGameShouldClose()) {
                if (!chunkSaveQueueDisk.isEmpty()) {
                    try {

                        thisChunk = (ChunkObject) chunkSaveQueueDisk.toArray()[0];

                        //System.out.println("saving chunk: " + thisChunk.x + " " + thisChunk.z);

                        savingObject = new ChunkSavingObject();

                        savingObject.I = thisChunk.ID;
                        savingObject.x = thisChunk.x;
                        savingObject.z = thisChunk.z;
                        savingObject.b = thisChunk.block;
                        savingObject.r = thisChunk.rotation;
                        savingObject.l = thisChunk.naturalLight;
                        savingObject.t = thisChunk.torchLight;
                        savingObject.h = thisChunk.heightMap;

                        String stringedChunk = mapper.writeValueAsString(savingObject);

                        //learned from https://www.journaldev.com/966/java-gzip-example-compress-decompress-file
                        ByteArrayInputStream bais = new ByteArrayInputStream(stringedChunk.getBytes());
                        FileOutputStream fos = new FileOutputStream("Worlds/world" + currentActiveWorld + "/" + savingObject.I + ".chunk");
                        GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
                        byte[] buffer = new byte[4096];
                        int len;
                        while((len=bais.read(buffer)) != -1){
                            gzipOS.write(buffer, 0, len);
                        }
                        //close resources
                        gzipOS.close();
                        fos.close();
                        bais.close();

                        chunkSaveQueueDisk.remove(thisChunk);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void saveChunk(ChunkObject thisChunk){
        chunkSaveQueueDisk.add(thisChunk);
    }

    public static void instantSave(ChunkObject thisChunk){
        ObjectMapper mapper = new ObjectMapper();
        try {
            ChunkSavingObject savingObject = new ChunkSavingObject();

            savingObject.I = thisChunk.ID;
            savingObject.x = thisChunk.x;
            savingObject.z = thisChunk.z;
            savingObject.b = thisChunk.block;
            savingObject.r = thisChunk.rotation;
            savingObject.l = thisChunk.naturalLight;
            savingObject.t = thisChunk.torchLight;
            savingObject.h = thisChunk.heightMap;

            String stringedChunk = mapper.writeValueAsString(savingObject);

            //learned from https://www.journaldev.com/966/java-gzip-example-compress-decompress-file
            ByteArrayInputStream bais = new ByteArrayInputStream(stringedChunk.getBytes());
            FileOutputStream fos = new FileOutputStream("Worlds/world" + currentActiveWorld + "/" + savingObject.I + ".chunk");
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[4096];
            int len;
            while((len=bais.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            bais.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

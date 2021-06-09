package engine.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import engine.disk.ChunkSavingObject;
import game.chunk.ChunkObject;
import game.player.Player;
import org.joml.Vector3d;

import java.io.*;
import java.util.Objects;

import static game.chunk.Chunk.genBiome;
import static game.player.Player.*;

public class Networking {

    private static final int port = 30_150;


    private static final Server server = new Server(10_000_000,10_000_000);

    public static void initializeNetworking(){



        Kryo kryo = server.getKryo();

        kryo.reset();

        kryo.register(NetworkHandshake.class);
        kryo.register(PlayerPosObject.class);
        kryo.register(ChunkRequest.class);
        kryo.register(ChunkObject.class);
        kryo.register(ChunkSavingObject.class);
        kryo.register(int[].class);
        kryo.register(byte[][].class);
        kryo.register(byte[].class);
        kryo.register(Vector3d.class);

        server.start();

        try {
            server.bind(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("AKA: THERE'S ALREADY A SERVER ON THIS PORT BOI");
        }


        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof NetworkHandshake encodedHandshake) {
                    if (!playerExists(encodedHandshake.name)){
                        connection.sendTCP(encodedHandshake);
                        addPlayer(encodedHandshake.name, connection.getID());
                        System.out.println("Connection ID: " + connection.getID());
                        System.out.println(encodedHandshake.name + "has joined the server");
                    } else {
                        connection.sendTCP(new NetworkHandshake());
                    }
                } else if (object instanceof ChunkRequest chunkRequest){
                    System.out.println(chunkRequest.playerName + " requested chunk: " + chunkRequest.x + " " + chunkRequest.z);
                    Objects.requireNonNull(getPlayerByName(chunkRequest.playerName)).chunkLoadingQueue.put(chunkRequest.x + " " + chunkRequest.z, chunkRequest.x + " " + chunkRequest.z);
                    genBiome(chunkRequest.x, chunkRequest.z);
                } else if (object instanceof PlayerPosObject playerPosObject){
                    Player thisPlayer = Objects.requireNonNull(getPlayerByName(playerPosObject.name));
                    thisPlayer.pos = playerPosObject.pos;
                    thisPlayer.rotation = playerPosObject.rotation;
                }
            }

            @Override
            public void disconnected(Connection connection) {
                Player thisDisconnectingPlayer = getPlayerByID(connection.getID());


                if (Objects.requireNonNull(thisDisconnectingPlayer).name != null) {
                    System.out.println(thisDisconnectingPlayer.name + " has disconnected");
                } else {
                    System.out.println("SOMEONE DISCONNECTED WITH A BROKEN INTERNAL ID!");
                }

                removePlayer(connection.getID());
            }
        });
    }

    public static void sendPlayerChunkData(int ID, ChunkObject thisChunk) {

        ChunkSavingObject savingObject = new ChunkSavingObject();

        savingObject.I = thisChunk.ID;
        savingObject.x = thisChunk.x;
        savingObject.z = thisChunk.z;
        savingObject.b = thisChunk.block;
        savingObject.r = thisChunk.rotation;
        savingObject.l = thisChunk.light;
        savingObject.h = thisChunk.heightMap;
        savingObject.e = thisChunk.lightLevel;

        server.sendToTCP(ID, savingObject);
    }


    public static void sendPlayerPosition(int ID, PlayerPosObject playerPosObject) {
        //System.out.println("sending position object to" + ID);
        server.sendToTCP(ID, playerPosObject);
    }
}

package engine.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import engine.disk.ChunkSavingObject;
import game.chunk.ChunkObject;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.*;
import java.util.Objects;

import static engine.disk.Disk.savePlayerPos;
import static game.chunk.Chunk.*;
import static game.player.Player.*;

public class Networking {

    private static int port = 30_150;

    public static void setPort(int newPort){
        port = newPort;
    }

    public static int getGamePort(){
        return port;
    }

    private static final Server server = new Server(20_000_000,20_000_000);

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
        kryo.register(Vector3f.class);
        kryo.register(BreakBlockClassThing.class);
        kryo.register(Vector3i.class);
        kryo.register(BlockBreakingReceiver.class);
        kryo.register(ItemSendingObject.class);
        kryo.register(ItemPickupNotification.class);
        kryo.register(ItemDeletionSender.class);
        kryo.register(BlockPlacingReceiver.class);
        kryo.register(NetworkMovePositionDemand.class);

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
                        System.out.println(encodedHandshake.name + " has joined the server");
                    } else {
                        connection.sendTCP(new NetworkHandshake());
                    }
                } else if (object instanceof ChunkRequest chunkRequest){
                    //System.out.println(chunkRequest.playerName + " requested chunk: " + chunkRequest.x + " " + chunkRequest.z);
                    Objects.requireNonNull(getPlayerByName(chunkRequest.playerName)).chunkLoadingQueue.put(chunkRequest.x + " " + chunkRequest.z, chunkRequest.x + " " + chunkRequest.z);
                    genBiome(chunkRequest.x, chunkRequest.z);
                } else if (object instanceof PlayerPosObject playerPosObject){
                    Player thisPlayer = Objects.requireNonNull(getPlayerByName(playerPosObject.name));
                    //System.out.println(playerPosObject.pos.x + " " + playerPosObject.pos.y + " " + playerPosObject.pos.z);
                    thisPlayer.pos = playerPosObject.pos;
                    thisPlayer.camRot = playerPosObject.cameraRot;
                } else if (object instanceof BreakBlockClassThing breakBlockClassThing){
                    digBlock(breakBlockClassThing.breakingPos.x, breakBlockClassThing.breakingPos.y, breakBlockClassThing.breakingPos.z);
                } else if (object instanceof BlockPlacingReceiver blockPlacingReceiver){
                    Vector3i c = blockPlacingReceiver.receivedPos;
                    placeBlock(c.x, c.y, c.z, blockPlacingReceiver.ID, blockPlacingReceiver.rotation);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                Player thisDisconnectingPlayer = getPlayerByID(connection.getID());


                if (Objects.requireNonNull(thisDisconnectingPlayer).name != null) {
                    System.out.println(thisDisconnectingPlayer.name + " has disconnected");
                    savePlayerPos(thisDisconnectingPlayer.name, thisDisconnectingPlayer.pos);
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
        savingObject.l = thisChunk.naturalLight;
        savingObject.t = thisChunk.torchLight;
        savingObject.h = thisChunk.heightMap;
        server.sendToTCP(ID, savingObject);
    }

    public static void sendPlayerBrokenBlockData(int ID, BlockBreakingReceiver blockBreakingReceiver){
        server.sendToTCP(ID, blockBreakingReceiver);
    }

    public static void sendPlayerPlacedBlockData(int ID, BlockPlacingReceiver blockPlacingReceiver){
        server.sendToTCP(ID, blockPlacingReceiver);
    }

    public static void sendPlayerItemData(int ID, ItemSendingObject itemSendingObject){
        server.sendToTCP(ID, itemSendingObject);
    }

    public static void sendPlayerPickupNotification(int ID, ItemPickupNotification itemPickupNotification){
        server.sendToTCP(ID, itemPickupNotification);
    }

    public static void sendPlayerItemDeletionSender(int ID, ItemDeletionSender itemDeletionSender){
        server.sendToTCP(ID, itemDeletionSender);
    }

    public static void sendPlayerPosition(int ID, PlayerPosObject playerPosObject) {
        //System.out.println("sending position object to" + ID);
        server.sendToTCP(ID, playerPosObject);
    }

    public static void sendPlayerNetworkMovePositionDemand(int ID, NetworkMovePositionDemand networkMovePositionDemand){
        server.sendToTCP(ID, networkMovePositionDemand);
    }
}

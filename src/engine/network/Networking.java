package engine.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import game.chunk.ChunkObject;
import game.crafting.InventoryObject;
import game.item.Item;
import game.player.Player;
import org.joml.*;

import java.io.IOException;
import java.util.Objects;

import static engine.compression.Compression.convertChunkToCompressedByteArray;
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

    private static final Server server = new Server(500_000,500_000);

    public static void initializeNetworking(){

        Kryo kryo = server.getKryo();


        //register classes to be serialized
        //DO PRIMITIVE CLASS FIRST!
        kryo.register(int[].class);
        kryo.register(byte[][].class);
        kryo.register(byte[].class);
        kryo.register(String[].class);
        kryo.register(String[][].class);
        kryo.register(Vector3d.class);
        kryo.register(Vector3f.class);
        kryo.register(Vector3i.class);
        kryo.register(NetworkHandshake.class);
        kryo.register(PlayerPosObject.class);
        kryo.register(ChunkRequest.class);
        kryo.register(BlockBreakUpdate.class);
        kryo.register(BlockPlaceUpdate.class);
        kryo.register(ItemSendingObject.class);
        kryo.register(ItemPickupNotification.class);
        kryo.register(ItemDeletionSender.class);
        kryo.register(NetworkMovePositionDemand.class);
        kryo.register(NetChunk.class);
        kryo.register(HotBarSlotUpdate.class);
        kryo.register(NetworkInventory.class);

        server.start();

        try {
            server.bind(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("THIS ERROR IS BECAUSE THERE IS ALREADY A SERVER RUNNING ON THIS PORT!");
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
                    Objects.requireNonNull(getPlayerByName(chunkRequest.playerName)).chunkLoadingQueue.put(chunkRequest.x + " " + chunkRequest.z, chunkRequest.x + " " + chunkRequest.z);
                    genBiome(chunkRequest.x, chunkRequest.z);
                } else if (object instanceof PlayerPosObject playerPosObject){
                    Player thisPlayer = Objects.requireNonNull(getPlayerByName(playerPosObject.name));
                    thisPlayer.pos = playerPosObject.pos;
                    thisPlayer.camRot = playerPosObject.cameraRot;
                } else if (object instanceof BlockBreakUpdate blockBreakUpdate){
                    digBlock(blockBreakUpdate.pos.x, blockBreakUpdate.pos.y, blockBreakUpdate.pos.z);
                } else if (object instanceof BlockPlaceUpdate blockPlaceUpdate){
                    Vector3i c = blockPlaceUpdate.pos;
                    placeBlock(c.x, c.y, c.z, blockPlaceUpdate.ID, blockPlaceUpdate.rot);
                } else if (object instanceof HotBarSlotUpdate hotBarSlotUpdate){
                    Player thisPlayer = getPlayerByID(connection.getID());
                    if (thisPlayer != null) {
                        thisPlayer.hotBarSlot = hotBarSlotUpdate.slot;
                    }
                } else if (object instanceof InventoryObject inventoryObject){
                    Player thisPlayer = getPlayerByID(connection.getID());
                    if (thisPlayer != null) {
                        System.out.println(thisPlayer.name + " updated their inventory!");
                        thisPlayer.updatePlayerInventory(inventoryObject);
                    }
                } else if (object instanceof NetworkInventory networkInventory){
                    Player thisPlayer = getPlayerByID(connection.getID());
                    if (thisPlayer != null) {
                        for (int x = 0; x < networkInventory.inventory.length; x++){
                            for (int y = 0; y < networkInventory.inventory[x].length; y++){
                                thisPlayer.mainInventory.set(x,y,new Item(networkInventory.inventory[x][y], 1));
                            }
                        }
                    }
                }
            }

            @Override
            public void disconnected(Connection connection) {
                Player thisDisconnectingPlayer = getPlayerByID(connection.getID());

                if (thisDisconnectingPlayer != null && thisDisconnectingPlayer.name != null) {
                    System.out.println(thisDisconnectingPlayer.name + " has disconnected");
                    savePlayerPos(thisDisconnectingPlayer.name, thisDisconnectingPlayer.pos);
                } else {
                    System.out.println("SOMEONE DISCONNECTED WITH A BROKEN INTERNAL ID!");
                }

                removePlayer(connection.getID());
            }
        });
    }

    public static void sendPlayerChunkData(int ID, ChunkObject thisChunk) throws IOException {
        //this is compressed by this method, then serialized by Kryo, then sent out to client by Kryonet to be reassembled
        server.sendToTCP(ID, new NetChunk(convertChunkToCompressedByteArray(thisChunk)));
    }

    public static void sendPlayerBrokenBlockData(int ID, BlockBreakUpdate blockBreakUpdate){
        server.sendToTCP(ID, blockBreakUpdate);
    }

    public static void sendPlayerPlacedBlockData(int ID, BlockPlaceUpdate blockPlaceUpdate){
        server.sendToTCP(ID, blockPlaceUpdate);
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
        server.sendToTCP(ID, playerPosObject);
    }

    public static void sendPlayerNetworkMovePositionDemand(int ID, NetworkMovePositionDemand networkMovePositionDemand){
        server.sendToTCP(ID, networkMovePositionDemand);
    }
}

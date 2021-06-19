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
import java.util.Arrays;
import java.util.Objects;

import static engine.FancyMath.getCameraRotationVector;
import static engine.compression.Compression.convertChunkToCompressedByteArray;
import static engine.disk.Disk.savePlayerPos;
import static game.chunk.Chunk.*;
import static game.item.ItemEntity.createItem;
import static game.player.Player.*;

public class Networking {

    private static int port = 30_150;

    public static void setPort(int newPort){
        port = newPort;
    }

    public static int getGamePort(){
        return port;
    }

    private static final Server server = new Server(50_000,50_000);

    public static void initializeNetworking(){

        Kryo kryo = server.getKryo();


        //register classes to be serialized
        //DO PRIMITIVE CLASS FIRST!
        kryo.register(int[].class, 90);
        kryo.register(byte[][].class,91);
        kryo.register(byte[].class,92);
        kryo.register(String.class,93);
        kryo.register(String[].class,94);
        kryo.register(String[][].class,95);
        kryo.register(Vector3d.class,96);
        kryo.register(Vector3f.class,97);
        kryo.register(Vector3i.class,98);
        kryo.register(NetworkHandshake.class,99);
        kryo.register(PlayerPosObject.class,100);
        kryo.register(ChunkRequest.class,101);
        kryo.register(BlockBreakUpdate.class,102);
        kryo.register(BlockPlaceUpdate.class,103);
        kryo.register(ItemSendingObject.class,104);
        kryo.register(ItemPickupNotification.class,105);
        kryo.register(ItemDeletionSender.class,106);
        kryo.register(NetworkMovePositionDemand.class,107);
        kryo.register(NetChunk.class,108);
        kryo.register(HotBarSlotUpdate.class,109);
        kryo.register(NetworkInventory.class,110);
        kryo.register(ThrowItemUpdate.class, 111);

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
                } else if (object instanceof NetworkInventory networkInventory){
                    Player thisPlayer = getPlayerByID(connection.getID());
                    if (thisPlayer != null) {
                        if (thisPlayer.mainInventory == null){
                            thisPlayer.mainInventory = new InventoryObject("main", 9,4, true);
                        }
                        System.out.println(thisPlayer.name + " updated their inventory!");
                        InventoryObject thisInv = thisPlayer.mainInventory;

                        for (int x = 0; x < networkInventory.inventory.length; x++){
                            for (int y = 0; y < networkInventory.inventory[x].length; y++){
                                thisInv.set(x,y,new Item(networkInventory.inventory[x][y], 1));
                            }
                        }
                    }
                } else if (object instanceof ThrowItemUpdate){
                    //System.out.println("new item thrown!");
                    Player thisPlayer = getPlayerByID(connection.getID());
                    if (thisPlayer != null) {
                        String itemName = thisPlayer.mainInventory.get(thisPlayer.hotBarSlot, 0);
                        if (itemName == null){
                            System.out.println(thisPlayer.name + " IS THROWING A NULL ITEM!");
                            return;
                        }
                        Vector3d pos = new Vector3d(thisPlayer.pos);
                        pos.y += thisPlayer.eyeHeight;

                        //System.out.println(Arrays.deepToString(thisPlayer.mainInventory.inventory));

                        //System.out.println("player is throwing: " + itemName);
                        createItem(itemName, pos, getCameraRotationVector(new Vector3f(thisPlayer.camRot)).mul(10f), 1);
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

package game.item;

import engine.network.ItemDeletionSender;
import engine.network.ItemPickupNotification;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static engine.FancyMath.getDistance;
import static engine.Time.getDelta;
import static engine.network.Networking.sendPlayerItemDeletionSender;
import static engine.network.Networking.sendPlayerPickupNotification;
import static game.collision.Collision.applyInertia;
import static game.item.Item.getCurrentID;
import static game.player.Player.getAllPlayers;

public class ItemEntity {
    private final static ConcurrentHashMap<Integer, Item> items = new ConcurrentHashMap<>();

    private final static float itemCollisionWidth = 0.2f;

    public static void createItem(String name, Vector3d pos, int stack){
        items.put(getCurrentID(), new Item(name, pos, stack));
    }

    public static void createItem(String name, Vector3d pos, int stack, float life){
        items.put(getCurrentID(), new Item(name, pos, stack, life));
    }

    public static void createItem(String name, Vector3d pos, Vector3f inertia, int stack){
        items.put(getCurrentID(), new Item(name, pos, inertia, stack));
    }

    public static void createItem(String name, Vector3d pos, Vector3f inertia, int stack, float life){
        items.put(getCurrentID(), new Item(name, pos, inertia, stack, life));
    }

    public static Collection<Item> getAllItemEntities(){
        return items.values();
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void itemsOnTick(){

        double delta = getDelta();

        for (Item thisItem : items.values()){

            if (thisItem.collectionTimer > 0f){
                thisItem.collectionTimer -= delta;
                if (thisItem.collectionTimer <= 0){
                    thisItem.deletionOkay = true;
                }
            }

            thisItem.timer += delta;

            //delete items that are too old
            if (thisItem.timer > 50f){
                deletionQueue.add(thisItem.ID);
            }

            //collect items after 3 seconds

            if (thisItem.timer > 3f){
                for (Player player : getAllPlayers()) {

                    Vector3d playerPos = new Vector3d(player.pos);

                    if (getDistance(thisItem.pos, playerPos) < 3f) {
                        if (!thisItem.collecting) {
                            thisItem.collecting = true;
                            thisItem.collectionTimer = 0.1f;

                            sendPlayerPickupNotification(player.ID, new ItemPickupNotification(thisItem.name, thisItem.stack));
                        }
                        //do not do else-if here, can go straight to this logic
                        Vector3d normalizedPos = new Vector3d(playerPos.add(0, player.collectionHeight,0));
                        normalizedPos.sub(thisItem.pos).normalize().mul(15f);

                        Vector3f normalizedDirection = new Vector3f();
                        normalizedDirection.x = (float) normalizedPos.x;
                        normalizedDirection.y = (float) normalizedPos.y;
                        normalizedDirection.z = (float) normalizedPos.z;

                        thisItem.inertia = normalizedDirection;
                    }

                    if (getDistance(thisItem.pos, new Vector3d(playerPos.add(0, player.collectionHeight,0))) < 0.2f || thisItem.deletionOkay) {
                        deletionQueue.add(thisItem.ID);
                    }
                }
            }

            if (thisItem.collecting) {
                applyInertia(thisItem.pos, thisItem.inertia, false, itemCollisionWidth, itemCollisionWidth, false, false, false, false, false);
            } else {
                applyInertia(thisItem.pos, thisItem.inertia, false, itemCollisionWidth, itemCollisionWidth, true, false, true, false, false);
            }


            if (thisItem.pos.y < 0){
                deletionQueue.add(thisItem.ID);
            }
        }

        while (!deletionQueue.isEmpty()){
            int thisItemKey = deletionQueue.pop();
            items.remove(thisItemKey);

            for (Player thisPlayer : getAllPlayers()){
                sendPlayerItemDeletionSender(thisPlayer.ID, new ItemDeletionSender(thisItemKey));
            }
        }
    }
}
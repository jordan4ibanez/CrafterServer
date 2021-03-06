package game.item;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.FancyMath.randomForceValue;
import static game.item.ItemDefinition.getItemDefinition;

public class Item {

    //KEEP THIS IN THIS CLASS
    //IT'S TOO COMPLEX TO REMOVE THIS OUT OF THIS CLASS!
    private static int currentID = 0;

    public String name;
    public int stack;
    public ItemDefinition definition;
    public Vector3d pos;
    public float timer;
    public boolean exists;
    public boolean collecting;
    public float collectionTimer = 0;
    public boolean deletionOkay = false;
    public Vector3f inertia;
    public int ID;

    public Vector3i oldFlooredPos = new Vector3i(0,0,0);


    //yes this is ridiculous, but it is also safe
    //internal integer overflow to 0
    private static void tickUpCurrentID(){
        currentID++;
        if (currentID == 2147483647){
            currentID = 0;
        }
    }

    public static int getCurrentID(){
        return currentID;
    }

    //inventory item
    public Item(String name, int stack){
        this.name = name;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //item being mined
    public Item(String name, Vector3d pos, int stack) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = new Vector3f(randomForceValue(2f), (float) Math.random() * 4f, randomForceValue(2f));
        this.exists = true;
        this.collecting = false;
        this.timer = 0f;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //item being mined with life
    public Item(String name, Vector3d pos, int stack, float life) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = new Vector3f(randomForceValue(2f), (float) Math.random() * 4f, randomForceValue(2f));
        this.exists = true;
        this.collecting = false;
        this.timer = life;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //item with inertia vector when spawned (mined, blown up, etc)
    public Item(String name, Vector3d pos, Vector3f inertia, int stack) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = inertia;
        this.exists = true;
        this.collecting = false;
        this.timer = 0f;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //item with inertia vector when spawned (mined, blown up, etc)
    public Item(String name, Vector3d pos, Vector3f inertia, int stack, float life) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = inertia;
        this.exists = true;
        this.collecting = false;
        this.timer = life;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //clone item
    public Item(Item thisItem) {
        this.name = thisItem.name;
        if (thisItem.pos == null){
            this.pos = new Vector3d();
        } else {
            this.pos = new Vector3d(thisItem.pos);
        }
        this.definition = getItemDefinition(name);
        this.stack = thisItem.stack;
        if (thisItem.inertia == null){
            this.inertia = new Vector3f();
        } else {
            this.inertia = new Vector3f(thisItem.inertia);
        }
        this.exists = true;
        this.collecting = false;
        this.timer = 0f;
        this.ID = currentID;
        tickUpCurrentID();
    }
}

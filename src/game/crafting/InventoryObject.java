package game.crafting;

import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class InventoryObject {
    public String[][] inventory;

    String name;
    Vector2i size;
    boolean mainInventory;

    public InventoryObject(String newName, int sizeX, int sizeY, boolean isMainInventory){
        this.name = newName;
        this.inventory = new String[sizeY][sizeX];
        this.size = new Vector2i(sizeX, sizeY);
        this.mainInventory = isMainInventory;
    }

    public void set(int x, int y, Item newItem){
        inventory[y][x] = null; //send to gc
        inventory[y][x] = newItem.name;
    }
    public String get(int x, int y){
        //leak memory to allow modification
        return inventory[y][x];
    }


    public Vector2i getSize(){
        //don't leak memory
        return new Vector2i(size);
    }

    public boolean isMainInventory(){
        return mainInventory;
    }
}

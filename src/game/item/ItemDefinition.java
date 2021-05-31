package game.item;

import java.util.HashMap;
import java.util.Map;

import static game.blocks.BlockDefinition.getIsOnPlaced;
import static game.blocks.BlockDefinition.getRightClickable;

public class ItemDefinition {
    private final static float itemSize   = 0.4f;
    private final static Map<String, ItemDefinition> definitions = new HashMap<>();

    public final String name;
    public int blockID;

    public final boolean isItem;
    public final ItemModifier itemModifier;
    public boolean isRightClickable;
    public boolean isOnPlaced;

    public float stoneMiningLevel;
    public float dirtMiningLevel;
    public float woodMiningLevel;
    public float leafMiningLevel;

    //block item
    public ItemDefinition(String name, int blockID){
        this.name = name;
        this.blockID = blockID;
        this.isItem = false;
        this.itemModifier = null;
        this.isRightClickable = getRightClickable(blockID);
        this.isOnPlaced = getIsOnPlaced(blockID);
    }

    //craft item
    public ItemDefinition(String name, ItemModifier itemModifier){
        this.name = name;
        this.isItem = true;
        this.itemModifier = itemModifier;
    }

    //tool item
    public ItemDefinition(String name, ItemModifier itemModifier, float stoneMiningLevel, float dirtMiningLevel, float woodMiningLevel, float leafMiningLevel){
        this.name = name;
        this.isItem = true;
        this.itemModifier = itemModifier;
        this.stoneMiningLevel = stoneMiningLevel;
        this.dirtMiningLevel = dirtMiningLevel;
        this.woodMiningLevel = woodMiningLevel;
        this.leafMiningLevel = leafMiningLevel;
    }

    public static ItemModifier getItemModifier(String name){
        return definitions.get(name).itemModifier;
    }

    //block item
    public static void registerItem(String name, int blockID){
        definitions.put(name, new ItemDefinition(name, blockID));
    }

    //craft item
    public static void registerItem(String name, String texturePath, ItemModifier itemModifier){
        definitions.put(name, new ItemDefinition(name, itemModifier));
    }

    //tool
    public static void registerItem(String name,String texturePath, ItemModifier itemModifier, float stoneMiningLevel, float dirtMiningLevel, float woodMiningLevel, float leafMiningLevel){
        definitions.put(name, new ItemDefinition(name, itemModifier, stoneMiningLevel, dirtMiningLevel, woodMiningLevel, leafMiningLevel));
    }

    public static ItemDefinition getItemDefinition(String name){
        return definitions.get(name);
    }

    public static ItemDefinition getRandomItemDefinition(){
        Object[] definitionsArray = definitions.values().toArray();
        int thisItem = (int)Math.floor(Math.random() * definitionsArray.length);
        return (ItemDefinition)definitionsArray[thisItem];
    }

}

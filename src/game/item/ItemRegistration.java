package game.item;

import org.joml.Vector3d;

import static game.blocks.BlockDefinition.isWalkable;
import static game.chunk.Chunk.getBlock;
import static game.chunk.Chunk.setBlock;
import static game.item.ItemDefinition.registerItem;

public class ItemRegistration {

    private final static String[] materials = new String[]{
            "wood",
            "coal",
            "stone",
            "iron",
            "gold",
            "lapis",
            "diamond",
            "emerald",
            "sapphire",
            "ruby",
    };

    public static void registerItems(){

        int toolLevel = 2;
        for (String material : materials) {
            registerItem(material + "pick", "textures/tools/" + material + "pick.png", null, toolLevel,0,0,0);
            registerItem(material + "shovel", "textures/tools/" + material + "shovel.png", null,0,toolLevel,0,0);
            registerItem(material + "axe", "textures/tools/" + material + "axe.png", null,0,0,toolLevel,0);

            if (!material.equals("wood") && !material.equals("stone")){
                registerItem(material, "textures/items/" + material + ".png", null);
            }

            toolLevel++;
        }


        registerItem("door", "textures/door.png", null);

        registerItem("boat", "textures/boatitem.png", null);

        registerItem("stick", "textures/items/stick.png", null);
    }
}

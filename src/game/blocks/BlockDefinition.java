package game.blocks;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static game.chunk.Chunk.*;
import static game.falling.FallingEntity.addFallingEntity;
import static game.item.ItemDefinition.registerItem;
import static game.item.ItemEntity.createItem;
import static game.light.Light.torchFloodFill;
import static game.tnt.TNTEntity.createTNT;

public class BlockDefinition {

    private final static BlockDefinition[] blockIDs = new BlockDefinition[(byte)30];

    //0: normal,
    private final static BlockShape[] blockShapeMap = new BlockShape[(byte)8];


    //actual block object fields
    public byte     ID;
    public String  name;
    public boolean dropsItem;
    public boolean walkable;
    public boolean steppable;
    public boolean isLiquid;
    public int drawType;
    public String placeSound;
    public String digSound;
    public BlockModifier blockModifier;
    public boolean isRightClickable;
    public boolean isOnPlaced;
    public float viscosity;
    public boolean pointable;
    public float stoneHardness;
    public float dirtHardness;
    public float woodHardness;
    public float leafHardness;
    public String droppedItem;

    public BlockDefinition(
            byte ID,
            float stoneHardness,
            float dirtHardness,
            float woodHardness,
            float leafHardness,
            String name,
            boolean dropsItem,
            int drawType,
            boolean walkable,
            boolean steppable,
            boolean isLiquid,
            BlockModifier blockModifier,
            String placeSound,
            String digSound,
            boolean isRightClickable,
            boolean isOnPlaced,
            float viscosity,
            boolean pointable,
            String droppedItem

    ){

        this.ID   = ID;
        this.stoneHardness = stoneHardness;
        this.dirtHardness = dirtHardness;
        this.woodHardness = woodHardness;
        this.leafHardness = leafHardness;
        this.name = name;
        this.dropsItem = dropsItem;
        this.drawType = drawType;
        this.walkable = walkable;
        this.steppable = steppable;
        this.isLiquid = isLiquid;
        this.blockModifier = blockModifier;
        this.placeSound = placeSound;
        this.digSound = digSound;
        this.isRightClickable = isRightClickable;
        this.isOnPlaced = isOnPlaced;
        this.viscosity = viscosity;
        this.pointable = pointable;
        this.droppedItem = droppedItem;
        blockIDs[ID] = this;

        registerItem(name, ID);
    }

    public static void onDigCall(int ID, Vector3d pos) {
        if(blockIDs[ID] != null){
            if(blockIDs[ID].dropsItem){
                //dropped defined item
                if (blockIDs[ID].droppedItem != null){
                    createItem(blockIDs[ID].droppedItem, pos.add(0.5d, 0.5d, 0.5d), 1, 2.5f);
                }
                //drop self
                else {
                    createItem(blockIDs[ID].name, pos.add(0.5d, 0.5d, 0.5d), 1, 2.5f);
                }
            }
            if(blockIDs[ID].blockModifier != null){
                try {
                    blockIDs[ID].blockModifier.onDig(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onPlaceCall(int ID, Vector3d pos) {
        if(blockIDs[ID] != null && blockIDs[ID].blockModifier != null) {
            try {
                blockIDs[ID].blockModifier.onPlace(pos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getBlockName(int ID){
        return blockIDs[ID].name;
    }

    public static boolean getRightClickable(int ID){
        return(blockIDs[ID].isRightClickable);
    }

    public static boolean getIsOnPlaced(int ID){
        return(blockIDs[ID].isOnPlaced);
    }

    public static int getBlockDrawType(int ID){
        if (ID < 0){
            return 0;
        }
        return blockIDs[ID].drawType;
    }

    public static boolean getIfLiquid(int ID){
        return blockIDs[ID].isLiquid;
    }

    public static double[][] getBlockShape(int ID, byte rot){

        double[][] newBoxes = new double[blockShapeMap[blockIDs[ID].drawType].getBoxes().length][6];


        int index = 0;

        //automated as base, since it's the same
        if (rot == 0) {
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                System.arraycopy(thisShape, 0, newBoxes[index], 0, 6);
                index++;
            }
        }

        if (rot == 2){
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                double blockDiffZ =  1d - thisShape[5];
                double widthZ = thisShape[5] - thisShape[2];

                double blockDiffX =  1d - thisShape[3];
                double widthX = thisShape[3] - thisShape[0];

                newBoxes[index][0] = blockDiffX;
                newBoxes[index][1] = thisShape[1];//-y
                newBoxes[index][2] = blockDiffZ; // -z

                newBoxes[index][3] = blockDiffX + widthX;
                newBoxes[index][4] = thisShape[4];//+y
                newBoxes[index][5] = blockDiffZ + widthZ; //+z
                index++;
            }
        }


        if (rot == 1){
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                double blockDiffZ =  1d - thisShape[5];
                double widthZ = thisShape[5] - thisShape[2];

                newBoxes[index][0] = blockDiffZ;
                newBoxes[index][1] = thisShape[1];//-y
                newBoxes[index][2] = thisShape[0]; // -z

                newBoxes[index][3] = blockDiffZ + widthZ;
                newBoxes[index][4] = thisShape[4];//+y
                newBoxes[index][5] = thisShape[3]; //+z
                index++;
            }
        }


        if (rot == 3){
            for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                double blockDiffX =  1d - thisShape[3];
                double widthX = thisShape[3] - thisShape[0];

                newBoxes[index][0] = thisShape[2];
                newBoxes[index][1] = thisShape[1];//-y
                newBoxes[index][2] = blockDiffX; // -z

                newBoxes[index][3] = thisShape[5];
                newBoxes[index][4] = thisShape[4];//+y
                newBoxes[index][5] = blockDiffX + widthX; //+z
                index++;
            }
        }

        return newBoxes/*blockShapeMap.get(blockIDs[ID].drawType).getBoxes()*/;
    }

    public static boolean isWalkable(int ID){
        return blockIDs[ID].walkable;
    }

    public static boolean isSteppable(int ID){
        return blockIDs[ID].steppable;
    }

    public static void initializeBlocks() {

        //air
        blockShapeMap[0] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});


        //normal
        blockShapeMap[1] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});

        //stair
        blockShapeMap[2] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,0.5f,1f},
                                {0f,0f,0f,1f,1f,0.5f}
                        });

        //slab
        blockShapeMap[3] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,0.5f,1f}
                        });

        //allfaces
        blockShapeMap[4] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,1f,1f}
                        });


        new BlockDefinition(
                (byte) 0,
                -1f,
                -1f,
                -1f,
                -1f,
                "air",
                false,
                0,
                false,
                false,
                false,
                null,
                "",
                "",
                false,
                false,
                0,
                false,
                null
        );

        new BlockDefinition(
                (byte) 1,
                0f,
                1f,
                0f,
                0f,
                "dirt",
                true,
                1,
                true,
                false,
                false,
                null,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 2,
                0,
                2f,
                0,
                0,
                "grass",
                true,
                1,
                true,
                false,
                false,
                null,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true,
                "dirt"
        );

        new BlockDefinition(
                (byte) 3,
                1,
                0,
                0,
                0,
                "stone",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "cobblestone"
        );

        new BlockDefinition(
                (byte) 4,
                1.5f,
                0,
                0,
                0,
                "cobblestone",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 5,
                -1,
                -1,
                -1,
                -1,
                "bedrock",
                false,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_1",
                false,
                false,
                0,
                true,
                null
        );


        //tnt explosion
        BlockModifier kaboom = new BlockModifier() {
            @Override
            public void onDig(Vector3d pos) {
                createTNT(pos, 0, true);
            }
        };

        new BlockDefinition(
                (byte) 6,
                0,
                0,
                2,
                0,
                "tnt",
                false,
                1,
                true,
                false,
                false,
                kaboom,
                "dirt_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 7,
                -1,
                -1,
                -1,
                -1,
                "water",
                false,
                1,
                false,
                false,
                true,
                null,
                "",
                "",
                false,
                false,
                40,
                false,
                null
        );

        new BlockDefinition(
                (byte) 8,
                4,
                0,
                0,
                0,
                "coal ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "coal"
        );

        new BlockDefinition(
                (byte)9,
                6,
                0,
                0,
                0,
                "iron ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 10,
                8,
                0,
                0,
                0,
                "gold ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 11,
                10,
                0,
                0,
                0,
                "diamond ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "diamond"
        );

        new BlockDefinition(
                (byte) 12,
                12,
                0,
                0,
                0,
                "emerald ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "emerald"
        );

        new BlockDefinition(
                (byte) 13,
                10,
                0,
                0,
                0,
                "lapis ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "lapis"
        );

        new BlockDefinition(
                (byte) 14,
                14,
                0,
                0,
                0,
                "sapphire ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "sapphire"
        );

        new BlockDefinition(
                (byte) 15,
                16,
                0,
                0,
                0,
                "ruby ore",
                true,
                1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "ruby"
        );

        new BlockDefinition(
                (byte) 16,
                2,
                0,
                0,
                0,
                "cobblestone stair",
                true,
                2,
                true,
                true,
                false,
                null,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );


        new BlockDefinition(
                (byte) 17,
                0,
                0,
                1,
                0,
                "pumpkin",
                true,
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );
        new BlockDefinition(
                (byte) 18,
                0,
                0,
                1,
                0,
                "jack 'o lantern unlit",
                true,
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );
        new BlockDefinition(
                (byte) 19,
                0,
                0,
                1,
                0,
                "jack 'o lantern lit",
                true,
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

        //falling sand
        BlockModifier fallSand = new BlockModifier() {
            @Override
            public void onPlace(Vector3d pos) {
                if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 0) {
                    digBlock((int) pos.x, (int) pos.y, (int) pos.z);
                    addFallingEntity(new Vector3d(pos.x + 0.5d, pos.y, pos.z + 0.5d), new Vector3f(0, 0, 0), (byte) 20);
                }
            }
        };
        new BlockDefinition(
                (byte) 20,
                0,
                1,
                0,
                0,
                "sand",
                true,
                1,
                true,
                false,
                false,
                fallSand,
                "sand_1",
                "sand_2",
                false,
                false,
                0,
                true,
                null
        );

        //door open
        blockShapeMap[5] =
                new BlockShape(
                        new double[][]{
                                {0f,0f,0f,2f/16f,1f,1f}
                        }
                );

        new BlockDefinition(
                (byte)21,
                0,
                0,
                1,
                0,
                "doorOpenTop",
                false,
                5,
                true,
                false,
                false,
                new BlockModifier() {
                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 22) {
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 22) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 23,rot);
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 24,rot);
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 22,
                0,
                0,
                1,
                0,
                "doorOpenBottom",
                false,
                5,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 21) {
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 21) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 23,rot);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 24,rot);
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

        //door closed
        blockShapeMap[6] =
                new BlockShape(
                        new double[][]{
                                {0f,0f,14f/16f,1f,1f,1f}
                        }
                );

        new BlockDefinition(
                (byte) 23,
                0,
                0,
                1,
                0,
                "doorClosedTop",
                false,
                6,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 24) {
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 24) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 21,rot);
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 22,rot);
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 24,
                0,
                0,
                1,
                0,
                "doorClosedBottom",
                false,
                6,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 23) {
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 0, 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 23) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 21,rot);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 22,rot);
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 25,
                0,
                0,
                3,
                0,
                "tree",
                true,
                1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 26,
                0,
                0,
                0,
                1,
                "leaves",
                false,
                4, //allfaces
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

        new BlockDefinition(
                (byte) 27,
                0,
                0,
                2,
                0,
                "wood",
                true,
                1, //regular
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

        BlockModifier workBench = new BlockModifier() {
            @Override
            public void onRightClick(Vector3d pos) {
                //BlockModifier.super.onRightClick(pos);
            }
        };

        new BlockDefinition(
                (byte) 28,
                0,
                0,
                2,
                0,
                "workbench",
                true,
                1, //regular
                true,
                false,
                false,
                workBench,
                "wood_1",
                "wood_2",
                true,
                false,
                0,
                true,
                null
        );

        BlockModifier torchPlace = new BlockModifier() {
            @Override
            public void onPlace(Vector3d pos) {
                torchFloodFill((int)pos.x, (int)pos.y, (int)pos.z);
            }
        };

        new BlockDefinition(
                (byte) 29,
                0,
                0,
                0,
                1,
                "torch",
                true,
                4, //allfaces
                false,
                false,
                false,
                torchPlace,
                "wood_1",
                "wood_2",
                false,
                true,
                0,
                true,
                null
        );
    }

    public static BlockDefinition getBlockDefinition(byte ID){
        return blockIDs[ID];
    }

    public static BlockDefinition getBlockDefinition(String name){
        for(BlockDefinition thisBlockDefinition : blockIDs){
            if (thisBlockDefinition.name.equals(name)){
                return thisBlockDefinition;
            }
        }
        return null;
    }

    public static boolean blockHasOnRightClickCall(int ID){
        return(blockIDs[ID].isRightClickable && blockIDs[ID].blockModifier != null);
    }

    public static boolean isBlockLiquid(int ID){
        return blockIDs[ID].isLiquid;
    }

    public static float getBlockViscosity(int ID){
        return blockIDs[ID].viscosity;
    }

    public static boolean isBlockPointable(int ID){
        return blockIDs[ID].pointable;
    }
}

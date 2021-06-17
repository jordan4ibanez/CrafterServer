package game.chunk;

public class ChunkObject {

    private final static int arraySize = 128 * 16 * 16;

    public String ID;

    public int x;
    public int z;

    public byte lightLevel = 15;

    public int [] block = new int[arraySize];
    public byte[] rotation = new byte[arraySize];
    public byte[] naturalLight = new byte[arraySize];
    public byte[] torchLight = new byte[arraySize];
    public byte[][] heightMap  = new byte[16][16];

    public boolean modified = false;

    public ChunkObject(){

    }

    public ChunkObject(int x, int z){
        this.ID = x + " " + z;

        this.x = x;
        this.z = z;
    }
}

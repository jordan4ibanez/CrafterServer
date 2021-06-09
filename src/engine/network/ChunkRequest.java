package engine.network;
public class ChunkRequest {
    public String playerName;
    public int x;
    public int z;

    //null creation
    public ChunkRequest(){
    }

    //data creation
    public ChunkRequest(int x, int z, String playerName){
        this.x = x;
        this.z = z;
        this.playerName = playerName;
    }
}

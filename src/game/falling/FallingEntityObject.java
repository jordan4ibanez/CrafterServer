package game.falling;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class FallingEntityObject {
    public Vector3d pos;
    public Vector3f inertia;
    public int key;
    public int ID;

    public FallingEntityObject(Vector3d pos, Vector3f inertia, int key, int ID){
        this.pos = pos;
        this.inertia = inertia;
        this.key = key;
        this.ID = ID;
    }
}

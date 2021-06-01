package engine;

public class Vector3fn {
    public float x;
    public float y;
    public float z;
    public String name;

    public Vector3fn(){
        x = 0;
        y = 0;
        z = 0;
        name = "";
    }

    public Vector3fn(String name, float x,float y,float z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }
}

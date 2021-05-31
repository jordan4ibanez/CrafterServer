package game.mob;

import org.joml.Vector3f;

public class MobDefinition {
    public MobInterface mobInterface;
    public String mobDefinitionKey;
    public float height;
    public float width;
    public String hurtSound;
    public int baseHealth;

    public MobDefinition(String name, int baseHealth, float height, float width, MobInterface mobInterface){
        this.mobInterface = mobInterface;
        this.mobDefinitionKey = name;
        this.height = height;
        this.width = width;
        this.hurtSound = hurtSound;
        this.baseHealth = baseHealth;
    }
}

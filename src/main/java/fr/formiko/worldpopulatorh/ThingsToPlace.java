package fr.formiko.worldpopulatorh;

import org.bukkit.Chunk;

public class ThingsToPlace {
    private String type;
    private int x;
    private int y;
    private int z;
    private boolean feature;
    private Chunk chunk;

    public ThingsToPlace(String type, int x, int y, int z, boolean feature, Chunk chunk) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.feature = feature;
        this.chunk = chunk;
    }

    public Chunk getChunk() { return chunk; }

    @Override
    public String toString() {
        return "ThingsToPlace{" + "type='" + type + '\'' + ", x=" + x + ", y=" + y + ", z=" + z + ", feature=" + feature + ", chunk="
                + chunk + '}';
    }

    public void place() {
        WorldPopulatorHPlugin
                .runCommand("place " + (feature ? "feature" : "structure") + " minecraft:" + type + " " + x + " " + y + " " + z);
    }
}

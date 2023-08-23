package fr.formiko.worldpopulatorh;

import fr.formiko.worldpopulatorh.commands.PopulateCommand;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Chunk.LoadLevel;

public class ThingsToPlace {
    private String type;
    private final int x;
    private final int y;
    private final int z;
    private boolean feature;
    private Chunk chunk;
    private boolean huge;
    private List<Chunk> chunksToLoad;

    public ThingsToPlace(String type, int x, int y, int z, boolean feature, Chunk chunk, boolean huge) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.feature = feature;
        this.chunk = chunk;
        this.huge = huge;
        PopulateCommand.addChunkToLoad(chunk, this);
        if (huge) {
            int radius = 5;
            // TODO try bigger radius to see if it fix "[21:22:20 WARN]: Trying to mark a block for PostProcessing @
            // MutableBlockPosition{x=3757, y=9, z=-14405}, but this operation is not supported."
            // It seems to be the same with 5 or 10.
            int chunkX = x / 16;
            int chunkZ = z / 16;
            chunksToLoad = WorldPopulatorHPlugin.getAllChunksBetween(chunkX - radius, chunkZ - radius, chunkX + radius, chunkZ + radius,
                    chunk.getWorld());
            for (Chunk c : chunksToLoad) {
                PopulateCommand.addChunkToLoad(c, this);
            }
        }
    }

    public Chunk getChunk() { return chunk; }
    public List<Chunk> getChunksToLoad() { return chunksToLoad; }
    public boolean isHuge() { return huge; }
    public String getLocationAsString() { return x + " " + y + " " + z; }

    public boolean isChunkLoaded() {
        if (!huge) {
            return getChunk().getLoadLevel() == LoadLevel.ENTITY_TICKING;
        } else {
            boolean allLoaded = getChunk().getLoadLevel() == LoadLevel.ENTITY_TICKING;
            for (Chunk c : chunksToLoad) {
                allLoaded = allLoaded && c.getLoadLevel() == LoadLevel.ENTITY_TICKING;
            }
            // if (allLoaded) {
            // Bukkit.getConsoleSender().sendMessage("All " + chunksToLoad.size() + " chunks loaded.");
            // }
            return allLoaded;
        }
    }

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

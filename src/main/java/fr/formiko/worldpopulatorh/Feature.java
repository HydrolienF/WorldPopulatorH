package fr.formiko.worldpopulatorh;

import java.util.List;
import java.util.Random;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

public class Feature {
    private static final Random random = new Random();
    private String name;
    private int minY;
    private int maxY;
    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;
    private double chanceToPlacePerColumn;
    private List<Biome> biomesWhereToPlace;
    private boolean feature;
    private boolean inAir;
    private boolean huge;

    public Feature(String name, int minY, int maxY, double chanceToPlacePerColumn, List<Biome> biomesWhereToPlace, boolean feature) {
        this.name = name;
        this.minY = minY;
        this.maxY = maxY;
        this.chanceToPlacePerColumn = chanceToPlacePerColumn;
        this.biomesWhereToPlace = biomesWhereToPlace;
        this.feature = feature;
        this.minX = Integer.MIN_VALUE;
        this.maxX = Integer.MAX_VALUE;
        this.minZ = Integer.MIN_VALUE;
        this.maxZ = Integer.MAX_VALUE;
    }

    public String getName() { return name; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }
    public double getChanceToPlacePerColumn() { return chanceToPlacePerColumn; }
    public List<Biome> getBiomesWhereToPlace() { return biomesWhereToPlace; }
    public boolean isFeature() { return feature; }
    public Feature setInAir(boolean inAir) {
        this.inAir = inAir;
        return this;
    }
    public Feature setHuge(boolean huge) {
        this.huge = huge;
        return this;
    }
    public Feature setMaxX(int maxX) {
        this.maxX = maxX;
        return this;
    }
    public Feature setMinX(int minX) {
        this.minX = minX;
        return this;
    }
    public Feature setMaxZ(int maxZ) {
        this.maxZ = maxZ;
        return this;
    }
    public Feature setMinZ(int minZ) {
        this.minZ = minZ;
        return this;
    }
    public boolean isInAir() { return inAir; }
    public boolean isHuge() { return huge; }

    public boolean canPlace(Biome biome, double rd) { return biomesWhereToPlace.contains(biome) && rd < chanceToPlacePerColumn; }
    public ThingsToPlace getThingsToPlace(Block block) {
        int y = getRandomY(block);
        if (y == Integer.MIN_VALUE)
            return null;
        return new ThingsToPlace(name, block.getX(), y, block.getZ(), feature, block.getChunk(), huge);
    }
    public boolean isCompatibleBiome(Biome biome) { return biomesWhereToPlace.contains(biome); }
    public boolean isCompatibleXZ(int x, int z) { return x >= minX && x <= maxX && z >= minZ && z <= maxZ; }

    /**
     * Return a random y or the lowest empty y if inAir is true.
     */
    public int getRandomY(Block block) {
        if (inAir) {
            for (int i = minY; i < maxY; i++) {
                if (block.getWorld().getBlockAt(block.getX(), i, block.getZ()).isEmpty()) {
                    return i;
                }
            }
            return Integer.MIN_VALUE;
        } else {
            return random.nextInt(maxY - minY) + minY;
        }

    }

    @Override
    public String toString() {
        return "Feature{" + "name='" + name + '\'' + ", minY=" + minY + ", maxY=" + maxY + ", chanceToPlacePerColumn="
                + chanceToPlacePerColumn + ", biomesWhereToPlace=" + biomesWhereToPlace + ", feature=" + feature + ", inAir=" + inAir
                + ", huge=" + huge + '}';
    }
}

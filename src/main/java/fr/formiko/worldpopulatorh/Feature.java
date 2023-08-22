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
    private double chanceToPlacePerColumn;
    private List<Biome> biomesWhereToPlace;
    private boolean feature;
    private boolean inAir;

    public Feature(String name, int minY, int maxY, double chanceToPlacePerColumn, List<Biome> biomesWhereToPlace, boolean feature) {
        this.name = name;
        this.minY = minY;
        this.maxY = maxY;
        this.chanceToPlacePerColumn = chanceToPlacePerColumn;
        this.biomesWhereToPlace = biomesWhereToPlace;
        this.feature = feature;
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
    public boolean isInAir() { return inAir; }

    public boolean canPlace(Biome biome, double rd) { return biomesWhereToPlace.contains(biome) && rd < chanceToPlacePerColumn; }
    public ThingsToPlace getThingsToPlace(Block block) {
        int y = getRandomY(block);
        if (y == Integer.MIN_VALUE)
            return null;
        return new ThingsToPlace(name, block.getX(), y, block.getZ(), feature, block.getChunk());
    }
    public boolean isCompatibleBiome(Biome biome) { return biomesWhereToPlace.contains(biome); }

    /**
     * Return a random y or the lowest empty y if inAir is true.
     */
    public int getRandomY(Block block) {
        if (inAir) {
            for (int i = minY; i < maxY; i++) {
                if (block.getWorld().getBlockAt(block.getX(), i, block.getY()).isEmpty()) {
                    return i;
                }
            }
            return Integer.MIN_VALUE;
        } else {
            return random.nextInt(maxY - minY) + minY;
        }

    }
}

package fr.formiko.worldpopulatorh.commands;

import fr.formiko.worldpopulatorh.Feature;
import fr.formiko.worldpopulatorh.ThingsToPlace;
import fr.formiko.worldpopulatorh.WorldPopulatorHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Chunk;
import org.bukkit.Chunk.LoadLevel;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class PopulateCommand implements CommandExecutor {
    private static final Random random = new Random();
    private static final List<Biome> oceanBiomes = List.of(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN,
            Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN, Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.WARM_OCEAN);
    private static final List<Biome> deepOceanBiomes = List.of(Biome.DEEP_OCEAN, Biome.DEEP_FROZEN_OCEAN, Biome.DEEP_LUKEWARM_OCEAN,
            Biome.DEEP_COLD_OCEAN);
    private static final List<Biome> frozenOceanBiomes = List.of(Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN);
    private static final List<Biome> allBiomes = List.of(Biome.values());
    private static final List<Biome> landBiomes = new ArrayList<>(allBiomes).stream().filter(b -> !oceanBiomes.contains(b)).toList();
    private static final List<Biome> aridBiomes = List.of(Biome.DESERT, Biome.SAVANNA, Biome.WOODED_BADLANDS, Biome.BADLANDS,
            Biome.ERODED_BADLANDS);
    private static final List<Biome> nonAridLandBiomes = new ArrayList<>(landBiomes).stream().filter(b -> !aridBiomes.contains(b)).toList();
    private static final List<Biome> coldLandBiomes = List.of(Biome.TAIGA, Biome.SNOWY_BEACH, Biome.SNOWY_PLAINS, Biome.SNOWY_SLOPES,
            Biome.SNOWY_TAIGA, Biome.WINDSWEPT_HILLS);
    private static final List<Biome> nonColdLandBiomes = new ArrayList<>(landBiomes).stream().filter(b -> !coldLandBiomes.contains(b))
            .toList();
    public static List<ThingsToPlace> thingsToPlace = new LinkedList<>();

    //@formatter:off
    private static final List<Feature> features = List.of(
            new Feature("amethyst_geode", -50, 30, 0.0001, landBiomes, true),
            new Feature("shipwreck", 60, 100, 0.0000001, deepOceanBiomes, false),
            new Feature("shipwreck_beached", 60, 100, 0.0000005, List.of(Biome.BEACH), false),
            // new Feature("mineshaft", -60, 45, 0.0000005, landBiomes, false), // Always fail "That position is not loaded", probably because the structure is too big.
            new Feature("iceberg_packed", 60, 100, 0.00000002, frozenOceanBiomes, true),
            new Feature("iceberg_blue", 60, 100, 0.00000002, frozenOceanBiomes, true),
            new Feature("moss_patch", 0, 50, 0.0008, nonAridLandBiomes, true).setInAir(true),
            new Feature("moss_patch_ceiling", 0, 50, 0.001, nonAridLandBiomes, true).setInAir(true),
            new Feature("moss_patch_ceiling", 0, 50, 0.004, List.of(Biome.RIVER), true).setInAir(true),
            new Feature("dripstone_cluster", -64, 50, 0.0007, landBiomes, true).setInAir(true),
            new Feature("dripstone_cluster", -64, 50, 0.002, aridBiomes, true).setInAir(true),
            new Feature("clay_pool_with_dripleaves", -64, 20, 0.0002, nonColdLandBiomes, true).setInAir(true),
            new Feature("clay_with_dripleaves", -64, 20, 0.0001, nonColdLandBiomes, true).setInAir(true),
            new Feature("lush_caves_clay", -64, 40, 0.00005, landBiomes, true).setInAir(true)
    );
    //@formatter:on

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        generateStructuresAndFeatures(sender);
        return true;
    }

    private static void generateStructuresAndFeatures(CommandSender sender) {
        sender.sendMessage("Populating...");
        thingsToPlace = new LinkedList<>();

        new BukkitRunnable() {
            private long printTime, cpt, cptTotal, startTime = System.currentTimeMillis();
            private Map<String, Integer> cptMap = features.stream().map(Feature::getName).collect(java.util.stream.Collectors.toSet())
                    .stream().collect(java.util.stream.Collectors.toMap(s -> s, s -> 0));

            @Override
            public void run() {
                long execTime = System.currentTimeMillis();

                // Place structres and features from last tick.
                List<ThingsToPlace> placed = new LinkedList<>();
                for (ThingsToPlace thing : thingsToPlace) {
                    if (thing.getChunk().getLoadLevel() == LoadLevel.ENTITY_TICKING) {
                        thing.place();
                        placed.add(thing);
                        thing.getChunk().setForceLoaded(false);
                        thing.getChunk().unload();
                    }
                }
                thingsToPlace.removeAll(placed);

                // Calculate the number of structures and features to place and there coordinates.
                while (execTime + 50 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
                    Block column = WorldSelectorHPlugin.getSelector().nextColumn();
                    Biome biome = column.getBiome();
                    Chunk chunk = column.getChunk();
                    double r = random.nextDouble();
                    for (Feature feature : features) {
                        if (feature.isCompatibleBiome(biome)) {
                            r = r - feature.getChanceToPlacePerColumn();
                            if (r < 0) {
                                ThingsToPlace ttp = feature.getThingsToPlace(column);
                                if (ttp == null) {
                                    // Bukkit.getConsoleSender().sendMessage("Can't place " + feature.getName() + " in " + chunk + " because
                                    // of no air found.");
                                    break;
                                }
                                chunk.setForceLoaded(true);
                                chunk.load();
                                // Bukkit.getConsoleSender().sendMessage("Want to place " + feature.getName() + " in " + chunk);
                                thingsToPlace.add(ttp);
                                cptMap.put(feature.getName(), cptMap.get(feature.getName()) + 1);
                                cpt++;
                                break;
                            }
                        }
                    }
                    cptTotal++;
                }

                if (printTime + 1000 < System.currentTimeMillis()) {
                    printTime = System.currentTimeMillis();
                    printProgress(sender, cpt, startTime);
                }
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0 && thingsToPlace.isEmpty()) {
                    printProgress(sender, cpt, startTime);
                    sender.sendMessage("Place " + cpt + " structures|features in " + cptTotal + " columns in "
                            + (System.currentTimeMillis() - startTime) + "ms.");
                    sender.sendMessage("Place " + cptMap);
                    cancel();
                }
            }
        }.runTaskTimer(WorldPopulatorHPlugin.plugin, 0, 2); // 0, 2 because 1 tick is not enoth to load the chunk.
    }

    private static void printProgress(CommandSender sender, long cpt, long startTime) {
        double progress = WorldSelectorHPlugin.getSelector().progress();
        sender.sendMessage("Progress: " + cpt + "   " + progress * 100 + "% ETA: "
                + ((long) ((System.currentTimeMillis() - startTime) * (1 - progress))) + "ms");
    }
}

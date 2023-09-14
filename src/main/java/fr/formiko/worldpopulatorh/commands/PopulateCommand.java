package fr.formiko.worldpopulatorh.commands;

import fr.formiko.worldpopulatorh.Feature;
import fr.formiko.worldpopulatorh.ThingsToPlace;
import fr.formiko.worldpopulatorh.WorldPopulatorHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
    private static final List<Biome> frozenOceanBiomes = List.of(Biome.DEEP_FROZEN_OCEAN, Biome.DEEP_COLD_OCEAN);
    private static final List<Biome> allBiomes = List.of(Biome.OCEAN, Biome.PLAINS, Biome.DESERT, Biome.WINDSWEPT_HILLS, Biome.FOREST,
            Biome.TAIGA, Biome.SWAMP, Biome.MANGROVE_SWAMP, Biome.RIVER, Biome.FROZEN_OCEAN, Biome.FROZEN_RIVER, Biome.SNOWY_PLAINS,
            Biome.MUSHROOM_FIELDS, Biome.BEACH, Biome.JUNGLE, Biome.SPARSE_JUNGLE, Biome.DEEP_OCEAN, Biome.STONY_SHORE, Biome.SNOWY_BEACH,
            Biome.BIRCH_FOREST, Biome.DARK_FOREST, Biome.SNOWY_TAIGA, Biome.OLD_GROWTH_PINE_TAIGA, Biome.WINDSWEPT_FOREST, Biome.SAVANNA,
            Biome.SAVANNA_PLATEAU, Biome.BADLANDS, Biome.WOODED_BADLANDS, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.COLD_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN, Biome.SUNFLOWER_PLAINS,
            Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.FLOWER_FOREST, Biome.ICE_SPIKES, Biome.OLD_GROWTH_BIRCH_FOREST,
            Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.WINDSWEPT_SAVANNA, Biome.ERODED_BADLANDS, Biome.BAMBOO_JUNGLE, Biome.BASALT_DELTAS,
            Biome.DRIPSTONE_CAVES, Biome.LUSH_CAVES, Biome.MEADOW, Biome.GROVE, Biome.SNOWY_SLOPES, Biome.FROZEN_PEAKS, Biome.JAGGED_PEAKS,
            Biome.STONY_PEAKS, Biome.CHERRY_GROVE, Biome.CUSTOM);
    private static final List<Biome> landBiomes = new ArrayList<>(allBiomes).stream().filter(b -> !oceanBiomes.contains(b)).toList();
    private static final List<Biome> aridBiomes = List.of(Biome.DESERT, Biome.SAVANNA, Biome.WOODED_BADLANDS, Biome.BADLANDS,
            Biome.ERODED_BADLANDS);
    private static final List<Biome> nonAridLandBiomes = new ArrayList<>(landBiomes).stream().filter(b -> !aridBiomes.contains(b)).toList();
    private static final List<Biome> coldLandBiomes = List.of(Biome.TAIGA, Biome.SNOWY_BEACH, Biome.SNOWY_PLAINS, Biome.SNOWY_SLOPES,
            Biome.SNOWY_TAIGA, Biome.WINDSWEPT_HILLS);
    private static final List<Biome> nonColdLandBiomes = new ArrayList<>(landBiomes).stream().filter(b -> !coldLandBiomes.contains(b))
            .toList();

    public static List<ThingsToPlace> thingsToPlace = new LinkedList<>();
    public static Map<Chunk, List<ThingsToPlace>> thingsToPlaceByChunk = new java.util.HashMap<>();
    private static Map<String, List<String>> thingsLocations;
    private static long printTime, cpt, cptTotal, startTime = -1;
    public static boolean stop = false;

    //@formatter:off
    private static final List<Feature> features = List.of(
            // new Feature("amethyst_geode", -50, 30, 0.0001, landBiomes, true),
            new Feature("shipwreck", 60, 100, 0.0000002, deepOceanBiomes, false),
            new Feature("shipwreck_beached", 60, 100, 0.000005, List.of(Biome.BEACH), false),
            // new Feature("mineshaft", -60, 45, 0.0000015, landBiomes, false).setHuge(true),
            new Feature("iceberg_packed", 60, 100, 0.0000001, frozenOceanBiomes, true),
            new Feature("iceberg_blue", 60, 100, 0.0000001, frozenOceanBiomes, true),
            new Feature("moss_patch", 0, 50, 0.0008, nonAridLandBiomes, true).setInAir(true),
            new Feature("moss_patch_ceiling", 0, 50, 0.001, nonAridLandBiomes, true).setInAir(true),
            new Feature("moss_patch_ceiling", 0, 50, 0.004, List.of(Biome.RIVER), true).setInAir(true),
            new Feature("dripstone_cluster", -64, 50, 0.0007, landBiomes, true).setInAir(true),
            new Feature("dripstone_cluster", -64, 50, 0.002, aridBiomes, true).setInAir(true),
            new Feature("clay_pool_with_dripleaves", -64, 20, 0.00015, nonColdLandBiomes, true).setInAir(true),
            new Feature("clay_with_dripleaves", -64, 20, 0.00008, nonColdLandBiomes, true).setInAir(true),
            new Feature("lush_caves_clay", -64, 40, 0.00002, landBiomes, true).setInAir(true)
    );
    //@formatter:on

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equals("stop")) {
            stop = true;
        } else {
            generateStructuresAndFeatures(sender, false);
        }
        return true;
    }

    public static void generateStructuresAndFeatures(CommandSender sender, boolean thenClean) {
        thingsToPlace = new LinkedList<>();
        thingsToPlaceByChunk = new java.util.HashMap<>();
        if (startTime == -1) { // need to init for the first time.
            sender.sendMessage("Populating...");
            thingsLocations = features.stream().map(Feature::getName).collect(java.util.stream.Collectors.toSet()).stream()
                    .collect(java.util.stream.Collectors.toMap(s -> s, s -> new LinkedList<>()));
            startTime = System.currentTimeMillis();
        } else {
            sender.sendMessage("Resuming populating...");
            printFullProgress(sender);
        }
        stop = false;

        new BukkitRunnable() {

            @Override
            public void run() {
                long execTime = System.currentTimeMillis();

                // Place structres and features from last tick.
                List<ThingsToPlace> placed = new LinkedList<>();
                for (ThingsToPlace thing : thingsToPlace) {
                    if (canBePlace(thing)) {
                        placeThingsToPlace(thing);
                        placed.add(thing);
                    }
                }
                thingsToPlace.removeAll(placed);

                // free chunck that aren't used anymore.
                freeUnusedChunks();

                // Calculate the number of structures and features to place and there coordinates.
                while (execTime + 50 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock() && !stop) {
                    Chunk chunk = WorldSelectorHPlugin.getSelector().nextChunk();
                    Biome biome = chunk.getBlock(0, 0, 0).getBiome();

                    for (int i = 0; i < 16; i++) {
                        for (int j = 0; j < 16; j++) {
                            Block column = chunk.getBlock(i, 0, j);
                            double r = random.nextDouble();
                            for (Feature feature : features) {
                                if (feature.isCompatibleBiome(biome)) {
                                    r = r - feature.getChanceToPlacePerColumn();
                                    if (r < 0) {
                                        ThingsToPlace ttp = feature.getThingsToPlace(column);
                                        if (ttp == null) {
                                            // Bukkit.getConsoleSender().sendMessage(
                                            // "Can't place " + feature.getName() + " in " + column + " because of no air found.");
                                            break;
                                        }
                                        Bukkit.getConsoleSender().sendMessage(
                                                "Want to place " + feature.getName() + " in " + "(" + biome + ")" + " " + column);
                                        thingsToPlace.add(ttp);
                                        thingsLocations.get(feature.getName()).add(ttp.getLocationAsString());
                                        cpt++;
                                        break;
                                    }
                                }
                            }
                            cptTotal++;
                        }
                    }

                }

                if (printTime + 1000 < System.currentTimeMillis()) {
                    printTime = System.currentTimeMillis();
                    printProgress(sender);
                }
                if (stop) {
                    sender.sendMessage("stopping. Wating to all things to be placed & all chunks to be unloaded. "
                            + thingsToPlaceByChunk.size() + " chunks to unload.");
                }
                if ((WorldSelectorHPlugin.getSelector().progress() >= 1.0 && thingsToPlace.isEmpty())
                        || (stop && thingsToPlaceByChunk.isEmpty())) {
                    printFullProgress(sender);
                    WorldPopulatorHPlugin.plugin.saveData(thingsLocations, cpt, cptTotal, startTime);
                    cancel();
                    if (thenClean && !stop) {
                        WorldSelectorHPlugin.resetSelector();
                        WorldPopulatorHPlugin.runCommand("clean");
                    }
                }
            }
        }.runTaskTimer(WorldPopulatorHPlugin.plugin, 0, 2); // 0, 2 because 1 tick is not enoth to load the chunk.
    }

    private static void printProgress(CommandSender sender) {
        double progress = WorldSelectorHPlugin.getSelector().progress();
        long timeForFullProgress = (long) ((System.currentTimeMillis() - startTime) / progress);
        long timeForFullProgressLeft = timeForFullProgress - (long) (System.currentTimeMillis() - startTime);
        sender.sendMessage("Progress: " + cpt + "   " + progress * 100 + "% ETA: " + Duration.ofMillis(timeForFullProgressLeft));
    }

    private static void printFullProgress(CommandSender sender) {
        printProgress(sender);
        sender.sendMessage("Place " + cpt + " structures|features in " + cptTotal + " columns in "
                + Duration.ofMillis(System.currentTimeMillis() - startTime) + ".");
        sender.sendMessage("Place " + thingsLocations.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue().size())
                .collect(java.util.stream.Collectors.joining(", ")));
    }

    public static void addChunkToLoad(Chunk chunk, ThingsToPlace thing) {
        if (!thingsToPlaceByChunk.containsKey(chunk)) {
            thingsToPlaceByChunk.put(chunk, new LinkedList<>());
        }
        thingsToPlaceByChunk.get(chunk).add(thing);
        chunk.setForceLoaded(true);
        chunk.load();
        // Bukkit.getConsoleSender().sendMessage("Add chunk to load " + chunk);
    }

    public static void placeThingsToPlace(ThingsToPlace thing) {
        // place it
        thing.place();
        // remove the thing from all the chunks waiting list so that chunk without any waiting things can be unloaded.
        releaseChunksLoaded(thing.getChunk(), thing);
        if (thing.isHuge()) {
            for (Chunk chunk : thing.getChunksToLoad()) {
                releaseChunksLoaded(chunk, thing);
            }
        }
    }

    private static void releaseChunksLoaded(Chunk chunk, ThingsToPlace thing) { thingsToPlaceByChunk.get(chunk).remove(thing); }

    private static void freeUnusedChunks() {
        List<Chunk> chunksToRemoveFromList = new LinkedList<>();
        for (Chunk chunk : thingsToPlaceByChunk.keySet()) {
            if (thingsToPlaceByChunk.get(chunk).isEmpty()) {
                chunk.setForceLoaded(false);
                // chunk.unload(); // Disable because it may cause issues with mineshaft generation.
                chunksToRemoveFromList.add(chunk);
            }
        }
        for (Chunk chunk : chunksToRemoveFromList) {
            thingsToPlaceByChunk.remove(chunk);
        }
    }

    public static boolean canBePlace(ThingsToPlace thingsToPlace) { return thingsToPlace.isChunkLoaded(); }

    @SuppressWarnings("unchecked")
    public static void loadData() {
        Map<String, Object> data = WorldPopulatorHPlugin.plugin.loadData();
        Bukkit.getConsoleSender().sendMessage("data: " + data);
        Bukkit.getConsoleSender().sendMessage("locations: " + data.get("locations"));
        Bukkit.getConsoleSender().sendMessage("locations.getClass(): " + data.get("locations").getClass());
        // TODO be able to load thingsLocations here as a Map<String, List<String>>.
        thingsLocations = (Map<String, List<String>>) data.getOrDefault("locations",
                features.stream().map(Feature::getName).collect(java.util.stream.Collectors.toSet()).stream()
                        .collect(java.util.stream.Collectors.toMap(s -> s, s -> new LinkedList<>())));
        cpt = (long) data.get("cpt");
        cptTotal = (long) data.get("cptTotal");
        startTime = (long) data.get("startTime");
        Bukkit.getConsoleSender().sendMessage("cpt: " + cpt);
        Bukkit.getConsoleSender().sendMessage("cptTotal: " + cptTotal);
        Bukkit.getConsoleSender().sendMessage("startTime: " + startTime);
    }
}

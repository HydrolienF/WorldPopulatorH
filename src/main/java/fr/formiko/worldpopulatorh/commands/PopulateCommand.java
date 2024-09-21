package fr.formiko.worldpopulatorh.commands;

import fr.formiko.worldpopulatorh.Feature;
import fr.formiko.worldpopulatorh.ThingsToPlace;
import fr.formiko.worldpopulatorh.WorldPopulatorHPlugin;
import fr.formiko.worldpopulatorh.biomes.NMSBiomeUtils;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import net.minecraft.world.level.biome.Biome;

// @CommandAlias("populate")
public class PopulateCommand {
    private static final Random random = new Random();
    private static final List<String> nonDeepOceanBiomes = List.of("mvndi:frozen_ocean", "mvndi:ocean", "mvndi:cold_ocean",
            "mvndi:lukewarm_ocean", "mvndi:warm_ocean", "mvndi:coral_reef_ocean");
    private static final List<String> frozenOceanBiomes = List.of("mvndi:cold_ocean", "mvndi:frozen_ocean");
    private static final List<String> beachBiomes = List.of("mvndi:beach", "mvndi:cold_beach", "mvndi:mediteranean_beach",
            "mvndi:warm_beach");
    private static final List<String> minesBiomes = List.of("mvndi:baltic_coast_copenhague", "mvndi:central_europe_prague",
            "mvndi:northen_forest_falun");

    public static List<ThingsToPlace> thingsToPlace = new LinkedList<>();
    public static Map<Chunk, List<ThingsToPlace>> thingsToPlaceByChunk = new HashMap<>();
    private static Map<String, List<String>> thingsLocations;
    private static long printTime, cpt, cptTotal, startTime = -1;
    public static boolean stop = false;

    //@formatter:off
    private static final List<Feature> features = List.of(
            new Feature("shipwreck", 60, 100, 0.0000005, nonDeepOceanBiomes, false),
            new Feature("shipwreck_beached", 60, 100, 0.000005, beachBiomes, false),
            new Feature("mineshaft", -60, 45, 0.00001, minesBiomes, false).setHuge(true),
            new Feature("iceberg_packed", 60, 100, 0.0000002, frozenOceanBiomes, true).setMaxZ(7000),
            new Feature("iceberg_blue", 60, 100, 0.0000002, frozenOceanBiomes, true).setMaxZ(7000)
    );
    //@formatter:on

    // @Subcommand("stop")
    // @Description("Stop the current population.")
    // @CommandPermission(ADMIN_PERMISSION)
    // public void onCommand(CommandSender commandSender, Command command, String label, String[] args) { stop = true; }

    // public void generateStructuresAndFeatures(CommandSender commandSender) { generateStructuresAndFeatures(commandSender, false); }

    // @Subcommand("thenClean")
    // @Description("Place structures and features in a selected area then clean the selected area.")
    // @CommandPermission(ADMIN_PERMISSION)
    // public void generateStructuresAndFeaturesThenClean(CommandSender commandSender) { generateStructuresAndFeatures(commandSender, true);
    // }

    public static void stop() { stop = true; }

    public static void generateStructuresAndFeatures(CommandSender commandSender, boolean thenClean) {
        thingsToPlace = new LinkedList<>();
        thingsToPlaceByChunk = new java.util.HashMap<>();
        if (startTime == -1) { // need to init for the first time.
            commandSender.sendMessage("Populating...");
            thingsLocations = features.stream().map(Feature::getName).collect(java.util.stream.Collectors.toSet()).stream()
                    .collect(java.util.stream.Collectors.toMap(s -> s, s -> new LinkedList<>()));
            startTime = System.currentTimeMillis();
        } else {
            commandSender.sendMessage("Resuming populating...");
            printFullProgress(commandSender);
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
                    Biome biome = NMSBiomeUtils.getBiome(chunk.getBlock(0, 0, 0).getLocation());

                    for (int i = 0; i < 16; i++) {
                        for (int j = 0; j < 16; j++) {
                            Block column = chunk.getBlock(i, 0, j);
                            double r = random.nextDouble();
                            for (Feature feature : features) {
                                if (feature.isCompatibleBiome(biome) && feature.isCompatibleXZ(column.getX(), column.getZ())) {
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
                    printProgress(commandSender);
                }
                if (stop) {
                    commandSender.sendMessage("stopping. Wating to all things to be placed & all chunks to be unloaded. "
                            + thingsToPlaceByChunk.size() + " chunks to unload.");
                }
                if ((WorldSelectorHPlugin.getSelector().progress() >= 1.0 && thingsToPlace.isEmpty())
                        || (stop && thingsToPlaceByChunk.isEmpty())) {
                    printFullProgress(commandSender);
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
        long timeForFullProgressLeft = timeForFullProgress - (System.currentTimeMillis() - startTime);
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

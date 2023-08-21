package fr.formiko.worldpopulatorh.commands;

import fr.formiko.worldpopulatorh.ThingsToPlace;
import fr.formiko.worldpopulatorh.WorldPopulatorHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Chunk.LoadLevel;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class PopulateCommand implements CommandExecutor {
    private static final Random random = new Random();
    private static final List<Biome> oceanBiomes = List.of(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN,
            Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN, Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.WARM_OCEAN);
    public static List<ThingsToPlace> thingsToPlace = new LinkedList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        generateStructuresAndFeatures(sender);
        return true;
    }

    private static void generateStructuresAndFeatures(CommandSender sender) {
        sender.sendMessage("Populating...");
        thingsToPlace = new LinkedList<>();

        new BukkitRunnable() {
            private long printTime, execTime, cpt, cptTotal;
            private Map<String, Integer> cptMap = List.of("amethyst_geode").stream()
                    .collect(java.util.stream.Collectors.toMap(s -> s, s -> 0));

            @Override
            public void run() {
                execTime = System.currentTimeMillis();

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
                    Chunk chunk = WorldSelectorHPlugin.getSelector().nextChunk();
                    if (chunk == null) {
                        break;
                    }
                    Biome biome = chunk.getBlock(0, 0, 0).getBiome();
                    if (oceanBiomes.contains(biome)) {
                        // TODO generate shipwrecks
                    } else {
                        double r = Math.random();
                        if (r < 0.1) {
                            chunk.setForceLoaded(true);
                            chunk.load();
                            thingsToPlace.add(new ThingsToPlace("amethyst_geode", chunk.getX() * 16, random.nextInt(100) - 64,
                                    chunk.getZ() * 16, true, chunk));
                            Bukkit.getConsoleSender().sendMessage("Want to place in " + chunk);
                            cptMap.put("amethyst_geode", cptMap.get("amethyst_geode") + 1);
                            cpt++;
                        }
                    }
                    cptTotal++;
                }
                if (printTime + 1000 < System.currentTimeMillis()) {
                    printTime = System.currentTimeMillis();
                    printProgress(sender, cpt);
                }
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0 && thingsToPlace.isEmpty()) {
                    printProgress(sender, cpt);
                    sender.sendMessage("Place " + cpt + " structures|features in " + cptTotal + " chunks.");
                    sender.sendMessage("Place " + cptMap);
                    cancel();
                }
            }
        }.runTaskTimer(WorldPopulatorHPlugin.plugin, 0, 2); // 0, 2 because 1 tick is not enoth to load the chunk.
    }

    private static void printProgress(CommandSender sender, long cpt) {
        sender.sendMessage("Progress: " + cpt + "   " + WorldSelectorHPlugin.getSelector().progress() * 100 + "%");
    }
}

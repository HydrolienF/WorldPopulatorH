package fr.formiko.worldpopulatorh.commands;

import fr.formiko.worldpopulatorh.WorldPopulatorHPlugin;
import fr.formiko.worldselectorh.WorldSelectorHPlugin;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class PopulateCommand implements CommandExecutor {
    private static final Random random = new Random();
    private static final List<Biome> oceanBiomes = List.of(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN,
            Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN, Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.WARM_OCEAN);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        generateStructuresAndFeatures(sender);
        return true;
    }

    private static void generateStructuresAndFeatures(CommandSender sender) {
        sender.sendMessage("Populating...");

        new BukkitRunnable() {
            private long printTime, execTime, cpt, cptTotal;
            private Map<String, Integer> cptMap = List.of("amethyst_geode").stream()
                    .collect(java.util.stream.Collectors.toMap(s -> s, s -> 0));

            @Override
            public void run() {
                execTime = System.currentTimeMillis();
                while (execTime + 50 > System.currentTimeMillis() && WorldSelectorHPlugin.getSelector().hasNextBlock()) {
                    Chunk chunk = WorldSelectorHPlugin.getSelector().nextChunk();
                    Biome biome = chunk.getBlock(0, 0, 0).getBiome();
                    if (oceanBiomes.contains(biome)) {
                        // TODO generate shipwrecks
                    } else {
                        double r = Math.random();
                        if (r < 0.1) {
                            if (!chunk.isLoaded()) {
                                // if (!chunk.load()) {
                                // sender.sendMessage("Fail to load chunk");
                                // }
                            }
                            placeFeature("amethyst_geode", chunk.getX() * 16 + random.nextInt(16), random.nextInt(100) - 64,
                                    chunk.getZ() * 16 + random.nextInt(16));
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
                if (WorldSelectorHPlugin.getSelector().progress() >= 1.0) {
                    printProgress(sender, cpt);
                    sender.sendMessage("Place " + cpt + " structures|features in " + cptTotal + " chunks.");
                    sender.sendMessage("Place " + cptMap);
                    cancel();
                }
            }
        }.runTaskTimer(WorldPopulatorHPlugin.plugin, 0, 1);
    }

    private static void placeFeature(String feature, int x, int y, int z) {
        Chunk chunk = WorldSelectorHPlugin.getSelector().getWorld().getBlockAt(x, y, z).getChunk();
        chunk.setForceLoaded(true);
        chunk.load();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runCommand("forceload add " + x + " " + z);
        Bukkit.getConsoleSender().sendMessage("place feature minecraft:" + feature + " " + x + " " + y + " " + z);
        runCommand("place feature minecraft:" + feature + " " + x + " " + y + " " + z);
        chunk.setForceLoaded(false);
        chunk.unload();
        // runCommand("forceload remove " + x + " " + z);
    }

    private static void runCommand(String command) { Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command); }

    private static void printProgress(CommandSender sender, long cpt) {
        sender.sendMessage("Progress: " + cpt + "   " + WorldSelectorHPlugin.getSelector().progress() * 100 + "%");
    }
}

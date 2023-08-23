package fr.formiko.worldpopulatorh;

import fr.formiko.worldpopulatorh.commands.PopulateAndCleanCommand;
import fr.formiko.worldpopulatorh.commands.PopulateCommand;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldPopulatorHPlugin extends JavaPlugin {
    public static WorldPopulatorHPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("populate").setExecutor(new PopulateCommand());
        getCommand("populate").setTabCompleter((CommandSender sender, Command command, String alias, String[] args) -> List.of());
        getCommand("populateAndClean").setExecutor(new PopulateAndCleanCommand());
        getCommand("populateAndClean").setTabCompleter((CommandSender sender, Command command, String alias, String[] args) -> List.of());
    }

    @Override
    public void onDisable() {
        // TODO stop current task with a boolean as if all column where done.
    }


    public static void runCommand(String command) {
        Bukkit.getConsoleSender().sendMessage("run: " + command);
        // TODO send a custom ConsolCommandSender to get back the message of placed or failed and deal with it.
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }


    public static List<Chunk> getAllChunksBetween(int x1, int z1, int x2, int z2, World world) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        List<Chunk> chunks = new java.util.LinkedList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }

    public boolean saveLocations(Map<String, List<String>> locations) {
        File dataFile = new File("plugins/WorldPopulatorH/locations.yml");
        File parentFile = dataFile.getParentFile();
        parentFile.mkdirs();
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        try {
            data.set("locations", locations);
            data.save(dataFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

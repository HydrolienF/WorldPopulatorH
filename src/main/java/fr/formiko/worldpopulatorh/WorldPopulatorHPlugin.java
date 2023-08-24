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
    private static String pathToData = "plugins/WorldPopulatorH/data.yml";

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("populate").setExecutor(new PopulateCommand());
        getCommand("populate").setTabCompleter((CommandSender sender, Command command, String alias, String[] args) -> List.of());
        getCommand("populateAndClean").setExecutor(new PopulateAndCleanCommand());
        getCommand("populateAndClean").setTabCompleter((CommandSender sender, Command command, String alias, String[] args) -> List.of());
        try {
            PopulateCommand.loadData();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("No data file found");
        }
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

    public boolean saveData(Map<String, List<String>> locations, long cpt, long cptTotal, long startTime) {
        File dataFile = new File(pathToData);
        File parentFile = dataFile.getParentFile();
        parentFile.mkdirs();
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        try {
            data.set("locations", locations);
            data.set("cpt", cpt);
            data.set("cptTotal", cptTotal);
            data.set("startTime", startTime);
            data.save(dataFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> loadData() {
        File dataFile = new File(pathToData);
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        return data.getValues(false);
    }
}

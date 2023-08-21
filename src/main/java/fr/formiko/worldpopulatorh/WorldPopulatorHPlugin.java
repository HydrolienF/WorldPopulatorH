package fr.formiko.worldpopulatorh;

import fr.formiko.worldpopulatorh.commands.PopulateCommand;
import fr.formiko.worldpopulatorh.commands.PopulateTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldPopulatorHPlugin extends JavaPlugin {
    public static WorldPopulatorHPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("populate").setExecutor(new PopulateCommand());
        getCommand("populate").setTabCompleter(new PopulateTabCompleter());
    }


    public static void runCommand(String command) {
        Bukkit.getConsoleSender().sendMessage("run: " + command);
        // TODO send a custom ConsolCommandSender to get back the message of placed or failed and deal with it.
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

}

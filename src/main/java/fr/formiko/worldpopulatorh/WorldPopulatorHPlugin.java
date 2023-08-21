package fr.formiko.worldpopulatorh;

import fr.formiko.worldpopulatorh.commands.PopulateCommand;
import fr.formiko.worldpopulatorh.commands.PopulateTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldPopulatorHPlugin extends JavaPlugin {
    public static WorldPopulatorHPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("populate").setExecutor(new PopulateCommand());
        getCommand("populate").setTabCompleter(new PopulateTabCompleter());
    }

}

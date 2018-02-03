package me.waldxn.mcairdrop;

import me.waldxn.mcairdrop.commands.AirdropCommand;
import me.waldxn.mcairdrop.commands.AirdropListCommand;
import me.waldxn.mcairdrop.commands.AirdropLocationsCommand;
import me.waldxn.mcairdrop.managers.AirdropManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public final class McAirdrop extends JavaPlugin {

    private AirdropManager airdropManager;

    @Override
    public void onEnable() {
        createConfig();
        airdropManager = new AirdropManager(this);
        registerCommands();
        registerEvents();
        airdropManager.saveAirdrops();
        airdropManager.saveWorldAirdrops();
        automaticAirdropTimer();
    }


    private void registerCommands() {
        getCommand("airdrop").setExecutor(new AirdropCommand(this, airdropManager));
        getCommand("airdroplist").setExecutor(new AirdropListCommand(this, airdropManager));
        getCommand("airdroplocations").setExecutor(new AirdropLocationsCommand(this, airdropManager));
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(airdropManager, this);
    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void automaticAirdropTimer() {
        long timer = getConfig().getInt("AutomaticAirdropDelay");
        if (getConfig().getBoolean("AutomaticAirdrops")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    airdropManager.autoSpawnAirdrop();
                }
            }.runTaskTimer(this, timer, timer);
        }
    }
}

package me.waldxn.mcairdrop.managers;

import me.waldxn.mcairdrop.McAirdrop;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AirdropManager implements Listener {

    private final McAirdrop pl;
    public final HashMap<String, ArrayList<ItemStack>> crates = new HashMap<>();
    private final HashMap<String, Boolean> enabledCrates = new HashMap<>();
    private final HashMap<World, Boolean> enabledWorlds = new HashMap<>();
    public final HashMap<Location, String> crateLocations = new HashMap<>();
    private final String prefix;
    private Location chestLocation;

    public AirdropManager(McAirdrop pl) {
        this.pl = pl;
        FileConfiguration config = pl.getConfig();
        String prefix = config.getString("Prefix");
        this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
    }

    // Loads Airdrops into a HashMap
    public void saveAirdrops() {
        for (String crateName : pl.getConfig().getConfigurationSection("Crates").getKeys(false)) {
            if (pl.getConfig().getBoolean("Crates." + crateName + ".AutomaticCrate")) {
                enabledCrates.put(crateName, true);
            }
            ArrayList<ItemStack> items = new ArrayList<>();
            for (String item : pl.getConfig().getConfigurationSection("Crates." + crateName + ".Items").getKeys(false)) {
                String quantity = pl.getConfig().getString("Crates." + crateName + ".Items." + item + ".Quantity");
                items.add(itemStackCreator(Material.valueOf(item), quantity));
            }
            crates.put(crateName, items);
        }
    }

    // Loads Automatic Airdrop enabled worlds into a HashMap
    public void saveWorldAirdrops() {
        for (String worldName : pl.getConfig().getConfigurationSection("AutomaticAirdropWorlds").getKeys(false)) {
            if (pl.getConfig().getBoolean("AutomaticAirdropWorlds." + worldName)) {
                if (Bukkit.getWorld(worldName) != null) {
                    World world = Bukkit.getWorld(worldName);
                    enabledWorlds.put(world, true);
                } else {
                    System.out.print(worldName + " doesn't exist!");
                }
            }
        }
    }

    // Creates Itemstacks for use in Airdrops
    private ItemStack itemStackCreator(Material material, String quantity) {
        try {
            Material mat = material;
            int x = Integer.valueOf(quantity);
        } catch (Exception e) {
            System.out.println("Could not identify material: " + material + " -> quantity: " + quantity);
            e.printStackTrace();
            return null;
        }
        return new ItemStack(material, Integer.valueOf(quantity));
    }

    // Spawns airdrops. Used in commands and Automatic Airdrop methods
    public void spawnAirdrop(World world, String crateName, Location location) {
        if (crates.containsKey(crateName)) {
            int x = (int) location.getX();
            int z = (int) location.getZ();

            String droppingMessage = pl.getConfig().getString("BroadcastDropping");
            droppingMessage = droppingMessage.replaceAll("%x%", String.valueOf(x));
            droppingMessage = droppingMessage.replaceAll("%z%", String.valueOf(z));
            droppingMessage = ChatColor.translateAlternateColorCodes('&', droppingMessage);
            Bukkit.broadcastMessage(prefix + " " + droppingMessage);

            new BukkitRunnable() {
                int height = 255;
                //Particle particle = (Particle) pl.getConfig().get("Crates." + crateName + ".Particle");
                final int particleAmount = pl.getConfig().getInt("Crates." + crateName + ".ParticleAmount");

                @Override
                public void run() {
                    if (world.getBlockAt(x, height, z).getType() == Material.AIR ||
                            world.getBlockAt(x, height, z).getType() == null ||
                            world.getBlockAt(x, height, z).getType() == Material.LONG_GRASS ||
                            world.getBlockAt(x, height, z).getType() == Material.CHORUS_FLOWER ||
                            world.getBlockAt(x, height, z).getType() == Material.YELLOW_FLOWER ||
                            world.getBlockAt(x, height, z).getType() == Material.RED_ROSE ||
                            world.getBlockAt(x, height, z).getType() == Material.CROPS ||
                            world.getBlockAt(x, height, z).getType() == Material.DOUBLE_PLANT ||
                            world.getBlockAt(x, height, z).getType() == Material.TORCH) {
                        world.getBlockAt(x, height, z).setType(Material.CHEST);
                        world.getBlockAt(x, height + 1, z).setType(Material.AIR);
                        world.spawnParticle(Particle.valueOf(pl.getConfig().getString("Crates." + crateName + ".Particle")), x, height + 1, z, particleAmount);
                        height--;
                    } else {
                        chestLocation = world.getBlockAt(x, height + 1, z).getLocation();
                        String landingMessage = pl.getConfig().getString("BroadcastLanding");
                        landingMessage = landingMessage.replaceAll("%x%", String.valueOf(x));
                        landingMessage = landingMessage.replaceAll("%z%", String.valueOf(z));
                        landingMessage = ChatColor.translateAlternateColorCodes('&', landingMessage);
                        Bukkit.broadcastMessage(prefix + " " + landingMessage);
                        fillChest(crateName);
                        this.cancel();
                    }
                }
            }.runTaskTimer(pl, 0L, pl.getConfig().getLong("AirdropFallingSpeed"));
        }
    }

    // Called upon form method in Main class
    public void autoSpawnAirdrop() {
        Random generator = new Random();
        Object[] keys = enabledCrates.keySet().toArray();
        Object randomKey = keys[generator.nextInt(keys.length)];
        for (World world : enabledWorlds.keySet()) {
            if (world.getPlayers().size() != 0) {
                spawnAirdrop(world, (String) randomKey, randomCoordinates(world, (String) randomKey));
            }
        }
    }

    // Fills chest once reaching ground level
    private void fillChest(String crateName) {
        if (chestLocation.getBlock().getType() == Material.CHEST) {
            Chest chest = (Chest) chestLocation.getBlock().getState();
            chest.setCustomName(ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("AirdropInventoryTitle")));
            chest.update(true);
            Inventory inv = chest.getInventory();
            ArrayList<ItemStack> itemStacks = crates.get(crateName);
            for (ItemStack i : itemStacks) {
                if (inv.firstEmpty() != -1) {
                    inv.addItem(i);
                } else {
                    crateLocations.put(chest.getLocation(), crateName);
                }
            }
            crateLocations.put(chest.getLocation(), crateName);
        }
    }

    // Generates random coordinates based on config values
    public Location randomCoordinates(World world, String crateName) {
        Location location = null;
        if (crates.containsKey(crateName)) {
            int xMin = pl.getConfig().getInt("Crates." + crateName + ".xMin");
            int xMax = pl.getConfig().getInt("Crates." + crateName + ".xMax");
            int zMin = pl.getConfig().getInt("Crates." + crateName + ".zMin");
            int zMax = pl.getConfig().getInt("Crates." + crateName + ".zMax");
            if (xMin >= xMax || zMin >= zMax) {
                throw new IllegalArgumentException("Max must be greater than Min");
            }
            Random random = new Random();
            int randomXValue = xMin + random.nextInt(xMax - xMin);
            int randomZValue = zMin + random.nextInt(zMax - zMin);
            location = world.getBlockAt(randomXValue, 256, randomZValue).getLocation();
        }
        return location;
    }

    // Checks if Airdrop is empty. If so, destroys the chest
    @EventHandler
    public void onPlayerLoot(InventoryCloseEvent event) {
        Location location = event.getInventory().getLocation();
        if (event.getInventory().getLocation().getBlock().getState() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getLocation().getBlock().getState();
            if (crateLocations.containsKey(location) || chest.getCustomName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&',
                    pl.getConfig().getString("AirdropInventoryTitle")))) {
                if (event.getInventory().firstEmpty() == 0) {
                    String claimMessage = ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("BroadcastClaim"));
                    claimMessage = claimMessage.replaceAll("%x%", String.valueOf((int) location.getX()));
                    claimMessage = claimMessage.replaceAll("%z%", String.valueOf((int) location.getZ()));
                    claimMessage = claimMessage.replaceAll("%username%", event.getPlayer().getName());
                    Bukkit.broadcastMessage(prefix + " " + claimMessage);
                    location.getBlock().setType(Material.AIR);
                    crateLocations.remove(location);
                }
            }
        }
    }

    // Claims the airdrop when the crate is destroyed
    @EventHandler
    public void onCrateDestroy(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        if (event.getBlock().getState() instanceof Chest) {
            Chest chest = (Chest) event.getBlock().getState();
            if (crateLocations.containsKey(location) || chest.getCustomName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&',
                    pl.getConfig().getString("AirdropInventoryTitle")))) {
                String claimMessage = ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("BroadcastClaim"));
                claimMessage = claimMessage.replaceAll("%x%", String.valueOf((int) location.getX()));
                claimMessage = claimMessage.replaceAll("%z%", String.valueOf((int) location.getZ()));
                claimMessage = claimMessage.replaceAll("%username%", event.getPlayer().getName());
                Bukkit.broadcastMessage(prefix + " " + claimMessage);
                crateLocations.remove(location);
            }
        }
    }
}

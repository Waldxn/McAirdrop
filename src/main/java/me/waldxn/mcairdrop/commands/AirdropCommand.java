package me.waldxn.mcairdrop.commands;

import me.waldxn.mcairdrop.McAirdrop;
import me.waldxn.mcairdrop.managers.AirdropManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AirdropCommand implements CommandExecutor {

    private final McAirdrop pl;
    private final AirdropManager airdropManager;

    public AirdropCommand(McAirdrop pl, AirdropManager airdropManager) {
        this.pl = pl;
        this.airdropManager = airdropManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            if (args.length == 2) {
                if (Bukkit.getWorld(args[0]) != null) {
                    World world = Bukkit.getWorld(args[0]);
                    if (airdropManager.crates.containsKey(args[1])) {
                        airdropManager.spawnAirdrop(Bukkit.getWorld(args[0]), args[1], airdropManager.randomCoordinates(world, args[0]));
                        return true;
                    } else {
                        sender.sendMessage("Crate \"" + args[1] + "\" doesn't exist!");
                        return true;
                    }
                } else {
                    sender.sendMessage("World \"" + args[0] + "\" doesn't exist!");
                    return true;
                }
            }
            if (args.length == 4) {
                try {
                    int x = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sender.sendMessage("Usage: /airdrop [world] [crate name] [x-Coord] [z-Coord]");
                    return true;
                }
                if (Bukkit.getWorld(args[0]) != null) {
                    World world = Bukkit.getWorld(args[0]);
                    Location location = world.getBlockAt(Integer.parseInt(args[2]), 256, Integer.parseInt(args[3])).getLocation();
                    if (airdropManager.crates.containsKey(args[1])) {
                        airdropManager.spawnAirdrop(Bukkit.getWorld(args[0]), args[1], location);
                        return true;
                    } else {
                        sender.sendMessage("Crate \"" + args[1] + "\" doesn't exist!");
                        return true;
                    }
                } else {
                    sender.sendMessage("World \"" + args[0] + "\" doesn't exist!");
                    return true;
                }
            }
            sender.sendMessage("Usage: /airdrop [world] [crate] [x-coord] [z-coord]");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("airdrop.call")) {
            player.sendMessage("You don't have permission to do this!");
            return true;
        }

        if (args.length == 1) {
            if (airdropManager.crates.containsKey(args[0])) {
                airdropManager.spawnAirdrop(player.getWorld(), args[0], airdropManager.randomCoordinates(player.getWorld(), args[0]));
                return true;
            } else {
                player.sendMessage("Crate \"" + args[0] + "\" doesn't exist!");
                return true;
            }
        }


        if (args.length == 3) {
            if (airdropManager.crates.containsKey(args[0])) {
                try {
                    int x = Integer.parseInt(args[1]);
                    int z = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sender.sendMessage("Usage: /airdrop [crate name] <x-coord> <z-coord>");
                    return true;
                }
                Location location = player.getWorld().getBlockAt(Integer.parseInt(args[1]), 256, Integer.parseInt(args[2])).getLocation();
                airdropManager.spawnAirdrop(player.getWorld(), args[0], location);
            } else {
                sender.sendMessage(pl.getConfig().getString("Prefix") + " that crate doesn't exist");
            }
            return true;
        }

        return false;
    }
}


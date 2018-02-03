package me.waldxn.mcairdrop.commands;

import me.waldxn.mcairdrop.McAirdrop;
import me.waldxn.mcairdrop.managers.AirdropManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AirdropLocationsCommand implements CommandExecutor {

    private final McAirdrop plugin;
    private final AirdropManager airdropManager;

    public AirdropLocationsCommand(McAirdrop pl, AirdropManager airdropManager) {
        this.plugin = pl;
        this.airdropManager = airdropManager;
    }

    //TODO: Grab message layout from config
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("airdrop.locations")) {
            if (args.length == 0) {
                String prefix = ChatColor.translateAlternateColorCodes('&' , plugin.getConfig().getString("Prefix"));
                String message = prefix + " ";
                int size = 0;
                if (airdropManager.crateLocations.size() != 0) {
                    for (Location crateLocation : airdropManager.crateLocations.keySet()) {
                        size = size + 1;
                        if (airdropManager.crateLocations.size() == size) {
                            String crateName = airdropManager.crateLocations.get(crateLocation);
                            message = message + crateName + " crate at x:" + crateLocation.getX() + " z:" + crateLocation.getZ();
                            sender.sendMessage(message);
                        } else {
                            String crateName = airdropManager.crateLocations.get(crateLocation);
                            message = message + crateName + " crate at x:" + crateLocation.getX() + " z:" + crateLocation.getZ() + ", ";
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage(prefix + " No airdrops are present");
                    return true;
                }
            }
            sender.sendMessage("You don't have permission to do that!");
            return true;
        }
        return true;
    }
}

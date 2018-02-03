package me.waldxn.mcairdrop.commands;

import me.waldxn.mcairdrop.McAirdrop;
import me.waldxn.mcairdrop.managers.AirdropManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AirdropListCommand implements CommandExecutor {

    private final McAirdrop plugin;
    private final AirdropManager airdropManager;
    private String prefix;

    public AirdropListCommand(McAirdrop pl, AirdropManager airdropManager) {
        this.plugin = pl;
        this.airdropManager = airdropManager;
        this.prefix = ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("Prefix"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.hasPermission("airdrop.list")) {
            if (args.length == 0) {
                int size = 0;
                String message = prefix + " &7Crates: ";
                if (airdropManager.crates.size() != 0) {
                    for (String crates : airdropManager.crates.keySet()) {
                        size = size + 1;
                        if (airdropManager.crates.keySet().size() == size) {
                            message = message + crates;
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&' , message));
                        } else {
                            message = message + crates + ", ";
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage(prefix + " No crates detected!");
                    return true;
                }
            }
        }
        return false;
    }
}

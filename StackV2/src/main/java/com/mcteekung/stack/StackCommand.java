package com.mcteekung.stack;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StackCommand implements CommandExecutor, TabCompleter {

    private final Stack plugin;

    public StackCommand(Stack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "on":
                plugin.getConfig().set("enabled", true);
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Stack display has been enabled.");
                plugin.updateAllExistingItems(); // Update items already on the ground
                break;
            case "off":
                plugin.getConfig().set("enabled", false);
                plugin.saveConfig();
                plugin.clearAllItemNames(); // Clean up existing names
                sender.sendMessage(ChatColor.RED + "Stack display has been disabled.");
                break;
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.YELLOW + "Stack plugin configuration has been reloaded.");
                // After reload, update all items according to the new config setting
                if (plugin.getConfig().getBoolean("enabled")) {
                    plugin.updateAllExistingItems();
                } else {
                    plugin.clearAllItemNames();
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /stack help for a list of commands.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "==============================================");
        sender.sendMessage(ChatColor.YELLOW + "Stack Version: " + ChatColor.WHITE + "Alpha 0.1");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "/stack on" + ChatColor.GRAY + "  => for allow Display a Count of any item on server");
        sender.sendMessage(ChatColor.AQUA + "/stack off" + ChatColor.GRAY + " => for Deny Display a Count of any Item on server");
        sender.sendMessage(ChatColor.AQUA + "/stack reload" + ChatColor.GRAY + " => for Reload this plugin");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Support YT: _MCTEEKUNG_");
        sender.sendMessage(ChatColor.GOLD + "==============================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("on", "off", "reload", "help"));
            // Filter completions based on what the user has already typed
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(); // No suggestions for further arguments
    }
}

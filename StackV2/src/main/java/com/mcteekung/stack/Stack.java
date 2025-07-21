package com.mcteekung.stack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Stack extends JavaPlugin {

    @Override
    public void onEnable() {
        // Setup config
        saveDefaultConfig();

        // Register command and tab completer
        StackCommand stackCommand = new StackCommand(this);
        this.getCommand("stack").setExecutor(stackCommand);
        this.getCommand("stack").setTabCompleter(stackCommand);

        // Register events
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);

        getLogger().info("Stack plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up item names when the plugin is disabled
        clearAllItemNames();
        getLogger().info("Stack plugin has been disabled!");
    }

    /**
     * Iterates through all items in all worlds and applies the name tag.
     * This is used when the plugin is enabled via command.
     */
    public void updateAllExistingItems() {
        if (!getConfig().getBoolean("enabled", true)) return;
        for (World world : Bukkit.getWorlds()) {
            for (Item item : world.getEntitiesByClass(Item.class)) {
                if (item.isValid()) {
                    updateItemName(item);
                }
            }
        }
    }

    /**
     * Iterates through all loaded items and removes their custom name tag.
     */
    public void clearAllItemNames() {
        for (World world : Bukkit.getWorlds()) {
            for (Item item : world.getEntitiesByClass(Item.class)) {
                // A simple check to avoid removing custom names set by other plugins/players
                if (item.getCustomName() != null && item.getCustomName().startsWith(ChatColor.AQUA + "x")) {
                     item.setCustomName(null);
                     item.setCustomNameVisible(false);
                }
            }
        }
    }

    /**
     * Sets or clears the custom name on an item entity based on its stack count and plugin status.
     * @param item The item entity.
     */
    public void updateItemName(Item item) {
        if (item == null || !item.isValid()) return;

        // If plugin is disabled, just clear the name and exit.
        if (!getConfig().getBoolean("enabled", true)) {
            if (item.getCustomName() != null) {
                item.setCustomName(null);
                item.setCustomNameVisible(false);
            }
            return;
        }

        ItemStack stack = item.getItemStack();
        int amount = stack.getAmount();
        String itemName = getFriendlyItemName(stack);

        String customName = ChatColor.AQUA + "x" + amount + " " + ChatColor.WHITE + itemName;

        item.setCustomName(customName);
        item.setCustomNameVisible(true);
    }

    /**
     * Generates a user-friendly name for an ItemStack.
     * @param itemStack The item stack.
     * @return A formatted name string.
     */
    private String getFriendlyItemName(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName(); // Use existing custom name if available
        }

        // Create a friendly name from the material type (e.g., OAK_LOG -> Oak Log)
        String materialName = itemStack.getType().toString();
        StringBuilder friendlyName = new StringBuilder();
        String[] words = materialName.split("_");
        for (String word : words) {
            if (word.isEmpty()) continue;
            friendlyName.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
        }
        return friendlyName.toString().trim();
    }
}

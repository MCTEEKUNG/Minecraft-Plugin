package com.mcteekung.stack;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    private final Stack plugin;

    public ItemListener(Stack plugin) {
        this.plugin = plugin;
    }

    private boolean isPluginEnabled() {
        return plugin.getConfig().getBoolean("enabled", true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!isPluginEnabled() || event.isCancelled()) return;

        Item newItem = event.getEntity();
        
        // Use a short delay to allow the item to properly spawn and to check for nearby items.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!newItem.isValid()) return; // Item was already picked up or removed

            ItemStack newItemStack = newItem.getItemStack();
            
            // Search for nearby items to stack with. A small radius is good for performance.
            for (Entity entity : newItem.getNearbyEntities(1.25, 1.25, 1.25)) {
                if (entity instanceof Item) {
                    Item existingItem = (Item) entity;
                    // Ensure we found a valid, different item that is similar
                    if (existingItem.isValid() && !existingItem.equals(newItem) && existingItem.getItemStack().isSimilar(newItemStack)) {
                        
                        // Found a compatible item. Merge them.
                        ItemStack existingItemStack = existingItem.getItemStack();
                        int newAmount = existingItemStack.getAmount() + newItemStack.getAmount();
                        
                        existingItemStack.setAmount(newAmount);
                        existingItem.setItemStack(existingItemStack);
                        
                        // Update the name of the merged stack
                        plugin.updateItemName(existingItem);
                        
                        // Remove the new item, as it has been merged.
                        newItem.remove();
                        
                        // We've handled this item, so we can exit the task.
                        return;
                    }
                }
            }
            
            // If we get here, no merge occurred. Just name the new item.
            plugin.updateItemName(newItem);
        }, 2L); // 2-tick delay for safety
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        // This handles vanilla merges. We just need to update the name of the resulting stack.
        if (!isPluginEnabled()) return;
        
        // Delay to ensure the merge is complete before updating the name.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (event.getTarget().isValid()) {
                plugin.updateItemName(event.getTarget());
            }
        }, 1L);
    }
}

package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

/**
 * Handle events for all Entity related events
 * @author Timberjaw
 */
public class SCEntityListener extends EntityListener {
    private final SpawnControl plugin;

    public SCEntityListener(SpawnControl instance) {
        plugin = instance;
    }
    
    public void onEntityDamage(EntityDamageEvent e)
    {
    	if(e.getEntity() instanceof Player)
    	{
    		Player p = (Player)e.getEntity();
    		if((p.getHealth() - e.getDamage()) <= 0)
    		{
    			// Mark player as dead
    			SpawnControl.log.info("[SpawnControl] Marking player " + p.getName() + " dead.");
    			plugin.markPlayerDead(p.getName());
    			
    			// Cancel event and restore health
    			e.setCancelled(true);
    			p.setHealth(20);
    			
    			// Notify the player of what has happened
    			p.sendMessage("You have died.");
    			
    			// Save death location
    			Location deathLoc = p.getLocation();
    			
    			// Send player to home
    			plugin.sendHome(p);
    			
    			// Drop items and clear inventory
    			ItemStack[] items = p.getInventory().getContents();
    			for(int i = 0; i < items.length; i++)
    			{
    				ItemStack is = items[i];
    				if(is != null && is.getAmount() > 0)
    				{
    					p.getWorld().dropItemNaturally(deathLoc, is);
    				}
    			}
    			
    			// Clear player's inventory
    			p.getInventory().clear();
    		}
    	}
    }
}

package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

import com.nijikokun.bukkit.Permissions.Permissions;

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
    	if(e.isCancelled())
    	{
    		return;
    	}
    	
    	int db = plugin.getSetting("behavior_death");
    	if(db != SpawnControl.Settings.DEATH_NONE)
    	{
	    	if(e.getEntity() instanceof Player)
	    	{
	    		Player p = (Player)e.getEntity();
	    		if((p.getHealth() - e.getDamage()) <= 0)
	    		{
	    			// Cancel event and restore health
	    			e.setCancelled(true);
	    			p.setHealth(20);
	    			
	    			// Notify the player of what has happened
	    			p.sendMessage("You have died.");
	    			
	    			// Save death location
	    			Location deathLoc = p.getLocation();
	    			
	    			// Send player to home, group spawn, or global spawn
	    			switch(db)
	    			{
	    				case SpawnControl.Settings.DEATH_HOME:
	    					plugin.sendHome(p);
	    					break;
	    				case SpawnControl.Settings.DEATH_GROUPSPAWN:
	    					plugin.sendToGroupSpawn(Permissions.Security.getGroup(p.getName()), p);
	    					break;
	    				case SpawnControl.Settings.DEATH_GLOBALSPAWN:
	    				default:
	    					plugin.sendToSpawn(p);
	    					break;
	    			}
	    			
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
}

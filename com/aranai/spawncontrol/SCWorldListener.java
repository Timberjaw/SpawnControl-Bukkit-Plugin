package com.aranai.spawncontrol;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Handle events for all World related events
 * @author Timberjaw
 */
public class SCWorldListener extends WorldListener {
    private final SpawnControl plugin;
    

    public SCWorldListener(SpawnControl instance) {
        plugin = instance;
    }
    
    public void onWorldLoad(WorldLoadEvent event)
    {
    	World w = event.getWorld();
    	String name = w.getName();
    	
    	/*
    	 * Set spawn if one has not yet been set (new world)
    	 */
    	
    	if(!plugin.getGroupData("scglobal", w))
		{
			// No spawn set, use world spawn location
			SpawnControl.log.info("[SpawnControl]: No global spawn found for world '"+name+"', setting global spawn to world spawn.");
			plugin.setGroupSpawn("scglobal", w.getSpawnLocation(), "onWorldLoaded");
		}
    	
    	/*
    	 * Override spawn if one is available and behavior_globalspawn is enabled
    	 */
    	
    	int db = plugin.getSetting("behavior_globalspawn");
    	if(db != SpawnControl.Settings.GLOBALSPAWN_DEFAULT)
    	{
	    	SpawnControl.log.info("[SpawnControl] Setting global spawn for '"+name+"'.");
	    	
	    	Location spawn = plugin.getSpawn(w);
	    	
	    	if(spawn != null)
	    	{
	    		// Set spawn
	    		w.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
	    	}
	    	else
	    	{
	    		// No spawn available
	    		SpawnControl.log.info("[SpawnControl] No spawn available for '"+name+"'!");
	    	}
    	}
    }
}
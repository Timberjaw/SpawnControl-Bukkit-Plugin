package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Handle events for all Player related events
 * @author Timberjaw
 */
public class SCPlayerListener extends PlayerListener {
    private final SpawnControl plugin;

    public SCPlayerListener(SpawnControl instance) {
        plugin = instance;
    }

    public void onPlayerCommand(PlayerChatEvent e)
    {
    	if(e.getMessage().equalsIgnoreCase("/sethome"))
    	{
    		if(plugin.setHome(e.getPlayer().getName(), e.getPlayer().getLocation(), e.getPlayer().getName()))
    		{
    			e.getPlayer().sendMessage("Home set successfully!");
    		}
    		else
    		{
    			e.getPlayer().sendMessage("Could not set Home!");
    		}
    		
    		e.setCancelled(true);
    	}
    	
    	if(e.getMessage().equalsIgnoreCase("/home"))
    	{
    		// Send player home
    		SpawnControl.log.info("[SPAWNCONTROL] Attempting to send player "+e.getPlayer().getName()+" to home.");
        	plugin.sendHome(e.getPlayer());
        	e.setCancelled(true);
    	}
    	
    	if(e.getMessage().equalsIgnoreCase("/scimportconfig"))
    	{
    		SpawnControl.log.info("[SPAWNCONTROL] Attempting to import configuration file.");
    		plugin.importConfig();
    		e.setCancelled(true);
    	}
    }
    
    public void onPlayerJoin(PlayerEvent e)
    {
    	if(plugin.sRespawnOnJoin)
    	{
	    	// Get player
	    	Player p = e.getPlayer();
	    	
	    	// Check for home
	    	SpawnControl.log.info("[SPAWNCONTROL] Attempting to send player "+p.getName()+" to home.");
	    	plugin.sendHome(p);
    	}
    }
}
package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * Handle events for all Player related events
 * @author Timberjaw
 */
public class SCPlayerListener extends PlayerListener {
    private final SpawnControl plugin;
    

    public SCPlayerListener(SpawnControl instance) {
        plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
    {
    	// Split the command in case it has parameters
    	String[] cmd = args;
        String commandName = command.getName().toLowerCase();
        Player p = null;
        
        if(sender instanceof Player)
        {
        	p = (Player)sender;
        }
    	
    	// Sethome
    	if(plugin.getSetting("enable_home") == SpawnControl.Settings.YES && commandName.equals("sethome"))
    	{
    		String setter = p.getName();;
    		String homeowner = setter;
    		Location l = p.getLocation();
    		
    		if(cmd.length > 0 && !Permissions.Security.permission(p, "SpawnControl.sethome.proxy"))
    		{
    			// User is trying to set home for another user but they don't have permission
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else if(!Permissions.Security.permission(p, "SpawnControl.sethome.basic"))
    		{
    			// User is trying to set home but they don't have permission
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else
    		{
    			if(cmd.length > 0)
    			{
    				// Setting home for different player
    				homeowner = cmd[0];
    			}
    			
	    		if(plugin.setHome(homeowner, l, setter))
	    		{
	    			p.sendMessage("Home set successfully!");
	    		}
	    		else
	    		{
	    			p.sendMessage("Could not set Home!");
	    		}
    		}
    		
    		return true;
    	}
    	
    	// Home
    	if(plugin.getSetting("enable_home") == SpawnControl.Settings.YES && commandName.equals("home"))
    	{
    		// Send player home
    		if(!Permissions.Security.permission(p, "SpawnControl.home.basic"))
    		{
    			// User doesn't have access to this command
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else
    		{
	    		SpawnControl.log.info("[SpawnControl] Attempting to send player "+p.getName()+" to home.");
	        	plugin.sendHome(p);
    		}
        	return true;
    	}
    	
    	// Spawn (globalspawn)
    	if(plugin.getSetting("enable_globalspawn") == SpawnControl.Settings.YES && (commandName.equals("spawn") || commandName.equals("globalspawn")))
    	{
    		// Send player to spawn
    		if(!Permissions.Security.permission(p, "SpawnControl.spawn.use"))
    		{
    			// User doesn't have access to this command
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else
    		{
	    		SpawnControl.log.info("[SpawnControl] Attempting to send player "+p.getName()+" to spawn.");
	        	plugin.sendToSpawn(p);
    		}
        	return true;
    	}
    	
    	// Set spawn (globalspawn)
    	if(plugin.getSetting("enable_globalspawn") == SpawnControl.Settings.YES && (commandName.equals("setspawn") || commandName.equals("setglobalspawn")))
    	{
    		// Set global spawn
    		if(!Permissions.Security.permission(p, "SpawnControl.spawn.set"))
    		{
    			// User doesn't have access to this command
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else
    		{
	    		SpawnControl.log.info("[SpawnControl] Attempting to set global spawn.");
	        	if(plugin.setSpawn(p.getLocation(), p.getName()))
	        	{
	        		p.sendMessage("Global spawn set successfully!");
	        	}
	        	else
	        	{
	        		p.sendMessage("Could not set global spawn.");
	        	}
    		}
        	return true;
    	}
    	
    	// Setgroupspawn
    	if(plugin.getSetting("enable_groupspawn") == SpawnControl.Settings.YES && commandName.equals("setgroupspawn"))
    	{
    		String group = null;
    		
    		// Set group spawn
    		if(!Permissions.Security.permission(p, "SpawnControl.groupspawn.set"))
    		{
    			// User doesn't have access to this command
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else if(!(cmd.length > 0))
    		{
    			// User didn't specify a group
    			p.sendMessage("Command format: /setgroupspawn [group]");
    		}
    		else
    		{
    			group = cmd[0];
	    		SpawnControl.log.info("[SpawnControl] Setting group spawn for '"+group+"'.");
	        	if(plugin.setGroupSpawn(group, p.getLocation(), p.getName()))
	        	{
	        		p.sendMessage("Group spawn for "+group+" set successfully!");
	        	}
	        	else
	        	{
	        		p.sendMessage("Could not set group spawn for "+group+".");
	        	}
    		}
        	return true;
    	}
    	
    	// Groupspawn
    	if(plugin.getSetting("enable_groupspawn") == SpawnControl.Settings.YES && commandName.equals("groupspawn"))
    	{
    		// Send player to group spawn
    		if(!Permissions.Security.permission(p, "SpawnControl.groupspawn.use"))
    		{
    			// User doesn't have access to this command
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else
    		{
    			// Get group spawn for player
    			String group = Permissions.Security.getGroup(p.getName());
	    		SpawnControl.log.info("[SpawnControl] Attempting to send player "+p.getName()+" to group spawn.");
	        	plugin.sendToGroupSpawn(group, p);
    		}
        	return true;
    	}
    	
    	// Check settings
    	
    	// Set setting
    	if(commandName.equals("sc_config") && Permissions.Security.permission(p, "SpawnControl.config"))
    	{
    		if(cmd.length < 2)
    		{
    			// Command format is wrong
    			p.sendMessage("Command format: /sc_config [setting] [value]");
    		}
    		else
    		{
	    		// Verify setting
	    		if(plugin.getSetting(cmd[0]) < 0)
	    		{
	    			// Bad setting key
	    			p.sendMessage("Unknown configuration value.");
	    		}
	    		else
	    		{
	    			// Parse value
	    			try
	    			{
	    				int tmpval = Integer.parseInt(cmd[1]);
	    				
	    				if(tmpval < 0)
	    				{
	    					p.sendMessage("Value must be >= 0.");
	    				}
	    				else
	    				{
	    					// Save
	    					if(!plugin.setSetting(cmd[0], tmpval, p.getName()))
	    					{
	    						p.sendMessage("Could not save value for '"+cmd[0]+"'!");
	    					}
	    					else
	    					{
	    						p.sendMessage("Saved value for '"+cmd[0]+"'.");
	    					}
	    				}
	    			}
	    			catch(Exception ex)
	    			{
	    				// Bad number
	    				p.sendMessage("Couldn't read value.");
	    			}
	    		}
    		}
    		return true;
    	}
    	
    	// Import config
    	if(commandName.equals("scimportconfig") && Permissions.Security.permission(p, "SpawnControl.import"))
    	{
    		SpawnControl.log.info("[SpawnControl] Attempting to import player configuration file.");
    		plugin.importConfig();
    		return true;
    	}
    	
    	// Import group config
    	if(commandName.equals("scimportgroupconfig") && Permissions.Security.permission(p, "SpawnControl.import"))
    	{
    		SpawnControl.log.info("[SpawnControl] Attempting to import group configuration file.");
    		plugin.importGroupConfig();
    		return true;
    	}
    	
    	return true;
    }
    
    public void onPlayerJoin(PlayerEvent e)
    {
    	if(Permissions.Security.getGroup(e.getPlayer().getName()).equalsIgnoreCase("default") && plugin.getHome(e.getPlayer().getName()) == null)
    	{
    		// Probably a new player
    		SpawnControl.log.info("[SpawnControl] Sending new player " + e.getPlayer().getName() + " to global spawn.");
    		
    		// Send player to global spawn
    		plugin.sendToSpawn(e.getPlayer());
    		
    		// Set home for player
    		plugin.setHome(e.getPlayer().getName(), plugin.getSpawn(), "SpawnControl");
    	}
    	
    	int jb = plugin.getSetting("behavior_join");
    	if(jb != SpawnControl.Settings.JOIN_NONE)
    	{
	    	// Get player
	    	Player p = e.getPlayer();
	    	
	    	// Check for home
	    	SpawnControl.log.info("[SpawnControl] Attempting to respawn player "+p.getName()+" (joining).");
	    	
	    	switch(jb)
	    	{
	    		case SpawnControl.Settings.JOIN_HOME:
	    			plugin.sendHome(p);
	    			break;
	    		case SpawnControl.Settings.JOIN_GROUPSPAWN:
	    			plugin.sendToGroupSpawn(Permissions.Security.getGroup(p.getName()), p);
	    			break;
	    		case SpawnControl.Settings.JOIN_GLOBALSPAWN:
	    		default:
	    			plugin.sendToSpawn(p);
	    			break;
	    	}
    	}
    }
    
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
    	int db = plugin.getSetting("behavior_death");
    	if(db != SpawnControl.Settings.DEATH_NONE)
    	{
    		// Get player
	    	Player p = e.getPlayer();
	    	
	    	// Check for home
	    	SpawnControl.log.info("[SpawnControl] Attempting to respawn player "+p.getName()+" (respawning).");
	    	
	    	// Build respawn location
	    	Location l;
	    	
    		switch(db)
	    	{
	    		case SpawnControl.Settings.DEATH_HOME:
	    			l = plugin.getHome(p.getName());
	    			break;
	    		case SpawnControl.Settings.DEATH_GROUPSPAWN:
	    			l = plugin.getGroupSpawn(Permissions.Security.getGroup(p.getName()));
	    			break;
	    		case SpawnControl.Settings.DEATH_GLOBALSPAWN:
	    		default:
	    			l = plugin.getGroupSpawn("scglobal");
	    			break;
	    	}
    		
    		if(l == null)
    		{
    			// Something has gone wrong
    			SpawnControl.log.warning("[SpawnControl] Could not find respawn for " + p.getName() + "!");
    		}
    		
    		e.setRespawnLocation(l);
    	}
    }
}
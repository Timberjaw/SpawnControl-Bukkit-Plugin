/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aranai.spawncontrol;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

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
    	
    	// Set Home
    	if(plugin.getSetting("enable_home") == SpawnControl.Settings.YES && commandName.equals("sethome"))
    	{
    		String setter = p.getName();;
    		String homeowner = setter;
    		Location l = p.getLocation();
    		
    		// Check basic permission first, as nothing else matters if they don't have this
    		if(!this.canUseSetHomeBasic(p))
    		{
    			// User is trying to set home but they don't have permission
    			p.sendMessage("You don't have permission to do that.");
    			return true;
    		}
    		
    		// Check cooldown exemption and status
    		long cooldown = this.cooldownLeft(p, "sethome");
    		if(cooldown > 0)
    		{
    			p.sendMessage("Cooldown is in effect. You must wait " + cooldown + " seconds.");
    			return true;
    		}
    		
    		// Handle player name
    		if(cmd.length > 0)
    		{
    			if(this.canUseSetHomeProxy(p))
    			{
	    			// Setting home for different player
					homeowner = cmd[0];
    			}
    			else
    			{
	    			// User is trying to set home for another user but they don't have permission
	    			p.sendMessage("You don't have permission to do that.");
	    			return true;
    			}
    		}
    		
    		// Handle world name
    		if(cmd.length > 1)
    		{
    			if(this.canUseSetHomeWorldProxy(p))
    			{
    				// Setting home for specific world
    				World w = this.plugin.getServer().getWorld(cmd[1]);
    				
    				if(w != null)
    				{
    					// Set the world to be saved as part of the home location
    					l.setWorld(w);
    				}
    				else
    				{
    					// The world does not exist
    					p.sendMessage("The specified world does not exist.");
    					return true;
    				}
    			}
    		}
    		
    		// Attempt to set the home
    		if(plugin.setHome(homeowner, l, setter))
    		{
    			p.sendMessage("Home set successfully!");
    			
    			// Set cooldown
        		this.setCooldown(p, "sethome");
    		}
    		else
    		{
    			p.sendMessage("Could not set Home!");
    		}
    		
    		return true;
    	}
    	
    	// Home
    	if(plugin.getSetting("enable_home") == SpawnControl.Settings.YES && commandName.equals("home"))
    	{
    		// Check cooldown exemption and status
    		long cooldown = this.cooldownLeft(p, "home");
    		if(cooldown > 0)
    		{
    			p.sendMessage("Cooldown is in effect. You must wait " + cooldown + " seconds.");
    			return true;
    		}
    		
    		// Set cooldown
    		this.setCooldown(p, "home");
    		
    		// Send player home
    		if(!this.canUseHomeBasic(p))
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
    		// Check cooldown exemption and status
    		long cooldown = this.cooldownLeft(p, "spawn");
    		if(cooldown > 0)
    		{
    			p.sendMessage("Cooldown is in effect. You must wait " + cooldown + " seconds.");
    			return true;
    		}
    		
    		// Set cooldown
    		this.setCooldown(p, "spawn");
    		
    		// Send player to spawn
    		if(!this.canUseSpawn(p))
    		{
    			// User doesn't have access to this command
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else
    		{
    			int spawnBehavior = plugin.getSetting("behavior_spawn");
    			String spawnType = "global";
    			
    			// Check permissions availability for group spawn
    			if(spawnBehavior == SpawnControl.Settings.SPAWN_GROUP && !plugin.getPermissions().isActive())
    			{
    				SpawnControl.log.warning("[SpawnControl] Spawn behavior set to 'group' but group support is not available. Using global spawn.");
    				spawnBehavior = SpawnControl.Settings.SPAWN_GLOBAL;
    			}
    			
    			switch(spawnBehavior)
    			{
	    			case SpawnControl.Settings.SPAWN_HOME:
						// Send player to home
						plugin.sendHome(p);
					break;
    				case SpawnControl.Settings.SPAWN_GROUP:
                        if(plugin.getPermissions().isActive())
                        {
                            // load all of the groups the player is in
                            List<String> groups = plugin.getPermissions().getGroups(p);

                            if(groups.size() > 0) {
                                plugin.sendToGroupSpawn(groups.get(0), p);
                            } else {
                                plugin.sendToSpawn(p);
                            }
                        }
                        else
                        {
                            plugin.sendToSpawn(p);
                        }
    				break;
    				case SpawnControl.Settings.SPAWN_GLOBAL:
    				default:
    					// Send player to global spawn
    					plugin.sendToSpawn(p);
    				break;
    			}
    			
	    		SpawnControl.log.info("[SpawnControl] Sending player "+p.getName()+" to spawn ("+spawnType+").");
    		}
        	return true;
    	}
    	
    	// Set spawn (globalspawn)
    	if(plugin.getSetting("enable_globalspawn") == SpawnControl.Settings.YES && (commandName.equals("setspawn") || commandName.equals("setglobalspawn")))
    	{
    		// Set global spawn
    		if(!this.canUseSetSpawn(p))
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
    	
    	// Set Group Spawn
    	if(plugin.getSetting("enable_groupspawn") == SpawnControl.Settings.YES && commandName.equals("setgroupspawn"))
    	{    		
    		String group = null;
    		
    		// Set group spawn
    		if(!this.canUseSetGroupSpawn(p))
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
    	
    	// Group Spawn
    	if(plugin.getSetting("enable_groupspawn") == SpawnControl.Settings.YES && commandName.equals("groupspawn"))
    	{
    		// Check cooldown exemption and status
    		long cooldown = this.cooldownLeft(p, "groupspawn");
    		if(cooldown > 0)
    		{
    			p.sendMessage("Cooldown is in effect. You must wait " + cooldown + " seconds.");
    			return true;
    		}
    		
    		// Set cooldown
    		this.setCooldown(p, "groupspawn");
    		
    		// Send player to group spawn
    		if(!this.canUseGroupSpawn(p))
    		{
    			// User doesn't have access to this command
    			p.sendMessage("You don't have permission to do that.");
    		}
    		else
    		{
    			// Get group spawn for player
                if(plugin.getPermissions().isActive())
	    		{
                    // load all of the groups the player is in
                    List<String> groups = plugin.getPermissions().getGroups(p);

                    if(groups.size() > 0) {
	    		        SpawnControl.log.info("[SpawnControl] Attempting to send player " + p.getName() + " to group spawn.");
                        plugin.sendToGroupSpawn(groups.get(0), p);
                    }
	    		}
    		}
        	return true;
    	}
    	
    	// Check settings
    	
    	// Set setting
    	if(commandName.equals("sc_config") && this.canUseScConfig(p))
    	{
    		if(cmd.length < 1)
    		{
    			// Command format is wrong
    			p.sendMessage("Command format: /sc_config [setting] <value>");
    		}
    		else
    		{
	    		// Verify setting
	    		if(!SpawnControl.validSettings.contains(cmd[0]))
	    		{
	    			// Bad setting key
	    			p.sendMessage("Unknown configuration value.");
	    		}
	    		else
	    		{
	    			if(cmd.length == 2)
	    			{
		    			// Parse and save value
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
	    			else
	    			{
	    				// Display current value
	    				p.sendMessage("Current value for '"+cmd[0]+"': "+plugin.getSetting(cmd[0]));
	    			}
	    		}
    		}
    		return true;
    	}
    	
    	// Import config
    	if(commandName.equals("scimportconfig") && p.isOp())
    	{
    		SpawnControl.log.info("[SpawnControl] Attempting to import player configuration file.");
    		plugin.importConfig();
    		return true;
    	}
    	
    	// Import group config
    	if(commandName.equals("scimportgroupconfig") && p.isOp())
    	{
    		SpawnControl.log.info("[SpawnControl] Attempting to import group configuration file.");
    		plugin.importGroupConfig();
    		return true;
    	}
    	
    	return true;
    }
    
    public void onPlayerJoin(PlayerJoinEvent e)
    {
    	if(plugin.getHome(e.getPlayer().getName(), e.getPlayer().getWorld()) == null)
    	{
    		// Probably a new player
    		SpawnControl.log.info("[SpawnControl] Sending new player " + e.getPlayer().getName() + " to global spawn.");
    		
    		// Send player to global spawn
    		plugin.sendToSpawn(e.getPlayer());
    		
    		// Set home for player
    		plugin.setHome(e.getPlayer().getName(), plugin.getSpawn(e.getPlayer().getWorld()), "SpawnControl");
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
	    			if(plugin.getPermissions().isActive())
	    			{
                        // load all of the groups the player is in
                        List<String> groups = plugin.getPermissions().getGroups(p);

                        if(groups.size() > 0) {
                            plugin.sendToGroupSpawn(groups.get(0), p);
                        } else {
                            plugin.sendToSpawn(p);
                        }
	    			}
	    			else
	    			{
	    				plugin.sendToSpawn(p);
	    			}
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
	    	Location l = null;
	    	
    		switch(db)
	    	{
	    		case SpawnControl.Settings.DEATH_HOME:
	    			l = plugin.getHome(p.getName(), p.getWorld());
	    			break;
	    		case SpawnControl.Settings.DEATH_GROUPSPAWN:
	    			if(plugin.getPermissions().isActive())
	    			{
                        // load all of the groups the player is in
                        List<String> groups = plugin.getPermissions().getGroups(p);

                        if(groups.size() > 0) {
                            for(String group : groups) {
                                if((l = plugin.getGroupSpawn(group, p.getWorld())) != null) {
                                    break;
                                }
                            }
                        } else {
                            l = plugin.getGroupSpawn("scglobal", p.getWorld());
                        }
	    			}
	    			else
	    			{
	    				l = plugin.getGroupSpawn("scglobal", p.getWorld());
	    			}
	    			break;
	    		case SpawnControl.Settings.DEATH_GLOBALSPAWN:
	    		default:
	    			l = plugin.getGroupSpawn("scglobal", p.getWorld());
	    			break;
	    	}
    		
    		if(l == null)
    		{
    			// Something has gone wrong
    			SpawnControl.log.warning("[SpawnControl] Could not find respawn for " + p.getName() + "!");
    			return;
    		}
    		else
    		{
    			// Set world
    			l.setWorld(p.getWorld());
    		}
    		
    		SpawnControl.log.info("[SpawnControl] DEBUG: Respawn Location: " + l.toString());
    		e.setRespawnLocation(l);
    	}
    }
    
    public boolean isExemptFromCooldowns(Player p, String cooldown)
    {
    	if(plugin.getPermissions().isActive())
    	{
            return plugin.getPermissions().permission(p, "SpawnControl.CooldownExempt." + cooldown);
    	}
    	
    	return p.isOp();
    }
    
    public long cooldownLeft(Player p, String cooldown)
    {
    	// Check cooldown setting
    	int cooldownAmount = plugin.getSetting("cooldown_"+cooldown);
    	
    	if(cooldownAmount > 0 && !this.isExemptFromCooldowns(p, cooldown))
    	{
    		// Check cooldown status for player
    		return plugin.getCooldownRemaining(p, cooldown);
    	}
    	
    	return 0;
    }
    
    public void setCooldown(Player p, String cooldown)
    {
    	if(!this.isExemptFromCooldowns(p, cooldown))
    	{
    		plugin.setCooldown(p, cooldown);
    	}
    }
    
    public boolean canUseSpawn(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.spawn.use");
        }
    	
    	return true;
    }
    
    public boolean canUseSetSpawn(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.spawn.set");
        }
    	
    	return p.isOp();
    }
    
    public boolean canUseSetGroupSpawn(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.groupspawn.set");
        }
    	
    	// Disabled without group support
    	return false;
    }
    
    public boolean canUseGroupSpawn(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.groupspawn.use");
        }
    	
    	// Disabled without group support
    	return false;
    }
    
    public boolean canUseHomeBasic(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.home.basic");
        }
    	
    	return true;
    }
    
    public boolean canUseSetHomeBasic(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.sethome.basic");
        }
    	
    	return true;
    }
    
    public boolean canUseSetHomeProxy(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.sethome.proxy");
        }
    	
    	return p.isOp();
    }
    
    public boolean canUseSetHomeWorldProxy(Player p)
    {
    	if(plugin.getPermissions().isActive())
    	{
    		return plugin.getPermissions().permission(p, "SpawnControl.sethome.worldproxy");
    	}
    	
    	return p.isOp();
    }
    
    public boolean canUseScConfig(Player p)
    {
        if(plugin.getPermissions().isActive()) {
            return plugin.getPermissions().permission(p, "SpawnControl.Config");
        }
    	
    	return p.isOp();
    }
}
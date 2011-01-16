package com.bukkit.timberjaw.spawncontrol;

/*
 * Features:
 * 1. Default spawn handling (exactspawn) - /setspawn, /spawn, /home, /globalspawn
 * 2. Group spawn handling - /setgroupspawn [group], /groupspawn <group>, /spawn, /home
 * 		- WAITING ON GROUP SUPPORT
 * 3. User spawn handling - /sethome <player>, /home <player>, /spawn <player>
 * 4. Resets - /resetgroupspawn [group], /resethome <player>
 * 
 * Tables:
 * 1. spawncontrol_config - active (?)
 * 2. spawncontrol_groups - group (name/id), X, Y, Z, R, P, last_updated (timestamp), updated_by (player)
 * 		- Default entry: sc_global, 0, 0, 0, 0, 0, 0, ''
 * 3. spawncontrol_users - user (name/id), X, Y, Z, R, P, last_updated (timestamp), updated_by (player)
 * 
 * Configuration values:
 * 1. home_priority (default: user) (values: user, group, global)
 * 		- If set to group, user homes are effectively DISABLED for members of groups with group spawns
 * 
 * Behaviors:
 * 1. /spawn ALWAYS points to group or global spawn
 * 2. /home points to user home if available and allowed, otherwise identical to /spawn
 * 3. Resetting REMOVES the specified spawn/home
 */

import java.io.File;
import java.util.HashMap;
import org.bukkit.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
 * SpawnControl for Bukkit
 *
 * @author Timberjaw
 */
public class SpawnControl extends JavaPlugin {
    private final SCPlayerListener playerListener = new SCPlayerListener(this);
    private final SCBlockListener blockListener = new SCBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

    public SpawnControl(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        // TODO: Place any custom initialisation code here

        // NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
    }

    

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        System.out.println("Goodbye world!");
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}
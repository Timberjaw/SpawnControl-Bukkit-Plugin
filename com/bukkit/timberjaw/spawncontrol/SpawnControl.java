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

import java.io.*;
import java.util.logging.*;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import java.sql.*;

/**
 * SpawnControl for Bukkit
 *
 * @author Timberjaw
 */
public class SpawnControl extends JavaPlugin {
    private final SCPlayerListener playerListener = new SCPlayerListener(this);
    private Connection conn;
    public static Logger log;
    public final static String directory = "plugins/SpawnControl";
    public final static String db = "jdbc:sqlite:" + SpawnControl.directory + File.separator + "spawncontrol.db";

    public SpawnControl(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        // TODO: Place any custom initialisation code here
    }
    
    private void initDB()
    {
    	ResultSet rs = null;
    	Statement st = null;
    	
    	try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	
        	DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, "players", null);
            if (!rs.next())
            {
            	// Create table
            	System.out.println("Table 'players' not found.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.executeUpdate("make mah player tabel");
                conn.commit();
            }
            
            rs = dbm.getTables(null, null, "groups", null);
            if (!rs.next())
            {
            	// Create table
            	System.out.println("Table 'groups' not found.");
            }
            
            rs = dbm.getTables(null, null, "settings", null);
            if (!rs.next())
            {
            	// Create table
            	System.out.println("Table 'settings' not found.");
            }
        	
	        rs.close();
	        conn.close();
        }
        catch(SQLException e)
        {
        	// ERROR
        	System.out.println("DB ERROR - " + e.getMessage());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
    }

    public void onEnable() {
    	log = Logger.getLogger("Minecraft");
    	
        if (!new File(directory).exists()) {
            try {
                (new File(directory)).mkdir();
            } catch (Exception e) {
                SpawnControl.log.log(Level.SEVERE, "[SPAWNCONTROL]: Unable to create spawncontrol/ directory.");
            }
        }
        
        this.initDB();
        
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        
        // Enable message
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    public void onDisable() {
        // Disable message
    	PluginDescriptionFile pdfFile = this.getDescription();
    	System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!" );
    }
}
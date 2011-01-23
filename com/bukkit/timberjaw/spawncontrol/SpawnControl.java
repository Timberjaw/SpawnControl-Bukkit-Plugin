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
 * 2. groups - group (name/id), X, Y, Z, R, P, last_updated (timestamp), updated_by (player)
 * 		- Default entry (untracked users): sc_global, 0, 0, 0, 0, 0, 0, ''
 * 3. players - user (name/id), X, Y, Z, R, P, last_updated (timestamp), updated_by (player)
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
import java.util.Hashtable;
import java.util.logging.*;
import java.sql.*;

// Import bukkit packages
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

// Import permissions package
import com.nijikokun.bukkit.Permissions.Permissions;

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
    
    // Permissions
    public static Permissions Permissions = null;
    public boolean usePermissions = false;
    
    // Cache variables
    private Hashtable<String,Integer> activePlayerIds;
    private Hashtable<Integer,Location> homes;
    private Hashtable<String,Integer> activeGroupIds;
    private Hashtable<Integer,Location> groupSpawns;
    
    // Settings
    public boolean sRespawnOnJoin = false;

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
            	log.info("[SpawnControl]: Table 'players' not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute("CREATE TABLE `players` (`id` INTEGER PRIMARY KEY, `name` varchar(32) NOT NULL, "
                		+"`x` REAL, `y` REAL, `z` REAL, `r` REAL, `p` REAL, "
                		+"`updated` INTEGER, `updated_by` varchar(32));");
                st.execute("CREATE UNIQUE INDEX playerIndex on `players` (`name`);");
                conn.commit();
                
                log.info("[SpawnControl]: Table 'players' created.");
            }
            
            rs = dbm.getTables(null, null, "groups", null);
            if (!rs.next())
            {
            	// Create table
            	log.info("[SpawnControl]: Table 'groups' not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute("CREATE TABLE `groups` (`id` INTEGER PRIMARY KEY, `name` varchar(32) NOT NULL, "
                		+"`x` REAL, `y` REAL, `z` REAL, `r` REAL, `p` REAL, "
                		+"`updated` INTEGER, `updated_by` varchar(32));");
                st.execute("CREATE UNIQUE INDEX groupIndex on `groups` (`name`);");
                conn.commit();
                
                log.info("[SpawnControl]: Table 'groups' created.");
            }
            
            rs = dbm.getTables(null, null, "settings", null);
            if (!rs.next())
            {
            	// Create table
            	//System.out.println("[SpawnControl]: Table 'settings' not found.");
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
    	
    	// Make sure we have a local folder for our database and such
        if (!new File(directory).exists()) {
            try {
                (new File(directory)).mkdir();
            } catch (Exception e) {
                SpawnControl.log.log(Level.SEVERE, "[SpawnControl]: Unable to create spawncontrol/ directory.");
            }
        }
        
        // Initialize the database
        this.initDB();
        
        // Initialize permissions system
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

    	if(SpawnControl.Permissions == null) {
    	    if(test != null) {
    	    	SpawnControl.Permissions = (Permissions)test;
    	    	this.usePermissions = true;
    	    } else {
    	    	log.info("[SpawnControl] Warning: Permissions system not enabled.");
    	    }
    	}
        
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        
        // Get player commands (used for /spawn, /home, etc)
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.High, this);
        
        // Get player join
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        
        // Initialize active player ids and homes
        this.activePlayerIds = new Hashtable<String,Integer>();
        this.homes = new Hashtable<Integer,Location>();
        
        // Initialize active group ids and group spawns
        this.activeGroupIds = new Hashtable<String,Integer>();
        this.groupSpawns = new Hashtable<Integer,Location>();
        
        // Enable message
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info( "[SpawnControl] version [" + pdfFile.getVersion() + "] loaded" );
    }
    
    public void onDisable() {
        // Disable message
    	PluginDescriptionFile pdfFile = this.getDescription();
    	log.info( "[SpawnControl] version [" + pdfFile.getVersion() + "] unloaded" );
    }
    
    // Spawn
    public void sendToSpawn(Player p)
    {
    	this.sendToGroupSpawn("scglobal", p);
    }
    
    // Set spawn
    public boolean setSpawn(Location l, String setter)
    {
    	return this.setGroupSpawn("scglobal", l, setter);
    }
    
    // Get spawn
    public Location getSpawn()
    {
    	return this.getGroupSpawn("scglobal");
    }
    
    // Home
    public void sendHome(Player p)
    {
    	// Check for home
    	if(!this.activePlayerIds.contains(p.getName()))
    	{
    		if(!this.getPlayerData(p.getName()))
    		{
    			// No home available, use global
    			return;
    		}
    	}
    	
    	// Teleport to home
    	p.teleportTo(this.homes.get(this.activePlayerIds.get(p.getName())));
    }
    
    // Get home
    public Location getHome(String name)
    {
    	// Check for home
    	if(!this.activePlayerIds.contains(name))
    	{
    		if(this.getPlayerData(name))
    		{
    			// Found home!
    			return this.homes.get(this.activePlayerIds.get(name));
    		}
    	}
    	
    	return null;
    }
    
    // Sethome
    public boolean setHome(String name, Location l, String updatedBy)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        Boolean success = false;
		
		// Save to database
		try
        {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(db);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement("REPLACE INTO `players` (id, name, x, y, z, r, p, updated, updated_by) VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?);");
			ps.setString(1, name);
			ps.setDouble(2, l.getX());
			ps.setDouble(3, l.getY());
			ps.setDouble(4, l.getZ());
			ps.setFloat(5, l.getYaw());
			ps.setFloat(6, l.getPitch());
			ps.setInt(7, (int)System.currentTimeMillis());
			ps.setString(8, updatedBy);
			ps.execute();
			conn.commit();
        	conn.close();
        	
        	success = true;
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
        
        if(success)
        {
        	// Update local cache
        	this.getPlayerData(name);
        }
        
        return success;
    }
    
    // Group spawn
    public void sendToGroupSpawn(String group, Player p)
    {
    	// Check for spawn
    	if(!this.activeGroupIds.contains(group))
    	{
    		if(!this.getGroupData(group))
    		{
    			// No group spawn available, use global
    			return;
    		}
    	}
    	
    	// Teleport to home
    	p.teleportTo(this.groupSpawns.get(this.activeGroupIds.get(group)));
    }
    
    // Set group spawn
    public boolean setGroupSpawn(String group, Location l, String updatedBy)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        Boolean success = false;
		
		// Save to database
		try
        {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(db);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement("REPLACE INTO `groups` (id, name, x, y, z, r, p, updated, updated_by) VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?);");
			ps.setString(1, group);
			ps.setDouble(2, l.getX());
			ps.setDouble(3, l.getY());
			ps.setDouble(4, l.getZ());
			ps.setFloat(5, l.getYaw());
			ps.setFloat(6, l.getPitch());
			ps.setInt(7, (int)System.currentTimeMillis());
			ps.setString(8, updatedBy);
			ps.execute();
			conn.commit();
        	conn.close();
        	
        	success = true;
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
        
        if(success)
        {
        	// Update local cache
        	this.getGroupData(group);
        }
        
        return success;
    }
    
    // Get group spawn
    public Location getGroupSpawn(String group)
    {
    	// Check for spawn
    	if(!this.activeGroupIds.contains(group))
    	{
    		if(!this.getGroupData(group))
    		{
    			// Found spawn!
    			return this.groupSpawns.get(this.activeGroupIds.get(group));
    		}
    	}
    	
    	return null;
    }
    
    // Utility
    private boolean getPlayerData(String name)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Boolean success = false;
        Integer id = 0;
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	conn.setAutoCommit(false);
        	ps = conn.prepareStatement("SELECT * FROM `players` WHERE `name` = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            conn.commit();
             
             while (rs.next()) {
                 success = true;
                 this.activePlayerIds.put(name, id);
                 Location l = new Location(null, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("r"), rs.getFloat("p"));
                 this.homes.put(id, l);
             }
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
        
        return success;
    }
    
    private boolean getGroupData(String name)
    {
    	Connection conn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Boolean success = false;
        Integer id = 0;
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	conn.setAutoCommit(false);
        	ps = conn.prepareStatement("SELECT * FROM `groups` WHERE `name` = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            conn.commit();
             
             while (rs.next()) {
                 success = true;
                 this.activeGroupIds.put(name, id);
                 Location l = new Location(null, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("r"), rs.getFloat("p"));
                 this.groupSpawns.put(id, l);
             }
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
        
        return success;
    }
    
    public void importConfig()
    {
    	File cf = new File(directory+"/spawncontrol-players.properties");
    	
    	if(cf.exists())
    	{
    		// Attempt import
            BufferedReader reader = null;

            try
            {
                reader = new BufferedReader(new FileReader(cf));
                String text = null;

                // Read a line
                while ((text = reader.readLine()) != null)
                {
                	// Skip if comment
                	if(!text.startsWith("#"))
                	{
                		// Format: Timberjaw=-86.14281646837361\:75.0\:233.43342838872454\:168.00002\:17.40001
                		text = text.replaceAll("\\\\", "");
                		String[] parts = text.split("=");
                		String name = parts[0];
                		String[] coords = parts[1].split(":");
                		Location l = new Location(null,
                				Double.parseDouble(coords[0]),
                				Double.parseDouble(coords[1]),
                				Double.parseDouble(coords[2]),
                				Float.parseFloat(coords[3]),
                				Float.parseFloat(coords[4]));
                		
                		// Set home
                		this.setHome(name, l, "ConfigImport");
                		
                		log.info("[SpawnControl] Found home for '"+name+"' at: "+l.getX()+","+l.getY()+","+l.getZ()+","+l.getYaw()+","+l.getPitch());
                	}
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
    	}
    }
    
    public void importGroupConfig()
    {
    	File cf = new File(directory+"/spawncontrol-groups.properties");
    	
    	if(cf.exists())
    	{
    		// Attempt import
            BufferedReader reader = null;

            try
            {
                reader = new BufferedReader(new FileReader(cf));
                String text = null;

                // Read a line
                while ((text = reader.readLine()) != null)
                {
                	// Skip if comment
                	if(!text.startsWith("#"))
                	{
                		// Format: admins=-56.50158762045817:12.0:265.4291449731157
                		text = text.replaceAll("\\\\", "");
                		String[] parts = text.split("=");
                		String name = parts[0];
                		String[] coords = parts[1].split(":");
                		Location l = new Location(null,
                				Double.parseDouble(coords[0]),
                				Double.parseDouble(coords[1]),
                				Double.parseDouble(coords[2]),
                				0.0f,
                				0.0f);
                		
                		// Set home
                		this.setGroupSpawn(name, l, "ConfigImport");
                		
                		log.info("[SpawnControl] Found group spawn for '"+name+"' at: "+l.getX()+","+l.getY()+","+l.getZ()+","+l.getYaw()+","+l.getPitch());
                	}
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
    	}
    }
}
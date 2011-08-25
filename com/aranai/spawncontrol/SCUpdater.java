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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SCUpdater {
	private static SpawnControl plugin;
	
	public static void run(int startVersion, SpawnControl plugin)
	{
		SCUpdater.plugin = plugin;
		
		if(startVersion < 0) { startVersion = 0; }
		int newVersion = SpawnControl.SchemaVersion;
		
		// Start updater
		SpawnControl.log.info("[SpawnControl] Schema has changed. Updating from " + startVersion + " to " + newVersion);
		
		for(int i = startVersion+1; i <= newVersion; ++i)
		{
			if(SCUpdater.update(i))
			{
				// Success
				SCUpdater.plugin.setSetting("schema_version", i, "SCUpdater");
			}
			else
			{
				SpawnControl.log.warning("[SpawnControl] Schema update " + i + "->" + newVersion + " failed. Stopping.");
				break;
			}
		}
	}
	
	private static boolean update(int v)
	{
		SpawnControl.log.info("[SpawnControl] Running schema update #"+v);
		
		switch(v)
		{
			case 1:
				/*
				 * Schema Update #1: Multi-World Support
				 * 1. Drop playerIndex index
				 * 2. Drop groupIndex index
				 * 3. Add column 'world' after column 'name' in table 'players'
				 * 4. Add index playerIndex(name,world) on table 'players'
				 * 5. Add column 'world' after column 'name' in table 'groups'
				 * 6. Add index groupIndex(name,world) on table 'groups'
				 */
				
				String worldName = SCUpdater.plugin.getServer().getWorlds().get(0).getName();
				
				try {
					Class.forName("org.sqlite.JDBC");
					Connection conn = DriverManager.getConnection(SpawnControl.db);
					Statement st;
					
					conn.setAutoCommit(false);
	                st = conn.createStatement();
	                
	                // Drop playerIndex index
	                st.execute("DROP INDEX playerIndex;");
	                
	                // Backup player table
	                st.execute("CREATE TEMPORARY TABLE players_backup(id,name,world,x,y,z,r,p,updated,updated_by);");
	                st.execute("INSERT INTO players_backup SELECT id,name,'"+worldName+"',x,y,z,r,p,updated,updated_by FROM players;");
	                
	                // Drop player table
	                st.execute("DROP TABLE players;");
	                
	                // Create new player table
	                st.execute(SpawnControl.SQLCreatePlayersTable);
	                
	                // Add player index
	                st.execute(SpawnControl.SQLCreatePlayersIndex);
	                
	                // Import saved data
	                st.execute("INSERT INTO players SELECT * FROM players_backup;");

	                // Drop backup table
	                st.execute("DROP TABLE players_backup;");
	                
	                // Drop groupIndex index
	                st.execute("DROP INDEX groupIndex;");
	                
	                // Backup group table
	                st.execute("CREATE TEMPORARY TABLE groups_backup(id,name,world,x,y,z,r,p,updated,updated_by);");
	                st.execute("INSERT INTO groups_backup SELECT id,name,'"+worldName+"',x,y,z,r,p,updated,updated_by FROM groups;");
	                
	                // Drop group table
	                st.execute("DROP TABLE groups;");
	                
	                // Create new group table
	                st.execute(SpawnControl.SQLCreateGroupsTable);
	                
	                // Add group index
	                st.execute(SpawnControl.SQLCreateGroupsIndex);
	                
	                // Import saved data
	                st.execute("INSERT INTO groups SELECT * FROM groups_backup;");
	                
	                // Drop backup table
	                st.execute("DROP TABLE groups_backup;");
	                
	                // Commit changes
	                conn.commit();
					
					return true;
				}
				catch (ClassNotFoundException e) { e.printStackTrace(); }
				catch (SQLException e) { e.printStackTrace(); }
				finally { try { if(SCUpdater.plugin.conn != null) { SCUpdater.plugin.conn.close(); } } catch(Exception e) { e.printStackTrace(); } }
			break;
			default:
				/*
				 * No schema updates for this version
				 */
				SpawnControl.log.info("[SpawnControl] No schema updates for #"+v+" [OK]");
				return true;
		}
		
		return false;
	}
}

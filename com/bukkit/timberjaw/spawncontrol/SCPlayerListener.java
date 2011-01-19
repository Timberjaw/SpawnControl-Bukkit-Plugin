package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.event.player.PlayerChatEvent;
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
    	System.out.println("Command: " + e.getMessage());
    }
}
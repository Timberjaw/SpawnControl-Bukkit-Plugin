package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.Location;
import org.bukkit.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author Timberjaw
 */
public class SCPlayerListener extends PlayerListener {
    private final SpawnControl plugin;

    public SCPlayerListener(SpawnControl instance) {
        plugin = instance;
    }

    //Insert Player related code here
}
package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.event.block.BlockListener;

/**
 * SpawnControl block listener
 * @author Timberjaw
 */
public class SCBlockListener extends BlockListener {
    private final SpawnControl plugin;

    public SCBlockListener(final SpawnControl plugin) {
        this.plugin = plugin;
    }

    //put all Block related code here
}
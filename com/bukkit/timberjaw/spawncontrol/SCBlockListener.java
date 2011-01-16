package com.bukkit.timberjaw.spawncontrol;

import org.bukkit.Block;
import org.bukkit.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

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
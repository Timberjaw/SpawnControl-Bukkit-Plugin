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

package com.aranai.integration.permissions;

import com.aranai.integration.IPermissions;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * BukkitPermissions is supported by the CraftBukkit Recommended Build #1000+ ONLY
 */
public class BukkitPermissions extends SuperPermsPermissions implements IPermissions {

    /**
     * The PermissionsBukkit handler
     */
    private PermissionsPlugin handler = null;

    public BukkitPermissions() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PermissionsBukkit");

        if (plugin == null) {
            return;
        }

        handler = (PermissionsPlugin) plugin;
    }

    @Override
    public boolean isActive() {
        return handler != null;
    }

    @Override
    public List<String> getGroups(Player player) {
        List<String> groups = super.getGroups(player);
        if (handler == null) {
            return groups;
        }

        List<Group> found = handler.getGroups(player.getName());

        if (found.size() == 0) {
            return groups;
        }

        // add in the groups
        for (Group group : found) {
            groups.add(group.getName());
        }

        return groups;
    }

}

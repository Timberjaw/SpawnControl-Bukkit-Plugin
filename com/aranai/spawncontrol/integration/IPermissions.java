/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aranai.spawncontrol.integration;

import org.bukkit.entity.Player;

import java.util.List;

public interface IPermissions {

    /**
     * @return true if permission handling is supported
     */
    public boolean isActive();

    /**
     * Check if a player has the specified permission node.
     *
     * @param player
     * @param node
     * @return
     */
    public boolean permission(Player player, String node);

    /**
     * Get the groups a player belongs to
     *
     * @param player
     * @return
     */
    public List<String> getGroups(Player player);

}

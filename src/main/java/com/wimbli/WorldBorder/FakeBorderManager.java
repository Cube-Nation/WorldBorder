package com.wimbli.WorldBorder;

import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FakeBorderManager {

    private static FakeBorderManager instance;

    public static FakeBorderManager instance() {
        return instance;
    }

    private final WorldBorder plugin;

    private final HashMap<String, HashMap<Player, BorderCorner>> borderCache = new HashMap<>();

    public FakeBorderManager(WorldBorder plugin) {
        instance = this;
        this.plugin = plugin;
    }

    /**
     * Used to update the visible WorldBorder according to the players position.
     * @param player the player
     */
    public void handleMove(Player player)
    {
        BorderData border = Config.Border(player.getWorld().getName());
        if (border == null || (border.getShape() == null && Config.ShapeRound()) || (border.getShape() != null && border.getShape())) {
            return;
        }

        if (Config.isPlayerBypassing(player.getUniqueId()) || player.hasPermission("worldborder.allowbypass")) {
            if (isCached(player)) {
                clearCache(player);
                removeFakeWorldBorder(player);
            }
            return;
        }

        WorldBorderApi api = plugin.getWorldBorderApi();
        if (border.getRadiusX() == border.getRadiusZ()) {
            // Square border
            if (isCached(player)) {
                return;
            }
            int size = border.getRadiusX() * 2;
            Location origin = new Location(player.getWorld(), border.getX(), 0, border.getZ());
            api.setBorder(player, size, origin);
            setCache(player);
        } else {
            // Rectangular border
            BorderCorner corner;
            if (player.getLocation().getZ() < border.getZ()) {
                if (player.getLocation().getX() < border.getX()) {
                    corner = BorderCorner.NORTH_WEST;
                } else {
                    corner = BorderCorner.NORTH_EAST;
                }
            } else {
                if (player.getLocation().getX() < border.getX()) {
                    corner = BorderCorner.SOUTH_WEST;
                } else {
                    corner = BorderCorner.SOUTH_EAST;
                }
            }
            if (isCached(player, corner)) {
                return;
            }

            int size;
            double originX = border.getX();
            double originZ = border.getZ();
            if (corner == BorderCorner.NORTH_EAST || corner == BorderCorner.NORTH_WEST) {
                originZ += 512;
            } else {
                originZ -= 512;
            }
            if (corner == BorderCorner.NORTH_WEST || corner == BorderCorner.SOUTH_WEST) {
                originX += 512;
            } else {
                originX -= 512;
            }

            if (border.getRadiusX() > border.getRadiusZ()) {
                 // X is long side
                double diff = border.getRadiusX() - border.getRadiusZ();
                if (corner == BorderCorner.SOUTH_WEST || corner == BorderCorner.SOUTH_EAST) {
                    diff = -diff;
                }
                originZ += diff;
                size = border.getRadiusX() * 2 + 1024;
            } else {
                // Z is long side
                double diff = border.getRadiusZ() - border.getRadiusX();
                if (corner == BorderCorner.NORTH_EAST || corner == BorderCorner.SOUTH_EAST) {
                    diff = -diff;
                }
                originX += diff;
                size = border.getRadiusZ() * 2 + 1024;
            }

            Location origin = new Location(player.getWorld(), originX, 0, originZ);
            api.setBorder(player, size, origin);
            setCache(player, corner);
        }
    }

    /**
     * Checks whether a player already sees a fake border.
     * @param player the player
     * @param corner the corner
     * @return true if the player sees a fake border
     */
    public boolean isCached(Player player, BorderCorner corner)
    {
        return borderCache.computeIfAbsent(player.getWorld().getName(), key -> new HashMap<>()).get(player) == corner;
    }

    /**
     * Checks whether a player already sees a fake border.
     * @param player the player
     * @return true if the player sees a fake border
     */
    public boolean isCached(Player player)
    {
        return borderCache.computeIfAbsent(player.getWorld().getName(), key -> new HashMap<>()).containsKey(player);
    }

    /**
     * Sets the cached corner for a player.
     * @param player the player
     * @param corner the corner
     */
    public void setCache(Player player, BorderCorner corner)
    {
        borderCache.computeIfAbsent(player.getWorld().getName(), key -> new HashMap<>()).put(player, corner);
    }

    /**
     * Sets the world cache for a player.
     * @param player the player
     */
    public void setCache(Player player)
    {
        borderCache.computeIfAbsent(player.getWorld().getName(), key -> new HashMap<>()).put(player, null);
    }

    /**
     * Clears the world cache.
     * @param world the world
     */
    public void clearCache(String world)
    {
        borderCache.computeIfAbsent(world, key -> new HashMap<>()).keySet().forEach(this::removeFakeWorldBorder);
        borderCache.remove(world);
    }

    /**
     * Clears the world cache.
     * @param player the player
     */
    public void clearCache(Player player)
    {
        borderCache.values().forEach(d -> d.remove(player));
    }

    /**
     * Clears the cache.
     */
    public void clearCache()
    {
        borderCache.values().forEach(map -> map.keySet().forEach(this::removeFakeWorldBorder));
        borderCache.clear();
    }

    /**
     * Clears the cache.
     * @param player the player
     */
    private void removeFakeWorldBorder(Player player)
    {
        WorldBorderApi api = plugin.getWorldBorderApi();
        api.resetWorldBorderToGlobal(player);
    }
}

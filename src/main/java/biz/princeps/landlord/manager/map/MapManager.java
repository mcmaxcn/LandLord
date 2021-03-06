package biz.princeps.landlord.manager.map;

import biz.princeps.landlord.Landlord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;

/**
 * File created by jcdesimp on 3/10/14. updated by SpatiumPrinceps on 19/07/17
 */
public class MapManager implements Listener {

    private HashMap<String, LandMap> mapList;

    private Landlord plugin;

    public MapManager() {
        this.mapList = new HashMap<>();
        this.plugin = Landlord.getInstance();
    }


    private void addMap(LandMap m) {
        mapList.put(m.getMapViewer().getName(), m);
    }

    public void toggleMap(Player p) {
        if (mapList.containsKey(p.getName())) {
            remMap(p.getName());
        } else {
            addMap(new LandMap(p, this.plugin));
        }
    }

    public void remMap(String pName) {

        if (mapList.containsKey(pName)) {
            LandMap curr = mapList.get(pName);
            curr.removeMap();
            mapList.remove(pName);
        }
    }

    public void removeAllMaps() {
        for (String k : mapList.keySet()) {
            mapList.get(k).removeMap();
        }
        mapList.clear();
    }

    public void updateAll() {
        for (String k : mapList.keySet()) {
            mapList.get(k).updateMap();
        }
    }


    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        if (mapList.containsKey(event.getPlayer().getName())) {
            remMap(event.getPlayer().getName());
        }

    }

    @EventHandler
    public void playerWorldChange(PlayerChangedWorldEvent event) {
        if (mapList.containsKey(event.getPlayer().getName())) {
            remMap(event.getPlayer().getName());
        }
    }


    /**
     * Update map when player teleports
     *
     * @param event that was triggered
     */
    @EventHandler
    public void playerTeleportKeepMap(PlayerTeleportEvent event) {
        final Player p = event.getPlayer();

        if (mapList.containsKey(event.getPlayer().getName())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Landlord.getInstance(), () -> {
                if (mapList.containsKey(p.getName())) {
                    mapList.get(p.getName()).updateMap();
                }
            }, 15L);
        }
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        final Player p = event.getPlayer();
        if (mapList.containsKey(event.getPlayer().getName())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Landlord.getInstance(), () -> {
                if (mapList.containsKey(p.getName())) {
                    mapList.get(p.getName()).updateMap();
                }
            }, 15L);
        }
    }


}
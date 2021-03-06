package biz.princeps.landlord.commands.admin;

import biz.princeps.landlord.commands.LandlordCommand;
import biz.princeps.landlord.util.UUIDFetcher;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by spatium on 19.07.17.
 */
public class Clear extends LandlordCommand {

    public void onClearWorld(Player player, String target) {
        if (this.worldDisabled(player)) {
            player.sendMessage(lm.getString("Disabled-World"));
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                World world = player.getWorld();
                RegionManager regionManager = plugin.getWgHandler().getWG().getRegionManager(world);

                Map<String, ProtectedRegion> regions = regionManager.getRegions();
                int count;

                // Clearing all regions in one world
                if (target == null) {
                    count = regions.size();
                    regions.keySet().forEach(regionManager::removeRegion);

                    player.sendMessage(lm.getString("Commands.ClearWorld.success")
                            .replace("%count%", String.valueOf(count))
                            .replace("%world%", world.getName()));

                    plugin.getMapManager().updateAll();
                } else {
                    // Clear only a specific player
                    UUIDFetcher.getUUID(target, uuid -> {

                        if (uuid == null) {
                            // Failure
                            player.sendMessage(lm.getString("Commands.ClearWorld.noPlayer")
                                    .replace("%players%", target));
                        } else {
                            // Success
                            Set<String> todelete = new HashSet<>();
                            regions.values().stream().filter(pr -> pr.getOwners().getUniqueIds().contains(uuid)).forEach(pr -> todelete.add(pr.getId()));


                            int amt = todelete.size();

                            todelete.forEach(regionManager::removeRegion);
                            player.sendMessage(lm.getString("Commands.ClearWorld.successPlayer")
                                    .replace("%count%", String.valueOf(amt))
                                    .replace("%player%", target));

                            plugin.getMapManager().updateAll();
                        }

                    });
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}

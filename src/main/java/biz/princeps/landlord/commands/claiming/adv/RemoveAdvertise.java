package biz.princeps.landlord.commands.claiming.adv;

import biz.princeps.landlord.commands.LandlordCommand;
import biz.princeps.landlord.persistent.Offers;
import biz.princeps.landlord.util.OwnedLand;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class RemoveAdvertise extends LandlordCommand {

    public void onAdvertise(Player player, String landname) {

        if (this.worldDisabled(player)) {
            player.sendMessage(lm.getString("Disabled-World"));
            return;
        }
        Chunk chunk = null;
        if (landname.equals("this")) {
            chunk = player.getWorld().getChunkAt(player.getLocation());
        } else {
            String[] split = landname.split("_");
            try {
                int x = Integer.valueOf(split[1]);
                int z = Integer.valueOf(split[2]);
                chunk = Bukkit.getWorld(split[0]).getChunkAt(x, z);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        OwnedLand pr = plugin.getWgHandler().getRegion(chunk);

        if (pr == null) {
            player.sendMessage(lm.getString("Commands.Advertise.notOwnFreeLand"));
            return;
        }

        if (!pr.isOwner(player.getUniqueId())) {
            player.sendMessage(lm.getString("Commands.Advertise.notOwn").replace("%owner%", pr.printOwners()));
            return;
        }

        Offers offer = plugin.getPlayerManager().getOffer(pr.getName());
        if (offer == null) {
            player.sendMessage(lm.getString("Commands.RemoveAdvertise.noAdvertise")
                    .replace("%landname%", pr.getName()));
        } else {
            plugin.getPlayerManager().removeOffer(offer.getLandname());
            player.sendMessage(lm.getString("Commands.RemoveAdvertise.success")
                    .replace("%landname%", landname));
        }
    }
}

package biz.princeps.landlord.commands;

import biz.princeps.landlord.Landlord;
import biz.princeps.landlord.commands.admin.AdminTeleport;
import biz.princeps.landlord.commands.admin.Clear;
import biz.princeps.landlord.commands.admin.GiveClaims;
import biz.princeps.landlord.commands.admin.Update;
import biz.princeps.landlord.commands.claiming.*;
import biz.princeps.landlord.commands.claiming.adv.Advertise;
import biz.princeps.landlord.commands.friends.Addfriend;
import biz.princeps.landlord.commands.friends.AddfriendAll;
import biz.princeps.landlord.commands.friends.Unfriend;
import biz.princeps.landlord.commands.friends.UnfriendAll;
import biz.princeps.landlord.commands.homes.Home;
import biz.princeps.landlord.commands.homes.SetHome;
import biz.princeps.landlord.commands.management.*;
import biz.princeps.landlord.manager.LangManager;
import biz.princeps.landlord.util.UUIDFetcher;
import biz.princeps.lib.PrincepsLib;
import biz.princeps.lib.chat.ChatAPI;
import biz.princeps.lib.chat.MultiPagedMessage;
import biz.princeps.lib.storage.AbstractDatabase;
import biz.princeps.lib.storage.MySQL;
import biz.princeps.lib.storage.SQLite;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by spatium on 16.07.17.
 */
@CommandAlias("ll|land|landlord|gs")
public class Landlordbase extends BaseCommand {

    private Map<String, LandlordCommand> subcommands;

    public Landlordbase() {
        subcommands = new HashMap<>();
        subcommands.put("claim", new Claim());
        subcommands.put("info", new Info());
        subcommands.put("unclaim", new Unclaim());
        subcommands.put("unclaimall", new UnclaimAll());
        subcommands.put("addfriend", new Addfriend());
        subcommands.put("unfriend", new Unfriend());
        subcommands.put("addfriendall", new AddfriendAll());
        subcommands.put("unfriendall", new UnfriendAll());
        subcommands.put("listlands", new ListLands());
        subcommands.put("landmap", new LandMap());
        subcommands.put("clearworld", new Clear());
        subcommands.put("manage", new Manage());
        subcommands.put("manageall", new ManageAll());
        subcommands.put("shop", new Shop());
        subcommands.put("claims", new Claims());
        subcommands.put("sethome", new SetHome());
        subcommands.put("home", new Home());
        subcommands.put("giveclaims", new GiveClaims());
        subcommands.put("update", new Update());
        subcommands.put("advertise", new Advertise());
        subcommands.put("borders", new Borders());
        subcommands.put("admintp", new AdminTeleport());
        subcommands.put("item", new LLItem());
    }

    @Default
    @UnknownHandler
    @Subcommand("help")
    @CommandPermission("landlord.use")
    public void onDefault(Player sender, String[] args) {
        LangManager lm = Landlord.getInstance().getLangManager();
        List<String> playersList = lm.getStringList("Commands.Help.players");
        List<String> adminList = lm.getStringList("Commands.Help.admins");

        int perSite = Landlord.getInstance().getConfig().getInt("HelpCommandPerSite");

        String[] argsN = new String[1];
        if (args.length == 1) {
            argsN[0] = (args[0] == null ? "0" : args[0]);
        }

        List<String> toDisplay = new ArrayList<>();
        if (sender.hasPermission("landlord.admin.help"))
            toDisplay.addAll(adminList);
        toDisplay.addAll(playersList);

        // System.out.println(toDisplay.size());

        MultiPagedMessage msg = ChatAPI.createMultiPagedMessge()
                .setElements(toDisplay)
                .setPerSite(perSite)
                .setHeaderString(lm.getRawString("Commands.Help.header"))
                .setNextString(lm.getRawString("Commands.Help.next"))
                .setPreviousString(lm.getRawString("Commands.Help.previous"))
                .setCommand("ll help", argsN).build();
        sender.spigot().sendMessage(msg.create());
    }

    @Subcommand("claim|buy|cl")
    @CommandAlias("claim")
    @Syntax("land claim - Claims the land you are currently standing on")
    @CommandPermission("landlord.player.own")
    public void onClaim(Player player) {
        ((Claim) subcommands.get("claim")).onClaim(player);
    }

    @Subcommand("info|i")
    @CommandAlias("landi|landinfo")
    @CommandPermission("landlord.player.info")
    @Syntax("land info - Shows information about the land you are standing on")
    public void onInfo(Player player) {
        ((Info) subcommands.get("info")).onInfo(player);
    }

    @Subcommand("unclaim|sell")
    @Syntax("land sell - Unclaim the chunk you are standing on")
    @CommandPermission("landlord.player.own")
    public void onUnClaim(Player player, @Default("null") String landname) {
        ((Unclaim) subcommands.get("unclaim")).onUnclaim(player, landname);
    }

    @Subcommand("unclaimall|sellall")
    @Syntax("land unclaimall - Unclaim all lands you are owning")
    @CommandPermission("landlord.player.own")
    public void onUnClaimAll(Player player) {
        ((UnclaimAll) subcommands.get("unclaimall")).onUnclaim(player);
    }


    @Subcommand("addfriend|friendadd")
    @Syntax("land addfriend - Adds friends to your land")
    @CommandPermission("landlord.player.own")
    public void onAddFriend(Player player, String[] names) {
        ((Addfriend) subcommands.get("addfriend")).onAddfriend(player, names);
    }

    @Subcommand("unfriend|friendremove|frienddelete")
    @Syntax("land unfriend - removes a friend from your land")
    @CommandPermission("landlord.player.own")
    public void onUnFriend(Player player, String[] names) {
        ((Unfriend) subcommands.get("unfriend")).onUnfriend(player, names);
    }

    @Subcommand("addfriendall|friendall")
    @Syntax("land addfriend - Adds friends to all your land")
    @CommandPermission("landlord.player.own")
    public void onAddfriendAll(Player player, String[] names) {
        ((AddfriendAll) subcommands.get("addfriendall")).onAddfriend(player, names);
    }

    @Subcommand("unfriendall|removeallfriends")
    @Syntax("land unfriendall - unfriend someone on all your lands")
    @CommandPermission("landlord.player.own")
    public void onUnfriendAll(Player player, String names) {
        ((UnfriendAll) subcommands.get("unfriendall")).onUnfriendall(player, names);
    }

    @Subcommand("list")
    @CommandAlias("listlands|landlist")
    @Syntax("land list - lists all your lands")
    @CommandPermission("landlord.player.own")
    public void onLandList(Player player, @Optional String target, @Default("0") String page) {

        // Want to know own lands
        if (target == null) {
            ((ListLands) subcommands.get("listlands")).onListLands(player, player, Integer.parseInt(page));
        } else if (player.hasPermission("landlord.admin.list")) {
            // Other lands, need to lookup their names
            UUIDFetcher.getUUID(target, uuid -> {

                if (uuid == null) {
                    // Failure
                    player.sendMessage(Landlord.getInstance().getLangManager().getString("Commands.ListLands.noPlayer").replace("%player%", target));
                } else {
                    // Success
                    OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                    if (op != null)
                        ((ListLands) subcommands.get("listlands")).onListLands(player, op, Integer.parseInt(page));
                    else {
                        player.sendMessage(Landlord.getInstance().getLangManager().getString("Commands.ListLands.noPlayer").replace("%player%", target));
                    }
                }

            });
        }
    }

    @Subcommand("map")
    @CommandAlias("landmap")
    @Syntax("land map - toggles the landmap")
    @CommandPermission("landlord.player.map")
    public void onToggleLandMap(Player player) {
        ((LandMap) subcommands.get("landmap")).onToggleLandMap(player);
    }


    @Subcommand("clear|clearworld")
    @CommandAlias("clearworld")
    @Syntax("land clear - clearing")
    @CommandPermission("landlord.admin.clearworld")
    public void onClearWorld(Player player, @Default("null") String target) {
        ((Clear) subcommands.get("clearworld")).onClearWorld(player, target);
    }


    @Subcommand("manage|mgn")
    @Syntax("land manage - manages the land you are standing on")
    @CommandPermission("landlord.player.manage")
    public void onLandManage(Player player, @Default("null") String[] args) {
        ((Manage) subcommands.get("manage")).onManage(player, args);
    }

    @Subcommand("manageall|mall")
    @Syntax("land manage all - manages all your lands at the same time")
    @CommandPermission("landlord.player.manage")
    public void onLandManageAll(Player player) {
        ((ManageAll) subcommands.get("manageall")).onManageAll(player);
    }

    @Subcommand("update")
    @Syntax("<-r> - updates all lands in one world. Parameter -r forces to reset all lands to their default state")
    @CommandPermission("landlord.admin.update")
    public void onLandUpdate(@Default("null") String param) {

        if (param.equals("-r")) {
            ((Update) subcommands.get("update")).onResetLands(getCurrentCommandIssuer());
        } else {
            ((Update) subcommands.get("update")).onUpdateLands(getCurrentCommandIssuer());
        }
    }

    @Subcommand("shop")
    @CommandPermission("landlord.player.shop")
    public void onShop(Player player) {
        ((Shop) subcommands.get("shop")).onShop(player);
    }

    @Subcommand("reload|rl")
    @CommandPermission("landlord.admin.reload")
    public void onReload() {
        String msg = Landlord.getInstance().getLangManager().getString("Commands.Reload.success");
        CommandIssuer issuer = getCurrentCommandIssuer();

        issuer.sendMessage(ChatColor.RED + "Reloading is not recommended! Before reporting any bugs, please restart your server.");

        Landlord.getInstance().getPluginLoader().disablePlugin(Landlord.getInstance());
        Landlord.getInstance().getPluginLoader().enablePlugin(Landlord.getInstance());
        issuer.sendMessage(msg);
    }

    @Subcommand("claims")
    @CommandPermission("landlord.player.shop")
    public void onClaims(Player player) {
        ((Claims) subcommands.get("claims")).onClaims(player);
    }

    @Subcommand("sethome")
    @CommandPermission("landlord.player.home")
    public void onSetHome(Player player) {
        ((SetHome) subcommands.get("sethome")).onSetHome(player);
    }

    @Subcommand("home|h")
    @CommandPermission("landlord.player.home")
    public void onHome(Player player, @Default("own") String target) {
        ((Home) subcommands.get("home")).onHome(player, target);
    }

    @Subcommand("giveclaims|gcl")
    @CommandPermission("landlord.claims.give")
    public void onGiveClaims(String target, Double price, Integer amount) {
        ((GiveClaims) subcommands.get("giveclaims")).onGiveClaims(getCurrentCommandIssuer(), target, price, amount);
    }

    @Subcommand("advertise|adv")
    @CommandPermission("landlord.player.advertise")
    public void onAdvertise(Player player, String landlandname, Double price) {
        if (Landlord.getInstance().isVaultEnabled())
            ((Advertise) subcommands.get("advertise")).onAdvertise(player, landlandname, price);
    }

    @Subcommand("advertise|adv")
    @CommandPermission("landlord.player.advertise")
    public void onAdvertise1(Player player, Double price) {
        if (Landlord.getInstance().isVaultEnabled())
            ((Advertise) subcommands.get("advertise")).onAdvertise(player, "this", price);
    }

    @Subcommand("borders")
    @CommandPermission("landlord.player.borders")
    public void onToggleBorder(Player player) {
        ((Borders) subcommands.get("borders")).onToggleBorder(player);
    }

    @Subcommand("admintp|adminteleport")
    @CommandPermission("landlord.admin.admintp")
    @Syntax("<Name> - teleports to a land of the player")
    public void onAdminTp(Player player, String target) {
        ((AdminTeleport) subcommands.get("admintp")).onAdminTeleport(player, target);
    }


    @Subcommand("item")
    @CommandPermission("landlord.player.item")
    @Syntax("<Name> - name of the receiving player")
    public void onItem(CommandSender sender, @Optional String target) {
        ((LLItem) subcommands.get("item")).onItem(sender, target);
    }

    @Subcommand("migrate")
    public void onMigrate(String[] args) {
        if (getCurrentCommandIssuer().hasPermission("landlord.admin.manage")) {
            Logger logger = Landlord.getInstance().getLogger();

            if (args.length > 0) {

                if (args[0].equals("v1")) {
                    // SQLite based migration

                    SQLite sqLite = new SQLite(logger, Landlord.getInstance().getDataFolder() + "/Landlord.db") {
                    };

                    logger.info("Starting to migrate from v1 Ebean Database...");
                    migrate(sqLite, "ll_land", "owner_name", "world_name", "x", "z");
                }
                if (args[0].equals("v2")) {
                    if (args.length == 2) {
                        if (args[1].equals("sqlite")) {
                            // SQLite based migration
                            SQLite sqLite = new SQLite(logger, Landlord.getInstance().getDataFolder() + "/database.db") {
                            };

                            logger.info("Starting to migrate from v2-SQLite Database...");
                            migrate(sqLite, "ll_land", "owneruuid", "world", "x", "z");

                        } else if (args[1].equals("mysql")) {
                            // mysql based migration

                            logger.info("In your plugin folder a file called MySQL.yml has been generated. You need to enter the credentials of your former landlord database.");
                            FileConfiguration mysqlConfig = PrincepsLib.prepareDatabaseFile();
                            MySQL mySQL = new MySQL(Landlord.getInstance().getLogger(), mysqlConfig.getString("MySQL.Hostname"),
                                    mysqlConfig.getInt("MySQL.Port"),
                                    mysqlConfig.getString("MySQL.Database"),
                                    mysqlConfig.getString("MySQL.User"),
                                    mysqlConfig.getString("MySQL.Password")) {
                            };
                            logger.info("Starting to migrate from v2-MySQL Database...");
                            migrate(mySQL, "ll_land", "owneruuid", "world", "x", "z");
                        }
                    }
                }
            }
        }
    }

    class DataObject {
        UUID owner;
        String world;
        int x, z;

        DataObject(UUID owner, String world, int x, int z) {
            this.owner = owner;
            this.world = world;
            this.x = x;
            this.z = z;
        }
    }


    void migrate(AbstractDatabase db, String tablename, String ownerColumn, String worldColumn, String xColumn, String zColumn) {
        List<DataObject> objs = new ArrayList<>();

        db.executeQuery("SELECT * FROM " + tablename, res -> {

            try {
                while (res.next()) {
                    UUID owner = UUID.fromString(res.getString(ownerColumn));
                    String world = res.getString(worldColumn);
                    int x = res.getInt(xColumn);
                    int z = res.getInt(zColumn);

                    objs.add(new DataObject(owner, world, x, z));
                }
            } catch (SQLException e) {
                Landlord.getInstance().getLogger().warning("There was an error while trying to fetching original data: " + e);
            }
        });
        db.getLogger().info("Finished fetching data from old database. Size: " + objs.size() + " lands");
        db.getLogger().info("The next step will take around " + objs.size() / 20 / 60 + " minutes");

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter >= objs.size() - 1) {
                    db.getLogger().info("Finished migrating database. Migrated " + objs.size() + " lands!");
                    cancel();
                }

                DataObject next = objs.get(counter);
                World world1 = Bukkit.getWorld(next.world);
                if (world1 != null) {
                    Chunk chunk = world1.getChunkAt(next.x, next.z);
                    Landlord.getInstance().getWgHandler().claim(chunk, next.owner);
                }
                counter++;

                if (counter % 600 == 0)
                    db.getLogger().info("Processed " + counter + " lands already. " + (objs.size() - counter) / 20 / 60 + " minutes remaining!");
            }
        }.runTaskTimer(Landlord.getInstance(), 0, 1);
    }
}

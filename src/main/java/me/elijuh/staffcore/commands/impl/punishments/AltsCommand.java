package me.elijuh.staffcore.commands.impl.punishments;

import com.google.common.collect.Lists;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.PType;
import me.elijuh.staffcore.managers.DatabaseManager;
import me.elijuh.staffcore.utils.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AltsCommand extends SpigotCommand {
    DatabaseManager databaseManager;

    public AltsCommand() {
        super("alts", Lists.newArrayList("ipcheck", "accounts"), "core.alts");
        databaseManager = Core.i().getDatabaseManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length == 1) {
            if (databaseManager.hasData(args[0])) {
                List<String> alts = databaseManager.getAccounts(databaseManager.getIP(databaseManager.getUUID(args[0])));
                p.sendMessage(ChatUtil.color("&4&lStaff &8⏐ &7Showing accounts on &f" + args[0] + "'s &7IP:"));
                for (String alt : alts) {
                    p.sendMessage(ChatUtil.color("&7- &f" + alt +
                            (databaseManager.isPunished(alt, PType.BAN) ? " &7[&4Banned&7]" : "")));
                }
            } else {
                p.sendMessage(ChatUtil.color("&cThat player has never joined!"));
            }
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /alts <player>"));
        }
    }

    @Override
    public void onConsole(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (databaseManager.hasData(args[0])) {
                List<String> alts = databaseManager.getAccounts(databaseManager.getIP(databaseManager.getUUID(args[0])));
                sender.sendMessage(ChatUtil.color("&4&lStaff &8⏐ &7Listing accounts on &f" + ChatColor.stripColor(ChatUtil.color(databaseManager.getDisplay(args[0]))) + "'s &7IP:"));
                for (String alt : alts) {
                    sender.sendMessage(ChatUtil.color("&7- &f" + alt));
                }
            } else {
                sender.sendMessage(ChatUtil.color("&cThat player has never joined!"));
            }
        } else {
            sender.sendMessage(ChatUtil.color("&cUsage: /alts <player>"));
        }
    }
}

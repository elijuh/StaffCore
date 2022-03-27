package me.elijuh.staffcore.commands.impl.punishments;

import com.google.common.collect.Lists;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.PType;
import me.elijuh.staffcore.data.redis.PunishmentInfo;
import me.elijuh.staffcore.managers.DatabaseManager;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IPUnBanCommand extends SpigotCommand {
    DatabaseManager databaseManager;

    public IPUnBanCommand() {
        super("unbanip", Lists.newArrayList("unblacklist"), "core.unbanip");
        databaseManager = Core.i().getDatabaseManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length > 0) {
            String executorDisplay = PlayerUtil.getColoredName(p);
            String punishedDisplay = databaseManager.getDisplay(args[0]);

            if (!databaseManager.isIPBanned(databaseManager.getIP(databaseManager.getUUID(args[0])))) {
                p.sendMessage(ChatUtil.color("&cThat player is not ip banned!"));
                return;
            }

            String reason;

            if (args.length > 1) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    builder.append(args[i]).append(" ");
                }
                reason = builder.toString().trim();
            } else reason = "None";

            PunishmentInfo info = new PunishmentInfo(PType.IPBAN, true, -1, reason, p.getName(),
                    args[0], executorDisplay, punishedDisplay);

            databaseManager.remove(databaseManager.getUUID(args[0]), info);
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /ipunban <player> [reason...]"));
        }
    }

    @Override
    public void onConsole(CommandSender sender, String[] args) {
        if (args.length > 0) {
            String executorDisplay = ChatUtil.color("&4&lConsole");
            String punishedDisplay = databaseManager.getDisplay(args[0]);

            if (!databaseManager.isIPBanned(databaseManager.getIP(databaseManager.getUUID(args[0])))) {
                sender.sendMessage(ChatUtil.color("&cThat player is not ip banned!"));
                return;
            }

            String reason;

            if (args.length > 1) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    builder.append(args[i]).append(" ");
                }
                reason = builder.toString().trim();
            } else reason = "None";

            PunishmentInfo info = new PunishmentInfo(PType.IPBAN, true, -1, reason, "Console",
                    args[0], executorDisplay, punishedDisplay);

            databaseManager.remove(databaseManager.getUUID(args[0]), info);
        } else {
            sender.sendMessage(ChatUtil.color("&cUsage: /ipunban <player> [reason...]"));
        }
    }
}

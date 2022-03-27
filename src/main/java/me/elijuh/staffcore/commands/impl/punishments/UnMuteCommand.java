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

public class UnMuteCommand extends SpigotCommand {
    DatabaseManager databaseManager;

    public UnMuteCommand() {
        super("unmute", Lists.newArrayList(), "core.unmute");
        databaseManager = Core.i().getDatabaseManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length > 0) {
            if (databaseManager.isPunished(args[0], PType.MUTE)) {
                String reason;

                if (args.length > 1) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        builder.append(args[i]).append(" ");
                    }
                    reason = builder.toString().trim();
                } else reason = "None";

                String display = PlayerUtil.getColoredName(p);

                databaseManager.remove(databaseManager.getUUID(args[0]), new PunishmentInfo(
                        PType.MUTE, true, -1, reason, p.getName(), args[0], display, databaseManager.getDisplay(args[0])));
            } else {
                p.sendMessage(ChatUtil.color("&cThat player is not muted!"));
            }
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /unmute <player> <reason...>"));
        }
    }

    @Override
    public void onConsole(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (databaseManager.isPunished(args[0], PType.MUTE)) {
                String reason;

                if (args.length > 1) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        builder.append(args[i]).append(" ");
                    }
                    reason = builder.toString().trim();
                } else reason = "None";

                String display = ChatUtil.color("&4&lConsole");

                databaseManager.remove(databaseManager.getUUID(args[0]), new PunishmentInfo(
                        PType.MUTE, true, -1, reason, "Console", args[0], display, databaseManager.getDisplay(args[0])));
            } else {
                sender.sendMessage(ChatUtil.color("&cThat player is not muted!"));
            }
        } else {
            sender.sendMessage(ChatUtil.color("&cUsage: /unmute <player> [reason...]"));
        }
    }
}

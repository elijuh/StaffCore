package me.elijuh.staffcore.commands.impl.punishments;

import com.google.common.collect.Lists;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.PType;
import me.elijuh.staffcore.data.redis.PunishmentInfo;
import me.elijuh.staffcore.managers.DatabaseManager;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.MathUtil;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TempMuteCommand extends SpigotCommand {
    DatabaseManager databaseManager;

    public TempMuteCommand() {
        super("tempmute", Lists.newArrayList(), "core.tempmute");
        databaseManager = Core.i().getDatabaseManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length > 2) {
            if (!databaseManager.hasData(args[0])) {
                p.sendMessage(ChatUtil.color("&cThat player has never joined!"));
                return;
            }

            String executorDisplay = PlayerUtil.getColoredName(p);
            String punishedDisplay = databaseManager.getDisplay(args[0]);

            if (databaseManager.isPunished(args[0], PType.MUTE)) {
                p.sendMessage(ChatUtil.color("&c" + args[0] + " is already muted!"));
                return;
            }

            long duration;

            try {
                duration = MathUtil.parseDate(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(ChatUtil.color("&cInvalid time: " + args[1]));
                p.sendMessage(ChatUtil.color("&cUse /temptimeformat to see how to format durations."));
                return;
            }

            StringBuilder reason = new StringBuilder(args[2]);

            for (int i = 3; i < args.length; i++) {
                reason.append(" ").append(args[i]);
            }

            PunishmentInfo info = new PunishmentInfo(PType.MUTE, false, duration, reason.toString(), p.getName(),
                    args[0], executorDisplay, punishedDisplay);

            databaseManager.punish(info);
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /tempmute <player> <time> <reason...>"));
        }
    }

    @Override
    public void onConsole(CommandSender sender, String[] args) {
        if (args.length > 2) {
            if (!databaseManager.hasData(args[0])) {
                sender.sendMessage(ChatUtil.color("&cThat player has never joined!"));
                return;
            }

            String executorDisplay = ChatUtil.color("&4&lConsole");
            String punishedDisplay = databaseManager.getDisplay(args[0]);

            if (databaseManager.isPunished(args[0], PType.MUTE)) {
                sender.sendMessage(ChatUtil.color("&c" + args[0] + " is already muted!"));
                return;
            }

            long duration;

            try {
                duration = MathUtil.parseDate(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtil.color("&cPlease provide a valid date! example: 30d"));
                return;
            }

            StringBuilder reason = new StringBuilder(args[2]);

            for (int i = 3; i < args.length; i++) {
                reason.append(" ").append(args[i]);
            }

            PunishmentInfo info = new PunishmentInfo(PType.MUTE, false, duration, reason.toString(), "Console",
                    args[0], executorDisplay, punishedDisplay);

            databaseManager.punish(info);
        } else {
            sender.sendMessage(ChatUtil.color("&cUsage: /tempmute <player> <time> <reason...>"));
        }
    }
}


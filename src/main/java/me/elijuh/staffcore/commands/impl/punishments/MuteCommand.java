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

public class MuteCommand extends SpigotCommand {
    private final DatabaseManager databaseManager;

    public MuteCommand() {
        super("mute", Lists.newArrayList(), "core.mute");
        databaseManager = Core.i().getDatabaseManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length > 1) {
            if (!databaseManager.hasData(args[0])) {
                p.sendMessage(ChatUtil.color("&cThat player has never joined!"));
                return;
            }

            String executorDisplay = PlayerUtil.getColoredName(p);
            String punishedDisplay = databaseManager.getDisplay(args[0]);

            if (databaseManager.isPunished(args[0], PType.MUTE)) {
                p.sendMessage(ChatUtil.color("&cTarget is already muted!"));
                return;
            }

            StringBuilder reason = new StringBuilder(args[1]);

            for (int i = 2; i < args.length; i++) {
                reason.append(" ").append(args[i]);
            }

            PunishmentInfo info = new PunishmentInfo(PType.MUTE, false, -1, reason.toString(), p.getName(),
                    args[0], executorDisplay, punishedDisplay);

            databaseManager.punish(info);
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /mute <player> <reason...>"));
        }
    }

    @Override
    public void onConsole(CommandSender sender, String[] args) {
        if (args.length > 1) {
            if (!databaseManager.hasData(args[0])) {
                sender.sendMessage(ChatUtil.color("&cThat player has never joined!"));
                return;
            }

            String executorDisplay = ChatUtil.color("&4&lConsole");
            String punishedDisplay = databaseManager.getDisplay(args[0]);

            if (databaseManager.isPunished(args[0], PType.MUTE)) {
                sender.sendMessage(ChatUtil.color("&cTarget is already muted!"));
                return;
            }

            StringBuilder reason = new StringBuilder(args[1]);

            for (int i = 2; i < args.length; i++) {
                reason.append(" ").append(args[i]);
            }

            PunishmentInfo info = new PunishmentInfo(PType.MUTE, false, -1, reason.toString(), "Console",
                    args[0], executorDisplay, punishedDisplay);

            databaseManager.punish(info);
        } else {
            sender.sendMessage(ChatUtil.color("&cUsage: /mute <player> <reason...>"));
        }
    }
}

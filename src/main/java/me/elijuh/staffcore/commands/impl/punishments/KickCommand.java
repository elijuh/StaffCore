package me.elijuh.staffcore.commands.impl.punishments;

import com.google.common.collect.Lists;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.PType;
import me.elijuh.staffcore.data.redis.PunishmentInfo;
import me.elijuh.staffcore.managers.RedisManager;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KickCommand extends SpigotCommand {
    private final RedisManager redisManager;

    public KickCommand() {
        super("kick", Lists.newArrayList(), "core.kick");
        redisManager = Core.i().getRedisManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length > 1) {
            Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                p.sendMessage(ChatUtil.color("&cThat player is not online!"));
                return;
            }

            String executorDisplay = PlayerUtil.getColoredName(p);
            String punishedDisplay = PlayerUtil.getColoredName(target);

            StringBuilder reason = new StringBuilder(args[1]);

            for (int i = 2; i < args.length; i++) {
                reason.append(" ").append(args[i]);
            }

            PunishmentInfo info = new PunishmentInfo(PType.KICK, false, -1, reason.toString(), p.getName(), target.getName(), executorDisplay, punishedDisplay);
            redisManager.getPubSubSender().async().publish("PUNISHMENT", redisManager.getGSON().toJson(info));

        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /kick <player> <reason...>"));
        }
    }

    @Override
    public void onConsole(CommandSender sender, String[] args) {
        if (args.length > 1) {
            Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                sender.sendMessage(ChatUtil.color("&cThat player is not online!"));
                return;
            }

            String executorDisplay = ChatUtil.color("&4&lConsole");
            String punishedDisplay = PlayerUtil.getColoredName(target);

            StringBuilder reason = new StringBuilder(args[1]);

            for (int i = 2; i < args.length; i++) {
                reason.append(" ").append(args[i]);
            }

            PunishmentInfo info = new PunishmentInfo(PType.KICK, false, -1, reason.toString(), "Console", target.getName(), executorDisplay, punishedDisplay);
            redisManager.getPubSubSender().async().publish("PUNISHMENT", redisManager.getGSON().toJson(info));

        } else {
            sender.sendMessage(ChatUtil.color("&cUsage: /kick <player> <reason...>"));
        }
    }
}

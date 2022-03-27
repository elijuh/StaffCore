package me.elijuh.staffcore.commands.impl.other;

import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.data.redis.ReportInfo;
import me.elijuh.staffcore.managers.RedisManager;
import me.elijuh.staffcore.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ReportCommand extends SpigotCommand {
    RedisManager redisManager;

    public ReportCommand() {
        super("report");
        redisManager = Core.i().getRedisManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User user = Core.i().getUser(p.getName());
        if (user.getData().containsKey("last-report")) {
            long since = (System.currentTimeMillis() - (long) user.getData("last-report")) / 1000;
            if (since < 30) {
                user.msg("&cYou are on a cooldown for " + (30 - since) + " more seconds.");
                return;
            }
        }

        if (args.length > 1) {
            Player reported = Bukkit.getPlayerExact(args[0]);
            if (reported == null) {
                p.sendMessage(ChatUtil.color("&cThat player is not online!"));
            } else {
                StringBuilder reason = new StringBuilder((args[1]));
                for (int i = 2; i < args.length; i++) {
                    reason.append(" ").append(args[i]);
                }
                user.getData().put("last-report", System.currentTimeMillis());
                p.sendMessage(ChatUtil.color("&aYour report has been sent, staff have been notified."));
                p.sendMessage(ChatUtil.color(" "));
                redisManager.getPubSubSender().async().publish("REPORT",
                        redisManager.getGSON().toJson(new ReportInfo(Core.i().getId(), p.getName(), reported.getName(), reason.toString())));
            }
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /report <player> <reason...>"));
        }
    }
}

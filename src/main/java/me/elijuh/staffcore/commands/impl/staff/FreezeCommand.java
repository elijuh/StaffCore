package me.elijuh.staffcore.commands.impl.staff;

import com.google.common.collect.ImmutableList;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FreezeCommand extends SpigotCommand {
    public FreezeCommand() {
        super("freeze", ImmutableList.of("ss"), "core.freeze");
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length == 1) {
            User user = Core.i().getUser(args[0]);
            if (user != null) {
                user.setFrozen(!user.isFrozen());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (user.isFrozen()) {
                            user.msg("");
                            user.msg("&8[&4&l!&8] &4&lDO NOT &clog out");
                            user.msg("&8[&4&l!&8] &eYou have been frozen by a staff member");
                            user.msg("&8[&4&l!&8] &eplease join &6" + Core.i().getConfig().getString("discord"));
                            user.msg("");
                        } else {
                            cancel();
                        }
                    }
                }.runTaskTimerAsynchronously(Core.i(), 0L, 200L);
                p.sendMessage(ChatUtil.color("&7You have &c" + (user.isFrozen() ? "frozen &7" : "unfrozen &7") + user.getName() + "."));
            } else {
                p.sendMessage(ChatUtil.color("&cThat player is not online!"));
            }
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /freeze <player>"));
        }
    }
}

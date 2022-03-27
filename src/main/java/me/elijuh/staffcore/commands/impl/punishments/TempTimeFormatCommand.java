package me.elijuh.staffcore.commands.impl.punishments;

import com.google.common.collect.ImmutableList;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.utils.ChatUtil;
import org.bukkit.entity.Player;

import java.util.List;

public class TempTimeFormatCommand extends SpigotCommand {

    public TempTimeFormatCommand() {
        super("temptimeformat", ImmutableList.of(), "core.tempmute");
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        p.sendMessage(ChatUtil.color("&fExample format: &b14d &7(14 Days)"));
        p.sendMessage(ChatUtil.color("&8(&7Suffixes: &by = Year, M = Month, w = Week, d = Day, h = Hour, m = Minute, s = Second&8)"));
    }
}

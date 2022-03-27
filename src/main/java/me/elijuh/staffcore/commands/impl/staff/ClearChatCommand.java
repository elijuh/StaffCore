package me.elijuh.staffcore.commands.impl.staff;

import com.google.common.collect.ImmutableList;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ClearChatCommand extends SpigotCommand {

    public ClearChatCommand() {
        super("clearchat", ImmutableList.of(), "core.clearchat");
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        for (int i = 0; i < 500; i++) {
            Bukkit.broadcastMessage(i % 2 == 0 ? "" : " ");
        }

        Bukkit.broadcastMessage(ChatUtil.color("&aChat has been cleared by " + PlayerUtil.getColoredName(p) + "&a."));
    }
}

package me.elijuh.staffcore.commands.impl.staff;

import com.google.common.collect.ImmutableList;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.User;
import org.bukkit.entity.Player;

import java.util.List;

public class StaffChatCommand extends SpigotCommand {
    public StaffChatCommand() {
        super("staffchat", ImmutableList.of("sc"), "core.staff");
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User user = Core.i().getUser(p.getName());
        if (args.length > 0) {
            StringBuilder message = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                message.append(" ").append(args[i]);
            }

            user.sendStaffChat(message.toString());
        } else {
            user.setStaffchat(!user.isStaffchat());
            user.msg("&b&lStaff &8⏐ &7You are " + (user.isStaffchat() ? "&anow" : "&cno longer") + " &7talking in &fStaff Chat&7.");
        }
    }
}

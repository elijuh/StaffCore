package me.elijuh.staffcore;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.utils.ChatUtil;
import org.bukkit.entity.Player;

public class StaffExpansion extends PlaceholderExpansion {
    private static StaffExpansion instance;

    public StaffExpansion() {
        instance = this;
    }

    public String getIdentifier() {
        return "staff";
    }


    public String getAuthor() {
        return "elijuh";
    }

    public String getVersion() {
        return "1.0";
    }

    public String onPlaceholderRequest(Player player, String params) {
        User user = Core.i().getUser(player.getName());
        if (user != null) {
            switch (params.toLowerCase()) {
                case "mod":
                case "modmode":
                case "staffmode":
                case "mode": {
                    return ChatUtil.color(user.isStaffmode() ? "&aEnabled" : "&cDisabled");
                }
                case "v":
                case "vanish": {
                    return ChatUtil.color(user.isVanished() ? "&aEnabled" : "&cDisabled");
                }
            }
        }
        return "";
    }

    public static StaffExpansion getInstance() {
        return instance;
    }
}

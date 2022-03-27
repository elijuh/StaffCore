package me.elijuh.staffcore.commands.impl.staff;

import com.google.common.collect.Lists;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public class VanishCommand extends SpigotCommand {
    private final StaffModeCommand smc;

    public VanishCommand() {
        super("vanish", Lists.newArrayList("v"), "core.vanish");
        smc = StaffModeCommand.getInstance();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User user;

        if (args.length > 0 && p.hasPermission("core.vanish.others")) {
            user = Core.i().getUser(args[0]);
        } else {
            user = Core.i().getUser(p.getName());
        }

        if (user == null) {
            p.sendMessage(ChatUtil.color("&cThat player is not online!"));
            return;
        }

        if (user.isVanished()) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                if (!all.canSee(user.getPlayer())) {
                    all.showPlayer(user.getPlayer());
                }
            }
            user.setVanished(false);
            user.getPlayer().removeMetadata("vanish", Core.i());
        } else {
            for (Player all : Bukkit.getOnlinePlayers()) {
                if (!all.hasPermission("core.vanish")) {
                    all.hidePlayer(user.getPlayer());
                }
            }
            user.setVanished(true);
            user.getPlayer().setMetadata("vanish", new FixedMetadataValue(Core.i(), true));
        }

        for (int i = 0; i < 36; i++) {
            ItemStack item = user.getPlayer().getInventory().getItem(i);

            if (item == null) continue;

            if (item.isSimilar(smc.getVanish()) || item.isSimilar(smc.getUnvanish())) {
                user.getPlayer().getInventory().setItem(i, user.isVanished() ? smc.getUnvanish() : smc.getVanish());
            }
        }

        user.msg("&b&lStaff &8⏐ &fVanish &7has been " + (user.isVanished() ? "&aEnabled" : "&cDisabled") + "&7.");
    }
}

package me.elijuh.staffcore.commands.impl.staff;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

@Getter
public class StaffModeCommand extends SpigotCommand {
    private static StaffModeCommand instance;
    private final ItemStack compass = new ItemBuilder(Material.COMPASS).setName("&bCompass").build();
    private final ItemStack book = new ItemBuilder(Material.BOOK).setName("&bInspect").build();
    private final ItemStack freeze = new ItemBuilder(Material.ICE).setName("&bFreeze").build();
    private final ItemStack randomTeleport = new ItemBuilder(Material.WATCH).setName("&bRandom Teleport").build();
    private final ItemStack staff = new ItemBuilder(Material.NETHER_STAR).setName("&bOnline Staff").build();
    private final ItemStack vanish = new ItemBuilder(Material.INK_SACK).setDura(10).setName("&bBecome Invisible").build();
    private final ItemStack unvanish = new ItemBuilder(Material.INK_SACK).setDura(8).setName("&bBecome Visible").build();

    public StaffModeCommand() {
        super("staffmode", Lists.newArrayList("staff", "mod", "modmode"), "core.staffmode");
        instance = this;
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User user = Core.i().getUser(p.getName());

        if (user.isStaffmode()) {
            user.getPlayer().getInventory().setContents(user.getData("staffmode-inv"));
            user.getPlayer().getInventory().setArmorContents(user.getData("staffmode-inv-armor"));
            user.getPlayer().setAllowFlight(false);
            user.getPlayer().setFlying(false);
            user.setStaffmode(false);
            user.getPlayer().removeMetadata("staffmode", Core.i());
        } else {
            PlayerInventory inv = user.getPlayer().getInventory();
            user.getData().put("staffmode-inv", inv.getContents());
            user.getData().put("staffmode-inv-armor", inv.getArmorContents());
            for (int i = 0; i < 40; i++) {
                inv.setItem(i, null);
            }
            inv.setItem(0, compass);
            inv.setItem(1, book);
            inv.setItem(2, freeze);
            inv.setItem(4, randomTeleport);
            inv.setItem(7, staff);
            inv.setItem(8, user.isVanished() ? unvanish : vanish);
            user.getPlayer().setAllowFlight(true);
            user.getPlayer().setFlying(true);
            user.setStaffmode(true);
            user.getPlayer().setMetadata("staffmode", new FixedMetadataValue(Core.i(), true));
        }
        user.msg("&b&lStaff &8⏐ &fStaff Mode &7has been " + (user.isStaffmode() ? "&aEnabled" : "&cDisabled") + "&7.");
    }

    public static StaffModeCommand getInstance() {
        return instance;
    }
}

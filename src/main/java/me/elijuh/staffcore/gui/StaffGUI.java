package me.elijuh.staffcore.gui;

import lombok.Getter;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.ItemBuilder;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StaffGUI {
    private final ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(3).setName(" ").build();
    private final Inventory inv;

    public StaffGUI() {
        inv = Bukkit.createInventory(null, 54, ChatUtil.color("&b&lOnline Staff"));
        setPlayers();
    }

    public void handle(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(inv.getTitle())) {
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            if (item.getType().equals(Material.SKULL_ITEM) && e.getAction() == InventoryAction.PICKUP_HALF) {
                Player target = Bukkit.getPlayerExact(((SkullMeta) item.getItemMeta()).getOwner());
                if (target == null) {
                    e.getWhoClicked().sendMessage(ChatUtil.color("&cThat player is no longer online."));
                } else {
                    ((Player)e.getWhoClicked()).performCommand("tp " + target.getName());
                    e.getView().close();
                }
            }
            e.setCancelled(true);
        }
    }

    public void setPlayers() {
        getInv().clear();
        for (int i = 0; i < 9; i++) {
            getInv().setItem(i, filler);
            getInv().setItem(i + 45, filler);
        }
        int index = 9;
        for (User user : Core.i().getUsers()) {
            Player staff = user.getPlayer();
            if (staff.hasPermission("core.staff")) {
                ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                meta.setDisplayName(PlayerUtil.getPrefix(staff) + staff.getName());
                meta.setOwner(staff.getName());
                List<String> lore = new ArrayList<>();
                lore.add(" ");
                lore.add(ChatUtil.color("&3&lStaff Information &8»"));
                lore.add(ChatUtil.color("&8⏐ &7Vanish: " + (user.isVanished() ? "&aEnabled" : "&cDisabled")));
                lore.add(ChatUtil.color("&8⏐ &7Staff Mode: " + (user.isStaffmode() ? "&aEnabled" : "&cDisabled")));
                lore.add(" ");
                lore.add(ChatUtil.color("&7Right-Click to teleport."));

                meta.setLore(lore);
                item.setItemMeta(meta);
                getInv().setItem(index, item);
                if (++index > 35) {
                    break;
                }
            }
        }
    }
}

package me.elijuh.staffcore.listeners;

import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.impl.punishments.HistoryCommand;
import me.elijuh.staffcore.commands.impl.staff.StaffModeCommand;
import me.elijuh.staffcore.data.PType;
import me.elijuh.staffcore.data.Punishment;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.managers.DatabaseManager;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.ItemBuilder;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(3).setName(" ").build();
    private final DatabaseManager databaseManager;

    public PlayerListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.i());
        databaseManager = Core.i().getDatabaseManager();
    }

    @EventHandler
    public void on(AsyncPlayerPreLoginEvent e) {
        String display = databaseManager.getDisplay(e.getName());
        databaseManager.updateData(e.getName(), e.getAddress().getHostAddress(), e.getUniqueId(), display);

        Punishment punishment = databaseManager.getActiveBan(e.getName());

        if (punishment == null) return;

        if (databaseManager.isIPBanned(e.getAddress().getHostAddress())) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            e.setKickMessage(ChatUtil.toLines(
                    " ",
                    "&cYou are currently IP-Banned.",
                    " ",
                    "&bReason: &7" + punishment.getReason()
            ));
        } else if (databaseManager.isPunished(e.getUniqueId(), PType.BAN)) {
            boolean perm = punishment.getLength() == -1;
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            e.setKickMessage(ChatUtil.toLines(
                    " ",
                    "&cYou are currently Banned.",
                    " ",
                    "&bReason: &7" + punishment.getReason(),
                    "&bDuration: &7" + (perm ? "Permanent" : ChatUtil.formatMillis(punishment.getTime() + punishment.getLength() - System.currentTimeMillis())
                    )));
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        User user = new User(e.getPlayer());
        Core.i().getUsers().add(user);
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());

        if (user != null) {
            user.unload();
            Core.i().getUsers().remove(user);
        }
    }

    @EventHandler
    public void on(PlayerKickEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());

        if (user != null) {
            user.unload();
            Core.i().getUsers().remove(user);
        }
    }

    @EventHandler
    public void on(BlockPlaceEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.isStaffmode() || user.isFrozen()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(BlockBreakEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.isStaffmode() || user.isFrozen()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.PLAYER) {
            User user = Core.i().getUser(e.getDamager().getName());

            if (user != null) {
                if (user.isStaffmode() || user.isVanished() || user.isFrozen()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            User user = Core.i().getUser(e.getEntity().getName());

            if (user != null) {
                if (user.isStaffmode() || user.isVanished() || user.isFrozen()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerPickupItemEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.isStaffmode() || user.isVanished() || user.isFrozen()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(PlayerDropItemEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.isStaffmode() || user.isVanished() || user.isFrozen()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        Player p = (Player) e.getWhoClicked();

        Core.i().getStaffGUI().handle(e);

        if (e.getView().getTitle().startsWith(ChatUtil.color("&eHistory: &6"))) {
            String target = e.getView().getTitle().substring(13);
            e.setCancelled(true);
            String type = "";
            if (e.getRawSlot() == 12) {
                type = "Mutes";
            } else if (e.getRawSlot() == 13) {
                type = "Bans";
            } else if (e.getRawSlot() == 14) {
                type = "IP Bans";
            } else if (e.getRawSlot() == 31) {
                e.getView().close();
            }
            if (!type.isEmpty()) {
                p.openInventory(HistoryCommand.getHistoryGui(target, type));
            }
        } else if (e.getView().getTitle().startsWith(ChatUtil.color("&eMutes: &6")) || e.getView().getTitle().startsWith(ChatUtil.color("&eBans: &6"))
                || e.getView().getTitle().startsWith(ChatUtil.color("&eIP Bans: &6"))) {
            e.setCancelled(true);
            String target = e.getView().getTitle().split(ChatColor.GOLD.toString())[1];
            if (e.getRawSlot() == 49) {
                Inventory inv = Bukkit.createInventory(null, 36, ChatUtil.color("&eHistory: &6" + target));
                for (int i = 0; i < 36; i++) {
                    inv.setItem(i, filler);
                }
                inv.setItem(12, new ItemBuilder(Material.BOOK).setName("&6&lMutes").build());
                inv.setItem(13, new ItemBuilder(Material.BOOK).setName("&6&lBans").build());
                inv.setItem(14, new ItemBuilder(Material.BOOK).setName("&6&lIP Bans").build());
                inv.setItem(31, new ItemBuilder(Material.NETHER_STAR).setName("&7» &a&lExit &7«").build());
                p.openInventory(inv);
            } else if (e.getCurrentItem().getType() == Material.EMPTY_MAP) {
                if (p.hasPermission("core.history.remove")) {
                    int id = Integer.parseInt(e.getCurrentItem().getItemMeta().getDisplayName().substring(17));
                    databaseManager.deletePunishment(id);
                    p.sendMessage(ChatUtil.color(String.format("&7Successfully removed punishment &c#%s&7!", id)));
                    p.playSound(p.getLocation(), Sound.CHICKEN_EGG_POP, 1f, 2f);
                    String type = e.getView().getTitle().substring(2).split(":")[0];
                    p.openInventory(HistoryCommand.getHistoryGui(target, type));
                } else {
                    p.sendMessage(ChatUtil.color("&cYou don't have permission to delete history."));
                }
            }
        }

        ItemStack item = e.getCurrentItem();
        if (item != null) {
            StaffModeCommand smc = StaffModeCommand.getInstance();
            if (item.isSimilar(smc.getCompass()) || item.isSimilar(smc.getBook()) || item.isSimilar(smc.getRandomTeleport()) ||
                    item.isSimilar(smc.getFreeze()) || item.isSimilar(smc.getStaff()) ||
                    item.isSimilar(smc.getVanish()) || item.isSimilar(smc.getUnvanish())) {
                e.setCancelled(true);
                e.getView().close();
            } else {
                User user = Core.i().getUser(e.getWhoClicked().getName());
                if (user != null) {
                    if (user.isStaffmode() && e.getAction() == InventoryAction.HOTBAR_SWAP) {
                        e.setCancelled(true);
                        e.getView().close();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerInteractEvent e) {
        StaffModeCommand smc = StaffModeCommand.getInstance();
        User user = Core.i().getUser(e.getPlayer().getName());
        ItemStack item = e.getItem();

        if (user == null || item == null) return;

        if (e.getAction().toString().contains("RIGHT")) {
            if (item.isSimilar(smc.getVanish()) || item.isSimilar(smc.getUnvanish())) {
                e.getPlayer().performCommand("vanish");
            } else if (item.isSimilar(smc.getStaff())) {
                Core.i().getStaffGUI().setPlayers();
                e.getPlayer().openInventory(Core.i().getStaffGUI().getInv());
            } else if (item.isSimilar(smc.getRandomTeleport())) {
                if (Bukkit.getOnlinePlayers().size() > 1) {
                    Player target = PlayerUtil.getRandomPlayer();
                    while (target == user.getPlayer()) {
                        target = PlayerUtil.getRandomPlayer();
                    }
                    e.getPlayer().teleport(target);
                } else {
                    user.msg("&cThere are not enough players online.");
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerInteractEntityEvent e) {
        StaffModeCommand smc = StaffModeCommand.getInstance();
        ItemStack item = e.getPlayer().getItemInHand();
        if (item != null && e.getRightClicked().getType() == EntityType.PLAYER) {
            if (item.isSimilar(smc.getBook())) {
                e.getPlayer().performCommand("invsee " + e.getRightClicked().getName());
            } else if (item.isSimilar(smc.getFreeze())) {
                e.getPlayer().performCommand("freeze " + e.getRightClicked().getName());
            }
        }
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.isStaffchat() || (e.getMessage().startsWith("@") && user.isStaff())) {
                e.setCancelled(true);
                String message = e.getMessage().startsWith("@") ? e.getMessage().substring(1) : e.getMessage();
                user.sendStaffChat(message);
            }
        }

        if (databaseManager.isPunished(e.getPlayer().getUniqueId(), PType.MUTE)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatUtil.color("&cYou are currently muted."));
        }
    }

    @EventHandler
    public void on(PlayerMoveEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null && user.isFrozen()) {
            if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
                e.getPlayer().teleport(e.getFrom());
            }
        }
    }
}

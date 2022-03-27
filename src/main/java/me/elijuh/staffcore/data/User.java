package me.elijuh.staffcore.data;

import lombok.Data;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.impl.staff.StaffModeCommand;
import me.elijuh.staffcore.data.redis.MessageInfo;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class User {
    private static final StaffModeCommand smc = StaffModeCommand.getInstance();
    private final Player player;
    private final String name;
    private final UUID uuid;
    private final Map<String, Object> data;
    private boolean staffmode, vanished, staffchat, frozen;

    public User(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId();
        this.data = new HashMap<>();

        Core.i().getDatabaseManager().updateData(this);

        if (player.hasPermission("core.vanish")) {
            if (Core.i().getConfig().getBoolean("staff.vanish-on-join")) {
                for (Player all : Bukkit.getOnlinePlayers()) {
                    if (!all.hasPermission("core.vanish")) {
                        all.hidePlayer(player);
                    }
                }
                vanished = true;
                player.setMetadata("vanish", new FixedMetadataValue(Core.i(), true));
            }
        } else {
            for (User user : Core.i().getUsers()) {
                if (user.isVanished()) {
                    player.hidePlayer(user.getPlayer());
                }
            }
        }

        if (player.hasPermission("core.staffmode") && Core.i().getConfig().getBoolean("staff.staffmode-on-join")) {
            PlayerInventory inv = player.getInventory();
            data.put("staffmode-inv", inv.getContents());
            data.put("staffmode-inv-armor", inv.getArmorContents());
            inv.clear();
            inv.setHelmet(null);
            inv.setChestplate(null);
            inv.setLeggings(null);
            inv.setBoots(null);
            inv.setItem(0, smc.getCompass());
            inv.setItem(1, smc.getBook());
            inv.setItem(2, smc.getFreeze());
            inv.setItem(4, smc.getRandomTeleport());
            inv.setItem(7, smc.getStaff());
            inv.setItem(8, vanished ? smc.getUnvanish() : smc.getVanish());
            player.setAllowFlight(true);
            player.setFlying(true);
            staffmode = true;
            player.setMetadata("staffmode", new FixedMetadataValue(Core.i(), true));
        }

            Bukkit.getScheduler().runTaskLater(Core.i(), () -> {
                if (isStaff()) {
                    MessageInfo info = new MessageInfo("core.staff", String.format("&3&l[S] &r%s &7has connected to &8[&b%s&8]", PlayerUtil.getColoredName(player), Core.i().getId()));
                    Core.i().getRedisManager().getPubSubSender().async().publish("MESSAGING", Core.i().getRedisManager().getGSON().toJson(info));
                }
            }, 5L);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) data.get(key);
    }

    public void msg(String s) {
        player.sendMessage(ChatUtil.color(s));
    }

    public boolean isStaff() {
        return player.hasPermission("core.staff");
    }

    public void unload() {
        Core.i().getDatabaseManager().updateData(this);

        if (staffmode) {
            player.getInventory().setContents(getData("staffmode-inv"));
            player.getPlayer().getInventory().setArmorContents(getData("staffmode-inv-armor"));
            player.setAllowFlight(false);
            player.setFlying(false);
            staffmode = false;
            player.removeMetadata("staffmode", Core.i());
        }
        if (vanished) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.canSee(player)) {
                    other.showPlayer(player);
                }
            }
            vanished = false;
            player.removeMetadata("vanish", Core.i());
        }
        if (frozen) {
            for (User user : Core.i().getUsers()) {
                if (user.isStaff()) {
                    user.msg("");
                    user.msg("&4&l" + getName() + " has logged out whilst frozen!");
                    user.msg("");
                }
            }
            frozen = false;
        }

        if (isStaff()) {
            MessageInfo info = new MessageInfo("core.staff", String.format("&3&l[S] &r%s &7has disconnected from &8[&b%s&8]", PlayerUtil.getColoredName(player), Core.i().getId()));
            Core.i().getRedisManager().getPubSubSender().async().publish("MESSAGING", Core.i().getRedisManager().getGSON().toJson(info));
        }
    }

    public void sendStaffChat(String msg) {
        MessageInfo info = new MessageInfo("core.staff", String.format("&3&l[S] &3[&b%s&3] &r%s&7: %s", Core.i().getId(), PlayerUtil.getColoredName(player), msg));
        Core.i().getRedisManager().getPubSubSender().async().publish("MESSAGING", Core.i().getRedisManager().getGSON().toJson(info));
    }

    public void kick(String... lines) {
        Bukkit.getScheduler().runTask(Core.i(), ()-> player.kickPlayer(ChatUtil.toLines(lines)));
    }
}

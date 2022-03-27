package me.elijuh.staffcore.commands.impl.punishments;

import com.google.common.collect.Lists;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.data.Punishment;
import me.elijuh.staffcore.data.PType;
import me.elijuh.staffcore.managers.DatabaseManager;
import me.elijuh.staffcore.utils.ChatUtil;
import me.elijuh.staffcore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HistoryCommand extends SpigotCommand {
    private static final ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(3).setName(" ").build();
    private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm aa");
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));
    }
    DatabaseManager databaseManager;

    public HistoryCommand() {
        super("history", Lists.newArrayList("h", "c"), "core.history");
        databaseManager = Core.i().getDatabaseManager();
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length > 0) {
            if (!databaseManager.hasData(args[0])) {
                p.sendMessage(ChatUtil.color("&cThat player has never joined!"));
                return;
            }
            String name = databaseManager.getName(databaseManager.getUUID(args[0]));
            Inventory inv = Bukkit.createInventory(null, 36, ChatUtil.color("&eHistory: &6" + name));
            for (int i = 0; i < 36; i++) {
                inv.setItem(i, filler);
            }
            inv.setItem(12, new ItemBuilder(Material.BOOK).setName("&6&lMutes").build());
            inv.setItem(13, new ItemBuilder(Material.BOOK).setName("&6&lBans").build());
            inv.setItem(14, new ItemBuilder(Material.BOOK).setName("&6&lIP Bans").build());
            inv.setItem(31, new ItemBuilder(Material.NETHER_STAR).setName("&7» &a&lExit &7«").build());
            p.openInventory(inv);
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /history <player>"));
        }
    }

    @Override
    public void onConsole(CommandSender sender, String[] args) {
        sender.sendMessage(ChatUtil.color("&cThis command uses a GUI and is not supported by console."));
    }

    public static Inventory getHistoryGui(String target, String type) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatUtil.color("&e" + type + ": &6" + target));
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, filler);
            inv.setItem(i + 45, filler);
        }
        List<Punishment> punishments = Core.i().getDatabaseManager().getPunishments(Core.i().getDatabaseManager().getUUID(target),
                type.equals("Mutes") ? PType.MUTE : (type.equals("Bans") ? PType.BAN : PType.IPBAN));
        for (int i = 0; i < Math.min(punishments.size(), 36); i++) {
            Punishment punishment = punishments.get(i);
            boolean inactive = (punishment.getLength() != -1 && System.currentTimeMillis() > punishment.getTime() + punishment.getLength()) || punishment.getRemoved() != null;
            ItemStack item = new ItemBuilder(Material.EMPTY_MAP).setName("&6Punishment: &c#" + punishment.getId())
                    .addLore("&7&m---------------------------------")
                    .addLore("&8» &bStatus: " + (inactive ? "&cInactive" : "&aActive"))
                    .addLore("&8» &bPunished By: &7" + punishment.getExecutor())
                    .addLore("&8» &bRemoved By: " + (punishment.getRemoved() == null ? "&cN/A" : "&7" + punishment.getRemoved()))
                    .addLore("&8» &bDate Of: &7" + dateFormat.format(new Date(punishment.getTime())) + " (EST)")
                    .addLore("&8» &bLength: &7" + (punishment.getLength() == -1 ? "Permanent" : ChatUtil.formatMillis(punishment.getLength())))
                    .addLore("&8» &bReason: &7" + punishment.getReason())
                    .addLore("&8» &bServer: &7" + punishment.getServer())
                    .addLore(" ")
                    .addLore("&a&lClick to remove this history")
                    .addLore("&7&m---------------------------------").build();
            inv.setItem(i + 9, item);
        }
        inv.setItem(49, new ItemBuilder(Material.NETHER_STAR).setName("&7» &a&lExit &7«").build());
        return inv;
    }
}

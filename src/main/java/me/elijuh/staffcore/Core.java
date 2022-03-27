package me.elijuh.staffcore;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.elijuh.staffcore.commands.SpigotCommand;
import me.elijuh.staffcore.commands.impl.other.ReportCommand;
import me.elijuh.staffcore.commands.impl.punishments.*;
import me.elijuh.staffcore.commands.impl.staff.*;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.gui.StaffGUI;
import me.elijuh.staffcore.listeners.PlayerListener;
import me.elijuh.staffcore.listeners.RedisListener;
import me.elijuh.staffcore.managers.DatabaseManager;
import me.elijuh.staffcore.managers.RedisManager;
import me.elijuh.staffcore.utils.ReflectionUtil;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@Getter
public class Core extends JavaPlugin {
    private final List<User> users = Lists.newArrayList();
    private static Core instance;
    private RedisManager redisManager;
    private DatabaseManager databaseManager;
    private StaffGUI staffGUI;
    private Chat chat;
    private String id;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("redis.host", "");
        getConfig().addDefault("redis.port", 6379);
        getConfig().addDefault("redis.password", "");
        getConfig().addDefault("mysql.host", "");
        getConfig().addDefault("mysql.port", 3306);
        getConfig().addDefault("mysql.database", "");
        getConfig().addDefault("mysql.username", "");
        getConfig().addDefault("mysql.password", "");
        getConfig().addDefault("id", "GamemodeName");
        getConfig().addDefault("discord", "discord.gg/example");
        getConfig().addDefault("staff.vanish-on-join", false);
        getConfig().addDefault("staff.staffmode-on-join", false);
        saveConfig();

        instance = this;
        redisManager = new RedisManager();
        databaseManager = new DatabaseManager();
        chat = Bukkit.getServicesManager().getRegistration(Chat.class).getProvider();
        staffGUI = new StaffGUI();
        id = getConfig().getString("id");

        new StaffExpansion().register();

        new StaffModeCommand();
        new AltsCommand();
        new BanCommand();
        new HistoryCommand();
        new IPBanCommand();
        new IPUnBanCommand();
        new KickCommand();
        new MuteCommand();
        new TempBanCommand();
        new TempMuteCommand();
        new TempTimeFormatCommand();
        new UnBanCommand();
        new UnMuteCommand();
        new VanishCommand();
        new ClearChatCommand();
        new FreezeCommand();
        new StaffChatCommand();
        new ReportCommand();

        new PlayerListener();
        new RedisListener();

        for (Player p : Bukkit.getOnlinePlayers()) {
            users.add(new User(p));
        }
    }

    @Override
    public void onDisable() {
        for (User user : users) {
            user.unload();
        }

        try {
            CommandMap map = (CommandMap) ReflectionUtil.getField(Bukkit.getServer().getClass(), "commandMap").get(Bukkit.getServer());
            ReflectionUtil.unregisterCommands(map, SpigotCommand.getRegisteredCommands());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (StaffExpansion.getInstance() != null) {
            StaffExpansion.getInstance().unregister();
        }

        users.clear();

        redisManager.shutdown();
        databaseManager.shutdown();
    }

    public User getUser(String name) {
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }

    public static Core i() {
        return instance;
    }
}

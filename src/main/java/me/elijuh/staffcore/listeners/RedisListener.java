package me.elijuh.staffcore.listeners;

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.data.redis.MessageInfo;
import me.elijuh.staffcore.data.redis.PunishmentInfo;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.data.redis.ReportInfo;
import me.elijuh.staffcore.managers.DatabaseManager;
import me.elijuh.staffcore.managers.RedisManager;
import me.elijuh.staffcore.utils.ChatUtil;

public class RedisListener extends RedisPubSubAdapter<String, String> {
    RedisManager manager;
    DatabaseManager databaseManager;

    public RedisListener() {
        manager = Core.i().getRedisManager();
        databaseManager = Core.i().getDatabaseManager();
        manager.getPubSubSubscriber().addListener(this);
    }

    @Override
    public void message(String channel, String json) {
        switch (channel) {
            case "REPORT": {
                ReportInfo report = manager.getGSON().fromJson(json, ReportInfo.class);
                for (User user : Core.i().getUsers()) {
                    if (user.isStaff()) {
                        user.msg("&8[&4&lReport&8] &7[" + report.getServer() + "&7] &f" + report.getReported() + " &7was reported by &f" + report.getReporter());
                        user.msg("&8[&4&l!&8] &cReason: &7" + report.getReason());
                    }
                }
                break;
            }
            case "MESSAGING": {
                MessageInfo info = manager.getGSON().fromJson(json, MessageInfo.class);
                for (User user : Core.i().getUsers()) {
                    if (user.getPlayer().hasPermission(info.getPermission())) {
                        user.msg(info.getMessage());
                    }
                }
                break;
            }
            case "PUNISHMENT": {
                handlePunishment(manager.getGSON().fromJson(json, PunishmentInfo.class));
                break;
            }
        }
    }

    private void handlePunishment(PunishmentInfo punishment) {
        switch (punishment.getType()) {
            case KICK: {
                if (Core.i().getUser(punishment.getPunished()) != null) {
                    Core.i().getUser(punishment.getPunished()).kick(
                            "&cYou have been Kicked.",
                            " ",
                            "&bReason: &7" + punishment.getReason());
                }

                for (User user : Core.i().getUsers()) {
                    if (user.isStaff()) {
                        user.msg("&7&m------------------------------------------");
                        user.msg("&8[&3&lStaff Alert&8] " + punishment.getExecutorDisplay() + " &ekicked " +
                                punishment.getPunishedDisplay());
                        user.msg("");
                        user.msg("&8» &bReason: &7" + punishment.getReason());
                        user.msg("&8» &bServer: &7" + punishment.getServer());
                        user.msg("&7&m------------------------------------------");
                    }
                }
                break;
            }
            case BAN: {
                if (punishment.isRemoval()) {
                    for (User user : Core.i().getUsers()) {
                        if (user.isStaff()) {
                            user.msg("&7&m------------------------------------------");
                            user.msg("&8[&3&lStaff Alert&8] " + punishment.getExecutorDisplay() + " &eunbanned " +
                                    punishment.getPunishedDisplay());
                            user.msg("");
                            user.msg("&8» &bReason: &7" + punishment.getReason());
                            user.msg("&8» &bServer: &7" + punishment.getServer());
                            user.msg("&7&m------------------------------------------");
                        }
                    }
                } else {
                    boolean perm = punishment.getLength() == -1;
                    if (Core.i().getUser(punishment.getPunished()) != null) {
                        Core.i().getUser(punishment.getPunished()).kick(
                                "&cYou are currently Banned.",
                                " ",
                                "&bReason: &7" + punishment.getReason(),
                                "&bDuration: &7" + (perm ? "Permanent" : ChatUtil.formatMillis(punishment.getLength())));
                    }

                    for (User user : Core.i().getUsers()) {
                        if (user.isStaff()) {
                            user.msg("&7&m------------------------------------------");
                            user.msg("&8[&3&lStaff Alert&8] " + punishment.getExecutorDisplay() + (perm ? " &ebanned " : " &etemporarily banned ") +
                                    punishment.getPunishedDisplay());
                            user.msg("");
                            user.msg("&8» &bDuration: &7" + (perm ? "Permanent" : ChatUtil.formatMillis(punishment.getLength())));
                            user.msg("&8» &bReason: &7" + punishment.getReason());
                            user.msg("&8» &bServer: &7" + punishment.getServer());
                            user.msg("&7&m------------------------------------------");
                        }
                    }
                }
                break;
            }
            case IPBAN: {
                if (punishment.isRemoval()) {
                    for (User user : Core.i().getUsers()) {
                        if (user.isStaff()) {
                            user.msg("&7&m------------------------------------------");
                            user.msg("&8[&3&lStaff Alert&8] " + punishment.getExecutorDisplay() + " &eipunbanned " + punishment.getPunishedDisplay());
                            user.msg("");
                            user.msg("&8» &bReason: &7" + punishment.getReason());
                            user.msg("&8» &bServer: &7" + punishment.getServer());
                            user.msg("&7&m------------------------------------------");
                        }
                    }
                } else {
                    String punished = punishment.getPunished();
                    String uuid = databaseManager.getUUID(punished);
                    String ip = databaseManager.getIP(uuid);

                    for (User user : Core.i().getUsers()) {
                        if (databaseManager.getIP(user.getPlayer().getUniqueId().toString()).equals(ip)) {
                            user.kick(
                                    "&cYou are currently IP-Banned.",
                                    " ",
                                    "&bReason: &7" + punishment.getReason());
                        }
                    }

                    for (User user : Core.i().getUsers()) {
                        if (user.isStaff()) {
                            user.msg("&7&m------------------------------------------");
                            user.msg("&8[&3&lStaff Alert&8] " + punishment.getExecutorDisplay() +  " &eipbanned " +
                                    punishment.getPunishedDisplay());
                            user.msg("");
                            user.msg("&8» &bDuration: &7Permanent");
                            user.msg("&8» &bReason: &7" + punishment.getReason());
                            user.msg("&8» &bServer: &7" + punishment.getServer());
                            user.msg("&7&m------------------------------------------");
                        }
                    }
                }
                break;
            }
            case MUTE: {
                User u = Core.i().getUser(punishment.getPunished());
                if (punishment.isRemoval()) {
                    if (u != null) {
                        u.msg(ChatUtil.color("&aYou have been unmuted."));
                    }

                    for (User user : Core.i().getUsers()) {
                        if (user.isStaff()) {
                            user.msg("&7&m------------------------------------------");
                            user.msg("&8[&3&lStaff Alert&8] " + punishment.getExecutorDisplay() + " &eunmuted " +
                                    punishment.getPunishedDisplay());
                            user.msg("");
                            user.msg("&8» &bReason: &7" + punishment.getReason());
                            user.msg("&8» &bServer: &7" + punishment.getServer());
                            user.msg("&7&m------------------------------------------");
                        }
                    }
                } else {
                    boolean perm = punishment.getLength() == -1;
                    if (u != null) {
                        u.msg(" ");
                        u.msg("&cYou have been muted! &8(&7Reason: &f" + punishment.getReason() + " &8| &7Duration: &f" + (perm ? "Permanent" : ChatUtil.formatMillis(punishment.getLength())) + "&8)");
                        u.msg(" ");
                    }

                    for (User user : Core.i().getUsers()) {
                        if (user.isStaff()) {
                            user.msg("&7&m------------------------------------------");
                            user.msg("&8[&3&lStaff Alert&8] " + punishment.getExecutorDisplay() +
                                    (perm ? " &emuted " : " &etemporarily muted ") + punishment.getPunishedDisplay());
                            user.msg("");
                            user.msg("&8» &bDuration: &7" + (perm ? "Permanent" : ChatUtil.formatMillis(punishment.getLength())));
                            user.msg("&8» &bReason: &7" + punishment.getReason());
                            user.msg("&8» &bServer: &7" + punishment.getServer());
                            user.msg("&7&m------------------------------------------");
                        }
                    }
                }
                break;
            }
        }
    }
}

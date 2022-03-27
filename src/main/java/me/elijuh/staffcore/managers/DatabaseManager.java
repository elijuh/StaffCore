package me.elijuh.staffcore.managers;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.elijuh.staffcore.Core;
import me.elijuh.staffcore.data.PType;
import me.elijuh.staffcore.data.Punishment;
import me.elijuh.staffcore.data.User;
import me.elijuh.staffcore.data.redis.PunishmentInfo;
import me.elijuh.staffcore.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.List;
import java.util.UUID;

@Getter
public class DatabaseManager {
    private final HikariDataSource hikariDataSource;
    private final Connection connection;
    private final RedisManager redisManager;

    public DatabaseManager() {
        redisManager = Core.i().getRedisManager();

        FileConfiguration config = Core.i().getConfig();
        String host = config.getString("mysql.host");
        int port = config.getInt("mysql.port");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName("Core");
        hikariConfig.setMaximumPoolSize(6);
        hikariConfig.setAutoCommit(true);

        hikariDataSource = new HikariDataSource(hikariConfig);

        Connection c;
        try {
            c = hikariDataSource.getConnection();
        } catch (SQLException e) {
            c = null;
        }

        this.connection = c;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS sc_punishments(`id` INT(16) PRIMARY KEY AUTO_INCREMENT, `UUID` VARCHAR(36), `IP` VARCHAR(20), " +
                    "`type` VARCHAR(10), `removed` VARCHAR(16) DEFAULT NULL, `time` BIGINT(16), `length` BIGINT(16), `reason` VARCHAR(255), `executor` VARCHAR(16), `server` VARCHAR(32))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS sc_userdata(`UUID` VARCHAR(36) PRIMARY KEY, `IP` VARCHAR(20), `name` VARCHAR(16), `display` VARCHAR(32))");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(Core.i(), ()-> {
            try (Statement statement = getConnection().createStatement()) {
                statement.execute("SELECT name FROM sc_userdata LIMIT 1");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 6000L, 6000L);
    }

    public boolean isConnected() {
        try {
            if (hikariDataSource.getConnection() != null) {
                return !hikariDataSource.getConnection().isClosed();
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void shutdown() {
        if (isConnected()) {
            try {
                if (hikariDataSource.getConnection() != null) {
                    hikariDataSource.getConnection().close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (!hikariDataSource.isClosed()) {
            hikariDataSource.close();
        }
    }

    public boolean isIPBanned(String ip) {
        if (ip.equals("Unknown")) {
            return false;
        }

        try (PreparedStatement statement = connection.prepareStatement("SELECT UUID FROM sc_punishments WHERE type = \"IPBAN\" AND IP = ? AND removed IS NULL")) {
            statement.setString(1, ip);
            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPunished(String name, PType type) {
        if (hasData(name)) {
            return isPunished(UUID.fromString(getUUID(name)), type);
        }
        return false;
    }

    public boolean isPunished(UUID uuid, PType type) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT time, length FROM sc_punishments WHERE UUID = ? AND type = ? AND removed IS NULL")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, type.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                if (result.getLong("length") == -1) {
                    return true;
                } else if (result.getLong("time") + result.getLong("length") > System.currentTimeMillis()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void punish(PunishmentInfo info) {
        redisManager.getPubSubSender().async().publish("PUNISHMENT", redisManager.getGSON().toJson(info));

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO sc_punishments(`UUID`, `IP`, `type`, `time`, " +
                "`length`, `reason`, `executor`, `server`) VALUES(?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, getUUID(info.getPunished()));
            statement.setString(2, getIP(getUUID(info.getPunished())));
            statement.setString(3, info.getType().toString());
            statement.setString(4, String.valueOf(System.currentTimeMillis()));
            statement.setString(5, String.valueOf(info.getLength()));
            statement.setString(6, info.getReason());
            statement.setString(7, info.getExecutor());
            statement.setString(8, info.getServer());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(String uuid, PunishmentInfo info) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE sc_punishments SET `removed` = ? WHERE `UUID` = ? AND `type` = ? AND removed IS NULL")) {
            statement.setString(1, info.getExecutor());
            statement.setString(2, uuid);
            statement.setString(3, info.getType().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        redisManager.getPubSubSender().async().publish("PUNISHMENT", redisManager.getGSON().toJson(info));
    }

    public String getIP(String uuid) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT IP FROM sc_userdata WHERE UUID = ?")) {
            statement.setString(1, uuid);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("IP");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public String getUUID(String name) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT UUID FROM sc_userdata WHERE LOWER(name) = ?")) {
            statement.setString(1, name.toLowerCase());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("UUID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public String getDisplay(String name) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT display FROM sc_userdata WHERE UUID = ?")) {
            statement.setString(1, getUUID(name));
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("display");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    public void updateData(User user) {
        updateData(user.getPlayer().getName(),
                user.getPlayer().getAddress().getAddress().getHostAddress(),
                user.getPlayer().getUniqueId(),
                PlayerUtil.getColoredName(user.getPlayer())
        );
    }

    public void updateData(String name, String ip, UUID uuid, String display) {
        if (hasData(uuid)) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE sc_userdata SET `name` = ?, `IP` = ?, `display` = ? WHERE UUID = ?")) {
                statement.setString(1, name);
                statement.setString(2, ip);
                statement.setString(3, display);
                statement.setString(4, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO sc_userdata(`UUID`, `IP`, `name`, `display`) VALUES(?, ?, ?, ?)")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, ip);
                statement.setString(3, name);
                statement.setString(4, display);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasData(String name) {
        return !getUUID(name).equals("Unknown");
    }

    public boolean hasData(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT UUID FROM sc_userdata WHERE UUID = ? LIMIT 1")) {
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Punishment> getPunishments(String uuid, PType type) {
        List<Punishment> punishments = Lists.newArrayList();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM sc_punishments WHERE UUID = ? AND type = ?")) {
            statement.setString(1, uuid);
            statement.setString(2, type.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                Punishment punishment = new Punishment(
                        result.getInt("id"),
                        result.getString("uuid"), type,
                        result.getString("removed"),
                        result.getLong("time"),
                        result.getLong("length"),
                        result.getString("reason"),
                        result.getString("executor"),
                        result.getString("server")
                );
                punishments.add(punishment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    public Punishment getActiveBan(String name) {
        if (isIPBanned(getIP(getUUID(name)))) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM sc_punishments WHERE IP = ? AND removed IS NULL")) {
                statement.setString(1, getIP(getUUID(name)));
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    if (result.getString("type").contains("BAN")) {
                        return new Punishment(
                                result.getInt("id"),
                                result.getString("uuid"),
                                PType.IPBAN,
                                result.getString("removed"),
                                result.getLong("time"),
                                result.getLong("length"),
                                result.getString("reason"),
                                result.getString("executor"),
                                result.getString("server")
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (isPunished(name, PType.BAN)) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM sc_punishments WHERE UUID = ? AND removed IS NULL")) {
                statement.setString(1, getUUID(name));
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    if (result.getString("type").endsWith("BAN")) {
                        Punishment punishment = new Punishment(
                                result.getInt("id"),
                                result.getString("uuid"),
                                PType.BAN,
                                result.getString("removed"),
                                result.getLong("time"),
                                result.getLong("length"),
                                result.getString("reason"),
                                result.getString("executor"),
                                result.getString("server")
                        );
                        if (punishment.getLength() == -1) {
                            return punishment;
                        } else if (punishment.getTime() + punishment.getLength() > System.currentTimeMillis()) {
                            return punishment;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<String> getAccounts(String ip) {
        List<String> alts = Lists.newArrayList();
        try (PreparedStatement statement = connection.prepareStatement("SELECT name FROM sc_userdata WHERE IP = ?")) {
            statement.setString(1, ip);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                alts.add(result.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alts;
    }

    public String getName(String uuid) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT name FROM sc_userdata WHERE UUID = ?")) {
            statement.setString(1, uuid);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deletePunishment(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM sc_punishments WHERE id = ?")) {
            statement.setString(1, String.valueOf(id));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
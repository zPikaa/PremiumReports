package it.pika.premiumreports.storage.impl;

import it.pika.libs.sql.mysql.Connection;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.api.events.ReportSubmitEvent;
import it.pika.premiumreports.objects.Report;
import it.pika.premiumreports.objects.User;
import it.pika.premiumreports.storage.MySQLCredentials;
import it.pika.premiumreports.storage.Storage;
import it.pika.premiumreports.storage.StorageType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
public class MySQL extends Storage {

    private final MySQLCredentials credentials;

    private Connection connection;

    @Override
    @SneakyThrows
    public void init() {
        connection = new Connection(credentials.getHost(), credentials.getPort(), credentials.getDatabase(),
                credentials.getUsername(), credentials.getPassword());
        connection.connect();

        if (connection.isConnectionValid()) {
            connection.update("CREATE TABLE IF NOT EXISTS `reports` (" +
                    "  `uuid` VARCHAR(255) NOT NULL," +
                    "  `reporter` VARCHAR(255) NOT NULL," +
                    "  `reported` VARCHAR(255) NOT NULL," +
                    "  `reason` TEXT NOT NULL," +
                    "  `date` BIGINT NOT NULL," +
                    "  `result` INT NOT NULL," +
                    "  `completedBy` VARCHAR(255) NULL," +
                    "  `completedDate` BIGINT NULL," +
                    "  PRIMARY KEY (`uuid`))");

            connection.update("CREATE TABLE IF NOT EXISTS `users` (" +
                    "  `uuid` VARCHAR(255) NOT NULL," +
                    "  `points` INT NOT NULL," +
                    "  `blocked` TINYINT NOT NULL," +
                    "  PRIMARY KEY (`uuid`))");

            var r = connection.query("SELECT * FROM reports");
            while (r.next())
                Main.getReports().add(getReport(UUID.fromString(r.getString("uuid"))));
            r.close();
        }
    }

    @Override
    public void close() {
        if (connection.isConnectionValid())
            connection.close();
    }

    @Override
    public StorageType getType() {
        return StorageType.MYSQL;
    }

    @Override
    @SneakyThrows
    public Report getReport(UUID uuid) {
        var r = connection.preparedQuery("SELECT * FROM reports WHERE uuid = ?", uuid.toString());

        if (!r.next())
            return null;

        var reporter = r.getString("reporter");
        var reported = r.getString("reported");
        var reason = r.getString("reason");
        var date = new Date(r.getLong("date"));
        var result = Report.Result.byId(r.getInt("result"));
        var completedBy = r.getString("completedBy");
        var completedDate = r.getLong("completedDate") != 0 ?
                new Date(r.getLong("completedDate")) : null;

        r.close();
        return new Report(uuid, reporter, reported, reason, date, result, completedBy, completedDate);
    }

    @Override
    @SneakyThrows
    public void createReport(Player player, String reported, String reason) {
        var uuid = UUID.randomUUID();

        var event = new ReportSubmitEvent(player, new Report(uuid, player.getName(), reported, reason,
                new Date(System.currentTimeMillis()), Report.Result.UNDEFINED, null, null));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        connection.preparedUpdate("INSERT INTO reports (uuid, reporter, reported, reason, date, result) VALUES " +
                "(?,?,?,?,?,?)", uuid.toString(),
                player.getName(), reported, reason, System.currentTimeMillis(), 0);

        Main.getReports().add(getReport(uuid));
    }

    @Override
    @SneakyThrows
    public void setResult(Report report, Report.Result result) {
        connection.preparedUpdate("UPDATE reports SET result = ? WHERE uuid = ?", result.getId(),
                report.getUuid().toString());
    }

    @Override
    @SneakyThrows
    public void setPoints(User user, int points) {
        connection.preparedUpdate("UPDATE users SET points = ? WHERE uuid = ?", points,
                user.getPlayer().getUniqueId().toString());
    }

    @Override
    @SneakyThrows
    public void setBlocked(User user, boolean block) {
        connection.preparedUpdate("UPDATE users SET blocked = ? WHERE uuid = ?", block ? 1 : 0,
                user.getPlayer().getUniqueId().toString());
    }

    @Override
    @SneakyThrows
    public User getUser(OfflinePlayer player) {
        if (player == null)
            return null;

        var r = connection.preparedQuery("SELECT * FROM users WHERE uuid = ?", player.getUniqueId().toString());

        if (!r.next())
            return null;

        var points = r.getInt("points");
        var blocked = r.getInt("blocked") == 1;

        r.close();
        return new User(player, points, blocked);
    }

    @Override
    @SneakyThrows
    public void createUser(Player player, int points) {
        connection.preparedUpdate("INSERT INTO users VALUES(?,?,?)", player.getUniqueId().toString(),
                points, 0);
    }

}

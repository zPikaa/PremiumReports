package it.pika.premiumreports;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.tchristofferson.configupdater.ConfigUpdater;
import fr.minuskube.inv.InventoryManager;
import it.pika.libs.config.Config;
import it.pika.premiumreports.api.events.ReportsInitializeEvent;
import it.pika.premiumreports.commands.ReportCmd;
import it.pika.premiumreports.listeners.JoinListener;
import it.pika.premiumreports.objects.Report;
import it.pika.premiumreports.storage.MySQLCredentials;
import it.pika.premiumreports.storage.Storage;
import it.pika.premiumreports.storage.StorageType;
import it.pika.premiumreports.storage.impl.MySQL;
import it.pika.premiumreports.storage.impl.SQLite;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance = null;
    @Getter
    private static final Logger console = Logger.getLogger("PremiumReports");
    @Getter
    private static Storage storage = null;
    @Getter
    private static InventoryManager inventoryManager = null;


    @Getter
    private static Config configFile = null;
    @Getter
    private static Config messagesFile = null;


    @Getter
    private static final List<Report> reports = Lists.newArrayList();

    @Override
    public void onEnable() {
        instance = this;
        var stopwatch = Stopwatch.createStarted();

        setupFiles();
        if (!setupStorage()) {
            stopwatch.stop();
            console.warning("Couldn't setup storage, check your config.yml! Disabling the plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        registerListeners();
        registerCommands();
        setupInventories();

        stopwatch.stop();
        Bukkit.getPluginManager().callEvent(new ReportsInitializeEvent(this));
        console.info("Plugin enabled in %s ms.".formatted(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.close();
    }

    @SneakyThrows
    private void setupFiles() {
        configFile = new Config(this, "config.yml");
        messagesFile = new Config(this, "messages.yml");

        ConfigUpdater.update(this, "config.yml", configFile.getFile());
        ConfigUpdater.update(this, "messages.yml", messagesFile.getFile());

        configFile.reload();
        messagesFile.reload();
    }

    private boolean setupStorage() {
        StorageType type;
        try {
            type = StorageType.valueOf(Objects.requireNonNull(configFile.getString("Storage.Type")).toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            console.warning("Database type not recognized!");
            return false;
        }

        switch (type) {
            case MYSQL -> {
                storage = new MySQL(MySQLCredentials.fromConfig(configFile));
                storage.init();
            }
            case SQLITE -> {
                storage = new SQLite();
                storage.init();
            }
        }

        console.info("Storage type: %s".formatted(storage.getType().name()));
        return true;
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinListener(), this);
    }

    private void registerCommands() {
        new ReportCmd(this, "report", "reports");
    }

    private void setupInventories() {
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
    }

    public static String parseColors(String s) {
        if (s == null)
            return "null";

        var parsed = new StringBuilder();
        var colorHex = "";
        if (s.contains("&")) {
            for (String color : s.split("&")) {
                if (color.length() < 1) continue;
                if (color.substring(0, 1).matches("[A-Fa-f0-9]|k|l|m|n|o|r")) {
                    String colorCode = color.substring(0, 1);
                    parsed.append(ChatColor.getByChar(colorCode.charAt(0)));
                    parsed.append(color.substring(1));
                    continue;
                }
                if (color.length() < 7) continue;
                if (color.substring(0, 7).matches("#[A-Fa-f0-9]{6}")) {
                    if (color.substring(0, 7).matches("#[A-Fa-f0-9]{6}")) {
                        colorHex = color.substring(0, 7);
                        parsed.append(net.md_5.bungee.api.ChatColor.of(colorHex));
                        parsed.append(color.substring(7));
                        continue;
                    }
                }
                parsed.append(color);
            }
        } else {
            parsed.append(s);
        }

        return parsed.toString();
    }

    public static List<String> parseColors(List<String> list) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(parseColors(s));

        return newList;
    }

    public static String parseMessage(String s, Report report) {
        var dateFormat = new SimpleDateFormat(Objects.requireNonNull(configFile.getString("Options.Date-Format")));

        return parseColors(s.replaceAll("%reporter%", report.getReporter())
                .replaceAll("%reported%", report.getReported())
                .replaceAll("%reason%", report.getReason())
                .replaceAll("%date%", dateFormat.format(report.getDate()))
                .replaceAll("%result%", report.getResult().getDisplayName())
                .replaceAll("%completedBy%", report.getCompletedBy() == null ? "//" : report.getCompletedBy())
                .replaceAll("%completedDate%", report.getCompletedDate() == null ? "//" : dateFormat.format(report.getCompletedDate())));
    }

    public static List<String> parseMessage(List<String> list, Report report) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(parseMessage(s, report));

        return newList;
    }

}

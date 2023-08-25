package it.pika.premiumreports;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import fr.minuskube.inv.InventoryManager;
import it.pika.libs.chat.Chat;
import it.pika.libs.config.Config;
import it.pika.libs.reflection.Reflections;
import it.pika.premiumreports.api.events.ReportsInitializeEvent;
import it.pika.premiumreports.commands.ReportCmd;
import it.pika.premiumreports.hooks.PlaceholdersHook;
import it.pika.premiumreports.listeners.JoinListener;
import it.pika.premiumreports.objects.Report;
import it.pika.premiumreports.storage.MySQLCredentials;
import it.pika.premiumreports.storage.Storage;
import it.pika.premiumreports.storage.StorageType;
import it.pika.premiumreports.storage.impl.MySQL;
import it.pika.premiumreports.storage.impl.SQLite;
import it.pika.premiumreports.utils.LanguageManager;
import it.pika.premiumreports.utils.Logger;
import it.pika.premiumreports.utils.UpdateChecker;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance = null;
    @Getter
    private static final Logger console = new Logger("PremiumReports");
    @Getter
    private static Storage storage = null;
    @Getter
    private static InventoryManager inventoryManager = null;
    @Getter
    private static LanguageManager languageManager = null;
    @Getter
    private static PlaceholdersHook placeholdersHook = null;

    @Getter
    private static Config configFile = null;


    @Getter
    private static final List<Report> reports = Lists.newArrayList();
    public static final String VERSION = "1.3.5";

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));

        if (!setupPlaceholders())
            console.warning("PlaceholderAPI not found, you will not be able to use placeholders!");
    }

    @Override
    public void onEnable() {
        instance = this;
        CommandAPI.onEnable();
        var stopwatch = Stopwatch.createStarted();

        console.info("§4  ____  ____  ");
        console.info("§4 |  _ \\|  _ \\ ");
        console.info("§4 | |_) | |_) |    §4Premium§cReports §7v%s §8| §aEnabling..".formatted(VERSION));
        console.info("§4 |  __/|  _ <     §7Made with §clove §7and §epizza §7by §bzPikaa§7.");
        console.info("§4 |_|   |_| \\_\\");
        console.info("§4              ");

        if (Reflections.getNumericalVersion() < 13) {
            stopwatch.stop();
            console.warning("Server version not supported, disabling the plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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
        checkForUpdates();

        if (isFolia())
            console.info("It sounds like you are using Folia. " +
                    "If you experience an issue while using it please report it on my Discord server.");

        stopwatch.stop();
        Bukkit.getPluginManager().callEvent(new ReportsInitializeEvent(this));
        new Metrics(this, 19232);

        console.info("Plugin enabled in %s ms.".formatted(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        console.info("For support join my Discord server: dsc.gg/premiumreports");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();

        if (storage != null)
            storage.close();
    }

    @SneakyThrows
    private void setupFiles() {
        configFile = new Config(this, "config.yml");

        ConfigUpdater.update(this, "config.yml", configFile.getFile());

        configFile.reload();

        languageManager = new LanguageManager();
        languageManager.init();
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
        console.info(""); // For a good appearance
        return true;
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinListener(), this);
    }

    private void registerCommands() {
        new ReportCmd().get().register();
    }

    private void setupInventories() {
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
    }

    private void checkForUpdates() {
        new UpdateChecker(this, 111482).getVersion(version -> {
            if (!version.equals(VERSION))
                console.warning("A new update is available! Download it from the official SpigotMC page");
        });
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean setupPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return false;

        placeholdersHook = new PlaceholdersHook();
        return true;
    }

    public static String parseMessage(String s, Report report) {
        var dateFormat = new SimpleDateFormat(Objects.requireNonNull(configFile.getString("Options.Date-Format")));

        return Chat.parseColors(s.replaceAll("%reporter%", report.getReporter())
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

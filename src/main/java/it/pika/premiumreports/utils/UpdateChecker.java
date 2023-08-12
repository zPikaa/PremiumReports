package it.pika.premiumreports.utils;

import it.pika.premiumreports.Main;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

@AllArgsConstructor
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;

    public void getVersion(Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId)
                    .openStream(); Scanner scanner = new Scanner(is)) {
                if (scanner.hasNext())
                    consumer.accept(scanner.next());
            } catch (IOException e) {
                Main.getConsole().warning("Unable to check for updates: %s".formatted(e.getMessage()));
            }
        });
    }

}
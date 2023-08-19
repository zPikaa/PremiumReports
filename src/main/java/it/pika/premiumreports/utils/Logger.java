package it.pika.premiumreports.utils;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;

@AllArgsConstructor
public class Logger {

    private String name;

    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage("[%s] %s".formatted(name, message));
    }

    public void warning(String message) {
        Bukkit.getConsoleSender().sendMessage("§e[%s] %s".formatted(name, message));
    }

    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§c[%s] %s".formatted(name, message));
    }

}

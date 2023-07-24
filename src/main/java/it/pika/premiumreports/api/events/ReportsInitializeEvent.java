package it.pika.premiumreports.api.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The event is triggered when the PremiumReports plugin finishes its initialization
 */
public class ReportsInitializeEvent extends Event {

    @Getter
    private final Plugin plugin;

    public ReportsInitializeEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}

package it.pika.premiumreports.api.events;

import it.pika.premiumreports.objects.Report;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The event is triggered when a new report is submitted
 */
public class ReportSubmitEvent extends Event implements Cancellable {

    @Getter
    @Setter
    private boolean cancelled = false;
    @Getter
    private final Player player;
    @Getter
    private final Report report;

    public ReportSubmitEvent(Player player, Report report) {
        this.player = player;
        this.report = report;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}

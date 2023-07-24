package it.pika.premiumreports.api.events;

import it.pika.premiumreports.objects.Report;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The event is triggered when a report is completed
 */
public class ReportCompleteEvent extends Event {

    @Getter
    private final Player completer;
    @Getter
    private final Report report;
    @Getter
    private final Report.Result result;

    public ReportCompleteEvent(Player player, Report report, Report.Result result) {
        this.completer = player;
        this.report = report;
        this.result = result;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}

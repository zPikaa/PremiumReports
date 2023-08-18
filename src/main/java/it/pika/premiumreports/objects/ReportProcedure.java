package it.pika.premiumreports.objects;

import io.papermc.paper.event.player.AsyncChatEvent;
import it.pika.libs.chat.Chat;
import it.pika.libs.reflection.Reflections;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.enums.Messages;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class ReportProcedure implements Listener {

    private final Player player;
    private boolean active;
    private Phase phase;
    private String reported;

    public ReportProcedure(Player player) {
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    public void start() {
        phase = Phase.REPORTED;
        active = true;


        player.sendTitle(Chat.parseColors(Main.getConfigFile().getString("Report-Procedure.Reported.Title")),
                Chat.parseColors(Main.getConfigFile().getString("Report-Procedure.Reported.Sub-Title")),
                10, 9999999, 0);
    }

    public void end() {
        phase = null;
        active = false;

        Reflections.sendTitle(player, "", "", 0, 1, 0);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var message = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (!active || phase == null || !event.getPlayer().equals(player))
            return;

        event.setCancelled(true);
        if (phase == Phase.REPORTED) {
            if (message.equalsIgnoreCase(player.getName())
                    && !Main.getConfigFile().getBoolean("Options.Report-Himself")) {
                error(player, Messages.REPORT_YOURSELF.get());
                end();
                return;
            }

            if (Bukkit.getPlayer(message) == null &&
                    Main.getConfigFile().getBoolean("Options.Reported-Online")) {
                error(player, Messages.REPORTED_OFFLINE.get());
                end();
                return;
            }

            reported = message;
            phase = Phase.REASON;

            player.sendTitle(Chat.parseColors(Main.getConfigFile().getString("Report-Procedure.Reason.Title")),
                    Chat.parseColors(Main.getConfigFile().getString("Report-Procedure.Reason.Sub-Title")),
                    10, 9999999, 0);
            return;
        }

        if (phase == Phase.REASON) {
            end();

            Main.getStorage().createReport(player, reported, message);
            success(player, Messages.REPORT_CREATED.get());
        }
    }

    enum Phase {
        REPORTED,
        REASON
    }

}

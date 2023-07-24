package it.pika.premiumreports.listeners;

import it.pika.premiumreports.Main;
import it.pika.premiumreports.objects.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        if (User.of(player) != null)
            return;

        Main.getStorage().createUser(player, Main.getConfigFile().getInt("Options-Default-Points"));
        Main.getConsole().info("Created user: %s".formatted(player.getName()));
    }

}

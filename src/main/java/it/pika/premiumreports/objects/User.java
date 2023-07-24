package it.pika.premiumreports.objects;

import it.pika.premiumreports.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

@AllArgsConstructor
@Getter @Setter
public class User {

    private OfflinePlayer player;
    private int points;
    private boolean blocked;

    public static User of(OfflinePlayer player) {
        return Main.getStorage().getUser(player);
    }

}

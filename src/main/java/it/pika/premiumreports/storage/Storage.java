package it.pika.premiumreports.storage;

import it.pika.premiumreports.objects.Report;
import it.pika.premiumreports.objects.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class Storage {

    public abstract void init();
    public abstract void close();
    public abstract StorageType getType();
    public abstract Report getReport(UUID id);
    public abstract User getUser(OfflinePlayer player);
    public abstract void createUser(Player player, int points);
    public abstract void createReport(Player player, String reported, String reason);
    public abstract void setResult(Report report, Report.Result result);
    public abstract void setPoints(User user, int points);
    public abstract void setBlocked(User user, boolean block);

}

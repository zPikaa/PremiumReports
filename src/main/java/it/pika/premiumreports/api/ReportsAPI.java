package it.pika.premiumreports.api;

import com.google.common.collect.Lists;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.objects.Report;
import it.pika.premiumreports.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class ReportsAPI {

    private ReportsAPI() {
    }

    public static ReportsAPI getInstance() {
        return Bukkit.getPluginManager().isPluginEnabled("PremiumReports") ? new ReportsAPI() : null;
    }

    public User getUser(OfflinePlayer player) {
        return User.of(player);
    }

    public List<Report> getReportsOf(User user) {
        var list = Lists.newArrayList(Main.getReports());
        list.removeIf(report -> !report.getReporter().equalsIgnoreCase(user.getPlayer().getName()));

        return list;
    }

    public Report getReport(UUID uuid) {
        return Report.of(uuid);
    }
}

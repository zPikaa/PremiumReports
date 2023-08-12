package it.pika.premiumreports.objects;

import com.google.common.collect.Lists;
import it.pika.premiumreports.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class User {

    private OfflinePlayer player;
    private int points;
    private boolean blocked;

    public static User of(OfflinePlayer player) {
        return Main.getStorage().getUser(player);
    }

    public List<Report> getReports() {
        List<Report> reports = Lists.newArrayList(Main.getReports());
        reports.removeIf(report -> !report.getReporter().equalsIgnoreCase(player.getName()));

        return reports;
    }

    public List<Report> getValidReports() {
        List<Report> reports = Lists.newArrayList(getReports());
        reports.removeIf(report -> report.getResult() != Report.Result.VALID);

        return reports;
    }

    public List<Report> getInvalidReports() {
        List<Report> reports = Lists.newArrayList(getReports());
        reports.removeIf(report -> report.getResult() != Report.Result.INVALID);

        return reports;
    }

}

package it.pika.premiumreports.hooks;

import it.pika.premiumreports.objects.User;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholdersHook extends PlaceholderExpansion {

    public PlaceholdersHook() {
        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "premiumreports";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Pika";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.3.5";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        var user = User.of(player);

        if (params.equalsIgnoreCase("points"))
            return String.valueOf(user.getPoints());
        if (params.equalsIgnoreCase("blocked"))
            return String.valueOf(user.isBlocked());
        if (params.equalsIgnoreCase("reports"))
            return String.valueOf(user.getReports().size());
        if (params.equalsIgnoreCase("validReports"))
            return String.valueOf(user.getValidReports().size());
        if (params.equalsIgnoreCase("invalidReports"))
            return String.valueOf(user.getInvalidReports().size());

        return "Unrecognized";
    }

}

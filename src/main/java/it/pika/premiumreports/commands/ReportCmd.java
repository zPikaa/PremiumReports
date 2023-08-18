package it.pika.premiumreports.commands;

import com.google.common.collect.Lists;
import it.pika.libs.chat.Chat;
import it.pika.libs.command.SubCommand;
import it.pika.libs.config.Config;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.Perms;
import it.pika.premiumreports.enums.Messages;
import it.pika.premiumreports.menu.StaffMenu;
import it.pika.premiumreports.menu.UserReportsMenu;
import it.pika.premiumreports.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class ReportCmd extends SubCommand {

    public ReportCmd(JavaPlugin plugin, String label, String... aliases) {
        super(plugin, label, aliases);
    }

    @Override
    public void noArgs(CommandSender sender) {
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission(Perms.USER)) {
            error(player, Messages.NO_PERMISSION.get());
            return;
        }

        new UserReportsMenu().get().open(player);
    }

    @SubCommandName("staff")
    @SubCommandPermission(Perms.STAFF)
    public void staff(CommandSender sender, String label, String[] args) {
        var player = Validator.getPlayerSender(sender);

        new StaffMenu().get().open(player);
    }

    @SubCommandName("reload")
    @SubCommandPermission(Perms.RELOAD)
    public void reload(CommandSender sender, String label, String[] args) {
        Main.getConfigFile().reload();
        Main.getLanguageManager().init();

        success(sender, Messages.PLUGIN_RELOADED.get());
    }

    @SubCommandName("block")
    @SubCommandMinArgs(1)
    @SubCommandUsage("<player>")
    @SubCommandPermission(Perms.BLOCK)
    public void block(CommandSender sender, String label, String[] args) {
        var user = User.of(Bukkit.getOfflinePlayer(args[0]));

        if (user == null) {
            error(sender, Messages.INVALID_USER.get());
            return;
        }

        if (user.isBlocked()) {
            Main.getStorage().setBlocked(user, false);
            success(sender, Messages.USER_UNBLOCKED.get());
        } else {
            Main.getStorage().setBlocked(user, true);
            success(sender, Messages.USER_BLOCKED.get());
        }
    }

    @SubCommandName("info")
    @SubCommandMinArgs(1)
    @SubCommandUsage("<player>")
    @SubCommandPermission(Perms.INFO)
    public void info(CommandSender sender, String label, String[] args) {
        var user = User.of(Bukkit.getOfflinePlayer(args[0]));

        if (user == null) {
            error(sender, Messages.INVALID_USER.get());
            return;
        }

        var config = new Config(Main.getInstance(), Main.getLanguageManager().getLanguage().getFile(), false);
        for (String s : parseUser(config.getStringList("user-info"), user))
            sender.sendMessage(s);
    }

    @SubCommandName("setPoints")
    @SubCommandUsage("<player> <points>")
    @SubCommandMinArgs(2)
    @SubCommandPermission(Perms.SET_POINTS)
    public void setPoints(CommandSender sender, String label, String[] args) {
        var user = User.of(Bukkit.getOfflinePlayer(args[0]));

        if (user == null) {
            error(sender, Messages.INVALID_USER.get());
            return;
        }

        if (!isInt(args[1])) {
            error(sender, Messages.INVALID_NUMBER.get());
            return;
        }

        Main.getStorage().setPoints(user, Integer.parseInt(args[1]));
        success(sender, Messages.POINTS_SET.get().formatted(user.getPlayer().getName()));
    }

    private List<String> parseUser(List<String> list, User user) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(Chat.parseColors(s)
                    .replaceAll("%name%", user.getPlayer().getName())
                    .replaceAll("%points%", String.valueOf(user.getPoints()))
                    .replaceAll("%blocked%", user.isBlocked() ? Messages.YES_MESSAGE.get()
                            : Messages.NO_MESSAGE.get())
                    .replaceAll("%reports%", String.valueOf(user.getReports().size()))
                    .replaceAll("%validReports%", String.valueOf(user.getValidReports().size()))
                    .replaceAll("%invalidReports%", String.valueOf(user.getInvalidReports().size())));

        return newList;
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

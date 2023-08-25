package it.pika.premiumreports.commands;

import com.google.common.collect.Lists;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import it.pika.libs.chat.Chat;
import it.pika.libs.config.Config;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.Perms;
import it.pika.premiumreports.enums.Messages;
import it.pika.premiumreports.menu.StaffMenu;
import it.pika.premiumreports.menu.UserReportsMenu;
import it.pika.premiumreports.objects.User;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Objects;

import static it.pika.libs.chat.Chat.error;
import static it.pika.libs.chat.Chat.success;

public class ReportCmd {

    public CommandAPICommand get() {
        return new CommandAPICommand("report")
                .withAliases("reports")
                .withPermission(Perms.USER)
                .withSubcommands(staff(), reload(), block(), info(), setPoints())
                .executesPlayer((player, args) -> {
                    new UserReportsMenu().get().open(player);
                });
    }

    private CommandAPICommand staff() {
        return new CommandAPICommand("staff")
                .withPermission(Perms.STAFF)
                .executesPlayer((player, args) -> {
                    new StaffMenu().get().open(player);
                });
    }

    private CommandAPICommand reload() {
        return new CommandAPICommand("reload")
                .withPermission(Perms.RELOAD)
                .executes((sender, args) -> {
                    Main.getConfigFile().reload();
                    Main.getLanguageManager().init();

                    success(sender, Messages.PLUGIN_RELOADED.get());
                });
    }

    private CommandAPICommand block() {
        return new CommandAPICommand("block")
                .withPermission(Perms.BLOCK)
                .withArguments(new OfflinePlayerArgument("player"))
                .executes((sender, args) -> {
                    var user = User.of((OfflinePlayer) args.get("player"));

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
                });
    }

    private CommandAPICommand info() {
        return new CommandAPICommand("info")
                .withPermission(Perms.INFO)
                .withArguments(new OfflinePlayerArgument("player"))
                .executes((sender, args) -> {
                    var user = User.of((OfflinePlayer) args.get("player"));

                    if (user == null) {
                        error(sender, Messages.INVALID_USER.get());
                        return;
                    }

                    var config = new Config(Main.getInstance(), Main.getLanguageManager().getLanguage().getFile(), false);
                    for (String s : parseUser(config.getStringList("user-info"), user))
                        sender.sendMessage(s);
                });
    }

    private CommandAPICommand setPoints() {
        return new CommandAPICommand("setPoints")
                .withPermission(Perms.SET_POINTS)
                .withArguments(new OfflinePlayerArgument("player"), new IntegerArgument("points"))
                .executes((sender, args) -> {
                    var user = User.of((OfflinePlayer) args.get("player"));
                    var points = (Integer) args.get("points");

                    if (points == null)
                        return;

                    if (user == null) {
                        error(sender, Messages.INVALID_USER.get());
                        return;
                    }

                    Main.getStorage().setPoints(user, points);
                    success(sender, Messages.POINTS_SET.get().formatted(user.getPlayer().getName()));
                });
    }

    private List<String> parseUser(List<String> list, User user) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(Chat.parseColors(s)
                    .replaceAll("%name%", Objects.requireNonNull(user.getPlayer().getName()))
                    .replaceAll("%points%", String.valueOf(user.getPoints()))
                    .replaceAll("%blocked%", user.isBlocked() ? Messages.YES_MESSAGE.get()
                            : Messages.NO_MESSAGE.get())
                    .replaceAll("%reports%", String.valueOf(user.getReports().size()))
                    .replaceAll("%validReports%", String.valueOf(user.getValidReports().size()))
                    .replaceAll("%invalidReports%", String.valueOf(user.getInvalidReports().size())));

        return newList;
    }

}

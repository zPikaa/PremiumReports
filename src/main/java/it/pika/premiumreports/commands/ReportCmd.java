package it.pika.premiumreports.commands;

import it.pika.libs.command.SubCommand;
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
        Main.getMessagesFile().reload();

        success(sender, Messages.PLUGIN_RELOADED.get());
    }

    @SubCommandName("block")
    @SubCommandMinArgs(1)
    @SubCommandUsage("<player>")
    @SubCommandPermission(Perms.BLOCK)
    public void block(CommandSender sender, String label, String[] args) {
        var user = User.of(Bukkit.getOfflinePlayer(args[0]));

        if (user.isBlocked()) {
            Main.getStorage().setBlocked(user, false);
            success(sender, Messages.USER_UNBLOCKED.get());
        } else {
            Main.getStorage().setBlocked(user, true);
            success(sender, Messages.USER_BLOCKED.get());
        }
    }

}

package it.pika.premiumreports.menu;

import com.google.common.collect.Lists;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.chat.Chat;
import it.pika.libs.config.Config;
import it.pika.libs.item.ItemBuilder;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.enums.Messages;
import it.pika.premiumreports.objects.ReportProcedure;
import it.pika.premiumreports.objects.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

import static it.pika.libs.chat.Chat.error;

public class UserReportsMenu implements InventoryProvider {

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(Main.getConfigFile().getString("User-Menu.Title"))
                .size(Main.getConfigFile().getInt("User-Menu.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        var pagination = contents.pagination();

        var reports = Lists.newArrayList(Main.getReports());
        reports.removeIf(report -> !report.getReporter().equalsIgnoreCase(player.getName()));

        ClickableItem[] items = new ClickableItem[reports.size()];

        for (int i = 0; i < items.length; i++) {
            var report = reports.get(i);

            int finalI = i;
            items[i] = ClickableItem.of(new ItemBuilder()
                    .material(Material.valueOf(Main.getConfigFile().getString("User-Menu.Report.Material")))
                    .name(Main.parseMessage(Main.getConfigFile().getString("User-Menu.Report.Name")
                            .replaceAll("%id%", String.valueOf(i + 1)), report))
                    .lore(Main.parseMessage(Main.getConfigFile().getStringList("User-Menu.Report.Lore"), report))
                    .build(), e -> {
                player.closeInventory();

                var config = new Config(Main.getInstance(), Main.getLanguageManager().getLanguage().getFile(), false);
                for (String s : config.getStringList("report-info"))
                    player.sendMessage(Component.text(Main.parseMessage(s, report)
                            .replaceAll("%id%", String.valueOf(finalI + 1))));
            });
        }

        pagination.setItems(items);
        pagination.setItemsPerPage((Main.getConfigFile().getInt("User-Menu.Size.Rows") * 9) - 9);

        var iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0);
        iterator.blacklist(5, 0).blacklist(5, 1).blacklist(5, 2)
                .blacklist(5, 3).blacklist(5, 4).blacklist(5, 5)
                .blacklist(5, 6).blacklist(5, 7).blacklist(5, 8);
        pagination.addToIterator(iterator);

        contents.set(SlotPos.of(5, 0), ClickableItem.empty(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("User-Menu.Your-Points.Material")))
                .name(Chat.parseColors(Main.getConfigFile().getString("User-Menu.Your-Points.Name")))
                .lore(parsePoints(Main.getConfigFile().getStringList("User-Menu.Your-Points.Lore"), player))
                .build()));

        contents.set(SlotPos.of(5, 4), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("User-Menu.New-Report.Material")))
                .name(Chat.parseColors(Main.getConfigFile().getString("User-Menu.New-Report.Name")))
                .lore(Chat.parseColors(Main.getConfigFile().getStringList("User-Menu.New-Report.Lore")))
                .build(), e -> {
            player.closeInventory();

            var user = User.of(player);
            if (user.isBlocked() || user.getPoints() <= 0) {
                error(player, Messages.CANT_CREATE.get());
                return;
            }

            new ReportProcedure(player).start();
        }));

        contents.set(SlotPos.of(5, 3), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("User-Menu.Previous-Page.Material")))
                .name(Chat.parseColors(Main.getConfigFile().getString("User-Menu.Previous-Page.Name")))
                .lore(Chat.parseColors(Main.getConfigFile().getStringList("User-Menu.Previous-Page.Lore")))
                .build(), e -> contents.inventory().open(player, pagination.previous().getPage())));

        contents.set(SlotPos.of(5, 5), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("User-Menu.Next-Page.Material")))
                .name(Chat.parseColors(Main.getConfigFile().getString("User-Menu.Next-Page.Name")))
                .lore(Chat.parseColors(Main.getConfigFile().getStringList("User-Menu.Next-Page.Lore")))
                .build(), e -> contents.inventory().open(player, pagination.next().getPage())));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private List<String> parsePoints(List<String> list, Player player) {
        List<String> newList = Lists.newArrayList();

        for (String s : list)
            newList.add(Chat.parseColors(s).replaceAll("%points%",
                    String.valueOf(User.of(player).getPoints())));

        return newList;
    }

}

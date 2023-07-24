package it.pika.premiumreports.menu;

import com.google.common.collect.Lists;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import it.pika.libs.item.ItemBuilder;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.api.events.ReportCompleteEvent;
import it.pika.premiumreports.enums.Messages;
import it.pika.premiumreports.objects.Report;
import it.pika.premiumreports.objects.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Objects;

import static it.pika.libs.chat.Chat.success;

public class StaffMenu implements InventoryProvider {

    public SmartInventory get() {
        return SmartInventory.builder()
                .id("inv")
                .title(Main.getConfigFile().getString("Staff-Menu.Title"))
                .size(Main.getConfigFile().getInt("Staff-Menu.Size.Rows"), 9)
                .provider(this)
                .manager(Main.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        var pagination = contents.pagination();

        var reports = Lists.newArrayList(Main.getReports());
        ClickableItem[] items = new ClickableItem[reports.size()];

        for (int i = 0; i < items.length; i++) {
            var report = reports.get(i);
            int finalI = i;

            if (report.getResult() == Report.Result.UNDEFINED) {
                items[i] = ClickableItem.of(new ItemBuilder()
                        .material(Material.valueOf(Main.getConfigFile().getString("Staff-Menu.Pending-Report.Material")))
                        .name(Main.parseMessage(Main.getConfigFile().getString("Staff-Menu.Pending-Report.Name")
                                .replaceAll("%id%", String.valueOf(i+1)), report))
                        .lore(Main.parseMessage(Main.getConfigFile().getStringList("Staff-Menu.Pending-Report.Lore"), report))
                        .build(), e -> {
                    if (e.isLeftClick()) {
                        player.closeInventory();

                        Main.getStorage().setResult(report, Report.Result.VALID);
                        Main.getReports().get(finalI).setResult(Report.Result.VALID);

                        var user = User.of(Bukkit.getOfflinePlayer(report.getReporter()));
                        Main.getStorage().setPoints(user, user.getPoints()
                                + Main.getConfigFile().getInt("Options.Valid-Report-Points"));

                        success(player, Messages.REPORT_VALIDATED.get());

                        var event = new ReportCompleteEvent(player, Main.getReports().get(finalI), Report.Result.VALID);
                        Bukkit.getPluginManager().callEvent(event);

                        if (user.getPlayer() != null)
                            success(Objects.requireNonNull(user.getPlayer().getPlayer()), Messages.REPORT_VALID.get()
                                    .formatted(Main.getConfigFile().getInt("Options.Valid-Report-Points")));
                    } else if (e.isRightClick()) {
                        player.closeInventory();

                        Main.getStorage().setResult(report, Report.Result.INVALID);
                        Main.getReports().get(finalI).setResult(Report.Result.VALID);

                        var user = User.of(Bukkit.getOfflinePlayer(report.getReporter()));
                        Main.getStorage().setPoints(user, user.getPoints()
                                - Main.getConfigFile().getInt("Options.Invalid-Report-Points"));

                        success(player, Messages.REPORT_INVALIDATED.get());

                        var event = new ReportCompleteEvent(player, Main.getReports().get(finalI), Report.Result.INVALID);
                        Bukkit.getPluginManager().callEvent(event);

                        if (user.getPlayer() != null)
                            success(Objects.requireNonNull(user.getPlayer().getPlayer()), Messages.REPORT_INVALID.get()
                                    .formatted(Main.getConfigFile().getInt("Options.Invalid-Report-Points")));
                    }
                });
            } else {
                items[i] = ClickableItem.of(new ItemBuilder()
                        .material(Material.valueOf(Main.getConfigFile().getString("Staff-Menu.Completed-Report.Material")))
                        .name(Main.parseMessage(Main.getConfigFile().getString("Staff-Menu.Completed-Report.Name"), report)
                                .replaceAll("%id%", String.valueOf(i+1)))
                        .lore(Main.parseMessage(Main.getConfigFile().getStringList("Staff-Menu.Completed-Report.Lore"), report))
                        .build(), e -> {
                    player.closeInventory();

                    for (String s : Main.getMessagesFile().getStringList("report-info"))
                        player.sendMessage(Component.text(Main.parseMessage(s, report)
                                .replaceAll("%id%", String.valueOf(finalI+1))));
                });
            }
        }

        pagination.setItems(items);
        pagination.setItemsPerPage((Main.getConfigFile().getInt("Staff-Menu.Size.Rows") * 9) - 9);

        var iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0);
        iterator.blacklist(5, 0).blacklist(5, 1).blacklist(5, 2)
                .blacklist(5, 3).blacklist(5, 4).blacklist(5, 5)
                .blacklist(5, 6).blacklist(5, 7).blacklist(5, 8);
        pagination.addToIterator(iterator);

        contents.set(SlotPos.of(5, 4), ClickableItem.empty(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Staff-Menu.Current-Page.Material")))
                .name(Main.parseColors(Main.getConfigFile().getString("Staff-Menu.Current-Page.Name")
                                .replaceAll("%page%", String.valueOf(pagination.getPage()+1))))
                .lore(Main.parseColors(Main.getConfigFile().getStringList("Staff-Menu.Current-Page.Lore")))
                .build()));

        contents.set(SlotPos.of(5, 3), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Staff-Menu.Previous-Page.Material")))
                .name(Main.parseColors(Main.getConfigFile().getString("Staff-Menu.Previous-Page.Name")))
                .lore(Main.parseColors(Main.getConfigFile().getStringList("Staff-Menu.Previous-Page.Lore")))
                .build(), e -> contents.inventory().open(player, pagination.previous().getPage())));

        contents.set(SlotPos.of(5, 5), ClickableItem.of(new ItemBuilder()
                .material(Material.valueOf(Main.getConfigFile().getString("Staff-Menu.Next-Page.Material")))
                .name(Main.parseColors(Main.getConfigFile().getString("Staff-Menu.Next-Page.Name")))
                .lore(Main.parseColors(Main.getConfigFile().getStringList("Staff-Menu.Next-Page.Lore")))
                .build(), e -> contents.inventory().open(player, pagination.next().getPage())));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}

package net.earthmc.quarters.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.quarters.api.QuartersMessaging;
import net.earthmc.quarters.object.Quarter;
import net.earthmc.quarters.object.QuartersTown;
import net.earthmc.quarters.util.CommandUtil;
import net.earthmc.quarters.util.QuarterUtil;
import org.bukkit.entity.Player;

@CommandAlias("quarters|q")
public class SellCommand extends BaseCommand {
    @Subcommand("sell")
    @Description("Sell a quarter")
    @CommandPermission("quarters.command.quarters.sell")
    @CommandCompletion("{price}|cancel")
    public void onSell(Player player, @Optional @Single String arg) {
        if (!CommandUtil.hasPermissionOrMayor(player, "quarters.action.sell"))
            return;

        if (!CommandUtil.isPlayerInQuarter(player))
            return;

        Quarter quarter = QuarterUtil.getQuarter(player.getLocation());
        assert quarter != null;
        if (!CommandUtil.isQuarterInPlayerTown(player, quarter))
            return;

        Town town = quarter.getTown();
        if (arg != null && arg.equals("cancel")) {
            cancelQuarterSale(player, quarter);

            QuartersMessaging.sendInfoMessageToTown(town, player, player.getName() + " 已经变更一个公寓的状态为取消出售 " + QuartersMessaging.getLocationString(player.getLocation()));
            return;
        }

        Double price = getSellPrice(player, arg);
        if (price == null)
            return;

        if (price < 0) {
            QuartersMessaging.sendErrorMessage(player, "价格必须大于等于 0");
            return;
        }

        setQuarterForSale(player, quarter, price);

        if (TownyEconomyHandler.isActive()) {
            QuartersMessaging.sendInfoMessageToTown(town, player, player.getName() + " 已经变更一个公寓的状态为正在出售 " + TownyEconomyHandler.getFormattedBalance(price) + " " + QuartersMessaging.getLocationString(player.getLocation()));
        } else if (!TownyEconomyHandler.isActive()) {
            QuartersMessaging.sendInfoMessageToTown(town, player, player.getName() + " 已经变更一个公寓的状态为正在出售 " + QuartersMessaging.getLocationString(player.getLocation()));
        }
    }

    public static void cancelQuarterSale(Player player, Quarter quarter) {
        quarter.setPrice(null);
        quarter.save();
        QuartersMessaging.sendSuccessMessage(player, "这个公寓不再出售了");
    }

    public static Double getSellPrice(Player player, String arg) {
        double price;
        try {
            if (arg == null) { // If the user has specified no argument we will set it to the configured default price or to 0
                QuartersTown quartersTown = new QuartersTown(TownyAPI.getInstance().getTown(player));
                Double defaultPrice = quartersTown.getDefaultSellPrice();
                if (defaultPrice != null) {
                    price = defaultPrice;
                } else {
                    price = 0.0;
                }
            } else {
                price = Double.parseDouble(arg);
            }
        } catch (NumberFormatException e) {
            QuartersMessaging.sendErrorMessage(player, "Invalid argument");
            return null;
        }

        return price;
    }

    public static void setQuarterForSale(Player player, Quarter quarter, double price) {
        if (!TownyEconomyHandler.isActive()) // If the server is using economy then we set it for sale at the designated price, otherwise it defaults to free
            price = 0.0;

        quarter.setPrice(price);
        quarter.setLastPrice(price);
        quarter.save();

        if (TownyEconomyHandler.isActive()) {
            QuartersMessaging.sendSuccessMessage(player, "这个公寓租价为 " + TownyEconomyHandler.getFormattedBalance(price) + "您可以通过/q sell 价格 来调整售价");
        } else if (!TownyEconomyHandler.isActive()) {
            QuartersMessaging.sendSuccessMessage(player, "这个公寓现在是可租用的，使用 /q claim 来租用");
        }
    }
}

package net.earthmc.quarters.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.quarters.api.QuartersMessaging;
import net.earthmc.quarters.object.Quarter;
import net.earthmc.quarters.object.QuartersPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class CommandUtil {
    public static boolean isPlayerInQuarter(Player player) {
        QuartersPlayer quartersPlayer = new QuartersPlayer(player);

        if (!quartersPlayer.isInQuarter()) {
            QuartersMessaging.sendErrorMessage(player, "您没有站在任何一个公寓内");
            return false;
        }

        return true;
    }

    public static boolean isSelectionValid(Player player, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            QuartersMessaging.sendErrorMessage(player, "You must select two valid positions using the Quarters wand, or by using /quarters {pos1/pos2}");
            return false;
        }

        return true;
    }

    public static boolean isQuarterInPlayerTown(Player player, Quarter quarter) {
        if (quarter.getTown() != TownyAPI.getInstance().getTown(player)) {
            QuartersMessaging.sendErrorMessage(player, "这个公寓不是您城镇的一部分");
            return false;
        }

        return true;
    }

    public static boolean hasPermission(Player player, String permission) {
        if (!(player.hasPermission(permission))) {
            QuartersMessaging.sendErrorMessage(player, "You do not have permission to perform this action");
            return false;
        }

        return true;
    }

    public static boolean hasPermissionOrMayor(Player player, String permission) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;

        if (!(player.hasPermission(permission) || resident.isMayor())) {
            QuartersMessaging.sendErrorMessage(player, "You do not have permission to perform this action");
            return false;
        }

        return true;
    }

    public static boolean hasPermissionOrMayorOrQuarterOwner(Player player, Quarter quarter, String permission) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;

        if (!Objects.equals(quarter.getOwnerResident(), resident)
                && (!player.hasPermission(permission) || !quarter.getTown().equals(resident.getTownOrNull()))
                && !quarter.getTown().getMayor().equals(resident)) {
            QuartersMessaging.sendErrorMessage(player, "You do not have permission to perform this action");
            return false;
        }

        return true;
    }
}

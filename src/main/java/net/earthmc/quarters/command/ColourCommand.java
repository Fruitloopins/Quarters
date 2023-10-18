package net.earthmc.quarters.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.earthmc.quarters.api.QuartersMessaging;
import net.earthmc.quarters.object.Quarter;
import net.earthmc.quarters.util.CommandUtil;
import net.earthmc.quarters.util.QuarterUtil;
import org.bukkit.entity.Player;

@CommandAlias("quarters|q")
public class ColourCommand extends BaseCommand {
    @Subcommand("colour")
    @Description("Change particle outline colour of a quarter")
    @CommandPermission("quarters.command.quarters.colour")
    @CommandCompletion("@range:0-255 @range:0-255 @range:0-255")
    public void onColour(Player player, int r, int g, int b) {
        if (!CommandUtil.hasPermissionOrMayor(player, "quarters.action.colour"))
            return;

        if (!CommandUtil.isPlayerInQuarter(player))
            return;

        Quarter quarter = QuarterUtil.getQuarter(player.getLocation());
        if (!CommandUtil.isQuarterInPlayerTown(player, quarter))
            return;

        int[] rgb = new int[]{r, g, b};

        for (int colour : rgb) {
            if (colour < 0 || colour > 255) {
                QuartersMessaging.sendErrorMessage(player, "Specified int is out of range, value must be between 0-255");
                return;
            }
        }

        quarter.setRGB(rgb);
        quarter.save();

        QuartersMessaging.sendSuccessMessage(player, "Successfully changed this quarter's colour");
    }
}
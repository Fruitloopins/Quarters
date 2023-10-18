package net.earthmc.quarters.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.quarters.api.QuartersMessaging;
import net.earthmc.quarters.object.Quarter;
import net.earthmc.quarters.util.CommandUtil;
import net.earthmc.quarters.util.QuarterUtil;
import org.bukkit.entity.Player;

import java.time.Instant;

@CommandAlias("quartersadmin|qa")
public class AdminSetOwnerCommand extends BaseCommand {
    @Subcommand("setowner")
    @Description("Forcefully change a quarter's owner")
    @CommandPermission("quarters.command.quartersadmin.setowner")
    @CommandCompletion("@players")
    public void onSetOwner(Player player, String target) {
        if (!CommandUtil.isPlayerInQuarter(player))
            return;

        Quarter quarter = QuarterUtil.getQuarter(player.getLocation());
        assert quarter != null;

        Resident targetResident = TownyAPI.getInstance().getResident(target);
        if (targetResident == null || targetResident.isNPC()) {
            QuartersMessaging.sendErrorMessage(player, "Specified player does not exist");
            return;
        }

        quarter.setOwnerUUID(targetResident.getUUID());
        quarter.setClaimedAt(Instant.now().toEpochMilli());
        quarter.save();

        QuartersMessaging.sendSuccessMessage(player, "Successfully set this quarter's owner to " + targetResident.getName());
    }
}
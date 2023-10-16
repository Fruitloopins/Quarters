package net.earthmc.quarters.task;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.quarters.Quarters;
import net.earthmc.quarters.api.QuartersAPI;
import net.earthmc.quarters.manager.TownMetadataManager;
import net.earthmc.quarters.object.Cuboid;
import net.earthmc.quarters.object.Quarter;
import net.earthmc.quarters.object.QuartersPlayer;
import net.earthmc.quarters.object.Selection;
import net.earthmc.quarters.manager.SelectionManager;
import net.earthmc.quarters.util.QuarterUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class OutlineParticleTask extends BukkitRunnable {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            QuartersPlayer quartersPlayer = QuartersAPI.getInstance().getQuartersPlayer(player);
            if (!QuarterUtil.shouldRenderOutlines(quartersPlayer, player.getInventory().getItemInMainHand().getType()))
                continue;

            createParticlesIfSelectionExists(player);

            createParticlesIfQuartersExist(player);
        }
    }

    public static void createParticlesIfSelectionExists(Player player) {
        Selection selection = SelectionManager.selectionMap.get(player);

        if (selection != null) {
            Location pos1 = selection.getPos1();
            Location pos2 = selection.getPos2();

            if (pos1 != null && pos2 != null) {
                createParticlesAtCuboidEdges(player, pos1, pos2, Particle.valueOf(Quarters.INSTANCE.getConfig().getString("selection_particle")));
            }
        }
    }

    public static void createParticlesIfQuartersExist(Player player) {
        Town town = TownyAPI.getInstance().getTown(player.getLocation());

        if (town != null) {
            List<Quarter> quarterList = TownMetadataManager.getQuarterListOfTown(town);
            if (quarterList != null) {
                for (Quarter quarter : quarterList) {
                    for (Cuboid cuboid : quarter.getCuboids()) {
                        createParticlesAtCuboidEdges(player, cuboid.getPos1(), cuboid.getPos2(), Particle.valueOf(Quarters.INSTANCE.getConfig().getString("created_particle")));
                    }
                }
            }
        }
    }

    private static void createParticlesAtCuboidEdges(Player player, Location pos1, Location pos2, Particle particle) {
        Cuboid cuboid = new Cuboid(pos1, pos2);
        int x1 = pos1.getBlockX();
        int y1 = pos1.getBlockY();
        int z1 = pos1.getBlockZ();
        int x2 = pos2.getBlockX();
        int y2 = pos2.getBlockY();
        int z2 = pos2.getBlockZ();

        int volume = cuboid.getLength() * cuboid.getHeight() * cuboid.getWidth();

        if (volume > Quarters.INSTANCE.getConfig().getInt("max_particle_volume"))
            return;

        List<int[]> edges = new ArrayList<>();
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            edges.add(new int[]{x, y1, z1});
            edges.add(new int[]{x, y2, z1});
            edges.add(new int[]{x, y1, z2});
            edges.add(new int[]{x, y2, z2});
        }

        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            edges.add(new int[]{x1, y, z1});
            edges.add(new int[]{x2, y, z1});
            edges.add(new int[]{x1, y, z2});
            edges.add(new int[]{x2, y, z2});
        }

        for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
            edges.add(new int[]{x1, y1, z});
            edges.add(new int[]{x2, y1, z});
            edges.add(new int[]{x1, y2, z});
            edges.add(new int[]{x2, y2, z});
        }

        for (int[] coordinate : edges) {
            player.spawnParticle(particle, coordinate[0] + 0.5, coordinate[1] + 0.5, coordinate[2] + 0.5, 1, 0, 0, 0, 0);
        }
    }
}

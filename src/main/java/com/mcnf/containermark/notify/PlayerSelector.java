package com.mcnf.containermark.notify;

import com.mcnf.containermark.config.MarkConfig;
import com.mcnf.containermark.team.TeamManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelector {

    public static List<ServerPlayerEntity> selectPlayers(MinecraftServer server, ServerPlayerEntity marker, BlockPos centerPos) {
        MarkConfig config = MarkConfig.get();
        List<ServerPlayerEntity> result = new ArrayList<>();

        switch (config.notifyRange) {
            case ALL:
                result.addAll(server.getPlayerManager().getPlayerList());
                break;

            case RADIUS:
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                    // Only same dimension
                    if (!p.getEntityWorld().getRegistryKey().equals(marker.getEntityWorld().getRegistryKey())) continue;
                    double dist = p.getEntityPos().distanceTo(Vec3d.ofCenter(centerPos));
                    if (dist <= config.radius) {
                        result.add(p);
                    }
                }
                // Always include marker
                if (!result.contains(marker)) result.add(marker);
                break;

            case TEAM:
                result = TeamManager.getTeammates(server, marker.getUuid());
                // Always include the marker themselves
                if (!result.contains(marker)) result.add(marker);
                break;
        }

        return result;
    }
}

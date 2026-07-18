package com.mcnf.containermark.team;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamManager {

    public static void createTeam(ServerPlayerEntity player, String name) {
        TeamState state = TeamState.getServerState(player.getEntityWorld().getServer());

        if (state.getTeam(name) != null) {
            player.sendMessage(Text.translatable("message.containermark.team_exists", name).formatted(Formatting.RED), false);
            return;
        }

        // Check if player is already in a team
        if (state.getTeamOfPlayer(player.getUuid()) != null) {
            player.sendMessage(Text.literal("You are already in a team. Leave first.").formatted(Formatting.RED), false);
            return;
        }

        Team team = new Team(name, player.getUuid());
        state.addTeam(team);
        player.sendMessage(Text.translatable("message.containermark.team_created", name).formatted(Formatting.GREEN), false);
    }

    public static void invitePlayer(ServerPlayerEntity leader, ServerPlayerEntity target) {
        TeamState state = TeamState.getServerState(leader.getEntityWorld().getServer());
        Team team = state.getTeamOfPlayer(leader.getUuid());

        if (team == null) {
            leader.sendMessage(Text.translatable("message.containermark.no_team").formatted(Formatting.RED), false);
            return;
        }

        if (!team.isLeader(leader.getUuid())) {
            leader.sendMessage(Text.translatable("message.containermark.not_leader").formatted(Formatting.RED), false);
            return;
        }

        if (team.isMember(target.getUuid())) {
            leader.sendMessage(Text.literal(target.getName().getString() + " is already in your team.").formatted(Formatting.YELLOW), false);
            return;
        }

        team.invite(target.getUuid(), System.currentTimeMillis());
        state.markDirty();

        leader.sendMessage(Text.translatable("message.containermark.invited", target.getName().getString(), team.getName()).formatted(Formatting.GREEN), false);
        target.sendMessage(Text.translatable("message.containermark.invite_received", leader.getName().getString(), team.getName(), team.getName()).formatted(Formatting.GREEN), false);
    }

    public static void acceptInvite(ServerPlayerEntity player, String teamName) {
        TeamState state = TeamState.getServerState(player.getEntityWorld().getServer());
        Team team = state.getTeam(teamName);

        if (team == null) {
            player.sendMessage(Text.translatable("message.containermark.no_team_exists", teamName).formatted(Formatting.RED), false);
            return;
        }

        if (!team.hasInvite(player.getUuid())) {
            player.sendMessage(Text.translatable("message.containermark.no_invite", teamName).formatted(Formatting.RED), false);
            return;
        }

        // Remove from old team if any
        Team oldTeam = state.getTeamOfPlayer(player.getUuid());
        if (oldTeam != null) {
            oldTeam.removeMember(player.getUuid());
            if (oldTeam.size() == 0) {
                state.removeTeam(oldTeam.getName());
            }
        }

        team.addMember(player.getUuid());
        state.markDirty();

        player.sendMessage(Text.translatable("message.containermark.joined", teamName).formatted(Formatting.GREEN), false);
    }

    public static void leaveTeam(ServerPlayerEntity player) {
        TeamState state = TeamState.getServerState(player.getEntityWorld().getServer());
        Team team = state.getTeamOfPlayer(player.getUuid());

        if (team == null) {
            player.sendMessage(Text.translatable("message.containermark.no_team").formatted(Formatting.RED), false);
            return;
        }

        String teamName = team.getName();
        team.removeMember(player.getUuid());

        if (team.size() == 0) {
            state.removeTeam(teamName);
        } else if (team.isLeader(player.getUuid())) {
            // Transfer leadership to first remaining member
            team.setLeader(team.getMembers().iterator().next());
        }

        state.markDirty();
        player.sendMessage(Text.translatable("message.containermark.left", teamName).formatted(Formatting.GREEN), false);
    }

    public static void disbandTeam(ServerPlayerEntity player) {
        TeamState state = TeamState.getServerState(player.getEntityWorld().getServer());
        Team team = state.getTeamOfPlayer(player.getUuid());

        if (team == null) {
            player.sendMessage(Text.translatable("message.containermark.no_team").formatted(Formatting.RED), false);
            return;
        }

        if (!team.isLeader(player.getUuid())) {
            player.sendMessage(Text.translatable("message.containermark.not_leader").formatted(Formatting.RED), false);
            return;
        }

        String teamName = team.getName();
        state.removeTeam(teamName);
        player.sendMessage(Text.translatable("message.containermark.disbanded", teamName).formatted(Formatting.GREEN), false);
    }

    public static void listTeam(ServerPlayerEntity player) {
        TeamState state = TeamState.getServerState(player.getEntityWorld().getServer());
        Team team = state.getTeamOfPlayer(player.getUuid());

        if (team == null) {
            player.sendMessage(Text.translatable("message.containermark.no_team").formatted(Formatting.YELLOW), false);
            return;
        }

        MinecraftServer server = player.getEntityWorld().getServer();
        StringBuilder memberNames = new StringBuilder();
        for (UUID memberUuid : team.getMembers()) {
            ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberUuid);
            String name = member != null ? member.getName().getString() : memberUuid.toString().substring(0, 8);
            if (memberNames.length() > 0) memberNames.append(", ");
            memberNames.append(name);
        }

        player.sendMessage(Text.translatable("message.containermark.team_list", team.getName(), memberNames.toString()).formatted(Formatting.GREEN), false);
    }

    public static List<ServerPlayerEntity> getTeammates(MinecraftServer server, UUID playerUuid) {
        List<ServerPlayerEntity> result = new ArrayList<>();
        TeamState state = TeamState.getServerState(server);
        Team team = state.getTeamOfPlayer(playerUuid);

        if (team == null) {
            // No team, return just the player
            ServerPlayerEntity self = server.getPlayerManager().getPlayer(playerUuid);
            if (self != null) result.add(self);
            return result;
        }

        for (UUID memberUuid : team.getMembers()) {
            ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberUuid);
            if (member != null) {
                result.add(member);
            }
        }

        return result;
    }
}

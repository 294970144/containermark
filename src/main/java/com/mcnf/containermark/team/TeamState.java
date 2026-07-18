package com.mcnf.containermark.team;

import com.mcnf.containermark.ContainerMark;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TeamState extends PersistentState {

    private final Map<String, Team> teams = new HashMap<>();

    public TeamState() {}

    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    public Team getTeam(String name) {
        return teams.get(name);
    }

    public void addTeam(Team team) {
        teams.put(team.getName(), team);
        markDirty();
    }

    public void removeTeam(String name) {
        teams.remove(name);
        markDirty();
    }

    public Team getTeamOfPlayer(java.util.UUID uuid) {
        for (Team team : teams.values()) {
            if (team.isMember(uuid)) return team;
        }
        return null;
    }

    public NbtCompound toNbt() {
        NbtList teamList = new NbtList();
        for (Team team : teams.values()) {
            teamList.add(team.toNbt());
        }
        NbtCompound nbt = new NbtCompound();
        nbt.put("teams", teamList);
        return nbt;
    }

    public static TeamState createFromNbt(NbtCompound nbt) {
        TeamState state = new TeamState();
        NbtList list = nbt.getListOrEmpty("teams");
        for (int i = 0; i < list.size(); i++) {
            NbtCompound teamNbt = list.getCompoundOrEmpty(i);
            Team team = Team.fromNbt(teamNbt);
            state.teams.put(team.getName(), team);
        }
        return state;
    }

    private static final Codec<TeamState> CODEC = NbtCompound.CODEC.flatXmap(
        nbt -> DataResult.success(TeamState.createFromNbt(nbt)),
        state -> DataResult.success(state.toNbt())
    );

    private static final PersistentStateType<TeamState> TYPE = new PersistentStateType<>(
        ContainerMark.MOD_ID,
        TeamState::new,
        CODEC,
        null
    );

    public static TeamState getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        TeamState state = manager.getOrCreate(TYPE);
        state.markDirty();
        return state;
    }
}

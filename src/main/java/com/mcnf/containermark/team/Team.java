package com.mcnf.containermark.team;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Uuids;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Team {
    private final String name;
    private UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final Map<UUID, Long> invites = new HashMap<>(); // invited UUID -> invite timestamp

    public Team(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public Set<UUID> getMembers() { return members; }
    public Map<UUID, Long> getInvites() { return invites; }

    public boolean isMember(UUID uuid) { return members.contains(uuid); }
    public boolean isLeader(UUID uuid) { return leader.equals(uuid); }

    public void addMember(UUID uuid) {
        members.add(uuid);
        invites.remove(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        invites.remove(uuid);
    }

    public void invite(UUID uuid, long timestamp) {
        invites.put(uuid, timestamp);
    }

    public boolean hasInvite(UUID uuid) {
        return invites.containsKey(uuid);
    }

    public int size() { return members.size(); }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putIntArray("leader", Uuids.toIntArray(leader));

        NbtList memberList = new NbtList();
        for (UUID member : members) {
            NbtCompound memberNbt = new NbtCompound();
            memberNbt.putIntArray("uuid", Uuids.toIntArray(member));
            memberList.add(memberNbt);
        }
        nbt.put("members", memberList);

        NbtList inviteList = new NbtList();
        for (Map.Entry<UUID, Long> entry : invites.entrySet()) {
            NbtCompound inviteNbt = new NbtCompound();
            inviteNbt.putIntArray("uuid", Uuids.toIntArray(entry.getKey()));
            inviteNbt.putLong("timestamp", entry.getValue());
            inviteList.add(inviteNbt);
        }
        nbt.put("invites", inviteList);

        return nbt;
    }

    public static Team fromNbt(NbtCompound nbt) {
        String name = nbt.getString("name", "");
        UUID leader = Uuids.toUuid(nbt.getIntArray("leader").orElse(new int[0]));
        Team team = new Team(name, leader);

        team.members.clear();
        NbtList memberList = nbt.getListOrEmpty("members");
        for (int i = 0; i < memberList.size(); i++) {
            NbtCompound memberNbt = memberList.getCompoundOrEmpty(i);
            team.members.add(Uuids.toUuid(memberNbt.getIntArray("uuid").orElse(new int[0])));
        }

        team.invites.clear();
        NbtList inviteList = nbt.getListOrEmpty("invites");
        for (int i = 0; i < inviteList.size(); i++) {
            NbtCompound inviteNbt = inviteList.getCompoundOrEmpty(i);
            team.invites.put(
                Uuids.toUuid(inviteNbt.getIntArray("uuid").orElse(new int[0])),
                inviteNbt.getLong("timestamp", 0L)
            );
        }

        return team;
    }
}

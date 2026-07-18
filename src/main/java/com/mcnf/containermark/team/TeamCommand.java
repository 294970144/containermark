package com.mcnf.containermark.team;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TeamCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("markteam")
            .then(CommandManager.literal("create")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> createTeam(ctx))))
            .then(CommandManager.literal("invite")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(ctx -> invitePlayer(ctx))))
            .then(CommandManager.literal("accept")
                .then(CommandManager.argument("team", StringArgumentType.word())
                    .executes(ctx -> acceptInvite(ctx))))
            .then(CommandManager.literal("leave")
                .executes(ctx -> leaveTeam(ctx)))
            .then(CommandManager.literal("disband")
                .executes(ctx -> disbandTeam(ctx)))
            .then(CommandManager.literal("list")
                .executes(ctx -> listTeam(ctx)))
        );
    }

    private static int createTeam(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        String name = StringArgumentType.getString(ctx, "name");
        TeamManager.createTeam(player, name);
        return 1;
    }

    private static int invitePlayer(CommandContext<ServerCommandSource> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        TeamManager.invitePlayer(player, target);
        return 1;
    }

    private static int acceptInvite(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        String teamName = StringArgumentType.getString(ctx, "team");
        TeamManager.acceptInvite(player, teamName);
        return 1;
    }

    private static int leaveTeam(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        TeamManager.leaveTeam(player);
        return 1;
    }

    private static int disbandTeam(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        TeamManager.disbandTeam(player);
        return 1;
    }

    private static int listTeam(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        TeamManager.listTeam(player);
        return 1;
    }
}

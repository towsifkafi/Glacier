package com.towsifkafi.glacier.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.towsifkafi.glacier.GlacierMain.replaceDefault;

public class GSudo {

    public GSudo(GlacierMain plugin) {

        CommandMeta meta = plugin.commandManager.metaBuilder(plugin.commands.getString("gsudo.command"))
        .aliases(plugin.commands.getStringList("gsudo.aliases").toArray(new String[0]))
        .plugin(this)
        .build();

        plugin.commandLoader.commandMetas.add(meta);
        plugin.commandManager.register(meta, GSudo.createBrigradierCommand(plugin));
    }

    public static BrigadierCommand createBrigradierCommand(GlacierMain plugin) {
        Component defaultMessage = plugin.mm.deserialize(
                plugin.messages.getString("gsudo-usage")
        );
        LiteralCommandNode<CommandSource> sudoMain = LiteralArgumentBuilder.<CommandSource>
                literal(plugin.commands.getString("gsudo.command"))
                .requires(source -> source.hasPermission(plugin.commands.getString("gsudo.permission")))
                .executes(context -> {
                    CommandSource source = context.getSource();

                    source.sendMessage(defaultMessage);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("target", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            String target = "";
                            try {
                                target = ctx.getArgument("target", String.class);
                            } catch(IllegalArgumentException ignored) {}
                            List<String> tab = new ArrayList<>();
                            tab.add("all");

                            plugin.server.getAllServers().forEach(server -> {
                                tab.add(server.getServerInfo().getName());
                                server.getPlayersConnected().forEach(player -> {
                                    tab.add(player.getUsername());
                                });
                            });

                            if(target != null) {
                                String finalTarget = target;
                                tab.stream().filter(e -> e.contains(finalTarget)).forEach(builder::suggest);
                            } else {
                                tab.forEach(builder::suggest);
                            }

                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String target = context.getArgument("target", String.class);
                            context.getSource().sendMessage(
                                    replaceDefault(defaultMessage, "<target>", target)
                            );
                            return 0;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("command", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> {

                                    List<String> tab = List.of(new String[]{"command or message"});
                                    String message = "";
                                    try {
                                        message = ctx.getArgument("command", String.class);
                                        builder.suggest(message);
                                    } catch(IllegalArgumentException ignored) {
                                        tab.forEach(builder::suggest);
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String target = context.getArgument("target", String.class);
                                    String message = context.getArgument("command", String.class);

                                    if(plugin.server.getAllServers().stream().anyMatch(server -> server.getServerInfo().getName().equalsIgnoreCase(target))) {
                                        plugin.server.getAllServers().stream()
                                                .filter(server -> server.getServerInfo().getName().equalsIgnoreCase(target))
                                                .findFirst().get().getPlayersConnected().forEach(player -> {
                                                    if(!player.hasPermission("glacier.exempt.sudo")) {
                                                        player.spoofChatInput(message);
                                                    }
                                                });

                                        successful(context, plugin, target, message);

                                        return 1;
                                    } else if(plugin.server.getAllPlayers().stream().anyMatch(player -> player.getUsername().equalsIgnoreCase(target))) {
                                        Optional<Player> player = plugin.server.getPlayer(target);
                                        if(player.isEmpty()) {

                                            context.getSource().sendMessage(
                                                    replaceDefault(
                                                            plugin.mm.deserialize(plugin.messages.getString("gsudo-unknown-player")),
                                                            "<target>",
                                                            target
                                                    )
                                            );
                                            return 0;
                                        } else {
                                            if(!player.get().hasPermission("glacier.exempt.sudo")) {
                                                player.get().spoofChatInput(message);
                                                successful(context, plugin, target, message);
                                            } else {
                                                context.getSource().sendMessage(
                                                        replaceDefault(
                                                                plugin.mm.deserialize(plugin.messages.getString("gsudo-exempt")),
                                                                "<target>",
                                                                target
                                                        )
                                                );
                                            }
                                            return 1;
                                        }
                                    } else if(Objects.equals(target, "all")) {
                                        plugin.server.getAllPlayers().forEach(player -> {
                                            if(!player.hasPermission("glacier.exempt.sudo")) {
                                                player.spoofChatInput(message);
                                            }
                                        });
                                        successful(context, plugin, target, message);
                                        return 1;
                                    } else {
                                        context.getSource().sendMessage(
                                                replaceDefault(
                                                        plugin.mm.deserialize(plugin.messages.getString("gsudo-unknown")),
                                                        "<target>",
                                                        target
                                                )
                                        );
                                    }

                                    return 0;
                                })
                        )
                )
                .build();

        return new BrigadierCommand(sudoMain);
    }

    private static void successful(CommandContext<CommandSource> context, GlacierMain plugin, String target, String message) {
        context.getSource().sendMessage(
                replaceDefault(
                        replaceDefault(plugin.mm.deserialize(plugin.messages.getString("gsudo-successful")),
                                "<target>",
                                target
                        ),
                        "<command>",
                        message
                )
        );
    }
}

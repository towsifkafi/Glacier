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
import net.kyori.adventure.text.Component;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.towsifkafi.glacier.GlacierMain.replaceDefault;


public class GKick {

    public GKick(GlacierMain plugin) {

        CommandMeta meta = plugin.commandManager.metaBuilder(plugin.commands.getString("gkick.command"))
        .aliases(plugin.commands.getStringList("gkick.aliases").toArray(new String[0]))
        .plugin(this)
        .build();

        plugin.commandLoader.commandMetas.add(meta);
        plugin.commandManager.register(meta, GKick.createBrigradierCommand(plugin));
    }

    public static int kickByName(CommandContext<CommandSource> context, GlacierMain plugin, String target, String reason) {

        List<String> players = new ArrayList<>();

        plugin.server.getAllServers().forEach(server -> {
            server.getPlayersConnected().forEach(player -> {
                if(player.getUsername().toLowerCase().contains(target.toLowerCase())) {
                    players.add(player.getUsername());
                }
            });
        });

        if(players.isEmpty()) {
            context.getSource().sendMessage(plugin.mm.deserialize(
                    plugin.messages.getString("gkick-no-players")
            ));

            return Command.SINGLE_SUCCESS;
        } else {
            for(String player : players) {
                plugin.server.getPlayer(player).ifPresent(p -> {
                    p.disconnect(plugin.lm.deserialize(reason));
                });
            }
            context.getSource().sendMessage(
                    replaceDefault(
                            plugin.mm.deserialize(plugin.messages.getString("gkick-players")),
                            "<target>",
                            String.join(", ", players)
                    )
            );
            return Command.SINGLE_SUCCESS;
        }

    }

    public static int kickByServer(CommandContext<CommandSource> context, GlacierMain plugin, String target, String reason) {
        List<String> players = new ArrayList<>();

        plugin.server.getAllServers().forEach(server -> {
            if(server.getServerInfo().getName().toLowerCase().contains(target.toLowerCase())) {
                server.getPlayersConnected().forEach(player -> {
                    players.add(player.getUsername());
                });
            }
        });

        if(players.isEmpty()) {
            context.getSource().sendMessage(plugin.mm.deserialize(
                    plugin.messages.getString("gkick-no-players")
            ));
            return Command.SINGLE_SUCCESS;
        } else {
            for(String player : players) {
                plugin.server.getPlayer(player).ifPresent(p -> {
                    p.disconnect(plugin.lm.deserialize(reason));
                });
            }
            context.getSource().sendMessage(
                    replaceDefault(
                            plugin.mm.deserialize(plugin.messages.getString("gkick-players")),
                            "<target>",
                            String.join(", ", players)
                    )
            );
            return Command.SINGLE_SUCCESS;
        }
    }

    public static int kickByIP(CommandContext<CommandSource> context, GlacierMain plugin, String target, String reason) {
        List<String> players = new ArrayList<>();

        plugin.server.getAllServers().forEach(server -> {
            server.getPlayersConnected().forEach(player -> {
                if(player.getRemoteAddress().getAddress().getHostAddress().contains(target)) {
                    players.add(player.getUsername());
                }
            });
        });

        if(players.isEmpty()) {
            context.getSource().sendMessage(plugin.mm.deserialize(
                    plugin.messages.getString("gkick-no-players")
            ));
            return Command.SINGLE_SUCCESS;
        } else {
            for(String player : players) {
                plugin.server.getPlayer(player).ifPresent(p -> {
                    p.disconnect(plugin.lm.deserialize(reason));
                });
            }
            return Command.SINGLE_SUCCESS;
        }
    }

    public static BrigadierCommand createBrigradierCommand(GlacierMain plugin) {
        Component defaultMessage = plugin.mm.deserialize(
                plugin.messages.getString("gkick-usage")
        );
        LiteralCommandNode<CommandSource> announcerMain = LiteralArgumentBuilder
                .<CommandSource>literal(plugin.commands.getString("gkick.command"))
                .requires(source -> source.hasPermission(plugin.commands.getString("gkick.permission")))
                .executes(context -> {
                    CommandSource source = context.getSource();
                    source.sendMessage(defaultMessage);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("subcommand", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            String[] tab = new String[] { "byname", "byip", "byserver" };
                            Arrays.stream(tab).forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {

                            String subcommand = context.getArgument("subcommand", String.class);

                            List<String> announcements = new ArrayList<>();
                            plugin.messageSchedules.forEach((k, v) -> {
                                announcements.add(k);
                            });

                            if(subcommand.equalsIgnoreCase("byname")) {
                                context.getSource().sendMessage(
                                        replaceDefault(defaultMessage, "<type>", "byname")
                                );
                                return Command.SINGLE_SUCCESS;
                            } else if(subcommand.equalsIgnoreCase("byip")) {
                                context.getSource().sendMessage(
                                        replaceDefault(defaultMessage, "<type>", "byip")
                                );
                                return Command.SINGLE_SUCCESS;
                            } else if(subcommand.equalsIgnoreCase("byserver")) {
                                context.getSource().sendMessage(
                                        replaceDefault(defaultMessage, "<type>", "byserver")
                                );
                                return Command.SINGLE_SUCCESS;
                            } else {
                                context.getSource().sendMessage(plugin.mm.deserialize(
                                        plugin.messages.getString("unknown-argument")
                                ));
                                return 0;
                            }

                        }).then(RequiredArgumentBuilder.<CommandSource, String>argument("target", StringArgumentType.word())
                                .suggests((ctx, builder) -> {

                                    String subcommand = ctx.getArgument("subcommand", String.class);
                                    String target = "";
                                    try {
                                        target = ctx.getArgument("target", String.class);
                                    } catch(IllegalArgumentException ignored) {

                                    }
                                    if(subcommand.equalsIgnoreCase("byname")) {

                                        List<String> tab = new ArrayList<>();

                                        plugin.server.getAllServers().forEach(server -> {
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

                                    } else if(subcommand.equalsIgnoreCase("byserver")) {

                                        List<String> tab = new ArrayList<>();

                                        plugin.server.getAllServers().forEach(server -> {
                                            tab.add(server.getServerInfo().getName());
                                        });

                                        if(target != null) {
                                            String finalTarget = target;
                                            tab.stream().filter(e -> e.contains(finalTarget)).forEach(builder::suggest);
                                        } else {
                                            tab.forEach(builder::suggest);
                                        }

                                    } else if(subcommand.equalsIgnoreCase("byip")) {

                                        List<String> tab = new ArrayList<>();

                                        plugin.server.getAllServers().forEach(server -> {
                                            server.getPlayersConnected().forEach(player -> {
                                                tab.add(player.getRemoteAddress().getAddress().getHostAddress());
                                            });
                                        });

                                        if(target != null) {
                                            String finalTarget = target;
                                            tab.stream().filter(e -> e.contains(finalTarget)).forEach(builder::suggest);
                                        } else {
                                            tab.forEach(builder::suggest);
                                        }

                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String subcommand = context.getArgument("subcommand", String.class);
                                    String target = context.getArgument("target", String.class);
                                    String defaultKickMessage = plugin.messages.getString("gkick-default-kick-message");

                                    if(subcommand.equalsIgnoreCase("byname")) {
                                        kickByName(context, plugin, target, defaultKickMessage);
                                    } else if (subcommand.equalsIgnoreCase("byserver")) {
                                        kickByServer(context, plugin, target, defaultKickMessage);
                                    } else if (subcommand.equalsIgnoreCase("byip")) {
                                        kickByIP(context, plugin, target, defaultKickMessage);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }).then(RequiredArgumentBuilder.<CommandSource, String>argument("reason", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> {
                                            List<String> tab = List.of(new String[]{"reason"});
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

                                            String subcommand = context.getArgument("subcommand", String.class);
                                            String target = context.getArgument("target", String.class);
                                            String reason = context.getArgument("reason", String.class);

                                            if(subcommand.equalsIgnoreCase("byname")) {
                                                kickByName(context, plugin, target, reason);
                                            } else if (subcommand.equalsIgnoreCase("byserver")) {
                                                kickByServer(context, plugin, target, reason);
                                            } else if (subcommand.equalsIgnoreCase("byip")) {
                                                kickByIP(context, plugin, target, reason);
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )

                .build();
        return new BrigadierCommand(announcerMain);
    }
}

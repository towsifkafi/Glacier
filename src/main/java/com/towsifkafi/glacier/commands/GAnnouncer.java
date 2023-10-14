package com.towsifkafi.glacier.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.towsifkafi.glacier.handlers.AnnouncerHandler.handleAnnouncement;
import static com.towsifkafi.glacier.handlers.AnnouncerHandler.sendAnnouncement;

public class GAnnouncer {
    public static BrigadierCommand createBrigradierCommand(GlacierMain plugin) {
        Component defaultMessage = plugin.mm.deserialize(
                plugin.messages.getString("gannouncer-usage")
        );
        LiteralCommandNode<CommandSource> announcerMain = LiteralArgumentBuilder
                .<CommandSource>literal("gannouncer")
                .requires(source -> source.hasPermission("glacier.admin.announcer"))
                .executes(context -> {
                    CommandSource source = context.getSource();
                    source.sendMessage(defaultMessage);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("subcommand", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            String[] tab = new String[] { "reload", "list", "view", "send" };
                            Arrays.stream(tab).forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {

                            String subcommand = context.getArgument("subcommand", String.class);

                            List<String> announcements = new ArrayList<>();
                            plugin.messageSchedules.forEach((k, v) -> {
                                announcements.add(k);
                            });

                            if(subcommand.equalsIgnoreCase("reload")) {
                                plugin.announcerConfig.loadConfig();
                                context.getSource().sendMessage(plugin.mm.deserialize(
                                        plugin.messages.getString("gannouncer-reload-config"),
                                        Placeholder.unparsed("version", plugin.properties.getProperty("version"))
                                ));
                                return Command.SINGLE_SUCCESS;
                            } else if(subcommand.equalsIgnoreCase("list")) {
                                Component message = plugin.mm.deserialize(
                                        plugin.messages.getString("gannouncer-list"),
                                        Placeholder.parsed("list", String.join(", ", announcements.stream().map(e -> "<color:#3ef438>"+e+"</color>" ).toList()  ))
                                );
                                context.getSource().sendMessage(message);
                                return Command.SINGLE_SUCCESS;
                            } else if(subcommand.equalsIgnoreCase("view")) {
                                Component message = plugin.mm.deserialize(
                                        plugin.messages.getString("gannouncer-usage-view"),
                                        Placeholder.parsed("list", String.join(", ", announcements.stream().map(e -> "<color:#3ef438>"+e+"</color>" ).toList()  ))
                                );
                                context.getSource().sendMessage(message);
                                return 0;
                            } else if(subcommand.equalsIgnoreCase("send")) {
                                Component message = plugin.mm.deserialize(
                                        plugin.messages.getString("gannouncer-usage-send"),
                                        Placeholder.parsed("list", String.join(", ", announcements.stream().map(e -> "<color:#3ef438>"+e+"</color>" ).toList()  ))
                                );
                                context.getSource().sendMessage(message);
                                return 0;
                            } else {
                                context.getSource().sendMessage(plugin.mm.deserialize(
                                        plugin.messages.getString("unknown-argument")
                                ));
                                return 0;
                            }

                        }).then(RequiredArgumentBuilder.<CommandSource, String>argument("id", StringArgumentType.word())
                                .suggests((ctx, builder) -> {

                                    String subcommand = ctx.getArgument("subcommand", String.class);
                                    if(subcommand.equalsIgnoreCase("reload")) {

                                        List<String> tab = new ArrayList<>();
                                        tab.add("config");
                                        tab.add("all");

                                        tab.forEach(builder::suggest);

                                    } else {
                                        List<String> announcements = new ArrayList<>();
                                        plugin.messageSchedules.forEach((k, v) -> {
                                            announcements.add(k);
                                        });

                                        announcements.forEach(builder::suggest);
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String subcommand = context.getArgument("subcommand", String.class);
                                    String id = context.getArgument("id", String.class);

                                    List<String> announcements = new ArrayList<>();
                                    plugin.messageSchedules.forEach((k, v) -> {
                                        announcements.add(k);
                                    });

                                    if(subcommand.equalsIgnoreCase("reload")) {

                                        if(id.equalsIgnoreCase("config")) {
                                            plugin.announcerConfig.loadConfig();
                                            context.getSource().sendMessage(plugin.mm.deserialize(
                                                    plugin.messages.getString("gannouncer-reload-config"),
                                                    Placeholder.unparsed("version", plugin.properties.getProperty("version"))
                                            ));
                                            return Command.SINGLE_SUCCESS;
                                        } else if(id.equalsIgnoreCase("all")) {
                                            plugin.reloadAnnouncer();
                                            context.getSource().sendMessage(plugin.mm.deserialize(
                                                    plugin.messages.getString("gannouncer-reload"),
                                                    Placeholder.unparsed("version", plugin.properties.getProperty("version"))
                                            ));
                                            return Command.SINGLE_SUCCESS;
                                        } else {
                                            context.getSource().sendMessage(plugin.mm.deserialize(
                                                    plugin.messages.getString("unknown-argument")
                                            ));
                                            return 0;
                                        }

                                    } else if (subcommand.equalsIgnoreCase("list")) {
                                        context.getSource().sendMessage(plugin.mm.deserialize(
                                                plugin.messages.getString("unknown-argument")
                                        ));
                                        return 0;
                                    }

                                    if(!announcements.contains(id)) {
                                        context.getSource().sendMessage(plugin.mm.deserialize(
                                                plugin.messages.getString("gannouncer-unknown-id"),
                                                Placeholder.parsed("list", String.join(", ", announcements.stream().map(e -> "<color:#3ef438>"+e+"</color>" ).toList()  ))
                                        ));
                                        return 0;
                                    }

                                    if(subcommand.equalsIgnoreCase("view")) {
                                        sendAnnouncement(plugin, id, (Player) context.getSource());
                                    } else if(subcommand.equalsIgnoreCase("send")) {
                                        handleAnnouncement(plugin, id);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                .build();
        return new BrigadierCommand(announcerMain);
    }

}

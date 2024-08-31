package com.towsifkafi.glacier.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Arrays;
import java.util.Objects;

public class Glacier {

    public static BrigadierCommand createBrigradierCommand(GlacierMain plugin) {
        LiteralCommandNode<CommandSource> glacierMain = LiteralArgumentBuilder
                .<CommandSource>literal("glacier")
                .requires(source -> source.hasPermission(plugin.commands.getString("glacier.permission")))
                .executes(context -> {
                    CommandSource source = context.getSource();

                    Component message = plugin.mm.deserialize(
                            plugin.messages.getString("main-command"),
                            Placeholder.unparsed("version", plugin.properties.getProperty("version"))
                    );
                    source.sendMessage(message);

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("subcommand", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            String[] tab = new String[] { "reload", "version" };
                            Arrays.stream(tab).forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {

                            String subcommand = context.getArgument("subcommand", String.class);
                            if(subcommand.equalsIgnoreCase("reload")) {
                                plugin.reload();
                                Component message = plugin.mm.deserialize(
                                        plugin.messages.getString("main-command-reload"),
                                        Placeholder.unparsed("version", plugin.properties.getProperty("version"))
                                );
                                context.getSource().sendMessage(message);
                            } else if(subcommand.equalsIgnoreCase("version")) {
                                Component message = plugin.mm.deserialize(
                                        plugin.messages.getString("main-command-version"),
                                        Placeholder.unparsed("version", plugin.properties.getProperty("version"))
                                );
                                context.getSource().sendMessage(message);
                            } else {
                                context.getSource().sendMessage(plugin.mm.deserialize(
                                        plugin.messages.getString("unknown-argument")
                                ));
                                return 0;
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                ).build();
        return new BrigadierCommand(glacierMain);
    }

}

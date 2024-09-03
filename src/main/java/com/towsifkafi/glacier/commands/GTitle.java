package com.towsifkafi.glacier.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.title.Title;

import java.time.Duration;

import static com.towsifkafi.glacier.GlacierMain.replaceDefault;

public class GTitle {

    public GTitle(GlacierMain plugin) {

        CommandMeta meta = plugin.commandManager.metaBuilder(plugin.commands.getString("gtitle.command"))
        .aliases(plugin.commands.getStringList("gtitle.aliases").toArray(new String[0]))
        .plugin(this)
        .build();

        plugin.commandLoader.commandMetas.add(meta);
        plugin.commandManager.register(meta, GTitle.createBrigradierCommand(plugin));
    }

    public static BrigadierCommand createBrigradierCommand(GlacierMain plugin) {
        Component defaultMessage = plugin.mm.deserialize(
                plugin.messages.getString("gtitle-usage")
        );
        LiteralCommandNode<CommandSource> titleMain = LiteralArgumentBuilder
                .<CommandSource>literal(plugin.commands.getString("gtitle.command"))
                .requires(source -> source.hasPermission(plugin.commands.getString("gtitle.permission")))
                .executes(context -> {
                    CommandSource source = context.getSource();
                    source.sendMessage(defaultMessage);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, Double>argument("fadeIn", DoubleArgumentType.doubleArg())
                        .suggests((ctx, builder) -> {
                            builder.suggest("0.5");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            double arg = context.getArgument("fadeIn", Double.class);
                            Component def = replaceDefault(defaultMessage, "<fadeIn>", String.valueOf(arg));
                            context.getSource().sendMessage(def);
                            return 0;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, Double>argument("stay", DoubleArgumentType.doubleArg())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("3");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    double fadeIn = context.getArgument("fadeIn", Double.class);
                                    double stay = context.getArgument("stay", Double.class);
                                    Component def = replaceDefault(defaultMessage, "<fadeIn>", String.valueOf(fadeIn));
                                    def = replaceDefault(def, "<stay>", String.valueOf(stay));
                                    context.getSource().sendMessage(def);
                                    return 0;
                                })
                                .then(RequiredArgumentBuilder.<CommandSource, Double>argument("fadeOut", DoubleArgumentType.doubleArg())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest("1");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            double fadeIn = context.getArgument("fadeIn", Double.class);
                                            double stay = context.getArgument("stay", Double.class);
                                            double fadeOut = context.getArgument("fadeOut", Double.class);
                                            Component def = replaceDefault(defaultMessage, "<fadeIn>", String.valueOf(fadeIn));
                                            def = replaceDefault(def, "<stay>", String.valueOf(stay));
                                            def = replaceDefault(def, "<fadeOut>", String.valueOf(fadeOut));
                                            context.getSource().sendMessage(def);
                                            return 0;
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("title", StringArgumentType.greedyString())
                                                .suggests((ctx, builder) -> {
                                                    builder.suggest("Example Title");
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {

                                                    double fadeIn = context.getArgument("fadeIn", Double.class);
                                                    double stay = context.getArgument("stay", Double.class);
                                                    double fadeOut = context.getArgument("fadeOut", Double.class);

                                                    Title.Times times = Title.Times.times(
                                                            Duration.ofMillis((long) (fadeIn*1000)),
                                                            Duration.ofMillis((long) (stay*1000)),
                                                            Duration.ofMillis((long) (fadeOut*1000))
                                                    );

                                                    String message = context.getArgument("title", String.class);

                                                    Component titleText;
                                                    Component subtitleText;

                                                    if(message.contains("|")) {
                                                        String[] split = message.split("\\|");
                                                        titleText = plugin.lm.deserialize(split[0]);
                                                        subtitleText = plugin.lm.deserialize(split[1]);
                                                    } else {
                                                        titleText = plugin.lm.deserialize(message);
                                                        subtitleText = Component.empty();
                                                    }

                                                    Title title = Title.title(titleText, subtitleText, times);

                                                    plugin.server.getAllPlayers().forEach(player -> {
                                                        player.showTitle(title);
                                                    });

                                                    context.getSource().sendMessage(
                                                            plugin.mm.deserialize(
                                                                    plugin.messages.getString("gtitle-sent")
                                                            )
                                                    );

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                ).build();
        return new BrigadierCommand(titleMain);
    }
}

package com.towsifkafi.glacier;

import com.google.inject.Inject;
import com.towsifkafi.glacier.commands.GActionBar;
import com.towsifkafi.glacier.commands.GTitle;
import com.towsifkafi.glacier.commands.Glacier;
import com.towsifkafi.glacier.config.ConfigProvider;
import com.towsifkafi.glacier.spicord.PlayerListAddon;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.slf4j.Logger;
import org.spicord.SpicordLoader;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "glacier",
        name = "Glacier",
        description = "Weird ass util plugin for ArcticRealms or whatever",
        authors = {"TowsifKafi"},
        url = "https://towsifkafi.com",
        version = "1.0-SNAPSHOT"
)
public class GlacierMain {

    public final ProxyServer server;
    public Logger logger;
    public final Path dataDirectory;
    public LoginLoggerAddon loginLogger;
    public PlayerListAddon playerList;

    public ConfigProvider messages;
    public ConfigProvider config;
    public ConfigProvider announcerConfig;

    public MiniMessage mm = MiniMessage.miniMessage();
    public LegacyComponentSerializer lm = LegacyComponentSerializer.legacyAmpersand();
    public Map<String, ScheduledTask> messageSchedules = new HashMap<>();

    @Inject
    public GlacierMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        this.messages = new ConfigProvider(this, dataDirectory, "messages.yml");
        this.config = new ConfigProvider(this, dataDirectory, "config.yml");
        this.announcerConfig = new ConfigProvider(this, dataDirectory, "announcer.yml");
        messages.loadConfig();
        config.loadConfig();
        announcerConfig.loadConfig();

        playerList = new PlayerListAddon((this));
        SpicordLoader.addStartupListener(spicord -> spicord.getAddonManager().registerAddon(playerList));

        loadCommands();
        loadAutoMessages();
    }

    public void loadCommands() {

        CommandManager commandManager = server.getCommandManager();

        // register /glacier command
        commandManager.register(
                commandManager.metaBuilder("glacier")
                        .aliases("glc", "glcr")
                        .plugin(this)
                        .build(), Glacier.createBrigradierCommand(this)
        );

        // register /gtitle command
        commandManager.register(
                commandManager.metaBuilder("gtitle")
                        .aliases("titleg")
                        .plugin(this)
                        .build(), GTitle.createBrigradierCommand(this)
        );

        //register /gactionbar command
        commandManager.register(
                commandManager.metaBuilder("gactiobar")
                        .aliases("gaction")
                        .plugin(this)
                        .build(), GActionBar.createBrigradierCommand(this)
        );

    }

    public void loadAutoMessages() {

        messageSchedules.forEach((k, s) -> {
            s.cancel();
        });
        messageSchedules.clear();
        if(config.getBoolean("announcer-enabled")) {
            announcerConfig.getMap("announcements").forEach((k, msg) -> {
                String key = "announcements."+ k;
                String type = announcerConfig.getString(key+".type");
                int delay = announcerConfig.getInt(key+".delay");
                String serverName = announcerConfig.getString(key+".server");

                List<String> text = announcerConfig.getStringList(key+".text");

                ScheduledTask task = server.getScheduler()
                        .buildTask(this, () -> {
                            server.getAllServers().forEach(server -> {
                                server.getPlayersConnected().forEach(player -> {

                                    if(player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(serverName)
                                            || serverName.equalsIgnoreCase("all")) {

                                        if(type.equalsIgnoreCase("message")) {
                                            player.sendMessage(
                                                    mm.deserialize(String.join("\n", text))
                                            );
                                        } else if(type.equalsIgnoreCase("title")) {
                                            int fadeIn = announcerConfig.getInt(key+".fadeIn");
                                            int stay = announcerConfig.getInt(key+".stay");
                                            int fadeOut = announcerConfig.getInt(key+".fadeOut");

                                            if(fadeIn < 0) fadeIn = 1;
                                            if(stay < 0) stay = 3;
                                            if(fadeOut < 0) fadeOut = 1;

                                            Title.Times times = Title.Times.times(
                                                    Duration.ofSeconds((long) (fadeIn)),
                                                    Duration.ofSeconds((long) (stay)),
                                                    Duration.ofSeconds((long) (fadeOut))
                                            );

                                            Component titleText = mm.deserialize("No Title");
                                            Component subtitleText = Component.empty();
                                            if(!text.isEmpty()) titleText = mm.deserialize(text.get(0));
                                            if(text.size() > 1)subtitleText = mm.deserialize(text.get(1));

                                            Title title = Title.title(
                                                    titleText,
                                                    subtitleText,
                                                    times
                                            );

                                            player.showTitle(title);
                                        } else if(type.equalsIgnoreCase("actionbar")) {
                                            player.sendActionBar(mm.deserialize(String.join(" ", text)));
                                        }

                                    }

                                });
                            });

                        })
                        .repeat(delay, TimeUnit.SECONDS)
                        .schedule();

                messageSchedules.put(k, task);
            });

        }

    }

    public void reload() {
        messages.loadConfig();
        config.loadConfig();
        announcerConfig.loadConfig();
        loadAutoMessages();
    }

    public static Component replaceDefault(Component defaultMessage, String match, String replace) {
        TextReplacementConfig config = TextReplacementConfig.builder()
                .matchLiteral(match)
                .replacement(replace)
                .build();
        return defaultMessage.replaceText(config);
    }
}

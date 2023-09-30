package com.towsifkafi.glacier;

import com.google.inject.Inject;
import com.towsifkafi.glacier.commands.GActionBar;
import com.towsifkafi.glacier.commands.GAnnouncer;
import com.towsifkafi.glacier.commands.GTitle;
import com.towsifkafi.glacier.commands.Glacier;
import com.towsifkafi.glacier.config.ConfigProvider;
import com.towsifkafi.glacier.spicord.PlayerListAddon;
import com.towsifkafi.glacier.utils.PAPIBridgeReplacer;
import com.towsifkafi.glacier.utils.SpicordHook;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spicord.SpicordLoader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.towsifkafi.glacier.announcer.AnnouncerHandler.scheduleAnnouncement;

@Plugin(
        id = "glacier",
        name = "Glacier",
        description = "Weird ass util plugin for minecraft servers",
        authors = {"TowsifKafi"},
        url = "https://towsifkafi.com",
        version = "1.0-SNAPSHOT",
        dependencies = {
                @Dependency(id = "spicord", optional = true),
                @Dependency(id = "papiproxybridge", optional = true)
        }
)
public class GlacierMain {

    public final ProxyServer server;
    public Logger logger;
    public final Path dataDirectory;

    public ConfigProvider messages;
    public ConfigProvider config;
    public ConfigProvider announcerConfig;

    public MiniMessage mm = MiniMessage.miniMessage();
    public LegacyComponentSerializer lm = LegacyComponentSerializer.legacyAmpersand();
    public Map<String, ScheduledTask> messageSchedules = new HashMap<>();
    public SpicordHook spicordHook;
    public PAPIBridgeReplacer papi;

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

        if(isPluginPresent("spicord")) {
            spicordHook = new SpicordHook(this);
        }

        if(isPluginPresent("papiproxybridge") && announcerConfig.getBoolean("enable-papi-bridge")) {
            papi = new PAPIBridgeReplacer(this);
        }

        loadCommands();
        loadAutoMessages();
    }

    @Subscribe


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
                commandManager.metaBuilder("gactionbar")
                        .aliases("gaction")
                        .plugin(this)
                        .build(), GActionBar.createBrigradierCommand(this)
        );

        //register /gannouncer command
        commandManager.register(
                commandManager.metaBuilder("gannouncer")
                        .aliases("gannounce")
                        .plugin(this)
                        .build(), GAnnouncer.createBrigradierCommand(this)
        );

    }

    public void loadAutoMessages() {

        messageSchedules.forEach((k, s) -> {
            s.cancel();
        });
        messageSchedules.clear();
        if(config.getBoolean("announcer-enabled")) {
            announcerConfig.getMap("announcements").forEach((k, msg) -> {
                scheduleAnnouncement(this, k);
            });
        }

    }

    public void reload() {
        messages.loadConfig();
        config.loadConfig();
        reloadAnnouncer();
    }

    public void reloadAnnouncer() {
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

    public boolean isPluginPresent(@NotNull String dependency) {
        return server.getPluginManager().getPlugin(dependency.toLowerCase(Locale.ENGLISH)).isPresent();
    }
}

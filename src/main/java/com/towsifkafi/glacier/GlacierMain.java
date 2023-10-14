package com.towsifkafi.glacier;

import com.google.inject.Inject;
import com.towsifkafi.glacier.commands.*;
import com.towsifkafi.glacier.config.ConfigProvider;
import com.towsifkafi.glacier.utils.Metrics;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static com.towsifkafi.glacier.handlers.AnnouncerHandler.scheduleAnnouncement;

@Plugin(
        id = "glacier",
        name = "Glacier",
        description = "Weird ass util plugin for minecraft servers",
        authors = {"TowsifKafi"},
        url = "https://towsifkafi.com",
        version = "1.1.0-SNAPSHOT",
        dependencies = {
                @Dependency(id = "spicord", optional = true),
                @Dependency(id = "papiproxybridge", optional = true)
        }
)
public class GlacierMain {

    public final ProxyServer server;
    private final CommandManager commandManager;
    public Logger logger;
    public final Path dataDirectory;
    public final Properties properties = new Properties();

    public ConfigProvider messages;
    public ConfigProvider config;
    public ConfigProvider announcerConfig;
    public ConfigProvider books;

    public MiniMessage mm = MiniMessage.miniMessage();
    public LegacyComponentSerializer lm = LegacyComponentSerializer.legacyAmpersand();
    public Map<String, ScheduledTask> messageSchedules = new HashMap<>();
    public SpicordHook spicordHook;
    public PAPIBridgeReplacer papi;
    private final Metrics.Factory metricsFactory;
    private Metrics metrics;

    @Inject
    public GlacierMain(ProxyServer server, Logger logger, Metrics.Factory metricsFactory, CommandManager commandManager, @DataDirectory Path dataDirectory) throws IOException {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
        this.commandManager = commandManager;

        properties.load(this.getClass().getClassLoader().getResourceAsStream("pom.properties"));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        enableMetrics();
        this.messages = new ConfigProvider(this, dataDirectory, "messages.yml");
        this.config = new ConfigProvider(this, dataDirectory, "config.yml");
        this.announcerConfig = new ConfigProvider(this, dataDirectory, "announcer.yml");

        messages.loadConfig();
        config.loadConfig();
        announcerConfig.loadConfig();

        if(isPluginPresent("spicord")) {
            spicordHook = new SpicordHook(this);
            addMetricsPie("uses_spicord", "true");
        } else addMetricsPie("uses_spicord", "false");

        if(isPluginPresent("papiproxybridge") && announcerConfig.getBoolean("enable-papi-bridge")) {
            papi = new PAPIBridgeReplacer(this);
            addMetricsPie("uses_papiproxybridge", "true");
        } else addMetricsPie("uses_papiproxybridge", "false");

        loadCommands();
        loadAutoMessages();
    }

    public void loadCommands() {

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

        //register /gsudo command
        commandManager.register(
                commandManager.metaBuilder("gsudo")
                        .aliases("vsudo")
                        .plugin(this)
                        .build(), GSudo.createBrigradierCommand(this)
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

    public void enableMetrics() {
        int pluginId = 19938;
        metrics = metricsFactory.make(this, pluginId);
    }

    public void addMetricsPie(String id, String value) {
        metrics.addCustomChart(new Metrics.SimplePie(id, () -> value));
    }
}

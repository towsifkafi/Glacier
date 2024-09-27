package com.towsifkafi.glacier;

import com.google.inject.Inject;
import com.towsifkafi.glacier.config.ConfigProvider;
import com.towsifkafi.glacier.events.PluginMessageEvents;
import com.towsifkafi.glacier.events.PostLogin;
import com.towsifkafi.glacier.handlers.AnnouncementManager;
import com.towsifkafi.glacier.handlers.TimedCommand;
import com.towsifkafi.glacier.handlers.CommandLoader;
import com.towsifkafi.glacier.handlers.ServerLinksManager;
import com.towsifkafi.glacier.utils.Metrics;
import com.towsifkafi.glacier.utils.PAPIBridgeReplacer;
import com.towsifkafi.glacier.utils.SpicordHook;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "glacier",
        name = "Glacier",
        description = "A util plugin for minecraft servers",
        authors = {"TowsifKafi"},
        url = "https://towsifkafi.com",
        version = "1.4.2-SNAPSHOT",
        dependencies = {
                @Dependency(id = "spicord", optional = true),
                @Dependency(id = "papiproxybridge", optional = true)
        }
)
public class GlacierMain {

    private static GlacierMain instance;
    public Logger logger;

    public final ProxyServer server;
    public final CommandManager commandManager;

    public final Path dataDirectory;
    public final Properties properties = new Properties();

    public ConfigProvider messages;
    public ConfigProvider config;
    public ConfigProvider announcerConfig;
    public ConfigProvider commands;
    public ConfigProvider serverlinksConfig;
    public ConfigProvider timedCommandConfig;
    public ConfigProvider books;

    public CommandLoader commandLoader;
    public ServerLinksManager serverLinksManager;
    public AnnouncementManager announcer;

    public static MinecraftChannelIdentifier pluginChannel = MinecraftChannelIdentifier.create("bukkit", "glacier");
    public TimedCommand timedCommand;

    public MiniMessage mm = MiniMessage.miniMessage();
    public LegacyComponentSerializer lm = LegacyComponentSerializer.legacyAmpersand();
    
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
        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        enableMetrics();
        server.getChannelRegistrar().register(pluginChannel);
        this.config = new ConfigProvider(this, dataDirectory, "config.yml");

        this.messages = new ConfigProvider(this, dataDirectory, "messages.yml");
        this.commands = new ConfigProvider(this, dataDirectory, "commands.yml");
        this.announcerConfig = new ConfigProvider(this, dataDirectory, "announcer.yml");
        this.serverlinksConfig = new ConfigProvider(this, dataDirectory, "serverlinks.yml");
        this.timedCommandConfig = new ConfigProvider(this, dataDirectory, "timedCommand.yml");

        messages.loadConfig();
        config.loadConfig();
        announcerConfig.loadConfig();
        commands.loadConfig();
        serverlinksConfig.loadConfig();
        timedCommandConfig.loadConfig();

        this.commandLoader = new CommandLoader(this);
        this.announcer = new AnnouncementManager(this);
        this.serverLinksManager = new ServerLinksManager(this);
        this.timedCommand = new TimedCommand(this);

        if(isPluginPresent("spicord")) {
            spicordHook = new SpicordHook(this);
            addMetricsPie("uses_spicord", "true");
        } else addMetricsPie("uses_spicord", "false");

        if(isPluginPresent("papiproxybridge") && announcerConfig.getBoolean("enable-papi-bridge")) {
            papi = new PAPIBridgeReplacer(this);
            addMetricsPie("uses_papiproxybridge", "true");
        } else addMetricsPie("uses_papiproxybridge", "false");

        loadEvents();
        commandLoader.loadCommands();
    
        announcer.loadAnnouncements();
        serverLinksManager.loadServerLinks();

        server.getScheduler().buildTask(this, () -> timedCommand.runScheduledTask())
                .repeat(1, TimeUnit.SECONDS) // Run every 1 seconds
                .schedule();

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        
    }

    public void loadEvents() {
        server.getEventManager().register(this, new PostLogin(this));
        server.getEventManager().register(this, new PluginMessageEvents(this));
    }

    public void reload() {
        commands.loadConfig();
        messages.loadConfig();
        config.loadConfig();
        serverlinksConfig.loadConfig();

        commandLoader.reloadCommands();
        announcer.reloadAnnouncer();
        serverLinksManager.reloadServerLinks();
        timedCommand.reloadTimedCommand();
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

    public static GlacierMain getInstance() {
        return instance;
    }
}

package com.towsifkafi.glacier;

import com.google.inject.Inject;
import com.towsifkafi.glacier.spicord.LoginLoggerAddon;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.spicord.SpicordLoader;

import java.nio.file.Path;

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

    @Inject
    public GlacierMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loginLogger = new LoginLoggerAddon(this);
        SpicordLoader.addStartupListener(spicord -> spicord.getAddonManager().registerAddon(loginLogger));
    }
}

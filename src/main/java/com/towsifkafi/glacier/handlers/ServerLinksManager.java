package com.towsifkafi.glacier.handlers;

import java.util.ArrayList;
import java.util.List;

import com.towsifkafi.glacier.GlacierMain;
import com.towsifkafi.glacier.config.ConfigProvider;
import com.velocitypowered.api.network.ProtocolState;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.ServerLink;

public class ServerLinksManager {
    
    private GlacierMain plugin;
    private ConfigProvider config;
    public List<ServerLink> serverLinks;

    private List<String> blacklistedServers;
    private Boolean blacklistInverted;

    public Boolean isEnabled;
    public Boolean isEnabledServerSwitch;

    public ServerLinksManager(GlacierMain plugin) {
        this.plugin = plugin;
        this.serverLinks = new ArrayList<>();
        this.config = plugin.serverlinksConfig;

        loadSettings();
    }

    public void loadSettings() {
        isEnabled = plugin.config.getBoolean("serverlinks-enabled");
        isEnabledServerSwitch = config.getBoolean("resend-links-on-switch");
        blacklistedServers = config.getStringList("server-blacklist");
        blacklistInverted = config.getBoolean("invert-blacklist");
    }

    public void reloadServerLinks() {
        loadServerLinks();
        if(config.getBoolean("resend-links-on-reload")) resendServerLinks();
    }

    public void loadServerLinks() {
        loadSettings();
        clearServerLinks();

        config.getMap("links").forEach((k, v) -> {
            
            String name = config.getString("links."+k+".name");
            String link = config.getString("links."+k+".link");
            String type = config.getString("links."+k+".type");
            
            if(type == null) {
                serverLinks.add(
                    ServerLink.serverLink(plugin.mm.deserialize(name), link)
                );
            } else {
                serverLinks.add(
                    ServerLink.serverLink(ServerLink.Type.valueOf(type), link)
                );
            }

        });
    }

    public List<ServerLink> getServerLinks() {
        return serverLinks;
    }

    public void clearServerLinks() {
        serverLinks.clear();
    }

    public void resendServerLinks() {

        plugin.server.getAllPlayers().forEach(player -> {
            
            if(player.getCurrentServer().isPresent()) {
                sendServerLinks(player, false);
            }

        });
    }

    public void sendServerLinks(Player player, boolean ignoreBlacklist) {

        if(ignoreBlacklist || checkServerBlacklist(player.getCurrentServer().get().getServerInfo().getName())) {
            sendServerLinksToPlayer(player, serverLinks);
        } else {
            sendServerLinksToPlayer(player, new ArrayList<>());
        }
        
    }

    public static void sendServerLinksToPlayer(Player player, List<ServerLink> serverLinks) {

        ProtocolState state = player.getProtocolState();
        ProtocolVersion version = player.getProtocolVersion();
        if (state != ProtocolState.CONFIGURATION && state != ProtocolState.PLAY) return;
        if (!version.noLessThan(ProtocolVersion.MINECRAFT_1_21)) return;

        player.setServerLinks(serverLinks);
    }

    public boolean checkServerBlacklist(String serverName) {
        return (blacklistInverted && blacklistedServers.contains(serverName)) || (!blacklistInverted && !blacklistedServers.contains(serverName));
    }




}

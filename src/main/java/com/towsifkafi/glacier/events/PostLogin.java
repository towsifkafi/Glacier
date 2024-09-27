package com.towsifkafi.glacier.events;

import java.util.Optional;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.towsifkafi.glacier.GlacierMain;
import com.towsifkafi.glacier.handlers.ServerLinksManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

public class PostLogin {

    private final GlacierMain plugin;


    public PostLogin(GlacierMain plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {

        if(plugin.serverLinksManager.isEnabled) {
            Player player = event.getPlayer();
            plugin.serverLinksManager.sendServerLinks(player, true);
        }
    }

    @Subscribe
    public void onServerPostLogin(ServerPostConnectEvent event) {

        if(plugin.serverLinksManager.isEnabled && plugin.serverLinksManager.isEnabledServerSwitch) {
            Player player = event.getPlayer();
            plugin.serverLinksManager.sendServerLinks(player, false);
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("postlogin");
        out.writeUTF("ping");

        Optional<ServerConnection> connection = event.getPlayer().getCurrentServer();
        if (connection.isPresent()) {
            connection.get().sendPluginMessage(GlacierMain.pluginChannel, out.toByteArray());
        }


    }

}

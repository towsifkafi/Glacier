package com.towsifkafi.glacier.events;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.event.connection.PluginMessageEvent;

public class PluginMessageEvents {
    
    private final GlacierMain plugin;

    public PluginMessageEvents(GlacierMain plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessageFromPlayer(PluginMessageEvent event) {
        
        if (!(event.getSource() instanceof Player)) {
           return;
        }
        Player player = (Player) event.getSource();

        // Ensure the identifier is what you expect before trying to handle the data
        if (event.getIdentifier() != GlacierMain.pluginChannel) {
           return;
        }
    
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        // handle packet data
        // plugin.logger.info(player.getUsername()+": " + in.toString());
    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }
        ServerConnection backend = (ServerConnection) event.getSource();

        // Ensure the identifier is what you expect before trying to handle the data
        if (event.getIdentifier() != GlacierMain.pluginChannel) {
           return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subchannel = in.readUTF();
        String args = in.readUTF();

        // handle packet data
        String log = new StringBuilder()
            .append("[PluginMessage] ")
            .append("(server) [")
            .append(backend.getServerInfo().getName()).append("] --> [")
            .append(subchannel).append("] --> ")
            .append(args).toString();

        plugin.logger.info(log);
    }

}

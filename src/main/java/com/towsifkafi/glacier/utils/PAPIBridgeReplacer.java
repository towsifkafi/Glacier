package com.towsifkafi.glacier.utils;

import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import net.william278.papiproxybridge.user.OnlineUser;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PAPIBridgeReplacer {

    private final PlaceholderAPI api;
    public PAPIBridgeReplacer(GlacierMain plugin) {
        this.api = PlaceholderAPI.createInstance();
    }

    public CompletableFuture<Component> formatComponentPlaceholders(String message, UUID player) {
        return api.formatComponentPlaceholders(message, player);
    }

}

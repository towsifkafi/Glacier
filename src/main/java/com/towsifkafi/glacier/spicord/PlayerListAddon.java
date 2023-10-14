package com.towsifkafi.glacier.spicord;

import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.proxy.ProxyServer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.spicord.api.addon.SimpleAddon;
import org.spicord.bot.command.DiscordBotCommand;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerListAddon extends SimpleAddon {

    GlacierMain plugin;
    public PlayerListAddon(GlacierMain pl) {
        super("Player List", "glacier::players", "TowsifKafi", new String[] { "players" });
        this.plugin = pl;
    }

    @Override
    public void onCommand(DiscordBotCommand command, String[] args) {

        ProxyServer server = plugin.server;

        Map<String, List<String>> playerList = new HashMap<>();

        server.getAllPlayers().forEach(player -> {
            String playerName = player.getUsername();
            String playerServer = player.getCurrentServer().get().getServerInfo().getName();

            List<String> list = playerList.get(playerServer);
            if(list == null) {
                list = new ArrayList<>();
                list.add(playerName);
                playerList.put(playerServer, list);
            } else {
                list.add(playerName);
                playerList.put(playerServer, list);
            }

        });
        plugin.logger.info(playerList.toString());

        String hexColor = plugin.config.getString("playerlist.color");
        String footer = plugin.config.getString("playerlist.footer-text");

        if(playerList.isEmpty()) {
            String noplayer = plugin.config.getString("playerlist.no-player");
            final EmbedBuilder builder = new EmbedBuilder()
                    .setDescription(noplayer)
                    .setColor(Color.decode(hexColor));
            command.reply(builder.build());
        } else {
            AtomicReference<String> description = new AtomicReference<>("");
            playerList.forEach((srvName, list) -> {
                String listString = String.join(", ", escapeUnderscores(list));
                description.set(description + "**" + srvName + " ("+ list.size() +"):** " + listString + "\n");
            });

            final EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("🎮 Total players: "+server.getAllPlayers().size())
                    .setDescription(description.get())
                    .setColor(Color.decode(hexColor))
                    .setFooter(footer);

            command.reply(builder.build());
        }

    }

    private String[] escapeUnderscores(List<String> players) {
        return players.stream()
                //.map(s -> s.replace("_", "\\_"))
                .map(s -> "`"+s+"`")
                .toArray(String[]::new);
    }
}

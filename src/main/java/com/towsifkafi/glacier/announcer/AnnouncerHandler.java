package com.towsifkafi.glacier.announcer;

import com.towsifkafi.glacier.GlacierMain;
import com.towsifkafi.glacier.config.ConfigProvider;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnnouncerHandler {

    public static void scheduleAnnouncement(GlacierMain plugin, String id) {

        String key = "announcements."+ id;
        int delay = plugin.announcerConfig.getInt(key+".delay");

        ScheduledTask task = plugin.server.getScheduler()
                .buildTask(plugin, () -> {
                    handleAnnouncement(plugin, id);
                })
                .repeat(delay, TimeUnit.SECONDS)
                .schedule();

        plugin.messageSchedules.put(id, task);

    }

    public static void handleAnnouncement(GlacierMain plugin, String id) {

        String key = "announcements."+ id;
        String serverName = plugin.announcerConfig.getString(key+".server");

        plugin.server.getAllServers().forEach(server -> {
            server.getPlayersConnected().forEach(player -> {

                if(player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(serverName)
                        || serverName.equalsIgnoreCase("all")) {
                    sendAnnouncement(plugin, id, player);
                }

            });
        });

    }

    public static void sendAnnouncement(GlacierMain plugin, String id, Player player) {

        String key = "announcements."+ id;
        String type = plugin.announcerConfig.getString(key+".type");
        List<String> text = plugin.announcerConfig.getStringList(key+".text");

        if(type.equalsIgnoreCase("message")) {

            if(plugin.papi == null) {
                player.sendMessage(
                        plugin.mm.deserialize(String.join("\n", text))
                );
            } else {
                plugin.papi.formatPlaceholders(String.join("\n", text), player.getUniqueId()).thenAccept(message -> {
                    player.sendMessage(
                            plugin.mm.deserialize(message)
                    );
                });
            }

        } else if(type.equalsIgnoreCase("title")) {
            int fadeIn = plugin.announcerConfig.getInt(key+".fadeIn");
            int stay = plugin.announcerConfig.getInt(key+".stay");
            int fadeOut = plugin.announcerConfig.getInt(key+".fadeOut");

            if(fadeIn < 0) fadeIn = 1;
            if(stay < 0) stay = 3;
            if(fadeOut < 0) fadeOut = 1;

            Title.Times times = Title.Times.times(
                    Duration.ofSeconds((long) (fadeIn)),
                    Duration.ofSeconds((long) (stay)),
                    Duration.ofSeconds((long) (fadeOut))
            );

            Component titleText = plugin.mm.deserialize("No Title");
            Component subtitleText = Component.empty();
            if(!text.isEmpty()) titleText = plugin.mm.deserialize(text.get(0));
            if(text.size() > 1)subtitleText = plugin.mm.deserialize(text.get(1));

            Title title = Title.title(
                    titleText,
                    subtitleText,
                    times
            );

            player.showTitle(title);
        } else if(type.equalsIgnoreCase("actionbar")) {
            if(plugin.papi == null) {
                player.sendActionBar(plugin.mm.deserialize(String.join(" ", text)));
            } else {
                plugin.papi.formatPlaceholders(String.join(" ", text), player.getUniqueId()).thenAccept(message -> {
                    player.sendActionBar(
                            plugin.mm.deserialize(message)
                    );
                });
            }
        }

    }

}

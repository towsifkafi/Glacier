package com.towsifkafi.glacier.handlers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.towsifkafi.glacier.GlacierMain;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AnnouncementManager {

    public GlacierMain plugin;
    public Map<String, ScheduledTask> messageSchedules = new HashMap<>();
    private boolean simpleRandom = false;

    private String defaultSound = "none";

    private int delay = 300;
    private int index = 0;

    public Map<String, LinkedHashMap<String, ?>> timedAnnouncements = new LinkedHashMap<>();
    public Map<String, LinkedHashMap<String, ?>> simpleAnnouncements = new LinkedHashMap<>();

    public AnnouncementManager(GlacierMain plugin) {
        this.plugin = plugin;
    }

    public void loadAnnouncements() {

        messageSchedules.forEach((k, s) -> {
            s.cancel();
        });
        messageSchedules.clear();
        simpleAnnouncements.clear();

        if(plugin.config.getBoolean("announcer-enabled")) {
            index = 0;
            simpleRandom = (plugin.announcerConfig.getString("default-mode").contentEquals("SIMPLE_RANDOM"));
            delay = plugin.announcerConfig.getInt("delay");
            defaultSound = plugin.announcerConfig.getString("default-sound");

            loadTimedAnnouncements();
        }

    }

    public void loadTimedAnnouncements() {
        plugin.announcerConfig.getMap("announcements").forEach((k, obj) -> {

            @SuppressWarnings("unchecked")
            LinkedHashMap<String, ?> map = (LinkedHashMap<String, ?>) obj;

            String mode = (String) map.get("mode");

            if(mode == null) mode = plugin.announcerConfig.getString("default-mode");
            if(mode.equalsIgnoreCase("time_based")) {
                timedAnnouncements.put(k, map);
                scheduleAnnouncement(k);
            } else if(mode.toLowerCase().contains("simple")) {
                simpleAnnouncements.put(k, map);
            } else {
                plugin.logger.warn(
                    new StringBuilder()
                    .append("The announcement `").append(k).append("` ")
                    .append("doesn't have correct `mode` value defined.").toString()
                );
            }
        });

        ScheduledTask task = plugin.server.getScheduler()
                .buildTask(plugin, () -> {
                    if(!simpleAnnouncements.isEmpty()) {
                        if(simpleRandom) index = (int) (Math.random() * simpleAnnouncements.size());
                        handleAnnouncement(simpleAnnouncements.keySet().toArray()[index].toString());
                        if(!simpleRandom) index = (index + 1) % simpleAnnouncements.size(); 
                    }
                })
                .repeat(delay, TimeUnit.SECONDS)
                .schedule();

        messageSchedules.put("simple", task);

    }


    public void reloadAnnouncer() {
        plugin.announcerConfig.loadConfig();
        loadAnnouncements();
    }

    public void scheduleAnnouncement(String id) {

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, ?> obj = (LinkedHashMap<String, ?>) plugin.announcerConfig.getMap("announcements").get(id);

        if(obj.get("delay") == null) {
            plugin.logger.warn(
                new StringBuilder()
                .append("The announcement `").append(id).append("` ")
                .append("doesn't have `delay` value defined.").toString()
            );
        } else {
            int post_delay = (int) obj.get("delay");

            ScheduledTask task = plugin.server.getScheduler()
                    .buildTask(plugin, () -> {
                        handleAnnouncement(id);
                    })
                    .repeat(post_delay, TimeUnit.SECONDS)
                    .schedule();

            messageSchedules.put(id, task);
        }

    }

    public void handleAnnouncement(String id) {

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, ?> obj = (LinkedHashMap<String, ?>) plugin.announcerConfig.getMap("announcements").get(id);

        String serverName = (String) obj.get("server");

        if(plugin.announcerConfig.getBoolean("show-in-console")) sendToConsole(id);

        plugin.server.getAllServers().forEach(server -> {
            server.getPlayersConnected().forEach(player -> {

                if(player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(serverName)
                        || serverName.equalsIgnoreCase("all")) {
                    sendAnnouncement(id, player);

                }

            });
        });

    }

    public void sendToConsole(String id) {
        
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, ?> obj = (LinkedHashMap<String, ?>) plugin.announcerConfig.getMap("announcements").get(id);

        String type = (String) obj.get("type");
        @SuppressWarnings("unchecked")
        List<String> text = (List<String>) obj.get("text");

        if(type.equalsIgnoreCase("message")) {
            plugin.server.getConsoleCommandSource().sendMessage(
                plugin.mm.deserialize(String.join("\n", text))
            );
        }

    }

    public void sendAnnouncement(String id, Player player) {

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, ?> obj = (LinkedHashMap<String, ?>) plugin.announcerConfig.getMap("announcements").get(id);

        String type = (String) obj.get("type");
        @SuppressWarnings("unchecked")
        List<String> text = (List<String>) obj.get("text");

        String sound = (String) obj.get("sound");
        if(sound == null) {
            sound = defaultSound;
        }

        if(sound.equalsIgnoreCase("none")) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("playsound");
        out.writeUTF(sound);

        Optional<ServerConnection> connection = player.getCurrentServer();
        if (connection.isPresent()) {
            connection.get().sendPluginMessage(GlacierMain.pluginChannel, out.toByteArray());
        }

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
            int fadeIn = (int) obj.get("fadeIn");
            int stay = (int) obj.get("stay");
            int fadeOut = (int) obj.get("fadeOut");

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

package com.towsifkafi.glacier.handlers;

import com.towsifkafi.glacier.GlacierMain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TimedCommand {
    public GlacierMain plugin;

    private ZoneId zoneId;
    private ZonedDateTime now;
    private DateTimeFormatter formatter, formatterDay, formatterDate;
    private String realTime, realDay, realDate, date, day, time, cmd;

    private boolean isEnable;
    private int count;

    public TimedCommand(GlacierMain plugin) {
        this.plugin = plugin;
    }

    private void ConfigValues() {
        zoneId = ZoneId.of("Asia/Dhaka");
        now = ZonedDateTime.now(zoneId);
        formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        formatterDay = DateTimeFormatter.ofPattern("E");
        formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        realTime = now.format(formatter);
        realDay = now.format(formatterDay);
        realDate = now.format(formatterDate);

        isEnable = plugin.config.getBoolean("timedCommand-enabled");
    }

    public void runScheduledTask() {
        if (!isEnable) return;

        now = ZonedDateTime.now(zoneId);
        realTime = now.format(formatter);
        checkCommands();
    }


    public void reloadTimedCommand() {
        plugin.timedCommandConfig.loadConfig();
        ConfigValues();
    }

    private void checkCommands() {
        Map<String, Object> values = (Map<String, Object>) plugin.timedCommandConfig.getMap("commands");

        if (values != null) {
            for (Map.Entry<String, Object> gameEntry : values.entrySet()) {
                String gameKey = gameEntry.getKey();
                Map<String, Object> gameValues = (Map<String, Object>) gameEntry.getValue();

                if (gameValues != null) {
                    for (Map.Entry<String, Object> commandEntry : gameValues.entrySet()) {
                        String commandKey = commandEntry.getKey();
                        Map<String, Object> commandValues = (Map<String, Object>) commandEntry.getValue();

                        if (commandValues != null) {

                            // Retrieve alert configuration values
                            day = (String) commandValues.get("day");
                            date = (String) commandValues.get("date");
                            time = (String) commandValues.get("time");
                            cmd = (String) commandValues.get("cmd");
                            count = (int) commandValues.get("count");


                            boolean dateMatches = (date == null || date.isEmpty() || realDate.equals(date));
                            boolean dayMatches = (day == null || day.isEmpty() || realDay.equals(day));
                            boolean timeMatches = realTime.equals(time);

                            if (dateMatches && dayMatches && timeMatches) {
                                for (int i = 0; i < count; i++) {
                                    plugin.server.getCommandManager().executeAsync(plugin.server.getConsoleCommandSource(), cmd);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

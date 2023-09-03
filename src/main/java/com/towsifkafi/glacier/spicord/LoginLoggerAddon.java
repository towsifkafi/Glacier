package com.towsifkafi.glacier.spicord;

import com.towsifkafi.glacier.GlacierMain;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.spicord.api.addon.SimpleAddon;
import org.spicord.bot.DiscordBot;

public class LoginLoggerAddon extends SimpleAddon {

    private GlacierMain plugin;
    private DiscordBot bot;
    private static LoginLoggerAddon instance;

    public LoginLoggerAddon(GlacierMain plugin) {
        super("Login Logger", "glacier::loginlogger", "TowsifKafi");
        this.plugin = plugin;
        instance = this;
    }

    public void sendMessage(String message, String channel) {
        TextChannel tc = bot.getJda().getTextChannelById(channel);
        if(tc != null) {
            tc.sendMessage(message).queue();
        }
    }

    public static LoginLoggerAddon getInstance() {
        return instance;
    }


}

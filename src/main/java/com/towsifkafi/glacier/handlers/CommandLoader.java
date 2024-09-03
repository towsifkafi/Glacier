package com.towsifkafi.glacier.handlers;

import java.util.ArrayList;
import java.util.List;

import com.towsifkafi.glacier.GlacierMain;
import com.towsifkafi.glacier.commands.GActionBar;
import com.towsifkafi.glacier.commands.GAnnouncer;
import com.towsifkafi.glacier.commands.GKick;
import com.towsifkafi.glacier.commands.GSudo;
import com.towsifkafi.glacier.commands.GTitle;
import com.towsifkafi.glacier.commands.Glacier;
import com.towsifkafi.glacier.config.ConfigProvider;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;

public class CommandLoader {

    private GlacierMain plugin;
    private ConfigProvider commands;
    private CommandManager commandManager;

    public List<CommandMeta> commandMetas;

    public CommandLoader(GlacierMain plugin) {
        this.plugin = plugin;
        this.commands = plugin.commands;
        this.commandManager = plugin.commandManager;

        this.commandMetas = new ArrayList<>();
    }

    public void loadCommands() {
        if(commands.getBoolean("glacier.enabled")) new Glacier(plugin);
        if(commands.getBoolean("gtitle.enabled")) new GTitle(plugin);
        if(commands.getBoolean("gactionbar.enabled")) new GActionBar(plugin);
        if(commands.getBoolean("gannouncer.enabled")) new GAnnouncer(plugin);
        if(commands.getBoolean("gsudo.enabled")) new GSudo(plugin);
        if(commands.getBoolean("gkick.enabled")) new GKick(plugin);
    }

    public void unloadCommands() {
        commandMetas.forEach(commandManager::unregister);
        commandMetas.clear();
    }

    public void reloadCommands() {
        unloadCommands();
        loadCommands();
    }

}
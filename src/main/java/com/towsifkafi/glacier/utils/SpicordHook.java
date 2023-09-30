package com.towsifkafi.glacier.utils;

import com.towsifkafi.glacier.GlacierMain;
import com.towsifkafi.glacier.spicord.PlayerListAddon;
import org.spicord.SpicordLoader;

public class SpicordHook {

    private final PlayerListAddon playerList;
    public SpicordHook(GlacierMain plugin) {
        super();
        this.playerList = new PlayerListAddon(plugin);
        SpicordLoader.addStartupListener(spicord -> spicord.getAddonManager().registerAddon(
                playerList
        ));
    }

}

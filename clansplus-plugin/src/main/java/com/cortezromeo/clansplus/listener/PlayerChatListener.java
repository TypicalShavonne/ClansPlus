package com.cortezromeo.clansplus.listener;

import com.cortezromeo.clansplus.ClansPlus;
import com.cortezromeo.clansplus.Settings;
import com.cortezromeo.clansplus.inventory.PaginatedInventory;
import com.cortezromeo.clansplus.language.Messages;
import com.cortezromeo.clansplus.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;

public class PlayerChatListener implements Listener {

    public static HashMap<Player, InventoryHolder> searchingQueryInventoryList = new HashMap<>();

    public PlayerChatListener() {
        Bukkit.getPluginManager().registerEvents(this, ClansPlus.plugin);
    }

    public static void addSearchPlayerQuery(Player player, InventoryHolder inventoryHolder) {
        MessageUtil.sendMessage(player, Messages.USING_CHAT_BOX_INVENTORY_LIST_SEARCH);
        MessageUtil.sendMessage(player, Messages.USING_CHAT_BOX_CANCEL_USING_CHAT_BOX.replace("%word%", Settings.CHAT_SETTING_STOP_USING_CHAT_WORD));
        searchingQueryInventoryList.put(player, inventoryHolder);
    }

    public static void removeSearchPlayerQuery(Player player) {
        searchingQueryInventoryList.remove(player);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();

        if (searchingQueryInventoryList.containsKey(player)) {
            InventoryHolder holder = searchingQueryInventoryList.get(player).getInventory().getHolder();
            if (holder instanceof PaginatedInventory)
                ((PaginatedInventory) holder).onSearch(event);
            searchingQueryInventoryList.remove(player);
        }
    }

}

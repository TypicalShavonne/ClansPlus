package com.cortezromeo.clansplus.api.server;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class VersionSupport {

    private final Plugin plugin;

    public VersionSupport(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public abstract ItemStack getItemStack(ItemStack itemStack);

    public abstract ItemStack createItemStack(String material, int amount, int customModelData, boolean glow);

    public abstract Sound createSound(String soundName);

    public abstract ItemStack getHeadItemFromPlayerName(String playerName);

    public abstract ItemStack getHeadItemFromBase64(String headValue);

    public abstract ItemStack addCustomData(ItemStack itemStack, String data);

    public abstract String getCustomData(ItemStack itemStack);

    public abstract String addColor(String textToTranslate);

    public abstract String stripColor(String textToStrip);

    public abstract Particle getParticle(String particleName);
}

package com.cortezromeo.clansplus.clan.subject;

import com.cortezromeo.clansplus.Settings;
import com.cortezromeo.clansplus.api.enums.IconType;
import com.cortezromeo.clansplus.api.enums.Rank;
import com.cortezromeo.clansplus.api.enums.Subject;
import com.cortezromeo.clansplus.api.storage.IClanData;
import com.cortezromeo.clansplus.clan.ClanManager;
import com.cortezromeo.clansplus.clan.SubjectManager;
import com.cortezromeo.clansplus.language.Messages;
import com.cortezromeo.clansplus.storage.PluginDataManager;
import com.cortezromeo.clansplus.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SetIcon extends SubjectManager {

    private IconType iconType;
    private String value;

    public SetIcon(Rank rank, Player player, String playerName, IconType type, String value) {
        super(rank, player, playerName, null, null);
        this.iconType = type;
        this.value = value;
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.SETICON));

        String commandPermission = "clanplus.seticon";
        if (!player.hasPermission(commandPermission)) {
            MessageUtil.sendMessage(player, Messages.PERMISSION_REQUIRED.replace("%permission%", commandPermission));
            return false;
        }

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        IClanData playerClanData = getPlayerClanData();

        if (iconType == IconType.MATERIAL) {
            try {
                XMaterial xMaterial = XMaterial.valueOf(value);
                Material material = xMaterial.get();
                if (material == null || material.equals(Material.AIR)) {
                    MessageUtil.sendMessage(player, Messages.INVALID_ICON_VALUE);
                    MessageUtil.sendMessage(player, "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
                    return false;
                }
            } catch (Exception exception) {
                MessageUtil.sendMessage(player, Messages.INVALID_ICON_VALUE);
                return false;
            }
        }

        playerClanData.setIconType(iconType);
        playerClanData.setIconValue(value);

        PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);
        MessageUtil.sendMessage(player, Messages.SET_ICON_SUCCESS.replace("%value%", value).replace("%type%", iconType.toString()));
        ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_SET_ICON.replace("%player%", playerName).replace("%value%", value).replace("%type%", iconType.toString()).replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank())));
        return true;
    }
}

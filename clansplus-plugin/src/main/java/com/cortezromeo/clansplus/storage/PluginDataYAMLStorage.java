package com.cortezromeo.clansplus.storage;

import com.cortezromeo.clansplus.ClansPlus;
import com.cortezromeo.clansplus.Settings;
import com.cortezromeo.clansplus.api.enums.IconType;
import com.cortezromeo.clansplus.api.enums.Rank;
import com.cortezromeo.clansplus.api.enums.Subject;
import com.cortezromeo.clansplus.api.storage.IClanData;
import com.cortezromeo.clansplus.api.storage.IPlayerData;
import com.cortezromeo.clansplus.clan.ClanManager;
import com.cortezromeo.clansplus.util.FileNameUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PluginDataYAMLStorage implements PluginStorage {

    private static File getClanFile(String clanName) {
        File file = new File(ClansPlus.plugin.getDataFolder() + "/banghoiData/" + clanName + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private static File getPlayerFile(String playerName) {
        File file = new File(ClansPlus.plugin.getDataFolder() + "/playerData/" + playerName + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @Override
    public ClanData getClanData(String clanName) {
        File clanFile = getClanFile(clanName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(clanFile);

        List<String> members = new ArrayList<>();
        List<String> allies = new ArrayList<>();
        HashMap<Integer, Integer> skillLevel = new HashMap<>();
        List<String> allyInvitation = new ArrayList<>();
        HashMap<Subject, Rank> permissionDefault = new HashMap<>();
        for (Subject subject : Subject.values())
            permissionDefault.put(subject, Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(subject));
        ClanData clanData = new ClanData(
                clanName,
                null,
                null,
                null,
                0,
                0,
                0,
                Settings.CLAN_SETTING_MAXIMUM_MEMBER_DEFAULT,
                new Date().getTime(),
                IconType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE),
                Settings.CLAN_SETTING_ICON_DEFAULT_VALUE,
                members,
                null,
                allies,
                skillLevel,
                permissionDefault,
                allyInvitation,
                0,
                null);

        if (!storage.contains("data"))
            return clanData;

        // old data before version 3.4
        if (storage.getString("data.ten") != null) {
            PluginDataManager.fixClansOldData = true;
            clanData.setName(storage.getString("data.ten"));
            clanData.setCustomName(storage.getString("data.ten_custom"));
            clanData.setOwner(storage.getString("data.leader"));
            clanData.setScore(storage.getInt("data.diem"));
            clanData.setWarning(storage.getInt("data.warn"));
            clanData.setCreatedDate(storage.getLong("data.ngay_thanh_lap"));
            clanData.setMaxMembers(storage.getInt("data.thanh_vien_toi_da"));
            for (String player : storage.getStringList("data.thanh_vien"))
                clanData.getMembers().add(player);

            storage.set("data.ten", null);
            storage.set("data.ten_custom", null);
            storage.set("data.leader", null);
            storage.set("data.diem", null);
            storage.set("data.warn", null);
            storage.set("data.ngay_thanh_lap", null);
            storage.set("data.thanh_vien_toi_da", null);
            storage.set("data.thanh_vien", null);

            // TODO <DATA FIX 3.4> Old database does not have icon type
            if (storage.getString("data.banghoiicon") != null) {
                String oldIconValue = storage.getString("data.banghoiicon").toUpperCase();
                try {
                    XMaterial xMaterial = XMaterial.valueOf(oldIconValue);
                    if (xMaterial.get() != null) {
                        clanData.setIconType(IconType.MATERIAL);
                        clanData.setIconValue(oldIconValue);
                    } else {
                        clanData.setIconType(IconType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE));
                        clanData.setIconValue(Settings.CLAN_SETTING_ICON_DEFAULT_VALUE);
                    }
                } catch (Exception exception) {
                    clanData.setIconType(IconType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE));
                    clanData.setIconValue(Settings.CLAN_SETTING_ICON_DEFAULT_VALUE);
                }
                storage.set("data.banghoiicon", null);
            }

            try {
                storage.save(clanFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            clanData.setName(storage.getString("data.name"));
            clanData.setCustomName(storage.getString("data.custom-name"));
            clanData.setOwner(storage.getString("data.owner"));
            clanData.setScore(storage.getInt("data.score"));
            clanData.setWarning(storage.getInt("data.warning"));
            clanData.setCreatedDate(storage.getLong("data.created-date"));
            clanData.setMaxMembers(storage.getInt("data.max-members"));
            clanData.setMembers(storage.getStringList("data.members"));
        }

        clanData.setAllies(storage.getStringList("data.allies"));

        // TODO <DATA FIX 3.4> All managers from the list will have rank MANAGER, delete managers list
        if (storage.getString("data.managers") != null) {
            for (String manager : storage.getStringList("data.managers"))
                ClanManager.managersFromOldData.put(manager, clanName);
            storage.set("data.managers", null);
        }

        clanData.setMessage(storage.getString("data.message"));
        clanData.setWarPoint(storage.getLong("data.warpoint"));

        String iconType = storage.getString("data.icon.type");
        if (iconType != null) {
            clanData.setIconType(IconType.valueOf(storage.getString("data.icon.type")));
            clanData.setIconValue(storage.getString("data.icon.value"));
        }

        String spawnWorld = storage.getString("data.spawn.world");
        if (spawnWorld != null) {
            Location location = new Location(Bukkit.getWorld(spawnWorld), storage.getDouble("data.spawn.x"), storage.getDouble("data.spawn.y"), storage.getDouble("data.spawn.z"));
            clanData.setSpawnPoint(location);
        }

        clanData.getSkillLevel().put(1, storage.getInt("data.skill.1"));
        clanData.getSkillLevel().put(2, storage.getInt("data.skill.2"));
        clanData.getSkillLevel().put(3, storage.getInt("data.skill.3"));
        clanData.getSkillLevel().put(4, storage.getInt("data.skill.4"));

/*        data.setSkillLevel(SkillType.critDamage, storage.getInt("data.skill.1"));
        data.setSkillLevel(SkillType.boostScore, storage.getInt("data.skill.2"));
        data.setSkillLevel(SkillType.dodge, storage.getInt("data.skill.3"));
        data.setSkillLevel(SkillType.vampire, storage.getInt("data.skill.4"));*/

        if (storage.getConfigurationSection("data.permission") == null) {
            HashMap<Subject, Rank> newPermissionDefault = new HashMap<>();
            for (Subject subject : Subject.values())
                newPermissionDefault.put(subject, Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(subject));
            clanData.setSubjectPermission(newPermissionDefault);
        } else {
            for (String subjectName : storage.getConfigurationSection("data.permission").getKeys(false)) {
                Subject subject = Subject.valueOf(subjectName);
                Rank rank = Rank.valueOf(storage.getString("data.permission." + subjectName));
                clanData.getSubjectPermission().put(subject, rank);
            }
        }

        clanData.setAllyInvitation(storage.getStringList("data.ally-invitation"));
        clanData.setDiscordChannelID(storage.getLong("data.discord.channel-id"));
        clanData.setDiscordJoinLink(storage.getString("data.discord.join-link"));

        return clanData;
    }

    @Override
    public void saveClanData(String clanName, IClanData clanData) {
        File file = getClanFile(clanName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(file);

        storage.set("data.name", clanData.getName());
        storage.set("data.custom-name", clanData.getCustomName());
        storage.set("data.owner", clanData.getOwner());
        storage.set("data.message", clanData.getMessage());
        storage.set("data.score", clanData.getScore());
        storage.set("data.created-date", clanData.getCreatedDate());
        storage.set("data.max-members", clanData.getMaxMembers());
        storage.set("data.members", clanData.getMembers());
        storage.set("data.allies", clanData.getAllies());
        storage.set("data.warn", clanData.getWarning());
        storage.set("data.warpoint", clanData.getWarPoint());
        storage.set("data.icon.type", clanData.getIconType().toString().toUpperCase());
        storage.set("data.icon.value", clanData.getIconValue());
        if (clanData.getSpawnPoint() != null) {
            storage.set("data.spawn.world", clanData.getSpawnPoint().getWorld().getName());
            storage.set("data.spawn.x", clanData.getSpawnPoint().getX());
            storage.set("data.spawn.y", clanData.getSpawnPoint().getY());
            storage.set("data.spawn.z", clanData.getSpawnPoint().getZ());
        }
        for (int skillID : clanData.getSkillLevel().keySet())
            storage.set("data.skill." + skillID, clanData.getSkillLevel().get(skillID));
        for (Subject subject : clanData.getSubjectPermission().keySet()) {
            storage.set("data.permission." + subject.toString(), clanData.getSubjectPermission().get(subject).toString().toUpperCase());
        }
        storage.set("data.ally-invitation", clanData.getAllyInvitation());
        storage.set("data.discord.channel-id", clanData.getDiscordChannelID());
        storage.set("data.discord.join-link", clanData.getDiscordJoinLink());
        try {
            storage.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData getPlayerData(String playerName) {
        File playerFile = getPlayerFile(playerName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(playerFile);

        PlayerData playerData = new PlayerData(playerName,
                (Bukkit.getPlayer(playerName) != null ? Bukkit.getPlayer(playerName).getUniqueId().toString() : null),
                null,
                null,
                0,
                0,
                new Date().getTime());

        if (!storage.contains("data"))
            return playerData;

        if (storage.getString("data.chuc_vu") != null || storage.getString("data.bang_hoi") != null) {
            PluginDataManager.fixMembersOldData = true;
            playerData.setClan(storage.getString("data.bang_hoi"));
            playerData.setJoinDate(storage.getLong("data.ngay_tham_gia"));
            playerData.setScoreCollected(storage.getLong("data.diem_kiem_duoc"));
            try {
                playerData.setRank(Rank.valueOf(storage.getString("data.chuc_vu").toUpperCase()));
            } catch (NullPointerException | IllegalArgumentException exception) {
                playerData.setRank(null);
            }

            storage.set("data.bang_hoi", null);
            storage.set("data.ngay_tham_gia", null);
            storage.set("data.diem_kiem_duoc", null);
            storage.set("data.chuc_vu", null);

            storage.set("data.clan", playerData.getClan());
            storage.set("data.rank", String.valueOf(playerData.getRank()));
            storage.set("data.join-date", playerData.getJoinDate());
            storage.set("data.score-collected", playerData.getScoreCollected());
            try {
                storage.save(playerFile);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            playerData.setPlayerName(storage.getString("data.playerName"));
            if (storage.getString("data.UUID") == null) {
                if (Bukkit.getPlayer(playerName) != null)
                    playerData.setUUID(storage.getString(Bukkit.getPlayer(playerName).getUniqueId().toString()));
            } else
                playerData.setUUID(storage.getString("data.UUID"));
            playerData.setClan(storage.getString("data.clan"));
            playerData.setJoinDate(storage.getLong("data.join-date"));
            playerData.setScoreCollected(storage.getInt("data.score-collected"));
            playerData.setLastActivated(storage.getLong("data.last-activated"));
            try {
                playerData.setRank(Rank.valueOf(storage.getString("data.rank").toUpperCase()));
            } catch (Exception exception) {
                playerData.setRank(null);
            }
        }

        return playerData;
    }

    @Override
    public void savePlayerData(String playerName, IPlayerData playerData) {
        File file = getPlayerFile(playerName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(file);

        storage.set("data.playerName", playerName);
        storage.set("data.UUID", playerData.getUUID());
        storage.set("data.clan", playerData.getClan());
        storage.set("data.rank", String.valueOf(playerData.getRank()));
        storage.set("data.join-date", playerData.getJoinDate());
        storage.set("data.score-collected", playerData.getScoreCollected());
        storage.set("data.last-activated", playerData.getLastActivated());

        try {
            storage.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteClanData(String clanName) {
        File clanFile = new File(ClansPlus.plugin.getDataFolder() + "/banghoiData/" + clanName + ".yml");
        if (!clanFile.exists())
            return true;

        try {
            return clanFile.delete();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getAllClans() {
        File clanFolder = new File(ClansPlus.plugin.getDataFolder() + "/banghoiData");
        File[] listOfFilesClan = clanFolder.listFiles();
        List<String> clans = new ArrayList<>();

        if (listOfFilesClan == null)
            return clans;

        for (File file : listOfFilesClan) {
            try {
                if (file.isFile()) {
                    String clanName = FileNameUtil.removeExtension(file.getName());
                    clans.add(clanName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return clans;
    }

    @Override
    public List<String> getAllPlayers() {
        File playerFolder = new File(ClansPlus.plugin.getDataFolder() + "/playerData");
        File[] listOfFilesPlayer = playerFolder.listFiles();
        List<String> players = new ArrayList<>();

        if (listOfFilesPlayer == null)
            return players;

        for (File file : listOfFilesPlayer) {
            try {
                if (file.isFile()) {
                    String playerName = FileNameUtil.removeExtension(file.getName());
                    players.add(playerName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }

    @Override
    public void disableStorage() {
    }
}

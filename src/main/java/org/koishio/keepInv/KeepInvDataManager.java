package org.koishio.keepInv;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class KeepInvDataManager {
    private final NamespacedKey key;

    public KeepInvDataManager(NamespacedKey key) {
        this.key = key;
    }

    public boolean getKeepInventoryState(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Byte value = pdc.get(key, PersistentDataType.BYTE);
        if (value == null) {
            // 使用世界默认设置
            return Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY));
        }
        return value == 1;
    }

    public boolean isParentFromGamerule(Player player) {
        return !player.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public void setKeepInventoryState(Player player, boolean state) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.BYTE, (byte) (state ? 1 : 0));
        player.saveData();
    }

    public void resetKeepInventoryState(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(key);
        player.saveData();
    }
}

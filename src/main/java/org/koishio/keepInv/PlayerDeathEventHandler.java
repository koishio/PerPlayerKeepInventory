package org.koishio.keepInv;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathEventHandler implements Listener {

    private KeepInvDataManager dataManager;
    public PlayerDeathEventHandler(KeepInvDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (dataManager.getKeepInventoryState(player) && !dataManager.isParentFromGamerule(player)) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            // 清空掉落物和经验掉落
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
}

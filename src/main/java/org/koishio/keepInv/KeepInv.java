package org.koishio.keepInv;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class KeepInv extends JavaPlugin {

    private final NamespacedKey KEEP_INV_KEY = new NamespacedKey(this, "keep_inventory");
    private final Logger LOGGER = this.getLogger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        LOGGER.info("插件已加载!");
        saveDefaultConfig();
        reloadConfig();
        FileConfiguration config = getConfig();
        String rootCommandNodeName = config.getString("rootCommandNodeName", "keepInv");
        KeepInvDataManager dataManager = new KeepInvDataManager(KEEP_INV_KEY);
        KeepInvCommand keepInvCommand = new KeepInvCommand(LOGGER, dataManager, this.getLifecycleManager());
        getServer().getPluginManager().registerEvents(new PlayerDeathEventHandler(dataManager), this);
        keepInvCommand.registerKeepInvCommand(rootCommandNodeName);
    }

    @Override
    public void onDisable() {
        LOGGER.info("插件已卸载!");
    }

}

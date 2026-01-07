package org.koishio.keepInv;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class KeepInv extends JavaPlugin {

    private final NamespacedKey KEEP_INV_KEY = new NamespacedKey(this, "keep_inventory");
    private final Logger LOGGER = this.getLogger();
    private KeepInvDataManager dataManager;
    private KeepInvCommand keepInvCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // TODO: 从config加载命令根节点名称
        // saveDefaultConfig();
        dataManager = new KeepInvDataManager(KEEP_INV_KEY);
        keepInvCommand = new KeepInvCommand(LOGGER, dataManager, this.getLifecycleManager());
        getServer().getPluginManager().registerEvents(new PlayerDeathEventHandler(dataManager), this);
        keepInvCommand.registerKeepInvCommand();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}

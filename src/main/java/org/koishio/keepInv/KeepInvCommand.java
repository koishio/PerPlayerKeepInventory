package org.koishio.keepInv;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;

import java.util.logging.Logger;

public class KeepInvCommand {

    private final Logger LOGGER;
    private KeepInvDataManager dataManager;
    private LifecycleEventManager<Plugin> lifecycleEventManager;

    public KeepInvCommand(Logger logger, KeepInvDataManager dataManager, LifecycleEventManager<Plugin> lifecycleEventManager) {
        LOGGER = logger;
        this.dataManager = dataManager;
        this.lifecycleEventManager = lifecycleEventManager;
    }

    public void registerKeepInvCommand() {
        registerKeepInvCommand("keepInv");
    }

    public void registerKeepInvCommand(String rootCommandNodeName) {
        // 主命令节点
        LiteralCommandNode<CommandSourceStack> keepInvCommand = Commands.literal(rootCommandNodeName)
                .requires(source -> source.getSender().hasPermission("keepinv.use"))
                .then(
                        Commands.literal("get")
                                .requires(source -> source.getSender().hasPermission("keepinv.get"))
                                .executes(ctx -> executeGetKeepInv(ctx.getSource()))
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.get.others"))
                                                .executes(ctx ->
                                                        executeGetKeepInv(ctx.getSource(),
                                                                ctx.getArgument("player", Player.class)))
                                )
                )
                .then(
                        Commands.literal("set")
                                .requires(source -> source.getSender().hasPermission("keepinv.set"))
                                .then(
                                        Commands.argument("status", BoolArgumentType.bool())
                                                .executes(ctx ->
                                                        executeSetKeepInv(ctx.getSource(),
                                                                ctx.getArgument("status", boolean.class)))
                                )
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.set.others"))
                                                .then(
                                                        Commands.argument("status", BoolArgumentType.bool())
                                                                .executes(ctx ->
                                                                        executeSetKeepInv(ctx.getSource(),
                                                                                ctx.getArgument("player", Player.class),
                                                                                ctx.getArgument("status", boolean.class)))
                                                )
                                )
                )
                .then(
                        Commands.literal("reset")
                                .requires(source -> source.getSender().hasPermission("keepinv.reset"))
                                .executes(ctx -> executeResetKeepInv(ctx.getSource()))
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.reset.others"))
                                                .executes(ctx ->
                                                        executeResetKeepInv(ctx.getSource(),
                                                                ctx.getArgument("player", Player.class)))
                                )
                )
                .build();

        // 注册命令
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(keepInvCommand);
        });
    }

    private int executeGetKeepInv(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 0;
        }
        return executeGetKeepInv(source, (Player) source.getSender());
    }

    private int executeGetKeepInv(CommandSourceStack source, Player player) {
        boolean status = dataManager.getKeepInventoryState(player);
        if (!dataManager.isParentFromGamerule(player)) {
            if (status) {
                source.getSender().sendMessage(Component.text(player.getName() + " 已主动开启保留物品栏选项."));
            }
            else {
                source.getSender().sendMessage(Component.text(player.getName() + "已主动关闭保留物品栏选项"));
            }
        } else {
            source.getSender().sendMessage(Component.text(player.getName() + " 的保留物品栏选项: " + (status ? "开启" : "关闭") + "(继承自世界默认设置)"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeSetKeepInv(CommandSourceStack source, boolean status) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 1;
        }
        return executeSetKeepInv(source, (Player) source.getSender(), status);
    }

    private int executeSetKeepInv(CommandSourceStack source, Player player, boolean status) {
        dataManager.setKeepInventoryState(player, status);
        source.getSender().sendMessage("已将" + player.getName() + "的保留物品栏选项设置为" + (status ? "开启" : "关闭"));
        return Command.SINGLE_SUCCESS;
    }

    private int executeResetKeepInv(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 1;
        }
        return executeResetKeepInv(source, (Player) source.getSender());
    }

    private int executeResetKeepInv(CommandSourceStack source, Player player) {
        dataManager.resetKeepInventoryState(player);
        Component message = Component.text("已重置玩家 " + player.getName() + "的保留物品栏选项")
                .appendNewline()
                .append(Component.text("现在这个玩家的保留物品栏选项继承自世界规则"));
        source.getSender().sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}

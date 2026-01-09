package org.koishio.keepInv;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;

import java.util.logging.Logger;

public class KeepInvCommand {

    private final KeepInvDataManager dataManager;
    private final LifecycleEventManager<Plugin> lifecycleEventManager;
    private String rootCommandNodeName;

    public KeepInvCommand(KeepInvDataManager dataManager, LifecycleEventManager<Plugin> lifecycleEventManager) {
        this.dataManager = dataManager;
        this.lifecycleEventManager = lifecycleEventManager;
    }

    public void registerKeepInvCommand(String rootCommandNodeName) {
        // 主命令节点
        this.rootCommandNodeName = rootCommandNodeName;
        LiteralCommandNode<CommandSourceStack> keepInvCommand = Commands.literal(rootCommandNodeName)
                .requires(source -> source.getSender().hasPermission("keepinv.use"))
                .then(
                        Commands.literal("get")
                                .requires(source -> source.getSender().hasPermission("keepinv.get"))
                                .executes(ctx -> executeGetKeepInv(ctx.getSource()))
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.get.others"))
                                                .executes(ctx ->{
                                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                    return executeGetKeepInv(ctx.getSource(), target);
                                                })
                                )
                )
                .then(
                        Commands.literal("set")
                                .requires(source -> source.getSender().hasPermission("keepinv.set"))
                                .then(
                                        Commands.argument("status", BoolArgumentType.bool())
                                                .executes(ctx -> executeSetKeepInv(ctx.getSource(), ctx.getArgument("status", boolean.class))
                                                )
                                )
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.set.others"))
                                                .then(
                                                        Commands.argument("status", BoolArgumentType.bool())
                                                                .executes(ctx -> {
                                                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                                    return executeSetKeepInv(ctx.getSource(),
                                                                            target,
                                                                            ctx.getArgument("status", boolean.class));
                                                                })
                                                )
                                )
                )
                .then(
                        Commands.literal("toggle")
                                .requires(source -> source.getSender().hasPermission("keepinv.toggle"))
                                .executes(ctx -> executeToggleKeepInv(ctx.getSource()))
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.toggle.others"))
                                                .executes(ctx -> {
                                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                    return executeToggleKeepInv(ctx.getSource(), target);
                                                })
                                )
                )
                .then(
                        Commands.literal("reset")
                                .requires(source -> source.getSender().hasPermission("keepinv.reset"))
                                .executes(ctx -> executeResetKeepInv(ctx.getSource()))
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.reset.others"))
                                                .executes(ctx -> {
                                                    final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                                    final Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                                    return executeResetKeepInv(ctx.getSource(), target);
                                                })
                                )
                )
                .then(
                        Commands.literal("help")
                                .executes(ctx -> {return executeShowHelp(ctx.getSource());})
                )
                .build();

        // 注册命令
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(keepInvCommand);
        });
    }

    public int executeGetKeepInv(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 0;
        }
        return executeGetKeepInv(source, (Player) source.getSender());
    }

    public int executeGetKeepInv(CommandSourceStack source, Player player) {
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

    public int executeSetKeepInv(CommandSourceStack source, boolean status) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 0;
        }
        return executeSetKeepInv(source, (Player) source.getSender(), status);
    }

    public int executeSetKeepInv(CommandSourceStack source, Player player, boolean status) {
        dataManager.setKeepInventoryState(player, status);
        source.getSender().sendMessage("已将" + player.getName() + "的保留物品栏选项设置为" + (status ? "开启" : "关闭"));
        return Command.SINGLE_SUCCESS;
    }

    public int executeToggleKeepInv(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 0;
        }
        return executeToggleKeepInv(source, (Player) source.getSender());
    }

    public int executeToggleKeepInv(CommandSourceStack source, Player player) {
        boolean status = !dataManager.getKeepInventoryState(player);
        dataManager.setKeepInventoryState(player, status);
        source.getSender().sendMessage("已将" + player.getName() + "的保留物品栏选项切换到" + (status ? "开启" : "关闭"));
        return Command.SINGLE_SUCCESS;
    }

    public int executeResetKeepInv(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 0;
        }
        return executeResetKeepInv(source, (Player) source.getSender());
    }

    public int executeResetKeepInv(CommandSourceStack source, Player player) {
        dataManager.resetKeepInventoryState(player);
        Component message = Component.text("已重置玩家 " + player.getName() + "的保留物品栏选项")
                .appendNewline()
                .append(Component.text("现在这个玩家的保留物品栏选项继承自世界规则"));
        source.getSender().sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }

    public int executeShowHelp(CommandSourceStack source) {
        Component message = Component.newline()
                .append(Component.text("====== [死亡不掉落相关帮助] ======"))
                .appendNewline()
                .append(Component.text("    * 鼠标悬浮查看更多信息")
                        .color(NamedTextColor.DARK_GRAY)
                        .style(style -> style.decorate(TextDecoration.ITALIC))
                )
                .appendNewline()
                .appendNewline();

        // 基本权限检查
        boolean hasGet = source.getSender().hasPermission("keepinv.get");
        boolean hasGetOthers = source.getSender().hasPermission("keepinv.get.others");
        boolean hasSet = source.getSender().hasPermission("keepinv.set");
        boolean hasSetOthers = source.getSender().hasPermission("keepinv.set.others");
        boolean hasToggle = source.getSender().hasPermission("keepinv.toggle");
        boolean hasToggleOthers = source.getSender().hasPermission("keepinv.toggle.others");
        boolean hasReset = source.getSender().hasPermission("keepinv.reset");
        boolean hasResetOthers = source.getSender().hasPermission("keepinv.reset.others");

        // 颜色常量
        final int PURPLE = 0x848ef4;
        final int BLUE = 0x56a8f5;

        // /keepInv get 命令帮助
        if (hasGet) {
            if (hasGetOthers) {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" get [玩家]").color(TextColor.color(BLUE))),
                        Component.text("查看指定玩家的死亡不掉落设置")
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName + " get "
                        ));
            } else {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" get").color(TextColor.color(BLUE))),
                        Component.text("查看自己的死亡不掉落设置")
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName + " get"
                ));
            }

            message = message.appendNewline();
        }

        // /keepInv set 命令帮助
        if (hasSet) {
            if (hasSetOthers) {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" set [玩家] <true|false>").color(TextColor.color(BLUE))),
                        Component.text("设置指定玩家的死亡不掉落状态")
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName + " set "
                        ));
            } else {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" set <true|false>").color(TextColor.color(BLUE))),
                        Component.text("设置自己的死亡不掉落状态(true为启用死亡不掉落)")
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName + " set"
                ));
            }

            message = message.appendNewline();
        }

        // /keepInv toggle 命令帮助
        if (hasToggle) {
            if (hasToggleOthers) {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" toggle [玩家]").color(TextColor.color(BLUE))),
                        Component.text("切换指定玩家的死亡不掉落设置")
                                .appendNewline()
                                .append(Component.text("如果不存在，将会设置为与当前世界规则相反的值")
                                        .color(NamedTextColor.DARK_GRAY)
                                        .style(style -> style.decorate(TextDecoration.ITALIC))
                                )
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName + " toggle "
                        ));
            } else {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" toggle").color(TextColor.color(BLUE))),
                        Component.text("切换自己的死亡不掉落设置")
                                .appendNewline()
                                .append(Component.text("如果不存在，将会设置为与当前世界规则相反的值")
                                        .color(NamedTextColor.DARK_GRAY)
                                        .style(style -> style.decorate(TextDecoration.ITALIC))
                                )
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName + " toggle"
                ));
            }
            message = message.appendNewline();
        }

        // /keepInv reset 命令帮助
        if (hasReset) {
            if (hasResetOthers) {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" reset [玩家]").color(TextColor.color(BLUE))),
                        Component.text("重置指定玩家的死亡不掉落设置(使其继承世界规则)")
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName + " reset "
                        ));
            } else {
                message = message.append(createClickableCommand(
                        Component.text("> /")
                                .append(Component.text(rootCommandNodeName))
                                .append(Component.text(" reset").color(TextColor.color(BLUE))),
                        Component.text("重置自己的死亡不掉落设置(使其继承世界规则)")
                                .appendNewline()
                                .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                        "/" + rootCommandNodeName +" reset"
                ));
            }
            message = message.appendNewline();
        }

        // 通用帮助命令
        message = message.append(createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" help").color(TextColor.color(BLUE))),
                Component.text("显示此帮助信息")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " help"
        ));

        source.getSender().sendMessage(message.appendNewline());
        return Command.SINGLE_SUCCESS;
    }

    public Component createClickableCommand(Component commandText, Component hoverText, String suggestCommand) {
        return Component.text()
                .append(commandText
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.suggestCommand(suggestCommand)))
                .build();
    }
}

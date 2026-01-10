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

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

public class KeepInvCommand {

    private final Logger LOGGER;
    private final KeepInvDataManager dataManager;
    private final LifecycleEventManager<Plugin> lifecycleEventManager;
    private String rootCommandNodeName;
    private final Map<HelpKey, Component> helpComponents = new EnumMap<>(HelpKey.class);

    private enum HelpKey {
        HEADER, HAS_GET, HAS_GET_OTHERS, HAS_SET, HAS_SET_OTHERS,
        HAS_TOGGLE, HAS_TOGGLE_OTHERS, HAS_RESET, HAS_RESET_OTHERS, HELP
    }

    public KeepInvCommand(Logger logger, KeepInvDataManager dataManager, LifecycleEventManager<Plugin> lifecycleEventManager) {
        LOGGER = logger;
        this.dataManager = dataManager;
        this.lifecycleEventManager = lifecycleEventManager;
    }

    public void registerKeepInvCommand(String rootCommandNodeName) {
        // 主命令节点
        this.rootCommandNodeName = rootCommandNodeName;

        // 先创建帮助消息组件，避免执行时创建大量组件
        GenerateHelpComponents();

        // 创建命令节点
        LiteralCommandNode<CommandSourceStack> keepInvCommand = Commands.literal(rootCommandNodeName)
                .requires(source -> source.getSender().hasPermission("keepinv.use"))
                .then(
                        Commands.literal("get")
                                .requires(source -> source.getSender().hasPermission("keepinv.get"))
                                .executes(ctx -> executeGetKeepInv(ctx.getSource()))
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .requires(source -> source.getSender().hasPermission("keepinv.get.others"))
                                                .executes(ctx -> {
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
                                .executes(ctx -> executeShowHelp(ctx.getSource()))
                )
                .build();

        // 注册命令
        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(keepInvCommand));
    }

    private void GenerateHelpComponents() {
        // 颜色常量
        final int PURPLE = 0x848ef4;
        final int BLUE = 0x56a8f5;

        // 帮助命令头
        Component header = Component.newline()
                .append(Component.text("====== [死亡不掉落相关帮助] ======"))
                .appendNewline()
                .append(Component.text("    * 鼠标悬浮查看更多信息")
                        .color(NamedTextColor.DARK_GRAY)
                        .style(style -> style.decorate(TextDecoration.ITALIC))
                )
                .appendNewline()
                .appendNewline();

        // keepInv get 命令帮助
        Component hasGet = createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" get").color(TextColor.color(BLUE))),
                Component.text("查看自己的死亡不掉落设置")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " get"
        );

        Component hasGetOthers = createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" get [玩家]").color(TextColor.color(BLUE))),
                Component.text("查看指定玩家的死亡不掉落设置")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " get "
        );

        // keepInv set 命令帮助
        Component hasSet = createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" set <true|false>").color(TextColor.color(BLUE))),
                Component.text("设置自己的死亡不掉落状态(true为启用死亡不掉落)")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " set"
        );

        Component hasSetOthers = createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" set [玩家] <true|false>").color(TextColor.color(BLUE))),
                Component.text("设置指定玩家的死亡不掉落状态")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " set "
        );

        // /keepInv toggle 命令帮助
        Component hasToggleOthers = createClickableCommand(
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
        );

        Component hasToggle = createClickableCommand(
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
        );

        // keepInv reset 命令帮助
        Component hasReset = createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" reset").color(TextColor.color(BLUE))),
                Component.text("重置自己的死亡不掉落设置(使其继承世界规则)")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " reset"
        );

        Component hasResetOthers = createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" reset [玩家]").color(TextColor.color(BLUE))),
                Component.text("重置指定玩家的死亡不掉落设置(使其继承世界规则)")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " reset "
        );

        // 帮助命令
        Component helpMessage = createClickableCommand(
                Component.text("> /")
                        .append(Component.text(rootCommandNodeName))
                        .append(Component.text(" help").color(TextColor.color(BLUE))),
                Component.text("显示此帮助信息")
                        .appendNewline()
                        .append(Component.text(">>点击输入<<").color(TextColor.color(PURPLE))),
                "/" + rootCommandNodeName + " help"
        );

        // 加入到 Map
        helpComponents.put(HelpKey.HEADER, header);
        helpComponents.put(HelpKey.HAS_GET, hasGet);
        helpComponents.put(HelpKey.HAS_GET_OTHERS, hasGetOthers);
        helpComponents.put(HelpKey.HAS_SET, hasSet);
        helpComponents.put(HelpKey.HAS_SET_OTHERS, hasSetOthers);
        helpComponents.put(HelpKey.HAS_TOGGLE, hasToggle);
        helpComponents.put(HelpKey.HAS_TOGGLE_OTHERS, hasToggleOthers);
        helpComponents.put(HelpKey.HAS_RESET, hasReset);
        helpComponents.put(HelpKey.HAS_RESET_OTHERS, hasResetOthers);
        helpComponents.put(HelpKey.HELP, helpMessage);
    }

    private Component createClickableCommand(Component commandText, Component hoverText, String suggestCommand) {
        return Component.text()
                .append(commandText
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.suggestCommand(suggestCommand)))
                .build();
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
            } else {
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
            return 0;
        }
        return executeSetKeepInv(source, (Player) source.getSender(), status);
    }

    private int executeSetKeepInv(CommandSourceStack source, Player player, boolean status) {
        dataManager.setKeepInventoryState(player, status);
        source.getSender().sendMessage("已将" + player.getName() + "的保留物品栏选项设置为" + (status ? "开启" : "关闭"));
        return Command.SINGLE_SUCCESS;
    }

    private int executeToggleKeepInv(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 0;
        }
        return executeToggleKeepInv(source, (Player) source.getSender());
    }

    private int executeToggleKeepInv(CommandSourceStack source, Player player) {
        boolean status = !dataManager.getKeepInventoryState(player);
        dataManager.setKeepInventoryState(player, status);
        source.getSender().sendMessage("已将" + player.getName() + "的保留物品栏选项切换到" + (status ? "开启" : "关闭"));
        return Command.SINGLE_SUCCESS;
    }

    private int executeResetKeepInv(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player)) {
            source.getSender().sendMessage("错误: 只能管理玩家的保留物品栏选项!");
            return 0;
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

    private int executeShowHelp(CommandSourceStack source) {
        // 消息头
        Component message = getHelpComponent(HelpKey.HEADER);

        // keepInv get 命令帮助
        if (source.getSender().hasPermission("keepinv.get")) {
            boolean hasOthers = source.getSender().hasPermission("keepinv.get.others");
            HelpKey key = hasOthers ? HelpKey.HAS_GET_OTHERS : HelpKey.HAS_GET;
            message = message.append(getHelpComponent(key)).appendNewline();
        }

        // keepInv set 命令帮助
        if (source.getSender().hasPermission("keepinv.set")) {
            boolean hasOthers = source.getSender().hasPermission("keepinv.set.others");
            HelpKey key = hasOthers ? HelpKey.HAS_SET_OTHERS : HelpKey.HAS_SET;
            message = message.append(getHelpComponent(key)).appendNewline();
        }

        // keepInv toggle 命令帮助
        if (source.getSender().hasPermission("keepinv.toggle")) {
            boolean hasOthers = source.getSender().hasPermission("keepinv.toggle.others");
            HelpKey key = hasOthers ? HelpKey.HAS_TOGGLE_OTHERS : HelpKey.HAS_TOGGLE;
            message = message.append(getHelpComponent(key)).appendNewline();
        }

        // keepInv reset 命令帮助
        if (source.getSender().hasPermission("keepinv.reset")) {
            boolean hasOthers = source.getSender().hasPermission("keepinv.reset.others");
            HelpKey key = hasOthers ? HelpKey.HAS_RESET_OTHERS : HelpKey.HAS_RESET;
            message = message.append(getHelpComponent(key)).appendNewline();
        }

        // 通用帮助命令
        message = message.append(getHelpComponent(HelpKey.HELP)).appendNewline();

        source.getSender().sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }

    private Component getHelpComponent(HelpKey key) {
        Component component = helpComponents.get(key);

        // 如果组件缺失，返回错误消息组件
        if (component == null) {
            LOGGER.warning("帮助组件 '" + key.toString() + "' 未找到，使用默认文本");
            return Component.text("[?]")
                    .color(NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(
                            Component.text("组件加载异常，请重试")
                    ));
        }
        return component;
    }
}

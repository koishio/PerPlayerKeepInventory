# PerPlayerKeepInventory

[![Paper 1.21.8+](https://img.shields.io/badge/Paper-1.21.8+-red?logo=paper)](https://papermc.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![GitHub](https://img.shields.io/github/license/koishio/PerPlayerKeepInventory)

> 一个现代化的 Paper 插件，允许**每位玩家独立控制**自己的死亡物品与经验掉落，无需修改全局游戏规则。

告别全服统一的 `keepInventory` 游戏规则！PerPlayerKeepInventory 为服务器管理员和玩家提供了精细化的控制能力。玩家可以自由选择开启或关闭自己的死亡掉落，其设置会被安全保存，并在服务器重启后依然有效。

## ✨ 特性

*   **👤 玩家独立设置**：每位玩家的死亡掉落状态独立存储与计算，互不干扰。
*   **⚙️ 行为可预测**：当玩家未自定义设置时，自动回退至其所在世界的 `keepInventory` 游戏规则。
*   **🛡️ 精细权限控制**：提供完整的权限节点，可精确控制谁可以查看、修改自己或他人的状态。
*   **💾 数据持久化**：使用玩家的 PersistentDataContainer 存储设置，安全可靠，随玩家数据保存与加载。

## 📥 下载与安装

1.  从 [Releases 页面](https://github.com/koishio/PerPlayerKeepInventory/releases) 下载最新版本的 `.jar` 文件。
2.  将下载的 `.jar` 文件放入您 Paper 服务器的 `plugins/` 目录下。
3.  重启或重载您的服务器。
4.  插件将自动生成默认配置文件 (`config.yml`)，您可以根据需要调整。

**前提条件**：本插件需要运行 **Paper 1.21.8** 或更高版本的服务器。

## 📖 命令与权限

插件的主命令默认为 `/keepinv`（可在配置文件中修改，重启服务器生效）。

### 命令列表

| 命令 | 描述 | 所需权限（默认） |
| :--- | :--- | :--- |
| `/keepinv get [玩家]` | 查看自己（或指定玩家）的死亡掉落状态。 | `keepinv.get` (自己)<br>`keepinv.get.others` (他人) |
| `/keepinv set <true\|false> [玩家]` | 为自己（或指定玩家）设置死亡掉落状态。 | `keepinv.set` (自己)<br>`keepinv.set.others` (他人) |
| `/keepinv toggle [玩家]` | 切换自己（或指定玩家）的死亡掉落状态。 | `keepinv.toggle` (自己)<br>`keepinv.toggle.others` (他人) |
| `/keepinv reset [玩家]` | 重置自己（或指定玩家）的设置，使其继承世界默认规则。 | `keepinv.reset` (自己)<br>`keepinv.reset.others` (他人) |
| `/keepinv help` | 显示此帮助信息。 | 无 |

### 权限节点

所有权限均可在 `plugin.yml` 中查看，默认 (`default: op`) 仅服务器管理员可用。

| 权限节点 | 描述 |
| :--- | :--- |
| `keepinv.use` | 使用 `/keepinv` 基础命令的权限。 |
| `keepinv.get` | 查看自己状态的权限。 |
| `keepinv.get.others` | 查看其他玩家状态的权限。 |
| `keepinv.set` | 设置自己状态的权限。 |
| `keepinv.set.others` | 设置其他玩家状态的权限。 |
| `keepinv.toggle` | 切换自己状态的权限。 |
| `keepinv.toggle.others` | 切换其他玩家状态的权限。 |
| `keepinv.reset` | 重置自己设置的权限。 |
| `keepinv.reset.others` | 重置其他玩家设置的权限。 |

## ⚙️ 配置

首次运行插件后，会在 `plugins/PerPlayerKeepInventory/` 目录下生成 `config.yml` 文件。

```yaml
# PerPlayerKeepInventory 配置文件
command-root: "keepinv" # 主命令的名称，重启服务器后生效
```


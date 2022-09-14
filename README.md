# Android Shortcut Helper

Android 快捷方式助手。

## 功能

获取系统中注册的所有应用的 shortcuts (快捷方式) ，以及它们的 intent —— 包括桌面快捷方式、长按桌面应用图标的快捷方式等。

> 具体可参见[应用快捷方式概览](https://developer.android.com/guide/topics/ui/shortcuts?hl=zh-cn)

你可以直接获得快捷方式的 intent URI （包括所有的 extras）。

> 快捷方式的 extras 被限制为基本类型（`PersistenceBundle` 支持的类型）

![](img/1.png)

你可以通过发起 `Intent.CREATE_SHORTCUT` 的 Intent 来获得这些快捷方式，也可以直接在该 App 中启动 intent 。

## 原理

1. ShortcutInfo 的获取

由于系统 API 的限制，通过 `LauncherApps` 或者 `ShortcutService` 获取的 shortcuts 都会被剔除部分信息，导致无法获得 intent 。   
因此目前通过解析目录 `/data/system_ce/0/shortcut_services` 下的 xml 获取 ShortcutInfo 。

2. 图标的获取

参考了 `LauncherApps` 的实现。

## 注意

1. 目前需要 root 权限（存在 shell 权限的方案）。  
2. 需要特殊权限的 Intent 暂时无法在 App 内启动。  
3. 目前仅支持解析主用户的 shortcut 。  
4. 有概率出现获取不完全的情况，此时可以尝试刷新。  
5. 目前 `CREATE_SHORTCUT` 返回的快捷方式不含图标。  


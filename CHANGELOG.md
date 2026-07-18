# ContainerMark 更新日志

所有重要更改都会记录在这个文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，并遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/) 语义化版本规范。

## [1.1.0] - 2026-07-18

### 新增
- **客户端可选服务端模式**：模组现在可以作为纯客户端 mod 运行。当服务端未安装 ContainerMark 时，客户端自动降级为本地模式，使用客户端渲染的浮空文字、粒子和公共聊天消息通知队友。
- **服务端检测**：客户端加入服务器时自动检测服务端是否安装 ContainerMark，并切换到对应模式。
- **客户端本地渲染**：降级模式下在标记位置生成仅自己可见的浮空文字和粒子效果。
- **Git 版本控制**：项目已连接 GitHub 远程仓库 `https://github.com/294970144/containermark`。

### 修复
- **1.21.11 Yarn API 兼容性**：全面适配 Minecraft 1.21.11 / Yarn 1.21.11+build.6 API 变更。
  - `NbtCompound.putUuid/getUuid` 替换为 `Uuids.toIntArray/toUuid` + `putIntArray/getIntArray`
  - `NbtCompound.getString/getCompound/getList` 适配新的 Optional 返回值 API
  - `PersistentState.Type` 迁移为 `PersistentStateType<T>` + Codec
  - `Entity.getPos/getWorld/getServerWorld/getServer` 替换为新的实体位置/世界获取方法
  - `ClickEvent`/`HoverEvent` 从类改为接口，使用新的构造方式
  - `TextDisplayEntity` setter 方法变为私有，使用反射调用
  - `KeyBinding` 分类使用 `KeyBinding.Category.create(Identifier)`
  - `KeyBinding.matchesKey` 改为接受 `KeyInput` 对象
  - `HitResult` 包路径从 `net.minecraft.hit` 迁移到 `net.minecraft.util.hit`
- **按键绑定翻译键**：修正分类翻译键为 `key.category.containermark.keys`，使其正确显示在游戏按键设置中。
- **HandledScreenMixin 签名**：修复 `keyPressed` Mixin 方法签名，适配 1.21.11 的 `KeyInput` 参数变更，解决客户端启动崩溃问题。
- **容器标记目标名字**：标记容器方块时现在正确显示容器名字（如“箱子”、“熔炉”），而不是容器内物品名字。

### 变更
- 项目 JDK 路径：`C:\Users\zhang\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot`
- 构建配置：Gradle 9.5.1 + Loom 1.17-SNAPSHOT + Fabric Loader 0.19.3 + Fabric API 0.141.5+1.21.11

## [1.0.0] - 2026-07-18

### 新增
- 初始版本发布。
- **世界标记**：玩家潜行 + 快捷键标记准星对准的容器方块或地面掉落物品。
- **物品槽标记**：在容器 UI 内按下快捷键标记鼠标悬停的物品槽位。
- **三重通知**：标记后同时触发浮空文字显示、粒子效果和聊天栏消息通知。
- **可配置通知范围**：支持 `ALL`（所有玩家）、`RADIUS`（半径内玩家）、`TEAM`（队伍成员）三种范围。
- **队伍系统**：提供 `/markteam` 命令创建/邀请/接受/离开/解散/查看队伍，队伍数据持久化保存。
- **配置系统**：通过 `config/containermark.json` 调整通知范围、半径、显示时长、粒子、冷却时间等参数。

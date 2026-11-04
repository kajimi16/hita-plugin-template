# HITA 插件模板

用 [OH-MY-HITA](https://github.com/kajimi16/OH-MY-HITA) 的插件 API 构建**逻辑插件**的最小模板。

产物是一个 `classes.dex`，用户在 OH-MY-HITA 的「插件」页安装后即可生效，无需重新打包宿主。

## 快速开始

```bash
# 1. 改 plugin/src/main/java/com/example/plugin/DemoPlugin.kt 里的 id / displayName
# 2. 生成插件 dex 与市场索引
./gradlew :plugin:publishToMarket
```

产物：

```
market/index.json   # 索引（含 sha256）
market/demo.dex     # 插件本体，约 3 KB
plugin/build/plugin-dex/classes.dex
```

把 `market/` 目录放到任意 HTTPS 地址（仓库 raw、GitHub Pages、自己的 CDN），
然后在 OH-MY-HITA 侧配置索引地址即可被用户安装。

## 本地调试

模拟器访问宿主机用 `10.0.2.2`：

```bash
cd market && python3 -m http.server 8765
# 宿主构建时指定 -PpluginIndexUrl=http://10.0.2.2:8765/index.json
```

## 目录结构

```
plugin/                 插件模块（Android library）
  src/main/java/...     DemoPlugin：实现 Plugin，注册一个服务
libs/                   宿主契约的 AAR（api / model / component），compileOnly
market/                 发布产物：index.json + dex
```

`libs/*.aar` 来自 OH-MY-HITA 的构建产物：

```bash
# 在 OH-MY-HITA 仓库里
./gradlew :api:assembleRelease :model:assembleRelease :component:assembleRelease
cp api/build/outputs/aar/api-release.aar \
   model/build/outputs/aar/model-release.aar \
   component/build/outputs/aar/component-release.aar  <本模板>/libs/
```

升级契约后记得同步 `requiresApi`。

## 限制（Android 的硬边界，不是模板的取舍）

- **没有界面、没有资源**：运行时插件不能声明四大组件，也拿不到资源表
- **没有 Context**：只能通过宿主提供的契约（服务、同步表、主题）做事
- **同进程**：插件崩溃会影响宿主，只上架可信代码
- 默认显示「未验证」，安装前宿主会提示用户

需要界面的功能，请做成**独立 APK 插件**（T2）：自带清单、资源与进程边界，
宿主通过 `com.stupidtree.hita.action.PLUGIN_APK` 发现并启动它。
协议见 OH-MY-HITA 的 `docs/api/plugin-api.md`。

## 能做什么

- 注册服务：`ctx.provide(ServiceKey<T>, impl)`
- 贡献云同步表：`ctx.register`，见 `SyncCodec`
- 调用宿主能力：如 `ctx.service(ThemeController.KEY)?.setNightMode(true)`

## 版本

- 模板对应 `HITA_API_VERSION = 2`
- 契约有破坏性改动时版本号会递增，宿主会拒绝 `requiresApi` 过高的插件

## License

MIT

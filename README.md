# HITA 插件模板

用 [OH-MY-HITA](https://github.com/kajimi16/OH-MY-HITA) 的插件契约构建**市场插件**的起点。

- 内置插件：编译进宿主 APK，需要改宿主源码，本模板不覆盖
- 市场插件：运行时安装、独立发版，本模板覆盖

市场插件有两种形态，模板各带一个示例：

| 形态 | 模块 | 产物 | 宿主渲染方式 |
|---|---|---|---|
| 逻辑插件 | `plugin/` | `demo.dex` | `DexClassLoader`，无界面 |
| 界面插件 | `ui-plugin/` | `ui-demo.apk` | Compose 容器，无 Activity/资源表 |

## 环境

| 项 | 版本 |
|---|---|
| 宿主契约 | OH-MY-HITA API 5 |
| Kotlin | 1.9.22 |
| AGP | 8.5.2 |
| Compose | 1.6.8 / compiler 1.5.8 |

- 逻辑插件通常 `requiresApi = 2` 即可；使用 `TextNavEntry` / `ViewSearchTab` 等 API 5 新契约时填 5
- 界面插件建议 `apiVersion = 5`，可使用多主界面 tab、自定义标题与图标

## 快速开始

```bash
./gradlew publishMarket
```

产物：

```
market/index.json   # plugins（dex）+ uiPlugins（APK）
market/demo.dex     # 逻辑插件
market/ui-demo.apk  # 界面插件
```

## 发布到市场

模板本身不会自动进入市场。正式流程见
[hita-community-plugins](https://github.com/kajimi16/hita-community-plugins)：

1. 用本模板创建**你自己的 GitHub 仓库**
2. 在社区仓库提交「插件发布请求」Issue，或由维护者运行 **Publish Plugin**
3. CI 构建、校验 `plugin.json`、计算 sha256，写入 `dist/` 与 `registry/index.json`
4. 宿主市场刷新后即可安装

不需要给宿主仓库提 PR；更新插件只需提升版本后重复发布。

## 逻辑插件

最小实现：

```kotlin
class DemoPlugin : Plugin {
    override val id = "com.example.demo"
    override val requiresApi = 2

    override fun install(ctx: PluginContext) {
        ctx.provide(GREETING, "hello from runtime plugin")
    }
}
```

可贡献的能力：

- `ctx.provide(ServiceKey, impl)`：服务
- `ctx.sync.register(...)`：云同步表
- `ctx.nav.register(TextNavEntry(...))`：抽屉导航项（API 5，字符串标题）
- `ctx.searchTabs.registerView(ViewSearchTab(...))`：搜索页（API 5，宿主承载 View）
- `ctx.tabs` 仅用于构建期插件；运行时插件用 `plugin.json` 的 `mainTabs`

## 界面插件

```kotlin
class TemplateUiPlugin : ComposeUiPlugin {
    override val id = "com.example.ui.template"
    override val displayName = "模板界面插件"
    override val requiresApi = 5

    override fun createView(context: Context, host: PluginUiHost): View =
        ComposeView(context).apply {
            setContent { MaterialTheme { /* Compose UI */ } }
        }
}
```

`assets/plugin.json`：

```json
{
  "id": "com.example.ui.template",
  "name": "模板界面插件",
  "version": "0.1.0",
  "entryClass": "com.example.ui.TemplateUiPlugin",
  "apiVersion": 5,
  "composeVersion": "1.6.8",
  "capabilities": ["hita.ui.mainTab"],
  "mainTabs": [
    {
      "id": "template",
      "title": "模板页",
      "order": 100,
      "capability": "hita.ui.mainTab",
      "iconAsset": "tab_icon.png"
    }
  ]
}
```

字段说明：

| 字段 | 说明 |
|---|---|
| `id` | 全局唯一，建议反向域名 |
| `entryClass` | 实现 `ComposeUiPlugin` 的类 |
| `apiVersion` | 依赖的宿主 API，过高会被拒绝 |
| `composeVersion` | 必须与宿主运行时一致 |
| `capabilities` | 能力声明，如 `hita.ui.mainTab` |
| `mainTabs` | 可选，支持多个主界面 tab；`iconAsset` 为 APK 内 `assets/` 图片 |

约束：

- 不能新增 Activity/Service/Receiver/Provider，界面由宿主容器承载
- 不能携带自己的 XML 资源表；UI 用 Compose 代码构建
- 与宿主同进程，以宿主权限运行；只安装可信来源

## 契约 AAR

`libs/*.aar` 来自宿主构建：

```bash
# 在 OH-MY-HITA 仓库
./gradlew :api:assembleRelease :model:assembleRelease :component:assembleRelease
cp api/build/outputs/aar/api-release.aar \
   model/build/outputs/aar/model-release.aar \
   component/build/outputs/aar/component-release.aar  <本模板>/libs/
```

社区仓库的发布工作流会在构建外部插件前自动注入自己 `libs/` 的最新 AAR，
所以 fork 后 AAR 略旧仍可构建；本地开发仍建议手动同步。

## 本地调试

```bash
./gradlew publishMarket
cd market && python3 -m http.server 8765

# 宿主 debug 包指向本地索引
./gradlew :app:assembleDebug -PpluginIndexUrl=http://10.0.2.2:8765/index.json
```

模拟器访问宿主机使用 `10.0.2.2`。

## 目录结构

```
plugin/             逻辑插件（Android library）
ui-plugin/          界面插件（Android application，Compose）
  src/main/assets/plugin.json
libs/               api / model / component 契约 AAR（compileOnly）
market/             publishMarket 产物
```

## 限制

- 平台硬边界：不能注册组件、不能带资源表
- 插件与宿主同进程，没有沙箱；索引与包完整性依赖 sha256，作者需可信
- 更新插件必须提升 `version`，否则市场不会提示更新

# HITA 插件模板

用 [OH-MY-HITA](https://github.com/kajimi16/OH-MY-HITA) 的插件契约构建**市场插件**的最小模板。

HITA 的插件分两类，这个仓库只覆盖第二类：

| 类型 | 说明 | 能否用本模板 |
|---|---|---|
| 内置插件 | 编译进宿主 APK，随版本发布，可停用不可卸载 | 不能（需要修改宿主源码） |
| 市场插件 | 运行时安装，用户可卸载 | 可以 |

市场插件又分两种形态，模板里各有一个示例：

| 形态 | 模块 | 产物 | 界面 |
|---|---|---|---|
| 逻辑插件 | `plugin/` | `demo.dex` | 无界面，贡献服务/同步表等 |
| 界面插件 | `ui-plugin/` | `ui-demo.apk` | 由宿主 Compose 容器渲染 |

## 快速开始

```bash
./gradlew publishMarket
```

产物：

```
market/index.json     # 索引：plugins（dex）+ uiPlugins（APK）
market/demo.dex       # 逻辑插件
market/ui-demo.apk    # 界面插件
```

把 `market/` 放到任意 HTTPS 地址，用户在 OH-MY-HITA 的「插件市场 → 市场插件」里即可安装。

## 两种插件怎么写

### 逻辑插件

`plugin/src/main/java/com/example/plugin/DemoPlugin.kt`：

```kotlin
class DemoPlugin : Plugin {
    override val id = "com.example.demo"
    override val requiresApi = 2          // 逻辑插件保持兼容已发布宿主；API 3 是当前开发分支
    override fun install(ctx: PluginContext) {
        ctx.provide(GREETING, "hello from runtime plugin")
    }
}
```

### 界面插件

`ui-plugin/src/main/java/com/example/ui/TemplateUiPlugin.kt`：

```kotlin
class TemplateUiPlugin : ComposeUiPlugin {
    override val id = "com.example.ui.template"
    override val requiresApi = 3          // 界面插件需要宿主 API 3（含 ComposeUiPlugin 契约）
    override fun createView(context: Context, host: PluginUiHost): View =
        ComposeView(context).apply { setContent { /* Compose UI */ } }
}
```

- 界面插件是**独立 APK 文件**，不会被系统安装；宿主用 `DexClassLoader` 加载
- 没有 Activity / Service / Receiver，也没有自己的 XML 资源表
- 界面用 Compose 构建；Compose 运行时由宿主提供
- 元数据 `assets/plugin.json` 必须包含 `id`、`version`、`entryClass`、`apiVersion`、`composeVersion`
- 通过 `host.service(...)` 使用宿主契约，通过 `host.arguments` 读取入口参数

## 契约 AAR

`libs/*.aar` 来自宿主仓库的构建产物：

```bash
# 在 OH-MY-HITA 仓库
./gradlew :api:assembleRelease :model:assembleRelease :component:assembleRelease
cp api/build/outputs/aar/api-release.aar \
   model/build/outputs/aar/model-release.aar \
   component/build/outputs/aar/component-release.aar  <本模板>/libs/
```

宿主升级契约后需要同步 AAR，并调整 `requiresApi` / `composeVersion`。

## 本地调试

```bash
# 1. 构建市场产物
./gradlew publishMarket

# 2. 起一个本地索引
cd market && python3 -m http.server 8765

# 3. 宿主编译时指向它
#    ./gradlew :app:assembleDebug \
#        -PpluginIndexUrl=http://10.0.2.2:8765/index.json
```

模拟器访问宿主机用 `10.0.2.2`。

## 目录结构

```
plugin/                 逻辑插件（Android library）
  src/main/java/...     DemoPlugin：实现 Plugin
ui-plugin/              界面插件（Android application，Compose）
  src/main/assets/plugin.json
  src/main/java/...     TemplateUiPlugin：实现 ComposeUiPlugin
libs/                   宿主契约 AAR（api / model / component），compileOnly
market/                 发布产物：index.json + demo.dex + ui-demo.apk
```

## 限制（Android 的硬边界，不是模板的取舍）

- 不能新增 Activity / Service / Receiver / Provider，界面只能由宿主容器承载
- 不能携带自己的 XML 资源（界面插件的 UI 用 Compose 代码构建）
- 与宿主同进程、以宿主权限运行：只安装可信来源的插件

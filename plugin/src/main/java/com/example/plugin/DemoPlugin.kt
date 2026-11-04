package com.example.plugin

import com.stupidtree.hita.api.Plugin
import com.stupidtree.hita.api.PluginContext
import com.stupidtree.hita.api.ServiceKey

/**
 * 最小插件示例。
 *
 * 运行时插件只能贡献「逻辑」：服务、同步表、主题等。
 * 它没有 Context、没有资源、也不能声明四大组件——需要界面的功能请做成独立 APK 插件。
 */
class DemoPlugin : Plugin {

    override val id = "com.example.demo"

    override val displayName = "示例插件"

    override val requiresApi = 2

    override fun install(ctx: PluginContext) {
        ctx.provide(GREETING, "hello from runtime plugin")
    }

    companion object {
        val GREETING = ServiceKey<String>("demo.greeting")
    }
}

package com.example.ui

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.stupidtree.hita.api.ComposeUiPlugin
import com.stupidtree.hita.api.PluginUiHost

/**
 * 运行时界面插件的最小模板。
 *
 * 界面由宿主 `PluginUiActivity` 里的 ComposeView 承载；插件本身
 * 没有 Activity、没有 XML 资源，Compose 运行时由宿主提供。
 */
class TemplateUiPlugin : ComposeUiPlugin {

    override val id = "com.example.ui.template"

    override val displayName = "模板界面插件"

    override val requiresApi = 3

    override fun createView(context: Context, host: PluginUiHost): View =
        ComposeView(context).apply {
            setContent {
                MaterialTheme {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("来自模板的界面插件", style = MaterialTheme.typography.h5)
                        Text("入口参数：${host.arguments.getString("query") ?: "（无）"}")
                        Button(onClick = { host.close() }) {
                            Text("关闭")
                        }
                    }
                }
            }
        }
}

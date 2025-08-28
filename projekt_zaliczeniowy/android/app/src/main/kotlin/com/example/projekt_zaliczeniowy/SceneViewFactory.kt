package com.example.projekt_zaliczeniowy

import android.content.Context
import android.view.View
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.plugin.common.StandardMessageCodec
import androidx.compose.ui.platform.ComposeView

class SceneViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        return SceneComposeView(context)
    }
}

class SceneComposeView(context: Context) : PlatformView {
    private val composeView = ComposeView(context).apply {
        setContent {
            SceneScreen()   // your Jetpack Compose Scene
        }
    }

    override fun getView(): View = composeView
    override fun dispose() {}
}

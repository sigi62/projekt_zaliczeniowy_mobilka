package com.example.projekt_zaliczeniowy

import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.ar/ar"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Register MethodChannel for launching full AR activity
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                if (call.method == "startAR") {
                    val intent = Intent(this, SceneScreen()::class.java)
                    startActivity(intent)
                    result.success(null)
                } else {
                    result.notImplemented()
                }
            }

        // Register the custom SceneView PlatformView
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("SceneViewFlutter", SceneViewFactory())
    }
}

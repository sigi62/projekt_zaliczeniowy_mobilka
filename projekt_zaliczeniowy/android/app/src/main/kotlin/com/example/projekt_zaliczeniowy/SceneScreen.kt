package com.example.projekt_zaliczeniowy

import android.os.Bundle
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.hitTest
import io.github.sceneview.ar.node.HitResultNode
import io.github.sceneview.createMaterialLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.CubeNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size

class SceneScreen : AppCompatActivity() {

    private lateinit var arSceneView: ARSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = FrameLayout(this)
        setContentView(container)

        // Initialize ARSceneView
        arSceneView = ARSceneView(this)
        container.addView(arSceneView)

        // Add a static cylinder in front of the camera
        val cylinder = CylinderNode(
            engine = arSceneView.engine,
            radius = 0.2f,
            height = 2.0f,
            materialInstance = arSceneView.engine.createMaterialLoader(this).createColorInstance(
                color = Color.Blue,
                metallic = 0.5f,
                roughness = 0.2f,
                reflectance = 0.4f
            )
        ).apply {
            transform(
                position = Position(0f, 1.0f, -1f),
                rotation = Rotation(x = 90f)
            )
        }
        arSceneView.addChildNode(cylinder)



        val backButton = ImageButton(this).apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    android.R.drawable.ic_menu_close_clear_cancel
                )
            )
            setBackgroundColor(0x55000000) // semi-transparent background
            setOnClickListener {
                // Return back to Flutter
                finish()  // simply finish this Activity
            }
        }

        // Layout params for top-left corner
        val layoutParams = FrameLayout.LayoutParams(
            150, 150 // width, height in pixels
        ).apply {
            leftMargin = 30
            topMargin = 50
        }

        container.addView(backButton, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }
}

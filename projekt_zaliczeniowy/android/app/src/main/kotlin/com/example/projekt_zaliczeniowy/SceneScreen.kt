package com.example.projekt_zaliczeniowy

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.PixelCopy
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.google.ar.core.HitResult
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.createEnvironmentLoader
import io.github.sceneview.createMaterialLoader
import io.github.sceneview.createModelLoader
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class SceneScreen : AppCompatActivity() {

    private lateinit var arSceneView: ARSceneView
    private var selectedNode: ModelNode? = null
    private var isDragging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = FrameLayout(this)
        setContentView(container)

        // Initialize ARSceneView
        arSceneView = ARSceneView(this)
        container.addView(arSceneView)

        // Add static cylinder
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

        // Inside SceneScreen.onCreate()

        val environmentLoader = arSceneView.engine.createEnvironmentLoader(this)

// Load HDR directly from APK assets
        val enviorment =  loadModelFromFlutterAsset("environment.hdr")
        arSceneView.environment = environmentLoader.createHDREnvironment(enviorment)!!


        arSceneView.setOnTouchListener { _, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val hitResult = arSceneView.frame
                        ?.hitTest(event)
                        ?.firstOrNull { it.trackable is com.google.ar.core.Plane }

                    hitResult?.let {
                        selectedNode = findNodeNearHit(it)
                        isDragging = selectedNode != null
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        val hitResult = arSceneView.frame
                            ?.hitTest(event)
                            ?.firstOrNull { it.trackable is com.google.ar.core.Plane }

                        hitResult?.let {
                            selectedNode?.transform(
                                position = Position(it.hitPose.tx(), it.hitPose.ty(), it.hitPose.tz())
                            )
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        val hitResult = arSceneView.frame
                            ?.hitTest(event)
                            ?.firstOrNull { it.trackable is com.google.ar.core.Plane }

                        hitResult?.let { placeModelAtHit(it) }
                    }

                    selectedNode = null
                    isDragging = false
                }
            }

            true
        }


        // Back button
        val backButton = ImageButton(this).apply {
            setImageDrawable(
                ContextCompat.getDrawable(context, android.R.drawable.ic_menu_close_clear_cancel)
            )
            setBackgroundColor(0x55000000)
            setOnClickListener { finish() }
        }
        val backParams = FrameLayout.LayoutParams(150, 150).apply {
            leftMargin = 30
            topMargin = 50
        }
        container.addView(backButton, backParams)

        val photoButton = ImageButton(this).apply {
            setBackgroundResource(R.drawable.photo_button)
            setOnClickListener { takeScenePhoto() }
        }

// Layout params for bottom-center
        val photoParams = FrameLayout.LayoutParams(150, 150).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 50
        }

        container.addView(photoButton, photoParams)

    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    private fun takeScenePhoto() {
        val bitmap = Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(arSceneView, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                saveBitmapToGallery(bitmap)
            } else {
                Toast.makeText(this, "Failed to capture", Toast.LENGTH_SHORT).show()
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "ARScene_${System.currentTimeMillis()}.png"
        val fos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/ARApp")
            }
            val uri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { contentResolver.openOutputStream(it) }
        } else {
            val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM)
            FileOutputStream(File(imagesDir, filename))
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(this, "Saved to gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun placeModelAtHit(hit: com.google.ar.core.HitResult) {
        val anchor = hit.createAnchor()
        val modelLoader = arSceneView.engine.createModelLoader(this)
        val modelbuffer = loadModelFromFlutterAsset("models/lion.glb")
        val modelInstance = modelLoader.createModelInstance(modelbuffer) ?: return

        val modelNode = ModelNode(modelInstance = modelInstance).apply {
            scale = Scale(0.01f,0.01f,0.01f)
            position = Position(anchor.pose.tx(), anchor.pose.ty(), anchor.pose.tz())
        }
        arSceneView.addChildNode(modelNode)
    }

    private fun findNodeNearHit(hit: HitResult): ModelNode? {
        val hitPos = Position(hit.hitPose.tx(), hit.hitPose.ty(), hit.hitPose.tz())

        fun distance(a: Position, b: Position): Float {
            val dx = a.x - b.x
            val dy = a.y - b.y
            val dz = a.z - b.z
            return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        }

        return arSceneView.childNodes
            .filterIsInstance<ModelNode>()
            .minByOrNull { node -> distance(node.position, hitPos) }
            ?.takeIf { distance(it.position, hitPos) < 0.2f }
    }

    private fun loadModelFromFlutterAsset(assetPath: String): ByteBuffer {
        assets.list("flutter_assets/assets/models")?.forEach { println(it) }

        val bytes = assets.open("flutter_assets/assets/$assetPath").use { it.readBytes() }
        return ByteBuffer.wrap(bytes)
    }


}

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
    private var modelName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get model name from intent (nullable)
        modelName = intent.getStringExtra("modelName")

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

        // Load HDR environment
        val environmentLoader = arSceneView.engine.createEnvironmentLoader(this)
        val environment = loadAssetAsByteBuffer("environment.hdr")
        arSceneView.environment = environmentLoader.createHDREnvironment(environment)!!

        // Touch listener
        arSceneView.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }

        // Back button
        val backButton = ImageButton(this).apply {
            setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_close_clear_cancel))
            setBackgroundColor(0x55000000)
            setOnClickListener { finish() }
        }
        container.addView(backButton, FrameLayout.LayoutParams(150, 150).apply {
            leftMargin = 30
            topMargin = 50
        })

        // Photo button
        val photoButton = ImageButton(this).apply {
            setBackgroundResource(R.drawable.photo_button)
            setOnClickListener { takeScenePhoto() }
        }
        container.addView(photoButton, FrameLayout.LayoutParams(150, 150).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 50
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val hit = arSceneView.frame?.hitTest(event)?.firstOrNull { it.trackable is com.google.ar.core.Plane }
                hit?.let {
                    selectedNode = findNodeNearHit(it)
                    isDragging = selectedNode != null
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val hit = arSceneView.frame?.hitTest(event)?.firstOrNull { it.trackable is com.google.ar.core.Plane }
                    hit?.let { selectedNode?.transform(position = Position(it.hitPose.tx(), it.hitPose.ty(), it.hitPose.tz())) }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    val hit = arSceneView.frame?.hitTest(event)?.firstOrNull { it.trackable is com.google.ar.core.Plane }
                    hit?.let { placeModelAtHit(it) }
                }
                selectedNode = null
                isDragging = false
            }
        }
    }

    private fun placeModelAtHit(hit: HitResult) {
        if (modelName == null) return // no model selected

        val anchor = hit.createAnchor()
        val modelLoader = arSceneView.engine.createModelLoader(this)
        val modelBuffer = loadAssetAsByteBuffer("models/$modelName")
        val modelInstance = modelLoader.createModelInstance(modelBuffer) ?: return

        val modelNode = ModelNode(modelInstance = modelInstance).apply {
            scale = Scale(0.01f,0.01f,0.01f)
            position = Position(anchor.pose.tx(), anchor.pose.ty(), anchor.pose.tz())
        }
        arSceneView.addChildNode(modelNode)
    }

    private fun findNodeNearHit(hit: HitResult): ModelNode? {
        val hitPos = Position(hit.hitPose.tx(), hit.hitPose.ty(), hit.hitPose.tz())
        return arSceneView.childNodes
            .filterIsInstance<ModelNode>()
            .minByOrNull { node ->
                val dx = node.position.x - hitPos.x
                val dy = node.position.y - hitPos.y
                val dz = node.position.z - hitPos.z
                kotlin.math.sqrt(dx*dx + dy*dy + dz*dz)
            }
            ?.takeIf { distance(it.position, hitPos) < 0.2f }
    }

    private fun distance(a: Position, b: Position) =
        kotlin.math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y) + (a.z-b.z)*(a.z-b.z))

    private fun takeScenePhoto() {
        val bitmap = Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(arSceneView, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) saveBitmapToGallery(bitmap)
            else Toast.makeText(this, "Failed to capture", Toast.LENGTH_SHORT).show()
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
            contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?.let { contentResolver.openOutputStream(it) }
        } else {
            FileOutputStream(File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM), filename))
        }
        fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun loadAssetAsByteBuffer(assetPath: String): ByteBuffer {
        val bytes = assets.open("flutter_assets/assets/$assetPath").use { it.readBytes() }
        return ByteBuffer.wrap(bytes)
    }
}

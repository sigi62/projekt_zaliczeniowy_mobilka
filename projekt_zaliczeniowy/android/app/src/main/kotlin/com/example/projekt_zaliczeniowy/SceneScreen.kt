package com.example.projekt_zaliczeniowy

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.PixelCopy
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.createMaterialLoader
import io.github.sceneview.createModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import java.io.File
import java.io.FileOutputStream

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

        arSceneView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val frame = arSceneView.frame
                val hit = frame?.hitTest(event)
                    ?.firstOrNull { it.trackable is com.google.ar.core.Plane }
                hit?.let { placeModelAtHit(it) }
            }
            true
        }


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

        val photoButton = ImageButton(this).apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    android.R.drawable.ic_menu_camera
                )
            )
            setBackgroundColor(0x55000000) // semi-transparent
            setOnClickListener {
                takeScenePhoto()
            }
        }

// Layout params for bottom-right corner
        val photoLayoutParams = FrameLayout.LayoutParams(150, 150).apply {
            rightMargin = 30
            bottomMargin = 50
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
        }

        container.addView(photoButton, photoLayoutParams)


        container.addView(backButton, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    private
    fun takeScenePhoto() {
        val bitmap = Bitmap.createBitmap(
            arSceneView.width,
            arSceneView.height,
            Bitmap.Config.ARGB_8888
        )

        PixelCopy.request(arSceneView, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                // Save or use bitmap
                saveBitmapToGallery(bitmap)
            } else {
                android.widget.Toast.makeText(this, "Failed to capture", android.widget.Toast.LENGTH_SHORT).show()
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
            val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM).toString()
            java.io.FileOutputStream(java.io.File(imagesDir, filename))
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            android.widget.Toast.makeText(this, "Saved to gallery", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun placeModelAtHit(hit: com.google.ar.core.HitResult) {
        val anchor = hit.createAnchor()
        val modelLoader = arSceneView.engine.createModelLoader(this)

        val modelInstance = modelLoader.createModelInstance("models/alphabet.glb")

        // Wrap it in a ModelNode (which is a Node)
        val modelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = 0.5f // optional scaling
        ).apply {
            scale = Scale(0.2f, 0.2f, 0.2f)
            position = Position(anchor.pose.tx(), anchor.pose.ty(), anchor.pose.tz())
        }

        arSceneView.addChildNode(modelNode)
    }


}

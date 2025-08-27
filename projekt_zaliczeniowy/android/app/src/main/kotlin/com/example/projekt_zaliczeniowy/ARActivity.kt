package com.example.projekt_zaliczeniowy

import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ARActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arFragment = ArFragment()
        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, arFragment)
            .commit()

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            placeCube(hitResult)
        }
        addBackButton()
    }

    private fun placeCube(hitResult: HitResult) {
        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
            .thenAccept { material ->
                val cube = ShapeFactory.makeCube(
                    Vector3(0.1f, 0.1f, 0.1f),
                    Vector3.zero(),
                    material
                )

                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment.arSceneView.scene)

                val node = TransformableNode(arFragment.transformationSystem)
                node.renderable = cube
                node.setParent(anchorNode)
                node.select()
            }
    }

    private fun addBackButton() {
        val button = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_previous) // simple back icon
            setBackgroundColor(0x55000000) // semi-transparent black
            setOnClickListener { finish() } // closes ARActivity and returns to Flutter
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            marginStart = 32
            topMargin = 32
        }

        addContentView(button, params)
    }
}

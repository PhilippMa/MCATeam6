package com.example.mcateam6.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.mcateam6.R
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mcateam6.activities.MainActivity

private const val REQUEST_CODE_PERMISSIONS = 10

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class BarcodeFragment : Fragment() {
    private lateinit var view_finder: TextureView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_barcode, container, false);
        view_finder = v.findViewById(R.id.view_finder)

        if (allPermissionGranted()) {
            view_finder.post{startCamera()}
        } else {
            ActivityCompat.requestPermissions(
                activity as MainActivity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        view_finder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
        return v
    }

    private fun startCamera(){
        Toast.makeText(activity, "START CAMERA", Toast.LENGTH_SHORT)
            .show()
        // Create configuration object for the view_finder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
        }.build()

        // Build the view_finder use case
        val preview = Preview(previewConfig)

        // Every time the view_finder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = view_finder.parent as ViewGroup
            parent.removeView(view_finder)
            parent.addView(view_finder, 0)

            view_finder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(activity, preview)
    }
    private fun updateTransform(){
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = view_finder.width / 2f
        val centerY = view_finder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(view_finder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        view_finder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode== REQUEST_CODE_PERMISSIONS){
            if (allPermissionGranted()) {
                Toast.makeText(activity, "PERMISSION GRATNED", Toast.LENGTH_SHORT).show()
                view_finder.post{ startCamera()}
            }
        }else {
            Toast.makeText(activity,"PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }
    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(
            activity as MainActivity, it) == PackageManager.PERMISSION_GRANTED
    }
}
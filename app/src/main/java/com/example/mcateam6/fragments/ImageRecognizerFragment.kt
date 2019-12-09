package com.example.mcateam6.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mcateam6.activities.MainActivity
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.util.Size
import com.example.mcateam6.R
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.image.TensorImage
import android.graphics.Bitmap
import android.media.ImageReader
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.mcateam6.activities.AddProductActivity
import com.example.mcateam6.activities.ProductInfoActivity
import com.example.mcateam6.database.RemoteDatabase
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageRecognizerFragment: Fragment(), Camera2API.Camera2Interface, TextureView.SurfaceTextureListener, View.OnClickListener {
    private lateinit var tflite: Interpreter
    private var labels = mutableListOf<String>()
    private var imageWidth: Int? = null
    private var imageHeight: Int? = null
    private lateinit var inputImageBuffer: TensorImage
    private lateinit var outputProbabilityBuffer: TensorBuffer
    private lateinit var probabilityProcessor: TensorProcessor

    private val db = RemoteDatabase()

    private val PROBABILITY_MIN = 0.0f
    private val PROBABILITY_MAX = 1.0f

    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    lateinit var viewFinder: TextureView
    lateinit var btnClassify: Button
    lateinit var mCamera: Camera2API

    lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_image_recognizer, container, false)
        viewFinder = v.findViewById(R.id.view_finder)
        viewFinder.surfaceTextureListener = this

        btnClassify = v.findViewById(R.id.btn_classify)
        btnClassify.setOnClickListener(this)

        try {
            val labelsPath = "labelmap.txt"
            labels = FileUtil.loadLabels(activity as MainActivity, labelsPath)

            val modelPath = "products.tflite"
            val tfliteModel = FileUtil.loadMappedFile(activity as MainActivity, modelPath)
            val tfliteOptions = Interpreter.Options()
            tfliteOptions.setNumThreads(2)

            tflite = Interpreter(tfliteModel, tfliteOptions)

        } catch (e: IOException) {
            e.printStackTrace()
            activity?.finish()
        }

        mCamera = Camera2API(this)
        mCamera.setViewFinder(viewFinder)

        var imageShape = tflite.getInputTensor(0).shape()
        imageWidth = imageShape[1]
        imageHeight = imageShape[2]
        Log.d("HEIGHT", imageWidth.toString())
        val imageDataType = tflite.getInputTensor(0).dataType()
        inputImageBuffer = TensorImage(imageDataType)

        val probabilityShape = tflite.getOutputTensor(0).shape()
        val probabilityDataType = tflite.getOutputTensor(0).dataType()
        outputProbabilityBuffer = TensorBuffer.createFixedSize(
            probabilityShape, probabilityDataType
        )
        probabilityProcessor = TensorProcessor.Builder().add(
            NormalizeOp(PROBABILITY_MIN, PROBABILITY_MAX)
        ).build()

        return v
    }

    protected fun classify(): Map<String, Float>? {
        mCamera.takePicture()
        val frameBitmap = mCamera.frameBitmap
        if (frameBitmap==null||imageWidth===null||imageHeight===null) return null
        val resized = Bitmap.createScaledBitmap(frameBitmap, frameBitmap.width, frameBitmap.height, true)
        inputImageBuffer = loadImage(resized, 90)
        tflite.run(inputImageBuffer.buffer, outputProbabilityBuffer.buffer.rewind())

        val labeledProbability = TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer)).mapWithFloatValue
        return labeledProbability
    }
    override fun onResume() {
        super.onResume()
        if (viewFinder.isAvailable) {
            openCamera()
        } else {
            viewFinder.surfaceTextureListener = this
        }
    }

    override fun onPause() {
        closeCamera()
        super.onPause()
    }
    fun closeCamera() {
        mCamera.closeCamera()
    }

    fun openCamera() {
        val cameraManager = mCamera.CameraManager_1(activity)
        val cameraId = mCamera.CameraCharacteristics_2(cameraManager)
        mCamera.CameraDevice_3(cameraManager, cameraId)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {
                Toast.makeText(activity, "Camera Permissions are not granted", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(
            activity as MainActivity, it
        ) == PackageManager.PERMISSION_GRANTED
    }
    private val thisFramgent = this
    private val PROB_CRITERIA = 0.6f
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_classify -> {
                val map = classify()
                var maxProb = 0.0f
                var maxKey = ""
                map?.let{
                    it.forEach {
                        var prob = it.value
                        if (prob>=PROB_CRITERIA&&prob >=maxProb) {
                            maxProb = prob
                            maxKey = it.key
                        }
                    }
                    Log.d("PRB", map.toString())
                    if (maxProb==0.0f) {
                        showProductNotFoundDialog()
                    } else {
                        val task = db.getProductById(maxKey)
                        task?.addOnCompleteListener{ res ->
                            if (res.isSuccessful && res.result != null) {
                                val name = res.result?.name_english
                                Toast.makeText(activity, name ?: maxKey, Toast.LENGTH_SHORT).show()
                                val intent = Intent(activity, ProductInfoActivity::class.java).apply {
                                    putExtra("id", maxKey)
                                }
                                activity?.startActivity(intent)
                            } else {
                                showProductNotFoundDialog()
                            }
                        }
                    }
                } ?: run {
                    Toast.makeText(activity, "NO ITEM", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showProductNotFoundDialog() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Can't recognize product")
        builder.setMessage("We can't recognize product. The product may not in the database. Do you want to add it?")
        builder.setPositiveButton("YES"){dialog, which ->
            Toast.makeText(context,":)",Toast.LENGTH_SHORT).show()
            val intent = Intent().setClass(context!!, AddProductActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("No"){dialog, which ->
            Toast.makeText(context,":(",Toast.LENGTH_SHORT).show()
            thisFramgent.onResume()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun loadImage(bitmap:Bitmap, sensorOrientation:Int): TensorImage {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap)
        // Creates processor for the TensorImage.
        val cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight())
        val numRoration = sensorOrientation / 90

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageWidth!!, imageHeight!!, ResizeOp.ResizeMethod.BILINEAR))
            .add(Rot90Op(numRoration))
            .add(NormalizeOp(127.5f, 127.5f))
            .build()
        return imageProcessor.process(inputImageBuffer)
    }
    override fun onCameraDeviceOpened(cameraDevice: CameraDevice?, cameraSize: Size?) {
        if (cameraSize==null) return
        val texture = viewFinder.surfaceTexture
        texture.setDefaultBufferSize(cameraSize.width, cameraSize.height)
        val surface = Surface(texture)

        mCamera.CaptureSession_4(cameraDevice, surface)
        mCamera.CaptureRequest_5(cameraDevice, surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        openCamera()
    }
}
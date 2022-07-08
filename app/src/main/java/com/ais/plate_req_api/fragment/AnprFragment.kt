package com.ais.plate_req_api.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ais.plate_req_api.R
import com.ais.plate_req_api.utils.getFileFromUri
import com.ais.plate_req_api.webService.ApiService
import com.ais.plate_req_api.webService.RetrofitHelper
import com.kotlinpermissions.KotlinPermissions
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_anpr.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AnprFragment : Fragment() {

    private val API_TOKEN = ""

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    var isFrontCamera = false

    var vrn: String = ""
    var countryCode: String = ""
    var vehicleType: String = ""
    var score: String = ""
    var bounding: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_anpr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        checkPermissions()
    }

    private fun checkPermissions() {
        // Request camera permissions
        KotlinPermissions.with(requireActivity()) // Where this is an FragmentActivity instance
            .permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .onAccepted {
                startCamera()
                initListeners()
            }
            .onDenied {
                Log.d(TAG, "User denied permissions")
            }
            .onForeverDenied {
                Log.d(TAG, "User forever denied permissions")
            }
            .ask()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initListeners() = try {
        iv_take_photo.setOnClickListener {
            takePhoto()
        }
        iv_switch_camera.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
            iv_switch_camera.setImageDrawable(
                resources.getDrawable(
                    if (isFrontCamera)
                        R.drawable.ic_outline_camera_front_24
                    else
                        R.drawable.ic_outline_camera_rear_24
                )
            )
        }
    } catch (e: Exception) {
        Log.d(TAG, "Photo capture failed: ${e.message}")
    }

    @SuppressLint("NewApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector =
                if (isFrontCamera)
                    CameraSelector.DEFAULT_FRONT_CAMERA
                else
                    CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed ${e.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    @SuppressLint("NewApi")
    private fun takePhoto() = try {
        val imageCapture = imageCapture ?: throw IOException("Camera not connected")
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ImageCapture.OutputFileOptions
                .Builder(
                    requireContext().contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).build()
        else
            ImageCapture.OutputFileOptions
                .Builder(
                    requireContext().contentResolver,
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    contentValues
                ).build()
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d(TAG, "Photo capture failed: ${exc.message}")
                    println("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
                    Toast.makeText(
                        requireContext(),
                        "Photo capture succeeded: ${output.savedUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                    uploadImageToServerAndGetResults(output.savedUri)
                }
            }
        )
    } catch (e: Exception) {
        Log.d(TAG, "Photo capture failed: ${e.message}")
        println("Photo capture failed: ${e.message}")
    }

    private fun uploadImageToServerAndGetResults(savedUri: Uri?) {
        if (savedUri != null) {
            progress_bar.visibility = View.VISIBLE
            val apiService: ApiService = RetrofitHelper.getInstance().create(ApiService::class.java)
            GlobalScope.launch(Dispatchers.IO) {
                val imgFile = requireContext().getFileFromUri(savedUri)
                val compressedImageFile = Compressor.compress(requireContext(), imgFile)
                val imageFilePart = MultipartBody.Part.createFormData(
                    "upload",
                    compressedImageFile.name,
                    RequestBody.create(
                        MediaType.parse("image/*"),
                        compressedImageFile
                    )
                )
                val response =
                    apiService.getNumberPlateDetails(
                        token = "TOKEN $API_TOKEN",
                        imagePart = imageFilePart
                    )
                if (response.isSuccessful && response.body() != null) {
                    if ((response.body()?.results?.size ?: 0) > 0) {
                        withContext(Dispatchers.Main) {
                            progress_bar.visibility = View.GONE
                            // Set variables for vehicle data.
                            vrn = response.body()?.results?.get(0)?.plate.toString().uppercase()
                            countryCode = response.body()?.results?.get(0)?.region?.code.toString()
                                .uppercase()
                            vehicleType = response.body()?.results?.get(0)?.vehicle?.type.toString()
                                .uppercase()

                            score = response.body()?.results?.get(0)?.score.toString()

                            bounding = response.body()?.results?.get(0)?.box.toString()

                            Log.d(TAG, response.body()?.results.toString())

                            updateNumberplate(vrn, countryCode)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "No VRN found in image.")
                            progress_bar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun updateNumberplate(vrn: String, country_code: String) {
        tv_numberplate.text = vrn
        tv_country_prefix.text = country_code
        tv_vehicle_type.text = vehicleType
        tv_vehicle_score.text = score
        tv_bounding.text = bounding
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

}

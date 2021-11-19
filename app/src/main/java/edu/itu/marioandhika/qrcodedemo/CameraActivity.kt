package edu.itu.marioandhika.qrcodedemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View.inflate
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.LayoutInflaterCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import edu.itu.marioandhika.qrcodedemo.ui.theme.QRCodeDemoTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity: ComponentActivity() {
    val showRationale = mutableStateOf(false)
    val permissionGranted = mutableStateOf(false)

    val barcodeResult = mutableStateOf("No code yet")

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.all{ it.value }) {
                Toast.makeText(this, "All granted", Toast.LENGTH_SHORT).show()
                showRationale.value = false
                permissionGranted.value = true
            } else {
                showRationale.value = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) && shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA)
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.CAMERA
            ))
        }

        setContent {
            QRCodeDemoTheme {
                Column(Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                    SimpleCameraPreview()
                }
            }
        }
    }

    @Composable
    fun SimpleCameraPreview() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        AndroidView(modifier = Modifier.fillMaxWidth(),factory = {
            PreviewView(it)

        }) { inflatedLayout ->

            cameraProviderFuture.addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(
                    lifecycleOwner,
                    inflatedLayout as PreviewView /*the inflated layout*/,
                    cameraProvider)
            }, ContextCompat.getMainExecutor(context))
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    fun bindPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        cameraProvider: ProcessCameraProvider
    ) {
        val preview: Preview = Preview.Builder().build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
//        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(previewView.createSurfaceProvider())

        val imageAnalyzer = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        // Pass image to an ML Kit Vision API
                        // ...
                        val options = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE)
                            .build()
                        val scanner = BarcodeScanning.getClient(options)
                        val result = scanner.process(image).addOnSuccessListener {
                            Log.d("MyQRCodeApp", "Success")
                            it.forEach {
                                it.rawValue?.let {
                                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener {
                            Log.d("MyQRCodeApp", "Failed to scan barcode")
                        }.addOnCompleteListener {
                            imageProxy.close()
                        }
                    }
                })
            }
        cameraProvider.unbindAll()
        var camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }
}

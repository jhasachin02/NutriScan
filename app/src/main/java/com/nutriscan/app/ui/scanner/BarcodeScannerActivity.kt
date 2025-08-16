package com.nutriscan.app.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nutriscan.app.R
import com.nutriscan.app.databinding.ActivityBarcodeScannerBinding
import com.nutriscan.app.data.models.FoodItem
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class BarcodeScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBarcodeScannerBinding
    private val viewModel: BarcodeScannerViewModel by viewModels()
    
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = true
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required for barcode scanning", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupObservers()
        requestCameraPermission()
        
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    private fun setupUI() {
        binding.apply {
            btnClose.setOnClickListener { finish() }
            btnFlashToggle.setOnClickListener { toggleFlash() }
            btnManualEntry.setOnClickListener { 
                // TODO: Navigate to manual entry screen
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.foodItem.observe(this, Observer { result ->
            result?.fold(
                onSuccess = { foodItem ->
                    handleScanSuccess(foodItem)
                },
                onFailure = { error ->
                    handleScanError(error.message ?: "Unknown error occurred")
                }
            )
        })
        
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.tvScanningStatus.text = if (isLoading) {
                "Looking up product..."
            } else {
                "Point camera at barcode"
            }
        })
    }
    
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
                
                imageCapture = ImageCapture.Builder().build()
                
                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
                    }
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
                
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun toggleFlash() {
        // TODO: Implement flash toggle
    }
    
    private fun handleScanSuccess(foodItem: FoodItem) {
        isScanning = false
        
        // Return the food item to the calling activity
        val intent = Intent().apply {
            putExtra(EXTRA_FOOD_ITEM, foodItem)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
    
    private fun handleScanError(error: String) {
        isScanning = true
        Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
    }
    
    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            if (!isScanning) {
                imageProxy.close()
                return
            }
            
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val scanner = BarcodeScanning.getClient()
                
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            when (barcode.valueType) {
                                Barcode.TYPE_PRODUCT -> {
                                    barcode.rawValue?.let { barcodeValue ->
                                        Log.d(TAG, "Barcode detected: $barcodeValue")
                                        viewModel.fetchFoodItemByBarcode(barcodeValue)
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Barcode scanning failed", exception)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    
    companion object {
        private const val TAG = "BarcodeScannerActivity"
        const val EXTRA_FOOD_ITEM = "extra_food_item"
    }
}

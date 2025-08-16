package com.nutriscan.app.ui.ar

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.nutriscan.app.R
import com.nutriscan.app.data.models.FoodItem
import com.nutriscan.app.databinding.ActivityArBinding
import com.nutriscan.app.nutrition.ARNutritionInsights
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ARActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityArBinding
    private val viewModel: ARViewModel by viewModels()
    
    private lateinit var arFragment: ArFragment
    private var foodItem: FoodItem? = null
    private var nutritionInsights: ARNutritionInsights? = null
    
    private val anchorNodes = mutableListOf<AnchorNode>()
    
    companion object {
        private const val TAG = "ARActivity"
        private const val MIN_OPENGL_VERSION = 3.0
        const val EXTRA_FOOD_ITEM = "extra_food_item"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }
        
        binding = ActivityArBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        foodItem = intent.getParcelableExtra(EXTRA_FOOD_ITEM)
        
        setupUI()
        setupObservers()
        setupArFragment()
        
        foodItem?.let { item ->
            viewModel.generateARInsights(item)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            btnClose.setOnClickListener { finish() }
            
            btnToggleInfo.setOnClickListener {
                toggleNutritionInfo()
            }
            
            instructionText.text = "Tap on a surface to place nutrition information"
            
            // Update food info
            foodItem?.let { item ->
                foodNameText.text = item.name
                foodBrandText.text = item.brand ?: ""
                caloriesText.text = "${item.nutritionFacts.calories.toInt()} cal"
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.arInsights.observe(this) { insights ->
            nutritionInsights = insights
            insights?.let {
                updateNutritionDisplay(it)
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupArFragment() {
        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (nutritionInsights == null) {
                Toast.makeText(this, "Loading nutrition data...", Toast.LENGTH_SHORT).show()
                return@setOnTapArPlaneListener
            }
            
            placeNutritionInfo(hitResult, motionEvent)
        }
        
        arFragment.arSceneView.planeRenderer.isEnabled = true
        arFragment.arSceneView.planeRenderer.material.thenAccept { material ->
            material.setFloat3(MaterialFactory.MATERIAL_COLOR, Color.valueOf(0f, 0f, 1f, 0.3f))
        }
    }
    
    private fun placeNutritionInfo(hitResult: HitResult, motionEvent: MotionEvent) {
        nutritionInsights?.let { insights ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)
            
            createNutritionVisualization(anchorNode, insights)
            anchorNodes.add(anchorNode)
            
            // Hide instruction text after first placement
            binding.instructionText.visibility = View.GONE
        }
    }
    
    private fun createNutritionVisualization(anchorNode: AnchorNode, insights: ARNutritionInsights) {
        // Create main nutrition card
        createNutritionCard(anchorNode, insights)
        
        // Create floating nutrition facts
        createFloatingNutritionFacts(anchorNode, insights)
        
        // Create recommendation indicator
        createRecommendationIndicator(anchorNode, insights)
    }
    
    private fun createNutritionCard(anchorNode: AnchorNode, insights: ARNutritionInsights) {
        MaterialFactory.makeOpaqueWithColor(this, com.google.ar.sceneform.rendering.Color(Color.parseColor(insights.colorCode)))
            .thenAccept { material ->
                val cardRenderable = ShapeFactory.makeCube(Vector3(0.3f, 0.2f, 0.02f))
                cardRenderable.material = material
                
                val cardNode = Node()
                cardNode.renderable = cardRenderable
                cardNode.setParent(anchorNode)
                
                // Add text overlay
                ViewRenderable.builder()
                    .setView(this, createNutritionCardView(insights))
                    .build()
                    .thenAccept { textRenderable ->
                        val textNode = Node()
                        textNode.renderable = textRenderable
                        textNode.setParent(cardNode)
                        textNode.localPosition = Vector3(0f, 0f, 0.025f)
                    }
            }
    }
    
    private fun createFloatingNutritionFacts(anchorNode: AnchorNode, insights: ARNutritionInsights) {
        insights.visualIndicators.forEachIndexed { index, indicator ->
            val angle = (index * 60f) * Math.PI / 180f
            val radius = 0.4f
            
            val x = (radius * Math.cos(angle)).toFloat()
            val z = (radius * Math.sin(angle)).toFloat()
            val y = 0.1f + (index * 0.05f)
            
            MaterialFactory.makeOpaqueWithColor(this, com.google.ar.sceneform.rendering.Color(Color.parseColor(indicator.color)))
                .thenAccept { material ->
                    val sphere = ShapeFactory.makeSphere(0.05f)
                    sphere.material = material
                    
                    val sphereNode = Node()
                    sphereNode.renderable = sphere
                    sphereNode.setParent(anchorNode)
                    sphereNode.localPosition = Vector3(x, y, z)
                    
                    // Add floating animation
                    animateFloating(sphereNode)
                    
                    // Add text label
                    ViewRenderable.builder()
                        .setView(this, createIndicatorLabel(indicator))
                        .build()
                        .thenAccept { labelRenderable ->
                            val labelNode = Node()
                            labelNode.renderable = labelRenderable
                            labelNode.setParent(sphereNode)
                            labelNode.localPosition = Vector3(0f, 0.1f, 0f)
                        }
                }
        }
    }
    
    private fun createRecommendationIndicator(anchorNode: AnchorNode, insights: ARNutritionInsights) {
        val recommendationColor = when {
            insights.recommendationText.contains("Great", ignoreCase = true) -> Color.GREEN
            insights.recommendationText.contains("Good", ignoreCase = true) -> Color.YELLOW
            insights.recommendationText.contains("Okay", ignoreCase = true) -> Color.ORANGE
            else -> Color.RED
        }
        
        MaterialFactory.makeOpaqueWithColor(this, com.google.ar.sceneform.rendering.Color(recommendationColor))
            .thenAccept { material ->
                val cylinder = ShapeFactory.makeCylinder(0.05f, 0.3f)
                cylinder.material = material
                
                val cylinderNode = Node()
                cylinderNode.renderable = cylinder
                cylinderNode.setParent(anchorNode)
                cylinderNode.localPosition = Vector3(0f, 0.35f, 0f)
                
                // Add pulsing animation for attention
                animatePulsing(cylinderNode)
            }
    }
    
    private fun createNutritionCardView(insights: ARNutritionInsights): View {
        val cardView = layoutInflater.inflate(R.layout.ar_nutrition_card, null)
        
        // Populate the card with nutrition data
        // This would be implemented with the actual layout
        
        return cardView
    }
    
    private fun createIndicatorLabel(indicator: com.nutriscan.app.nutrition.VisualIndicator): View {
        val labelView = layoutInflater.inflate(R.layout.ar_indicator_label, null)
        
        // Populate with indicator data
        // This would be implemented with the actual layout
        
        return labelView
    }
    
    private fun animateFloating(node: Node) {
        val animator = ObjectAnimator.ofFloat(0f, 1f).setDuration(3000)
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val originalPosition = node.localPosition
            node.localPosition = Vector3(
                originalPosition.x,
                originalPosition.y + (value * 0.05f),
                originalPosition.z
            )
        }
        
        animator.start()
    }
    
    private fun animatePulsing(node: Node) {
        val animator = ObjectAnimator.ofFloat(0.8f, 1.2f).setDuration(1500)
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        
        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            node.localScale = Vector3(scale, scale, scale)
        }
        
        animator.start()
    }
    
    private fun updateNutritionDisplay(insights: ARNutritionInsights) {
        binding.apply {
            primaryMetricText.text = insights.primaryMetric
            recommendationText.text = insights.recommendationText
            recommendationText.setTextColor(Color.parseColor(insights.colorCode))
            
            // Update quick facts
            val factsText = insights.quickFacts.joinToString(" • ")
            quickFactsText.text = factsText
            
            // Show alternatives if available
            if (insights.alternativeSuggestions.isNotEmpty()) {
                alternativesText.text = "Try: ${insights.alternativeSuggestions.first()}"
                alternativesText.visibility = View.VISIBLE
            } else {
                alternativesText.visibility = View.GONE
            }
        }
    }
    
    private fun toggleNutritionInfo() {
        val currentVisibility = binding.nutritionInfoPanel.visibility
        binding.nutritionInfoPanel.visibility = if (currentVisibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
    
    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(this, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        
        val openGlVersionString = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        
        return true
    }
    
    override fun onResume() {
        super.onResume()
        if (!ArCoreUtils.isArCoreAvailable(this)) {
            Toast.makeText(this, "ARCore is not available on this device", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up AR resources
        anchorNodes.forEach { anchorNode ->
            arFragment.arSceneView.scene.removeChild(anchorNode)
            anchorNode.anchor?.detach()
        }
        anchorNodes.clear()
    }
}

// Helper object for ARCore utilities
object ArCoreUtils {
    fun isArCoreAvailable(context: Context): Boolean {
        return try {
            when (ArCoreApk.getInstance().checkAvailability(context)) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                    // ARCore is supported but not installed or needs update
                    false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}

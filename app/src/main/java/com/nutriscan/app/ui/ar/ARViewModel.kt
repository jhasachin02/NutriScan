package com.nutriscan.app.ui.ar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriscan.app.data.models.FoodItem
import com.nutriscan.app.data.repository.UserRepository
import com.nutriscan.app.nutrition.ARNutritionInsights
import com.nutriscan.app.nutrition.NutritionAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ARViewModel @Inject constructor(
    private val nutritionAnalyzer: NutritionAnalyzer,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _arInsights = MutableLiveData<ARNutritionInsights?>()
    val arInsights: LiveData<ARNutritionInsights?> = _arInsights
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun generateARInsights(foodItem: FoodItem) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val userProfile = userRepository.getCurrentUserProfile()
                if (userProfile != null) {
                    val insights = nutritionAnalyzer.generateARInsights(foodItem, userProfile)
                    _arInsights.value = insights
                } else {
                    _error.value = "User profile not found. Please set up your profile first."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to generate AR insights"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

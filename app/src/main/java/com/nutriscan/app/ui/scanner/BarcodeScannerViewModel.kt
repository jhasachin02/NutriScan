package com.nutriscan.app.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriscan.app.data.models.FoodItem
import com.nutriscan.app.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {
    
    private val _foodItem = MutableLiveData<Result<FoodItem>?>()
    val foodItem: LiveData<Result<FoodItem>?> = _foodItem
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun fetchFoodItemByBarcode(barcode: String) {
        if (_isLoading.value == true) return // Prevent multiple requests
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = foodRepository.getFoodItemByBarcode(barcode)
                _foodItem.value = result
            } catch (e: Exception) {
                _foodItem.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetState() {
        _foodItem.value = null
        _isLoading.value = false
    }
}

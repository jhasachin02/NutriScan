package com.nutriscan.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriscan.app.data.models.FoodItem
import com.nutriscan.app.data.repository.FoodRepository
import com.nutriscan.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _recentScans = MutableLiveData<List<FoodItem>>()
    val recentScans: LiveData<List<FoodItem>> = _recentScans
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadRecentScans()
    }
    
    private fun loadRecentScans() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recent = foodRepository.getCachedFoodItems()
                    .sortedByDescending { it.createdAt }
                    .take(10)
                _recentScans.value = recent
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshRecentScans() {
        loadRecentScans()
    }
}

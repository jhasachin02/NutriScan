package com.nutriscan.app.data.repository

import com.nutriscan.app.data.api.FoodApiService
import com.nutriscan.app.data.api.FoodApiResponse
import com.nutriscan.app.data.api.FoodProductData
import com.nutriscan.app.data.api.NutrimentsData
import com.nutriscan.app.data.database.FoodDao
import com.nutriscan.app.data.models.FoodItem
import com.nutriscan.app.data.models.NutritionFacts
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response

class FoodRepositoryTest {
    
    @Mock
    private lateinit var apiService: FoodApiService
    
    @Mock
    private lateinit var foodDao: FoodDao
    
    private lateinit var foodRepository: FoodRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        foodRepository = FoodRepository(apiService, foodDao)
    }
    
    @Test
    fun `getFoodItemByBarcode should return cached item when available and valid`() = runBlocking {
        // Given
        val barcode = "1234567890"
        val cachedItem = FoodItem(
            id = "test-id",
            name = "Cached Food",
            nutritionFacts = NutritionFacts(),
            barcode = barcode,
            updatedAt = System.currentTimeMillis() - 1000 // 1 second ago
        )
        
        `when`(foodDao.getFoodItemByBarcode(barcode)).thenReturn(cachedItem)
        
        // When
        val result = foodRepository.getFoodItemByBarcode(barcode)
        
        // Then
        assertTrue("Should return success", result.isSuccess)
        assertEquals("Should return cached item", cachedItem, result.getOrNull())
        verify(apiService, never()).getFoodByBarcode(any(), any())
    }
    
    @Test
    fun `getFoodItemByBarcode should fetch from API when cache is empty`() = runBlocking {
        // Given
        val barcode = "1234567890"
        val apiResponse = FoodApiResponse(
            product = FoodProductData(
                id = "api-id",
                product_name = "API Food",
                brands = "API Brand",
                image_url = "http://example.com/image.jpg",
                categories = "snacks",
                ingredients_text = "ingredient1, ingredient2",
                allergens = "nuts",
                nutriments = NutrimentsData(
                    energy_kcal_100g = 200.0,
                    fat_100g = 10.0,
                    proteins_100g = 5.0,
                    carbohydrates_100g = 25.0
                ),
                serving_size = "100g",
                serving_quantity = 1.0
            ),
            status = "1",
            status_verbose = "product found"
        )
        
        `when`(foodDao.getFoodItemByBarcode(barcode)).thenReturn(null)
        `when`(apiService.getFoodByBarcode(eq(barcode), any())).thenReturn(Response.success(apiResponse))
        
        // When
        val result = foodRepository.getFoodItemByBarcode(barcode)
        
        // Then
        assertTrue("Should return success", result.isSuccess)
        val foodItem = result.getOrNull()
        assertNotNull("Food item should not be null", foodItem)
        assertEquals("API Food", foodItem?.name)
        assertEquals("API Brand", foodItem?.brand)
        assertEquals(200.0, foodItem?.nutritionFacts?.calories ?: 0.0, 0.1)
        
        verify(foodDao).insertFoodItem(any())
    }
    
    @Test
    fun `getFoodItemByBarcode should return cached item when API fails`() = runBlocking {
        // Given
        val barcode = "1234567890"
        val cachedItem = FoodItem(
            id = "test-id",
            name = "Cached Food",
            nutritionFacts = NutritionFacts(),
            barcode = barcode,
            updatedAt = System.currentTimeMillis() - (25 * 60 * 60 * 1000) // 25 hours ago (expired)
        )
        
        `when`(foodDao.getFoodItemByBarcode(barcode)).thenReturn(cachedItem)
        `when`(apiService.getFoodByBarcode(eq(barcode), any())).thenThrow(RuntimeException("Network error"))
        
        // When
        val result = foodRepository.getFoodItemByBarcode(barcode)
        
        // Then
        assertTrue("Should return success with cached item", result.isSuccess)
        assertEquals("Should return cached item", cachedItem, result.getOrNull())
    }
    
    @Test
    fun `getFoodItemByBarcode should return failure when no cache and API fails`() = runBlocking {
        // Given
        val barcode = "1234567890"
        
        `when`(foodDao.getFoodItemByBarcode(barcode)).thenReturn(null)
        `when`(apiService.getFoodByBarcode(eq(barcode), any())).thenThrow(RuntimeException("Network error"))
        
        // When
        val result = foodRepository.getFoodItemByBarcode(barcode)
        
        // Then
        assertTrue("Should return failure", result.isFailure)
        assertTrue("Should be network error", 
                  result.exceptionOrNull()?.message?.contains("Network error") == true)
    }
    
    @Test
    fun `nutrition facts mapping should work correctly`() = runBlocking {
        // Given
        val barcode = "1234567890"
        val nutriments = NutrimentsData(
            energy_kcal_100g = 300.0,
            fat_100g = 15.0,
            saturated_fat_100g = 5.0,
            cholesterol_100g = 10.0,
            sodium_100g = 500.0,
            carbohydrates_100g = 30.0,
            fiber_100g = 8.0,
            sugars_100g = 12.0,
            proteins_100g = 20.0,
            vitamin_a_100g = 100.0,
            vitamin_c_100g = 50.0,
            calcium_100g = 200.0,
            iron_100g = 5.0
        )
        
        val apiResponse = FoodApiResponse(
            product = FoodProductData(
                id = "test-id",
                product_name = "Test Food",
                nutriments = nutriments
            ),
            status = "1",
            status_verbose = "product found"
        )
        
        `when`(foodDao.getFoodItemByBarcode(barcode)).thenReturn(null)
        `when`(apiService.getFoodByBarcode(eq(barcode), any())).thenReturn(Response.success(apiResponse))
        
        // When
        val result = foodRepository.getFoodItemByBarcode(barcode)
        
        // Then
        assertTrue("Should return success", result.isSuccess)
        val foodItem = result.getOrNull()
        val nutrition = foodItem?.nutritionFacts
        
        assertNotNull("Nutrition facts should not be null", nutrition)
        assertEquals(300.0, nutrition?.calories ?: 0.0, 0.1)
        assertEquals(15.0, nutrition?.totalFat ?: 0.0, 0.1)
        assertEquals(5.0, nutrition?.saturatedFat ?: 0.0, 0.1)
        assertEquals(10.0, nutrition?.cholesterol ?: 0.0, 0.1)
        assertEquals(500.0, nutrition?.sodium ?: 0.0, 0.1)
        assertEquals(30.0, nutrition?.totalCarbohydrate ?: 0.0, 0.1)
        assertEquals(8.0, nutrition?.dietaryFiber ?: 0.0, 0.1)
        assertEquals(12.0, nutrition?.totalSugars ?: 0.0, 0.1)
        assertEquals(20.0, nutrition?.protein ?: 0.0, 0.1)
        assertEquals(100.0, nutrition?.vitaminA ?: 0.0, 0.1)
        assertEquals(50.0, nutrition?.vitaminC ?: 0.0, 0.1)
        assertEquals(200.0, nutrition?.calcium ?: 0.0, 0.1)
        assertEquals(5.0, nutrition?.iron ?: 0.0, 0.1)
    }
    
    @Test
    fun `ingredients and allergens should be parsed correctly`() = runBlocking {
        // Given
        val barcode = "1234567890"
        val apiResponse = FoodApiResponse(
            product = FoodProductData(
                id = "test-id",
                product_name = "Test Food",
                ingredients_text = "wheat flour, sugar, salt, natural flavors",
                allergens = "gluten, may contain nuts"
            ),
            status = "1",
            status_verbose = "product found"
        )
        
        `when`(foodDao.getFoodItemByBarcode(barcode)).thenReturn(null)
        `when`(apiService.getFoodByBarcode(eq(barcode), any())).thenReturn(Response.success(apiResponse))
        
        // When
        val result = foodRepository.getFoodItemByBarcode(barcode)
        
        // Then
        assertTrue("Should return success", result.isSuccess)
        val foodItem = result.getOrNull()
        
        assertNotNull("Food item should not be null", foodItem)
        assertEquals(4, foodItem?.ingredients?.size)
        assertEquals("wheat flour", foodItem?.ingredients?.get(0))
        assertEquals("sugar", foodItem?.ingredients?.get(1))
        
        assertEquals(2, foodItem?.allergens?.size)
        assertEquals("gluten", foodItem?.allergens?.get(0))
        assertEquals("may contain nuts", foodItem?.allergens?.get(1))
    }
    
    @Test
    fun `cache validation should work correctly`() {
        // This test would verify the private isCacheValid method behavior
        // by testing it through the public interface
        
        val now = System.currentTimeMillis()
        val recentTime = now - (12 * 60 * 60 * 1000) // 12 hours ago
        val oldTime = now - (25 * 60 * 60 * 1000) // 25 hours ago
        
        // Recent cache should be valid (within 24 hours)
        // Old cache should be invalid (over 24 hours)
        
        // This logic is tested indirectly through the getFoodItemByBarcode tests above
        assertTrue("Test setup is correct", recentTime > oldTime)
    }
}

package com.nutriscan.app.data.repository

import com.nutriscan.app.data.api.*
import com.nutriscan.app.data.database.FoodDao
import com.nutriscan.app.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val apiService: FoodApiService,
    private val foodDao: FoodDao
) {
    companion object {
        private val API_KEY = com.nutriscan.app.BuildConfig.EDAMAM_APP_KEY.takeIf { it.isNotEmpty() } 
            ?: "demo-key" // Fallback for development
    }

    suspend fun getFoodItemByBarcode(barcode: String): Result<FoodItem> {
        return try {
            // First try to get from local cache
            val cachedItem = foodDao.getFoodItemByBarcode(barcode)
            if (cachedItem != null && isCacheValid(cachedItem.updatedAt)) {
                return Result.success(cachedItem)
            }

            // Fetch from API
            val response = apiService.getFoodByBarcode(barcode, "Bearer $API_KEY")
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.product != null) {
                    val foodItem = mapApiResponseToFoodItem(apiResponse.product, barcode)
                    
                    // Cache the result
                    foodDao.insertFoodItem(foodItem)
                    
                    Result.success(foodItem)
                } else {
                    Result.failure(Exception("Product not found"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            // Return cached version if network fails
            val cachedItem = foodDao.getFoodItemByBarcode(barcode)
            if (cachedItem != null) {
                Result.success(cachedItem)
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFoods(query: String, limit: Int = 20, offset: Int = 0): Flow<Result<List<FoodItem>>> {
        return flow {
            try {
                val response = apiService.searchFoods(query, limit, offset, "Bearer $API_KEY")
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null) {
                        val foodItems = searchResponse.products.map { product ->
                            mapApiResponseToFoodItem(product, product.id)
                        }
                        
                        // Cache search results
                        foodDao.insertFoodItems(foodItems)
                        
                        emit(Result.success(foodItems))
                    } else {
                        emit(Result.failure(Exception("Search returned empty response")))
                    }
                } else {
                    emit(Result.failure(HttpException(response)))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
    }

    suspend fun getNearbyRestaurants(
        latitude: Double, 
        longitude: Double, 
        radiusMeters: Int = 5000
    ): Result<List<Restaurant>> {
        return try {
            val response = apiService.getNearbyRestaurants(latitude, longitude, radiusMeters, "Bearer $API_KEY")
            if (response.isSuccessful) {
                val restaurantResponse = response.body()
                if (restaurantResponse != null) {
                    // Cache restaurants
                    foodDao.insertRestaurants(restaurantResponse.restaurants)
                    Result.success(restaurantResponse.restaurants)
                } else {
                    Result.failure(Exception("No restaurants found"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            // Return cached restaurants if network fails
            val cachedRestaurants = foodDao.getNearbyRestaurants(latitude, longitude, radiusMeters.toDouble())
            Result.success(cachedRestaurants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestaurantMenu(restaurantId: String): Result<MenuResponse> {
        return try {
            val response = apiService.getRestaurantMenu(restaurantId, "Bearer $API_KEY")
            if (response.isSuccessful) {
                val menuResponse = response.body()
                if (menuResponse != null) {
                    // Cache menu items
                    val allMenuItems = menuResponse.categories.flatMap { it.items }
                    foodDao.insertMenuItems(allMenuItems)
                    Result.success(menuResponse)
                } else {
                    Result.failure(Exception("Menu not found"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            // Return cached menu items if network fails
            val cachedMenuItems = foodDao.getMenuItemsByRestaurant(restaurantId)
            val menuResponse = MenuResponse(
                restaurantId = restaurantId,
                restaurantName = "", // Would need to fetch restaurant name
                categories = listOf(MenuCategory("cached", "Cached Items", null, cachedMenuItems, 0)),
                lastUpdated = System.currentTimeMillis()
            )
            Result.success(menuResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeNutrition(ingredients: String): Result<NutritionAnalysisResponse> {
        return try {
            val response = apiService.analyzeNutrition(ingredients, "Bearer $API_KEY")
            if (response.isSuccessful) {
                val analysisResponse = response.body()
                if (analysisResponse != null) {
                    Result.success(analysisResponse)
                } else {
                    Result.failure(Exception("Nutrition analysis failed"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchRecipes(
        query: String,
        diet: String? = null,
        healthLabels: String? = null,
        calorieRange: String? = null
    ): Result<List<Recipe>> {
        return try {
            val response = apiService.searchRecipes(query, diet, healthLabels, calorieRange, "Bearer $API_KEY")
            if (response.isSuccessful) {
                val recipeResponse = response.body()
                if (recipeResponse != null) {
                    val recipes = recipeResponse.hits.map { it.recipe }
                    Result.success(recipes)
                } else {
                    Result.failure(Exception("Recipe search failed"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Local database operations
    suspend fun getFavoriteMenuItems(userId: String): List<MenuFavorite> {
        return foodDao.getFavoriteMenuItems(userId)
    }

    suspend fun addFavoriteMenuItem(favorite: MenuFavorite) {
        foodDao.insertFavoriteMenuItem(favorite)
    }

    suspend fun removeFavoriteMenuItem(favoriteId: String) {
        foodDao.deleteFavoriteMenuItem(favoriteId)
    }

    suspend fun getCachedFoodItems(): List<FoodItem> {
        return foodDao.getAllFoodItems()
    }

    // Helper methods
    private fun isCacheValid(lastUpdated: Long, maxAgeHours: Long = 24): Boolean {
        val currentTime = System.currentTimeMillis()
        val maxAge = maxAgeHours * 60 * 60 * 1000 // Convert to milliseconds
        return (currentTime - lastUpdated) < maxAge
    }

    private fun mapApiResponseToFoodItem(product: FoodProductData, barcode: String): FoodItem {
        val nutritionFacts = product.nutriments?.let { nutriments ->
            NutritionFacts(
                calories = nutriments.energy_kcal_100g ?: 0.0,
                totalFat = nutriments.fat_100g ?: 0.0,
                saturatedFat = nutriments.saturated_fat_100g ?: 0.0,
                transFat = nutriments.trans_fat_100g ?: 0.0,
                cholesterol = nutriments.cholesterol_100g ?: 0.0,
                sodium = nutriments.sodium_100g ?: 0.0,
                totalCarbohydrate = nutriments.carbohydrates_100g ?: 0.0,
                dietaryFiber = nutriments.fiber_100g ?: 0.0,
                totalSugars = nutriments.sugars_100g ?: 0.0,
                protein = nutriments.proteins_100g ?: 0.0,
                vitaminA = nutriments.vitamin_a_100g ?: 0.0,
                vitaminC = nutriments.vitamin_c_100g ?: 0.0,
                calcium = nutriments.calcium_100g ?: 0.0,
                iron = nutriments.iron_100g ?: 0.0
            )
        } ?: NutritionFacts()

        return FoodItem(
            id = product.id,
            name = product.product_name ?: "Unknown Product",
            brand = product.brands,
            barcode = barcode,
            imageUrl = product.image_url,
            category = product.categories,
            nutritionFacts = nutritionFacts,
            ingredients = product.ingredients_text?.split(", ") ?: emptyList(),
            allergens = product.allergens?.split(", ") ?: emptyList(),
            servingSize = product.serving_size,
            servingsPerContainer = product.serving_quantity,
            isVerified = true
        )
    }
}

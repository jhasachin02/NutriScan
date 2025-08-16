package com.nutriscan.app.data.api

import com.nutriscan.app.data.models.FoodItem
import com.nutriscan.app.data.models.MenuResponse
import com.nutriscan.app.data.models.RestaurantResponse
import retrofit2.Response
import retrofit2.http.*

interface FoodApiService {
    
    @GET("products/{barcode}")
    suspend fun getFoodByBarcode(
        @Path("barcode") barcode: String,
        @Header("Authorization") apiKey: String
    ): Response<FoodApiResponse>
    
    @GET("search")
    suspend fun searchFoods(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Header("Authorization") apiKey: String
    ): Response<FoodSearchResponse>
    
    @GET("restaurants/nearby")
    suspend fun getNearbyRestaurants(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusMeters: Int = 5000,
        @Header("Authorization") apiKey: String
    ): Response<RestaurantResponse>
    
    @GET("restaurants/{restaurantId}/menu")
    suspend fun getRestaurantMenu(
        @Path("restaurantId") restaurantId: String,
        @Header("Authorization") apiKey: String
    ): Response<MenuResponse>
    
    @GET("nutrition/analyze")
    suspend fun analyzeNutrition(
        @Query("ingredients") ingredients: String,
        @Header("Authorization") apiKey: String
    ): Response<NutritionAnalysisResponse>
    
    @GET("recipes/search")
    suspend fun searchRecipes(
        @Query("q") query: String,
        @Query("diet") diet: String? = null,
        @Query("health") healthLabels: String? = null,
        @Query("calories") calorieRange: String? = null,
        @Header("Authorization") apiKey: String
    ): Response<RecipeSearchResponse>
    
    @POST("products")
    suspend fun submitFoodData(
        @Body foodItem: FoodItem,
        @Header("Authorization") apiKey: String
    ): Response<ApiSubmissionResponse>
}

// Response data classes
data class FoodApiResponse(
    val product: FoodProductData?,
    val status: String,
    val status_verbose: String
)

data class FoodProductData(
    val id: String,
    val product_name: String?,
    val brands: String?,
    val image_url: String?,
    val categories: String?,
    val ingredients_text: String?,
    val allergens: String?,
    val nutriments: NutrimentsData?,
    val serving_size: String?,
    val serving_quantity: Double?
)

data class NutrimentsData(
    val energy_kcal_100g: Double?,
    val fat_100g: Double?,
    val saturated_fat_100g: Double?,
    val trans_fat_100g: Double?,
    val cholesterol_100g: Double?,
    val sodium_100g: Double?,
    val carbohydrates_100g: Double?,
    val fiber_100g: Double?,
    val sugars_100g: Double?,
    val proteins_100g: Double?,
    val salt_100g: Double?,
    val vitamin_a_100g: Double?,
    val vitamin_c_100g: Double?,
    val calcium_100g: Double?,
    val iron_100g: Double?
)

data class FoodSearchResponse(
    val products: List<FoodProductData>,
    val count: Int,
    val page: Int,
    val page_count: Int,
    val page_size: Int,
    val skip: Int
)

data class NutritionAnalysisResponse(
    val calories: Double,
    val totalWeight: Double,
    val dietLabels: List<String>,
    val healthLabels: List<String>,
    val cautions: List<String>,
    val totalNutrients: Map<String, NutrientInfo>,
    val totalDaily: Map<String, NutrientInfo>
)

data class NutrientInfo(
    val label: String,
    val quantity: Double,
    val unit: String
)

data class RecipeSearchResponse(
    val hits: List<RecipeHit>,
    val count: Int,
    val more: Boolean
)

data class RecipeHit(
    val recipe: Recipe
)

data class Recipe(
    val uri: String,
    val label: String,
    val image: String?,
    val source: String,
    val url: String,
    val shareAs: String?,
    val yield: Int,
    val dietLabels: List<String>,
    val healthLabels: List<String>,
    val cautions: List<String>,
    val ingredientLines: List<String>,
    val ingredients: List<RecipeIngredient>,
    val calories: Double,
    val totalWeight: Double,
    val totalTime: Int?,
    val cuisineType: List<String>?,
    val mealType: List<String>?,
    val dishType: List<String>?,
    val totalNutrients: Map<String, NutrientInfo>
)

data class RecipeIngredient(
    val text: String,
    val quantity: Double?,
    val measure: String?,
    val food: String,
    val weight: Double,
    val foodCategory: String?,
    val foodId: String,
    val image: String?
)

data class ApiSubmissionResponse(
    val success: Boolean,
    val message: String,
    val id: String?
)

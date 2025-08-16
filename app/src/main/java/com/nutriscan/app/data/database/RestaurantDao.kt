package com.nutriscan.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.nutriscan.app.data.models.Restaurant

@Dao
interface RestaurantDao {
    
    @Query("SELECT * FROM restaurants")
    fun getAllRestaurantsFlow(): Flow<List<Restaurant>>
    
    @Query("SELECT * FROM restaurants WHERE id = :restaurantId LIMIT 1")
    suspend fun getRestaurantById(restaurantId: String): Restaurant?
    
    @Query("SELECT * FROM restaurants WHERE id = :restaurantId LIMIT 1")
    fun getRestaurantByIdFlow(restaurantId: String): Flow<Restaurant?>
    
    @Query("""
        SELECT * FROM restaurants 
        WHERE name LIKE '%' || :query || '%' 
        OR address LIKE '%' || :query || '%'
        ORDER BY rating DESC, name ASC
    """)
    suspend fun searchRestaurants(query: String): List<Restaurant>
    
    @Query("""
        SELECT * FROM restaurants 
        WHERE cuisine LIKE '%' || :cuisineType || '%'
        ORDER BY rating DESC
    """)
    suspend fun getRestaurantsByCuisine(cuisineType: String): List<Restaurant>
    
    @Query("SELECT * FROM restaurants WHERE hasNutritionInfo = 1 ORDER BY rating DESC")
    suspend fun getRestaurantsWithNutritionInfo(): List<Restaurant>
    
    @Query("SELECT * FROM restaurants WHERE deliveryAvailable = 1 ORDER BY rating DESC")
    suspend fun getRestaurantsWithDelivery(): List<Restaurant>
    
    @Query("SELECT * FROM restaurants WHERE takeoutAvailable = 1 ORDER BY rating DESC")
    suspend fun getRestaurantsWithTakeout(): List<Restaurant>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: Restaurant)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<Restaurant>)
    
    @Update
    suspend fun updateRestaurant(restaurant: Restaurant)
    
    @Delete
    suspend fun deleteRestaurant(restaurant: Restaurant)
    
    @Query("DELETE FROM restaurants WHERE lastUpdated < :cutoffTime")
    suspend fun deleteOldRestaurants(cutoffTime: Long)
    
    @Query("SELECT DISTINCT cuisine FROM restaurants")
    suspend fun getAllCuisineTypes(): List<String>
}

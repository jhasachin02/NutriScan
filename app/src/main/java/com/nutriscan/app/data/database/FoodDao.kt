package com.nutriscan.app.data.database

import androidx.room.*
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.nutriscan.app.data.models.*

@Dao
interface FoodDao {
    
    // Food Items
    @Query("SELECT * FROM food_items")
    fun getAllFoodItems(): List<FoodItem>
    
    @Query("SELECT * FROM food_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getFoodItemByBarcode(barcode: String): FoodItem?
    
    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    suspend fun getFoodItemById(id: String): FoodItem?
    
    @Query("SELECT * FROM food_items WHERE name LIKE :query OR brand LIKE :query ORDER BY updatedAt DESC")
    suspend fun searchFoodItems(query: String): List<FoodItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(foodItems: List<FoodItem>)
    
    @Update
    suspend fun updateFoodItem(foodItem: FoodItem)
    
    @Delete
    suspend fun deleteFoodItem(foodItem: FoodItem)
    
    @Query("DELETE FROM food_items WHERE updatedAt < :cutoffTime")
    suspend fun deleteOldFoodItems(cutoffTime: Long)
    
    // Restaurants
    @Query("SELECT * FROM restaurants")
    suspend fun getAllRestaurants(): List<Restaurant>
    
    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    suspend fun getRestaurantById(id: String): Restaurant?
    
    @Query("""
        SELECT * FROM restaurants 
        WHERE (latitude BETWEEN :minLat AND :maxLat) 
        AND (longitude BETWEEN :minLng AND :maxLng)
        ORDER BY 
        ((latitude - :userLat) * (latitude - :userLat) + 
         (longitude - :userLng) * (longitude - :userLng)) ASC
    """)
    suspend fun getNearbyRestaurants(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double,
        userLat: Double, userLng: Double
    ): List<Restaurant>
    
    @Query("""
        SELECT * FROM restaurants 
        WHERE name LIKE :query 
        OR address LIKE :query
        ORDER BY name ASC
    """)
    suspend fun searchRestaurants(query: String): List<Restaurant>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: Restaurant)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<Restaurant>)
    
    @Update
    suspend fun updateRestaurant(restaurant: Restaurant)
    
    @Delete
    suspend fun deleteRestaurant(restaurant: Restaurant)
    
    // Menu Items
    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
    suspend fun getMenuItemsByRestaurant(restaurantId: String): List<MenuItem>
    
    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemById(id: String): MenuItem?
    
    @Query("""
        SELECT * FROM menu_items 
        WHERE name LIKE :query 
        OR description LIKE :query
        ORDER BY isPopular DESC, name ASC
    """)
    suspend fun searchMenuItems(query: String): List<MenuItem>
    
    @Query("""
        SELECT * FROM menu_items 
        WHERE restaurantId = :restaurantId 
        AND category = :category
        ORDER BY name ASC
    """)
    suspend fun getMenuItemsByCategory(restaurantId: String, category: String): List<MenuItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: MenuItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(menuItems: List<MenuItem>)
    
    @Update
    suspend fun updateMenuItem(menuItem: MenuItem)
    
    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItem)
    
    // Menu Favorites
    @Query("SELECT * FROM menu_favorites WHERE userId = :userId ORDER BY addedAt DESC")
    suspend fun getFavoriteMenuItems(userId: String): List<MenuFavorite>
    
    @Query("SELECT * FROM menu_favorites WHERE userId = :userId AND menuItemId = :menuItemId LIMIT 1")
    suspend fun getFavorite(userId: String, menuItemId: String): MenuFavorite?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteMenuItem(favorite: MenuFavorite)
    
    @Query("DELETE FROM menu_favorites WHERE id = :favoriteId")
    suspend fun deleteFavoriteMenuItem(favoriteId: String)
    
    @Query("DELETE FROM menu_favorites WHERE userId = :userId AND menuItemId = :menuItemId")
    suspend fun removeFavorite(userId: String, menuItemId: String)
    
    // Complex queries for analytics
    @Query("""
        SELECT * FROM food_items 
        WHERE createdAt > :startTime 
        ORDER BY createdAt DESC 
        LIMIT :limit
    """)
    suspend fun getRecentScans(startTime: Long, limit: Int): List<FoodItem>
    
    @Query("SELECT DISTINCT category FROM food_items WHERE category IS NOT NULL")
    suspend fun getAllFoodCategories(): List<String>
    
    @Query("SELECT DISTINCT category FROM menu_items WHERE category IS NOT NULL")
    suspend fun getAllMenuCategories(): List<String>
    
    // Utility methods for the repository pattern helper
    fun getNearbyRestaurants(latitude: Double, longitude: Double, radiusKm: Double): List<Restaurant> {
        val latRange = radiusKm / 111.0 // Rough conversion from km to degrees
        val lngRange = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(latitude)))
        
        return kotlinx.coroutines.runBlocking {
            getNearbyRestaurants(
                latitude - latRange, latitude + latRange,
                longitude - lngRange, longitude + lngRange,
                latitude, longitude
            )
        }
    }
}

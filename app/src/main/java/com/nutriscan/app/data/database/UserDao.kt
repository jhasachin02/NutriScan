package com.nutriscan.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.nutriscan.app.data.models.UserProfile

@Dao
interface UserDao {
    
    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfile?
    
    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    fun getUserProfileFlow(userId: String): Flow<UserProfile?>
    
    @Query("SELECT * FROM user_profiles")
    suspend fun getAllUserProfiles(): List<UserProfile>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
    
    @Update
    suspend fun updateUserProfile(profile: UserProfile)
    
    @Delete
    suspend fun deleteUserProfile(profile: UserProfile)
    
    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteUserProfileById(userId: String)
    
    @Query("UPDATE user_profiles SET weight = :weight, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateUserWeight(userId: String, weight: Double, timestamp: Long)
    
    @Query("UPDATE user_profiles SET dailyCalorieGoal = :calories, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateDailyCalorieGoal(userId: String, calories: Double, timestamp: Long)
    
    @Query("UPDATE user_profiles SET fitnessAppConnected = :connected, fitnessAppType = :appType, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateFitnessAppConnection(userId: String, connected: Boolean, appType: String?, timestamp: Long)
}

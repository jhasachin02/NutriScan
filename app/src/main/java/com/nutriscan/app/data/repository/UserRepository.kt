package com.nutriscan.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nutriscan.app.data.database.UserDao
import com.nutriscan.app.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    suspend fun getCurrentUserProfile(): UserProfile? {
        val userId = getCurrentUserId() ?: return null
        
        // Try local cache first
        val localProfile = userDao.getUserProfile(userId)
        if (localProfile != null) {
            return localProfile
        }
        
        // Fetch from Firebase if not in cache
        return try {
            val document = firestore.collection("user_profiles")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                profile?.let { userDao.insertUserProfile(it) }
                profile
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun getCurrentUserProfileFlow(): Flow<UserProfile?> {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        return userDao.getUserProfileFlow(userId)
    }
    
    suspend fun createOrUpdateUserProfile(profile: UserProfile): Result<UserProfile> {
        return try {
            // Save locally
            userDao.insertUserProfile(profile)
            
            // Sync with Firebase
            firestore.collection("user_profiles")
                .document(profile.id)
                .set(profile)
                .await()
            
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserWeight(weight: Double): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val timestamp = System.currentTimeMillis()
        
        return try {
            // Update locally
            userDao.updateUserWeight(userId, weight, timestamp)
            
            // Update in Firebase
            firestore.collection("user_profiles")
                .document(userId)
                .update(
                    mapOf(
                        "weight" to weight,
                        "updatedAt" to timestamp
                    )
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateDailyCalorieGoal(calories: Double): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val timestamp = System.currentTimeMillis()
        
        return try {
            // Update locally
            userDao.updateDailyCalorieGoal(userId, calories, timestamp)
            
            // Update in Firebase
            firestore.collection("user_profiles")
                .document(userId)
                .update(
                    mapOf(
                        "dailyCalorieGoal" to calories,
                        "updatedAt" to timestamp
                    )
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun connectFitnessApp(appType: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val timestamp = System.currentTimeMillis()
        
        return try {
            // Update locally
            userDao.updateFitnessAppConnection(userId, true, appType, timestamp)
            
            // Update in Firebase
            firestore.collection("user_profiles")
                .document(userId)
                .update(
                    mapOf(
                        "fitnessAppConnected" to true,
                        "fitnessAppType" to appType,
                        "updatedAt" to timestamp
                    )
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun disconnectFitnessApp(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val timestamp = System.currentTimeMillis()
        
        return try {
            // Update locally
            userDao.updateFitnessAppConnection(userId, false, null, timestamp)
            
            // Update in Firebase
            firestore.collection("user_profiles")
                .document(userId)
                .update(
                    mapOf(
                        "fitnessAppConnected" to false,
                        "fitnessAppType" to null,
                        "updatedAt" to timestamp
                    )
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut() {
        try {
            // Clear local data
            getCurrentUserId()?.let { userId ->
                userDao.deleteUserProfileById(userId)
            }
            
            // Sign out from Firebase
            auth.signOut()
        } catch (e: Exception) {
            // Handle error
        }
    }
}

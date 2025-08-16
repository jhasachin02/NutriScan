package com.nutriscan.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val email: String,
    val profileImageUrl: String? = null,
    val age: Int? = null,
    val height: Double? = null, // in cm
    val weight: Double? = null, // in kg
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goals: List<HealthGoal> = emptyList(),
    val dietaryRestrictions: List<DietaryRestriction> = emptyList(),
    val allergens: List<String> = emptyList(),
    val dailyCalorieGoal: Double? = null,
    val dailyProteinGoal: Double? = null,
    val dailyCarbGoal: Double? = null,
    val dailyFatGoal: Double? = null,
    val waterIntakeGoal: Double = 2000.0, // in ml
    val isMetricSystem: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val fitnessAppConnected: Boolean = false,
    val fitnessAppType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
enum class ActivityLevel : Parcelable {
    SEDENTARY,
    LIGHT,
    MODERATE,
    ACTIVE,
    VERY_ACTIVE
}

@Parcelize
enum class HealthGoal : Parcelable {
    WEIGHT_LOSS,
    WEIGHT_GAIN,
    MUSCLE_GAIN,
    MAINTENANCE,
    BETTER_NUTRITION,
    DIABETES_MANAGEMENT,
    HEART_HEALTH
}

@Parcelize
enum class DietaryRestriction : Parcelable {
    VEGETARIAN,
    VEGAN,
    KETO,
    PALEO,
    LOW_CARB,
    LOW_FAT,
    GLUTEN_FREE,
    DAIRY_FREE,
    NUT_FREE,
    KOSHER,
    HALAL,
    LOW_SODIUM
}

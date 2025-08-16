package com.nutriscan.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val nutritionFacts: NutritionFacts,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val servingSize: String? = null,
    val servingsPerContainer: Double? = null,
    val description: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class NutritionFacts(
    val calories: Double = 0.0,
    val totalFat: Double = 0.0,
    val saturatedFat: Double = 0.0,
    val transFat: Double = 0.0,
    val cholesterol: Double = 0.0,
    val sodium: Double = 0.0,
    val totalCarbohydrate: Double = 0.0,
    val dietaryFiber: Double = 0.0,
    val totalSugars: Double = 0.0,
    val addedSugars: Double = 0.0,
    val protein: Double = 0.0,
    val vitaminA: Double = 0.0,
    val vitaminC: Double = 0.0,
    val calcium: Double = 0.0,
    val iron: Double = 0.0,
    val vitaminD: Double = 0.0,
    val potassium: Double = 0.0,
    val magnesium: Double = 0.0,
    val zinc: Double = 0.0
) : Parcelable

package com.nutriscan.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String? = null,
    val website: String? = null,
    val rating: Double? = null,
    val priceLevel: Int? = null, // 1-4 scale
    val cuisine: List<String> = emptyList(),
    val imageUrl: String? = null,
    val isOpen: Boolean = true,
    val openingHours: List<String> = emptyList(),
    val deliveryAvailable: Boolean = false,
    val takeoutAvailable: Boolean = false,
    val hasNutritionInfo: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class RestaurantResponse(
    val restaurants: List<Restaurant>,
    val totalCount: Int,
    val hasMore: Boolean
) : Parcelable

@Parcelize
@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val currency: String = "USD",
    val category: String,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val nutritionFacts: NutritionFacts? = null,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val dietaryTags: List<String> = emptyList(), // vegetarian, vegan, gluten-free, etc.
    val spiceLevel: Int? = null, // 1-5 scale
    val preparationTime: Int? = null, // minutes
    val calories: Double? = null,
    val servingSize: String? = null,
    val isPopular: Boolean = false,
    val customizations: List<MenuCustomization> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class MenuCustomization(
    val id: String,
    val name: String,
    val type: CustomizationType,
    val options: List<CustomizationOption>,
    val isRequired: Boolean = false,
    val maxSelections: Int = 1
) : Parcelable

@Parcelize
enum class CustomizationType : Parcelable {
    SINGLE_SELECT,
    MULTI_SELECT,
    TEXT_INPUT,
    QUANTITY
}

@Parcelize
data class CustomizationOption(
    val id: String,
    val name: String,
    val priceModifier: Double = 0.0,
    val calorieModifier: Double = 0.0,
    val isDefault: Boolean = false
) : Parcelable

@Parcelize
data class MenuResponse(
    val restaurantId: String,
    val restaurantName: String,
    val categories: List<MenuCategory>,
    val lastUpdated: Long
) : Parcelable

@Parcelize
data class MenuCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val items: List<MenuItem>,
    val displayOrder: Int = 0
) : Parcelable

@Parcelize
@Entity(tableName = "menu_favorites")
data class MenuFavorite(
    @PrimaryKey
    val id: String,
    val userId: String,
    val restaurantId: String,
    val menuItemId: String,
    val customizations: List<SelectedCustomization> = emptyList(),
    val notes: String? = null,
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class SelectedCustomization(
    val customizationId: String,
    val selectedOptions: List<String>
) : Parcelable

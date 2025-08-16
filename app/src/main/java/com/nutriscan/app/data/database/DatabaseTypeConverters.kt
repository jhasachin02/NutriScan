package com.nutriscan.app.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nutriscan.app.data.models.*

class DatabaseTypeConverters {
    
    private val gson = Gson()
    
    // String List Converter
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // HealthGoal List Converter
    @TypeConverter
    fun fromHealthGoalList(value: List<HealthGoal>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toHealthGoalList(value: String): List<HealthGoal> {
        val listType = object : TypeToken<List<HealthGoal>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // DietaryRestriction List Converter
    @TypeConverter
    fun fromDietaryRestrictionList(value: List<DietaryRestriction>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toDietaryRestrictionList(value: String): List<DietaryRestriction> {
        val listType = object : TypeToken<List<DietaryRestriction>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // ActivityLevel Converter
    @TypeConverter
    fun fromActivityLevel(value: ActivityLevel): String {
        return value.name
    }
    
    @TypeConverter
    fun toActivityLevel(value: String): ActivityLevel {
        return ActivityLevel.valueOf(value)
    }
    
    // NutritionFacts Converter
    @TypeConverter
    fun fromNutritionFacts(value: NutritionFacts): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toNutritionFacts(value: String): NutritionFacts {
        return gson.fromJson(value, NutritionFacts::class.java)
    }
    
    // MenuCustomization List Converter
    @TypeConverter
    fun fromMenuCustomizationList(value: List<MenuCustomization>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toMenuCustomizationList(value: String): List<MenuCustomization> {
        val listType = object : TypeToken<List<MenuCustomization>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // SelectedCustomization List Converter
    @TypeConverter
    fun fromSelectedCustomizationList(value: List<SelectedCustomization>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toSelectedCustomizationList(value: String): List<SelectedCustomization> {
        val listType = object : TypeToken<List<SelectedCustomization>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}

package com.nutriscan.app.nutrition

import com.nutriscan.app.data.models.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NutritionAnalyzerTest {
    
    private lateinit var nutritionAnalyzer: NutritionAnalyzer
    
    @Before
    fun setup() {
        nutritionAnalyzer = NutritionAnalyzer()
    }
    
    @Test
    fun `calculateDailyGoals should return proper calorie goal for weight loss`() {
        // Given
        val profile = UserProfile(
            id = "test-user",
            displayName = "Test User",
            email = "test@example.com",
            age = 30,
            height = 175.0,
            weight = 80.0,
            activityLevel = ActivityLevel.MODERATE,
            goals = listOf(HealthGoal.WEIGHT_LOSS)
        )
        
        // When
        val dailyGoals = nutritionAnalyzer.calculateDailyGoals(profile)
        
        // Then
        val expectedBMR = 10 * 80 + 6.25 * 175 - 5 * 30 + 5 // 1778.75
        val expectedTDEE = expectedBMR * 1.55 // 2757.06
        val expectedCalories = expectedTDEE * 0.85 // 2333.5 (15% deficit)
        
        assertEquals(expectedCalories, dailyGoals.calories, 0.1)
        assertTrue("Protein goal should be reasonable", dailyGoals.protein > 100)
        assertTrue("Water goal should be reasonable", dailyGoals.water > 2000)
    }
    
    @Test
    fun `calculateDailyGoals should return proper calorie goal for muscle gain`() {
        // Given
        val profile = UserProfile(
            id = "test-user",
            displayName = "Test User",
            email = "test@example.com",
            age = 25,
            height = 180.0,
            weight = 75.0,
            activityLevel = ActivityLevel.ACTIVE,
            goals = listOf(HealthGoal.MUSCLE_GAIN)
        )
        
        // When
        val dailyGoals = nutritionAnalyzer.calculateDailyGoals(profile)
        
        // Then
        val expectedBMR = 10 * 75 + 6.25 * 180 - 5 * 25 + 5 // 1755
        val expectedTDEE = expectedBMR * 1.725 // 3027.375
        val expectedCalories = expectedTDEE * 1.15 // 3481.48 (15% surplus)
        
        assertEquals(expectedCalories, dailyGoals.calories, 0.1)
        assertTrue("Protein goal should be higher for muscle gain", dailyGoals.protein > 200)
    }
    
    @Test
    fun `analyzeAgainstGoals should return proper percentage for calorie intake`() {
        // Given
        val userProfile = UserProfile(
            id = "test-user",
            displayName = "Test User",
            email = "test@example.com",
            dailyCalorieGoal = 2000.0
        )
        
        val foodItem = FoodItem(
            id = "test-food",
            name = "Test Food",
            nutritionFacts = NutritionFacts(calories = 300.0)
        )
        
        // When
        val analysis = nutritionAnalyzer.analyzeAgainstGoals(foodItem, userProfile)
        
        // Then
        assertEquals(15.0, analysis.caloriePercentage, 0.1) // 300/2000 * 100 = 15%
    }
    
    @Test
    fun `analyzeRecipeNutrition should calculate per serving correctly`() {
        // Given
        val ingredients = listOf("chicken breast", "brown rice", "broccoli")
        val servings = 4
        
        // When
        val analysis = nutritionAnalyzer.analyzeRecipeNutrition(ingredients, servings)
        
        // Then
        assertNotNull(analysis.totalNutrition)
        assertNotNull(analysis.perServingNutrition)
        assertEquals(servings, analysis.servings)
        assertTrue("Health score should be between 0 and 100", 
                   analysis.healthScore >= 0 && analysis.healthScore <= 100)
        assertNotNull(analysis.nutritionGrade)
    }
    
    @Test
    fun `generateARInsights should return proper color coding`() {
        // Given
        val userProfile = UserProfile(
            id = "test-user",
            displayName = "Test User",
            email = "test@example.com",
            goals = listOf(HealthGoal.WEIGHT_LOSS)
        )
        
        val healthyFood = FoodItem(
            id = "healthy-food",
            name = "Grilled Chicken Salad",
            nutritionFacts = NutritionFacts(
                calories = 250.0,
                protein = 30.0,
                dietaryFiber = 8.0,
                sodium = 300.0
            )
        )
        
        // When
        val insights = nutritionAnalyzer.generateARInsights(healthyFood, userProfile)
        
        // Then
        assertNotNull(insights.colorCode)
        assertNotNull(insights.primaryMetric)
        assertNotNull(insights.recommendationText)
        assertTrue("Should have visual indicators", insights.visualIndicators.isNotEmpty())
        assertTrue("Should have quick facts", insights.quickFacts.isNotEmpty())
    }
    
    @Test
    fun `nutrition facts should be scaled correctly by quantity`() {
        // Given
        val foodItem = FoodItem(
            id = "test-food",
            name = "Test Food",
            nutritionFacts = NutritionFacts(
                calories = 100.0,
                protein = 10.0,
                totalFat = 5.0
            )
        )
        
        val userProfile = UserProfile(
            id = "test-user",
            displayName = "Test User",
            email = "test@example.com"
        )
        
        val quantity = 2.5
        
        // When
        val analysis = nutritionAnalyzer.analyzeAgainstGoals(foodItem, userProfile, quantity)
        
        // Then
        // The analysis should reflect scaled values
        // This tests that the scaling logic works correctly
        assertTrue("Analysis should account for quantity", analysis.caloriePercentage > 0)
    }
    
    @Test
    fun `dietary restriction analysis should work correctly`() {
        // Given
        val ketoProfile = UserProfile(
            id = "keto-user",
            displayName = "Keto User",
            email = "keto@example.com",
            dietaryRestrictions = listOf(DietaryRestriction.KETO)
        )
        
        // When
        val goals = nutritionAnalyzer.calculateDailyGoals(ketoProfile)
        
        // Then
        assertTrue("Keto diet should have very low carb goal", goals.carbohydrates < 50)
        assertTrue("Keto diet should have high fat goal", goals.fat > goals.protein)
        assertTrue("Keto diet should have high fat goal", goals.fat > goals.carbohydrates)
    }
    
    @Test
    fun `activity level should affect calorie calculation`() {
        // Given
        val baseProfile = UserProfile(
            id = "base-user",
            displayName = "Base User",
            email = "base@example.com",
            age = 30,
            height = 175.0,
            weight = 70.0,
            activityLevel = ActivityLevel.SEDENTARY
        )
        
        val activeProfile = baseProfile.copy(
            id = "active-user",
            activityLevel = ActivityLevel.VERY_ACTIVE
        )
        
        // When
        val sedentaryGoals = nutritionAnalyzer.calculateDailyGoals(baseProfile)
        val activeGoals = nutritionAnalyzer.calculateDailyGoals(activeProfile)
        
        // Then
        assertTrue("Active person should have higher calorie goal", 
                   activeGoals.calories > sedentaryGoals.calories)
        assertTrue("Calorie difference should be significant", 
                   activeGoals.calories - sedentaryGoals.calories > 500)
    }
}

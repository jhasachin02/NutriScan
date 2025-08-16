package com.nutriscan.app.nutrition

import com.nutriscan.app.data.models.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class NutritionAnalyzer @Inject constructor() {

    /**
     * Calculate daily nutrition goals based on user profile
     */
    fun calculateDailyGoals(profile: UserProfile): DailyNutritionGoals {
        val bmr = calculateBasalMetabolicRate(profile)
        val tdee = calculateTotalDailyEnergyExpenditure(bmr, profile.activityLevel)
        
        val adjustedCalories = when {
            profile.goals.contains(HealthGoal.WEIGHT_LOSS) -> tdee * 0.85 // 15% deficit
            profile.goals.contains(HealthGoal.WEIGHT_GAIN) -> tdee * 1.15 // 15% surplus
            else -> tdee
        }
        
        return DailyNutritionGoals(
            calories = adjustedCalories,
            protein = calculateProteinGoal(adjustedCalories, profile),
            carbohydrates = calculateCarbGoal(adjustedCalories, profile),
            fat = calculateFatGoal(adjustedCalories, profile),
            fiber = calculateFiberGoal(profile),
            sodium = 2300.0, // mg - standard recommendation
            sugar = adjustedCalories * 0.10 / 4, // 10% of calories from added sugars
            water = calculateWaterGoal(profile)
        )
    }

    /**
     * Analyze recipe nutrition and provide insights
     */
    fun analyzeRecipeNutrition(
        ingredients: List<String>,
        servings: Int = 1
    ): RecipeNutritionAnalysis {
        val totalNutrition = calculateRecipeNutrition(ingredients)
        val perServingNutrition = totalNutrition.divideByServings(servings)
        
        return RecipeNutritionAnalysis(
            totalNutrition = totalNutrition,
            perServingNutrition = perServingNutrition,
            servings = servings,
            healthScore = calculateHealthScore(perServingNutrition),
            insights = generateNutritionInsights(perServingNutrition),
            dietaryLabels = determineDietaryLabels(ingredients),
            allergenWarnings = detectAllergens(ingredients),
            nutritionGrade = calculateNutritionGrade(perServingNutrition)
        )
    }

    /**
     * Compare food item against user's dietary goals
     */
    fun analyzeAgainstGoals(
        foodItem: FoodItem,
        userProfile: UserProfile,
        quantity: Double = 1.0
    ): GoalAnalysis {
        val dailyGoals = calculateDailyGoals(userProfile)
        val adjustedNutrition = foodItem.nutritionFacts.multiplyByQuantity(quantity)
        
        return GoalAnalysis(
            caloriePercentage = (adjustedNutrition.calories / dailyGoals.calories) * 100,
            proteinPercentage = (adjustedNutrition.protein / dailyGoals.protein) * 100,
            carbPercentage = (adjustedNutrition.totalCarbohydrate / dailyGoals.carbohydrates) * 100,
            fatPercentage = (adjustedNutrition.totalFat / dailyGoals.fat) * 100,
            sodiumPercentage = (adjustedNutrition.sodium / dailyGoals.sodium) * 100,
            fiberPercentage = (adjustedNutrition.dietaryFiber / dailyGoals.fiber) * 100,
            recommendationScore = calculateRecommendationScore(adjustedNutrition, userProfile),
            warnings = generateWarnings(adjustedNutrition, userProfile),
            benefits = generateBenefits(adjustedNutrition, userProfile)
        )
    }

    /**
     * Generate nutrition insights for AR display
     */
    fun generateARInsights(foodItem: FoodItem, userProfile: UserProfile): ARNutritionInsights {
        val analysis = analyzeAgainstGoals(foodItem, userProfile)
        
        return ARNutritionInsights(
            primaryMetric = getPrimaryMetric(analysis, userProfile),
            visualIndicators = generateVisualIndicators(analysis),
            quickFacts = generateQuickFacts(foodItem, analysis),
            colorCode = getColorCodeForFood(analysis),
            recommendationText = getRecommendationText(analysis),
            alternativeSuggestions = generateAlternatives(foodItem, userProfile)
        )
    }

    // Private calculation methods
    private fun calculateBasalMetabolicRate(profile: UserProfile): Double {
        val weight = profile.weight ?: 70.0
        val height = profile.height ?: 170.0
        val age = profile.age ?: 30
        
        // Mifflin-St Jeor Equation (assuming male, add gender field for better accuracy)
        return 10 * weight + 6.25 * height - 5 * age + 5
    }

    private fun calculateTotalDailyEnergyExpenditure(bmr: Double, activityLevel: ActivityLevel): Double {
        val activityMultiplier = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHT -> 1.375
            ActivityLevel.MODERATE -> 1.55
            ActivityLevel.ACTIVE -> 1.725
            ActivityLevel.VERY_ACTIVE -> 1.9
        }
        return bmr * activityMultiplier
    }

    private fun calculateProteinGoal(calories: Double, profile: UserProfile): Double {
        val baseProtein = when {
            profile.goals.contains(HealthGoal.MUSCLE_GAIN) -> calories * 0.30 / 4 // 30% of calories
            profile.goals.contains(HealthGoal.WEIGHT_LOSS) -> calories * 0.25 / 4 // 25% of calories
            else -> calories * 0.20 / 4 // 20% of calories
        }
        return baseProtein
    }

    private fun calculateCarbGoal(calories: Double, profile: UserProfile): Double {
        return when {
            profile.dietaryRestrictions.contains(DietaryRestriction.KETO) -> calories * 0.05 / 4 // 5% for keto
            profile.dietaryRestrictions.contains(DietaryRestriction.LOW_CARB) -> calories * 0.20 / 4 // 20% for low carb
            else -> calories * 0.50 / 4 // 50% for standard diet
        }
    }

    private fun calculateFatGoal(calories: Double, profile: UserProfile): Double {
        return when {
            profile.dietaryRestrictions.contains(DietaryRestriction.KETO) -> calories * 0.70 / 9 // 70% for keto
            profile.dietaryRestrictions.contains(DietaryRestriction.LOW_FAT) -> calories * 0.20 / 9 // 20% for low fat
            else -> calories * 0.30 / 9 // 30% for standard diet
        }
    }

    private fun calculateFiberGoal(profile: UserProfile): Double {
        // Age-based fiber recommendations (assuming adult)
        return 25.0 // grams per day for average adult
    }

    private fun calculateWaterGoal(profile: UserProfile): Double {
        val weight = profile.weight ?: 70.0
        return weight * 35 // 35ml per kg of body weight
    }

    private fun calculateRecipeNutrition(ingredients: List<String>): NutritionFacts {
        // This would integrate with nutrition databases to calculate total nutrition
        // For now, return a placeholder
        return NutritionFacts()
    }

    private fun calculateHealthScore(nutrition: NutritionFacts): Double {
        var score = 50.0 // Base score
        
        // Positive factors
        if (nutrition.protein > 10) score += 10
        if (nutrition.dietaryFiber > 5) score += 15
        if (nutrition.vitaminC > 0) score += 5
        if (nutrition.calcium > 0) score += 5
        
        // Negative factors
        if (nutrition.saturatedFat > 5) score -= 10
        if (nutrition.sodium > 400) score -= 15
        if (nutrition.totalSugars > 15) score -= 10
        if (nutrition.transFat > 0) score -= 20
        
        return score.coerceIn(0.0, 100.0)
    }

    private fun generateNutritionInsights(nutrition: NutritionFacts): List<String> {
        val insights = mutableListOf<String>()
        
        if (nutrition.protein > 15) insights.add("High in protein - great for muscle building")
        if (nutrition.dietaryFiber > 8) insights.add("Excellent source of fiber")
        if (nutrition.sodium > 600) insights.add("High sodium content - consume in moderation")
        if (nutrition.saturatedFat > 8) insights.add("High in saturated fat")
        if (nutrition.vitaminC > 15) insights.add("Good source of Vitamin C")
        
        return insights
    }

    private fun determineDietaryLabels(ingredients: List<String>): List<String> {
        val labels = mutableListOf<String>()
        val ingredientText = ingredients.joinToString(" ").lowercase()
        
        if (!ingredientText.contains("meat") && !ingredientText.contains("fish") && 
            !ingredientText.contains("chicken") && !ingredientText.contains("beef")) {
            labels.add("Vegetarian")
        }
        
        if (!ingredientText.contains("dairy") && !ingredientText.contains("milk") &&
            !ingredientText.contains("cheese") && !ingredientText.contains("butter") &&
            !ingredientText.contains("egg")) {
            labels.add("Vegan")
        }
        
        if (!ingredientText.contains("gluten") && !ingredientText.contains("wheat") &&
            !ingredientText.contains("rye") && !ingredientText.contains("barley")) {
            labels.add("Gluten-Free")
        }
        
        return labels
    }

    private fun detectAllergens(ingredients: List<String>): List<String> {
        val allergens = mutableListOf<String>()
        val ingredientText = ingredients.joinToString(" ").lowercase()
        
        if (ingredientText.contains("nuts") || ingredientText.contains("peanut")) allergens.add("Tree Nuts/Peanuts")
        if (ingredientText.contains("dairy") || ingredientText.contains("milk")) allergens.add("Dairy")
        if (ingredientText.contains("egg")) allergens.add("Eggs")
        if (ingredientText.contains("soy")) allergens.add("Soy")
        if (ingredientText.contains("wheat") || ingredientText.contains("gluten")) allergens.add("Wheat/Gluten")
        if (ingredientText.contains("fish") || ingredientText.contains("shellfish")) allergens.add("Fish/Shellfish")
        
        return allergens
    }

    private fun calculateNutritionGrade(nutrition: NutritionFacts): String {
        val score = calculateHealthScore(nutrition)
        return when {
            score >= 85 -> "A+"
            score >= 80 -> "A"
            score >= 75 -> "B+"
            score >= 70 -> "B"
            score >= 65 -> "C+"
            score >= 60 -> "C"
            score >= 50 -> "D"
            else -> "F"
        }
    }

    private fun calculateRecommendationScore(nutrition: NutritionFacts, profile: UserProfile): Double {
        var score = 50.0
        
        // Adjust based on user goals
        profile.goals.forEach { goal ->
            when (goal) {
                HealthGoal.WEIGHT_LOSS -> {
                    if (nutrition.calories < 300) score += 20
                    if (nutrition.protein > 10) score += 15
                    if (nutrition.dietaryFiber > 5) score += 10
                }
                HealthGoal.MUSCLE_GAIN -> {
                    if (nutrition.protein > 20) score += 25
                    if (nutrition.calories > 400) score += 10
                }
                HealthGoal.HEART_HEALTH -> {
                    if (nutrition.saturatedFat < 2) score += 15
                    if (nutrition.sodium < 200) score += 15
                    if (nutrition.dietaryFiber > 5) score += 10
                }
                else -> { /* No specific adjustments */ }
            }
        }
        
        return score.coerceIn(0.0, 100.0)
    }

    private fun generateWarnings(nutrition: NutritionFacts, profile: UserProfile): List<String> {
        val warnings = mutableListOf<String>()
        
        if (nutrition.sodium > 600) warnings.add("High sodium content")
        if (nutrition.saturatedFat > 8) warnings.add("High in saturated fat")
        if (nutrition.totalSugars > 20) warnings.add("High sugar content")
        if (nutrition.transFat > 0) warnings.add("Contains trans fats")
        
        // Profile-specific warnings
        if (profile.goals.contains(HealthGoal.WEIGHT_LOSS) && nutrition.calories > 500) {
            warnings.add("High calorie content for weight loss goal")
        }
        
        return warnings
    }

    private fun generateBenefits(nutrition: NutritionFacts, profile: UserProfile): List<String> {
        val benefits = mutableListOf<String>()
        
        if (nutrition.protein > 15) benefits.add("Excellent protein source")
        if (nutrition.dietaryFiber > 8) benefits.add("High fiber content")
        if (nutrition.vitaminC > 15) benefits.add("Rich in Vitamin C")
        if (nutrition.calcium > 200) benefits.add("Good source of calcium")
        if (nutrition.iron > 3) benefits.add("Contains iron")
        
        return benefits
    }

    private fun getPrimaryMetric(analysis: GoalAnalysis, profile: UserProfile): String {
        return when {
            profile.goals.contains(HealthGoal.WEIGHT_LOSS) -> "Calories: ${analysis.caloriePercentage.roundToInt()}% of daily goal"
            profile.goals.contains(HealthGoal.MUSCLE_GAIN) -> "Protein: ${analysis.proteinPercentage.roundToInt()}% of daily goal"
            else -> "Calories: ${analysis.caloriePercentage.roundToInt()}% of daily goal"
        }
    }

    private fun generateVisualIndicators(analysis: GoalAnalysis): List<VisualIndicator> {
        return listOf(
            VisualIndicator("Calories", analysis.caloriePercentage, getColorForPercentage(analysis.caloriePercentage)),
            VisualIndicator("Protein", analysis.proteinPercentage, getColorForPercentage(analysis.proteinPercentage)),
            VisualIndicator("Sodium", analysis.sodiumPercentage, getColorForSodium(analysis.sodiumPercentage))
        )
    }

    private fun generateQuickFacts(foodItem: FoodItem, analysis: GoalAnalysis): List<String> {
        return listOf(
            "${foodItem.nutritionFacts.calories.roundToInt()} calories",
            "${foodItem.nutritionFacts.protein.roundToInt()}g protein",
            "${foodItem.nutritionFacts.totalCarbohydrate.roundToInt()}g carbs",
            "${foodItem.nutritionFacts.totalFat.roundToInt()}g fat"
        )
    }

    private fun getColorCodeForFood(analysis: GoalAnalysis): String {
        return when {
            analysis.recommendationScore >= 80 -> "#4CAF50" // Green
            analysis.recommendationScore >= 60 -> "#FF9800" // Orange
            else -> "#F44336" // Red
        }
    }

    private fun getRecommendationText(analysis: GoalAnalysis): String {
        return when {
            analysis.recommendationScore >= 80 -> "Great choice for your goals!"
            analysis.recommendationScore >= 60 -> "Okay choice, consume in moderation"
            else -> "Consider a healthier alternative"
        }
    }

    private fun generateAlternatives(foodItem: FoodItem, profile: UserProfile): List<String> {
        // This would use ML/AI to suggest alternatives based on the food item and user profile
        return listOf(
            "Try grilled instead of fried",
            "Consider a smaller portion size",
            "Add vegetables for more nutrients"
        )
    }

    private fun getColorForPercentage(percentage: Double): String {
        return when {
            percentage <= 25 -> "#4CAF50" // Green
            percentage <= 50 -> "#8BC34A" // Light green
            percentage <= 75 -> "#FF9800" // Orange
            else -> "#F44336" // Red
        }
    }

    private fun getColorForSodium(percentage: Double): String {
        return when {
            percentage <= 15 -> "#4CAF50" // Green
            percentage <= 30 -> "#FF9800" // Orange
            else -> "#F44336" // Red
        }
    }
}

// Extension functions
private fun NutritionFacts.divideByServings(servings: Int): NutritionFacts {
    return this.copy(
        calories = calories / servings,
        totalFat = totalFat / servings,
        saturatedFat = saturatedFat / servings,
        cholesterol = cholesterol / servings,
        sodium = sodium / servings,
        totalCarbohydrate = totalCarbohydrate / servings,
        dietaryFiber = dietaryFiber / servings,
        totalSugars = totalSugars / servings,
        protein = protein / servings
    )
}

private fun NutritionFacts.multiplyByQuantity(quantity: Double): NutritionFacts {
    return this.copy(
        calories = calories * quantity,
        totalFat = totalFat * quantity,
        saturatedFat = saturatedFat * quantity,
        cholesterol = cholesterol * quantity,
        sodium = sodium * quantity,
        totalCarbohydrate = totalCarbohydrate * quantity,
        dietaryFiber = dietaryFiber * quantity,
        totalSugars = totalSugars * quantity,
        protein = protein * quantity
    )
}

package com.nutriscan.app.nutrition

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyNutritionGoals(
    val calories: Double,
    val protein: Double,
    val carbohydrates: Double,
    val fat: Double,
    val fiber: Double,
    val sodium: Double,
    val sugar: Double,
    val water: Double
) : Parcelable

@Parcelize
data class RecipeNutritionAnalysis(
    val totalNutrition: com.nutriscan.app.data.models.NutritionFacts,
    val perServingNutrition: com.nutriscan.app.data.models.NutritionFacts,
    val servings: Int,
    val healthScore: Double,
    val insights: List<String>,
    val dietaryLabels: List<String>,
    val allergenWarnings: List<String>,
    val nutritionGrade: String
) : Parcelable

@Parcelize
data class GoalAnalysis(
    val caloriePercentage: Double,
    val proteinPercentage: Double,
    val carbPercentage: Double,
    val fatPercentage: Double,
    val sodiumPercentage: Double,
    val fiberPercentage: Double,
    val recommendationScore: Double,
    val warnings: List<String>,
    val benefits: List<String>
) : Parcelable

@Parcelize
data class ARNutritionInsights(
    val primaryMetric: String,
    val visualIndicators: List<VisualIndicator>,
    val quickFacts: List<String>,
    val colorCode: String,
    val recommendationText: String,
    val alternativeSuggestions: List<String>
) : Parcelable

@Parcelize
data class VisualIndicator(
    val name: String,
    val percentage: Double,
    val color: String
) : Parcelable

@Parcelize
data class NutrientBreakdown(
    val macronutrients: MacronutrientBreakdown,
    val micronutrients: MicronutrientBreakdown,
    val dailyValuePercentages: Map<String, Double>
) : Parcelable

@Parcelize
data class MacronutrientBreakdown(
    val proteinPercentage: Double,
    val carbPercentage: Double,
    val fatPercentage: Double
) : Parcelable

@Parcelize
data class MicronutrientBreakdown(
    val vitamins: Map<String, Double>,
    val minerals: Map<String, Double>
) : Parcelable

@Parcelize
data class NutritionRecommendation(
    val score: Double,
    val level: RecommendationLevel,
    val reasons: List<String>,
    val improvements: List<String>,
    val alternatives: List<AlternativeFood>
) : Parcelable

@Parcelize
enum class RecommendationLevel : Parcelable {
    EXCELLENT,
    GOOD,
    MODERATE,
    POOR,
    AVOID
}

@Parcelize
data class AlternativeFood(
    val name: String,
    val reason: String,
    val improvementPoints: List<String>
) : Parcelable

@Parcelize
data class DailyNutritionSummary(
    val date: String,
    val totalCalories: Double,
    val goalCalories: Double,
    val macroBreakdown: MacronutrientBreakdown,
    val goalsMet: List<String>,
    val goalsToImprove: List<String>,
    val topFoods: List<String>,
    val nutritionScore: Double
) : Parcelable

@Parcelize
data class WeeklyNutritionTrends(
    val averageCalories: Double,
    val averageProtein: Double,
    val averageCarbs: Double,
    val averageFat: Double,
    val trendDirection: TrendDirection,
    val improvements: List<String>,
    val achievements: List<String>
) : Parcelable

@Parcelize
enum class TrendDirection : Parcelable {
    IMPROVING,
    STABLE,
    DECLINING
}

@Parcelize
data class FoodCompatibilityAnalysis(
    val compatibilityScore: Double,
    val dietaryRestrictionViolations: List<String>,
    val allergenConflicts: List<String>,
    val goalAlignment: String,
    val recommendations: List<String>
) : Parcelable

@Parcelize
data class PortionRecommendation(
    val recommendedServing: Double,
    val unit: String,
    val reason: String,
    val calorieImpact: Double,
    val nutritionImpact: String
) : Parcelable

@Parcelize
data class TimingRecommendation(
    val optimalMealTimes: List<String>,
    val reasonForTiming: String,
    val metabolicBenefits: List<String>
) : Parcelable

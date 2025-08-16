package com.nutriscan.app.fitness

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessIntegrationService @Inject constructor() {
    
    companion object {
        private val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
            .build()
    }
    
    fun isGoogleFitAvailable(context: Context): Boolean {
        return GoogleSignIn.hasPermissions(
            GoogleSignIn.getLastSignedInAccount(context),
            fitnessOptions
        )
    }
    
    suspend fun getTodaySteps(context: Context): Result<Int> {
        return try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)
            
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val response = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()
            
            var totalSteps = 0
            for (dataSet in response.dataSets) {
                for (dp in dataSet.dataPoints) {
                    for (field in dp.dataType.fields) {
                        if (field == Field.FIELD_STEPS) {
                            totalSteps += dp.getValue(field).asInt()
                        }
                    }
                }
            }
            
            Result.success(totalSteps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTodayCaloriesBurned(context: Context): Result<Float> {
        return try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)
            
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val response = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()
            
            var totalCalories = 0f
            for (dataSet in response.dataSets) {
                for (dp in dataSet.dataPoints) {
                    for (field in dp.dataType.fields) {
                        if (field == Field.FIELD_CALORIES) {
                            totalCalories += dp.getValue(field).asFloat()
                        }
                    }
                }
            }
            
            Result.success(totalCalories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRecentWeight(context: Context): Result<Float?> {
        return try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(30) // Last 30 days
            
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val response = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()
            
            var latestWeight: Float? = null
            var latestTime = 0L
            
            for (dataSet in response.dataSets) {
                for (dp in dataSet.dataPoints) {
                    if (dp.getTimestamp(TimeUnit.MILLISECONDS) > latestTime) {
                        for (field in dp.dataType.fields) {
                            if (field == Field.FIELD_WEIGHT) {
                                latestWeight = dp.getValue(field).asFloat()
                                latestTime = dp.getTimestamp(TimeUnit.MILLISECONDS)
                            }
                        }
                    }
                }
            }
            
            Result.success(latestWeight)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWeeklyActivitySummary(context: Context): Result<WeeklyActivitySummary> {
        return try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(7)
            
            // Get steps
            val stepsRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val stepsResponse = Fitness.getHistoryClient(context, account)
                .readData(stepsRequest)
                .await()
            
            // Get calories
            val caloriesRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val caloriesResponse = Fitness.getHistoryClient(context, account)
                .readData(caloriesRequest)
                .await()
            
            var totalSteps = 0
            var totalCalories = 0f
            val dailyData = mutableListOf<DailyActivity>()
            
            // Process steps data
            for (bucket in stepsResponse.buckets) {
                var daySteps = 0
                for (dataSet in bucket.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        for (field in dp.dataType.fields) {
                            if (field == Field.FIELD_STEPS) {
                                daySteps = dp.getValue(field).asInt()
                                totalSteps += daySteps
                            }
                        }
                    }
                }
                
                if (daySteps > 0) {
                    dailyData.add(
                        DailyActivity(
                            date = bucket.getStartTime(TimeUnit.MILLISECONDS),
                            steps = daySteps,
                            calories = 0f // Will be filled from calories response
                        )
                    )
                }
            }
            
            // Process calories data and merge with steps
            var dayIndex = 0
            for (bucket in caloriesResponse.buckets) {
                var dayCalories = 0f
                for (dataSet in bucket.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        for (field in dp.dataType.fields) {
                            if (field == Field.FIELD_CALORIES) {
                                dayCalories = dp.getValue(field).asFloat()
                                totalCalories += dayCalories
                            }
                        }
                    }
                }
                
                if (dayIndex < dailyData.size) {
                    dailyData[dayIndex] = dailyData[dayIndex].copy(calories = dayCalories)
                    dayIndex++
                }
            }
            
            val summary = WeeklyActivitySummary(
                totalSteps = totalSteps,
                totalCalories = totalCalories,
                averageStepsPerDay = totalSteps / 7,
                averageCaloriesPerDay = totalCalories / 7f,
                dailyActivities = dailyData
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun writeCalorieData(context: Context, calories: Float, timestamp: Long): Result<Unit> {
        return try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            val dataSource = DataSource.Builder()
                .setAppPackageName(context.packageName)
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setStreamName("NutriScan - Calories")
                .setType(DataSource.TYPE_RAW)
                .build()
            
            val dataPoint = DataPoint.builder(dataSource)
                .setField(Field.FIELD_CALORIES, calories)
                .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
                .build()
            
            val dataSet = DataSet.builder(dataSource)
                .add(dataPoint)
                .build()
            
            Fitness.getHistoryClient(context, account)
                .insertData(dataSet)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class WeeklyActivitySummary(
    val totalSteps: Int,
    val totalCalories: Float,
    val averageStepsPerDay: Int,
    val averageCaloriesPerDay: Float,
    val dailyActivities: List<DailyActivity>
)

data class DailyActivity(
    val date: Long,
    val steps: Int,
    val calories: Float
)

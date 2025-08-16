package com.nutriscan.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.nutriscan.app.data.models.*

@Database(
    entities = [
        FoodItem::class,
        UserProfile::class,
        Restaurant::class,
        MenuItem::class,
        MenuFavorite::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class NutriScanDatabase : RoomDatabase() {
    
    abstract fun foodDao(): FoodDao
    abstract fun userDao(): UserDao
    abstract fun restaurantDao(): RestaurantDao
    
    companion object {
        @Volatile
        private var INSTANCE: NutriScanDatabase? = null
        
        fun getDatabase(context: Context): NutriScanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriScanDatabase::class.java,
                    "nutriscan_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

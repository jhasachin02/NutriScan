package com.nutriscan.app.di

import android.content.Context
import androidx.room.Room
import com.nutriscan.app.data.database.NutriScanDatabase
import com.nutriscan.app.data.database.FoodDao
import com.nutriscan.app.data.database.UserDao
import com.nutriscan.app.data.database.RestaurantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNutriScanDatabase(@ApplicationContext context: Context): NutriScanDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            NutriScanDatabase::class.java,
            "nutriscan_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideFoodDao(database: NutriScanDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideUserDao(database: NutriScanDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideRestaurantDao(database: NutriScanDatabase): RestaurantDao {
        return database.restaurantDao()
    }
}

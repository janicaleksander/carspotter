package com.example.carspotter.database

import android.content.Context
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.build(context)
    }

    @Provides
    fun provideCategoryDao(db: AppDatabase) = db.categoryDao()

    @Provides
    fun provideBrandDao(db: AppDatabase) = db.brandDao()

    @Provides
    fun provideCarDao(db: AppDatabase) = db.carDao()

    @Provides
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    fun provideUserCarDao(db: AppDatabase) = db.userCarDao()

    @Provides
    fun provideMediaDao(db: AppDatabase) = db.mediaDao()

    @Provides
    fun provideSettingDao(db: AppDatabase) = db.settingDao()

    @Provides
    fun provideFavouriteDao(db: AppDatabase) = db.favouriteDao()

    @Provides
    fun provideCarDetailDao(db: AppDatabase) = db.carDetailsDao()


}
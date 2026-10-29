package com.vaultx.core.database.di

import android.content.Context
import androidx.room.Room
import com.vaultx.core.database.VaultXDatabase
import com.vaultx.core.database.dao.CollectibleDao
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
    fun provideVaultXDatabase(
        @ApplicationContext context: Context
    ): VaultXDatabase {
        return Room.databaseBuilder(
            context,
            VaultXDatabase::class.java,
            VaultXDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideCollectibleDao(database: VaultXDatabase): CollectibleDao {
        return database.collectibleDao()
    }
}

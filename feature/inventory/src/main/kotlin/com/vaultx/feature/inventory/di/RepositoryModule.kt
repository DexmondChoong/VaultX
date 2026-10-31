package com.vaultx.feature.inventory.di

import com.vaultx.feature.inventory.data.repository.CollectibleRepositoryImpl
import com.vaultx.feature.inventory.domain.repository.CollectibleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCollectibleRepository(
        impl: CollectibleRepositoryImpl,
    ): CollectibleRepository
}

package com.hitechs.janitor.di

import com.hitechs.janitor.domain.CalculateTripsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JanitorModule {
    @Provides
    @Singleton
    fun provideCalculateTripsUseCase(): CalculateTripsUseCase {
        return CalculateTripsUseCase()
    }
}
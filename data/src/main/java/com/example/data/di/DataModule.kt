package com.example.data.di

import com.example.data.cart.InMemoryCartRepository
import com.example.data.promo.LocalPromoCodeRepository
import com.example.data.time.SystemClockProvider
import com.example.domain.repository.CartRepository
import com.example.domain.repository.PromoCodeRepository
import com.example.domain.time.ClockProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Привязки источников данных :data к интерфейсам :domain. */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePromoCodeRepository(): PromoCodeRepository = LocalPromoCodeRepository()

    @Provides
    @Singleton
    fun provideCartRepository(): CartRepository = InMemoryCartRepository()

    @Provides
    @Singleton
    fun provideClockProvider(): ClockProvider = SystemClockProvider()
}

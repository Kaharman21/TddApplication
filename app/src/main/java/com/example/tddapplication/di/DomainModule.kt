package com.example.tddapplication.di

import com.example.domain.calculation.CartValidator
import com.example.domain.calculation.PromoCodeResolver
import com.example.domain.calculation.PromoDiscountCalculator
import com.example.domain.calculation.ShippingPolicy
import com.example.domain.calculation.SubtotalCalculator
import com.example.domain.calculation.ThresholdShippingPolicy
import com.example.domain.repository.PromoCodeRepository
import com.example.domain.time.ClockProvider
import com.example.domain.usecase.CalculateBasketUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Сборка Hilt-графа чистых domain-компонентов расчёта. */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun provideCartValidator(): CartValidator = CartValidator()

    @Provides
    fun provideSubtotalCalculator(): SubtotalCalculator = SubtotalCalculator()

    @Provides
    fun providePromoDiscountCalculator(): PromoDiscountCalculator = PromoDiscountCalculator()

    @Provides
    fun provideShippingPolicy(): ShippingPolicy = ThresholdShippingPolicy()

    @Provides
    fun providePromoCodeResolver(
        repository: PromoCodeRepository,
        clockProvider: ClockProvider,
    ): PromoCodeResolver = PromoCodeResolver(repository, clockProvider)

    @Provides
    fun provideCalculateBasketUseCase(
        cartValidator: CartValidator,
        subtotalCalculator: SubtotalCalculator,
        promoCodeResolver: PromoCodeResolver,
        promoDiscountCalculator: PromoDiscountCalculator,
        shippingPolicy: ShippingPolicy,
    ): CalculateBasketUseCase = CalculateBasketUseCase(
        cartValidator,
        subtotalCalculator,
        promoCodeResolver,
        promoDiscountCalculator,
        shippingPolicy,
    )
}

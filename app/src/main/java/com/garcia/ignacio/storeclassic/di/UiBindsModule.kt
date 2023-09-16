package com.garcia.ignacio.storeclassic.di

import com.garcia.ignacio.storeclassic.domain.checkout.CheckoutDataComputer
import com.garcia.ignacio.storeclassic.domain.checkout.StoreCheckoutDataComputer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
interface UiBindsModule {
    @Binds
    fun bindCheckoutDataComputer(computer: StoreCheckoutDataComputer): CheckoutDataComputer
}

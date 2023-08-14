package com.garcia.ignacio.storeclassic.di

import com.garcia.ignacio.storeclassic.domain.models.Product
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Suppress("unused")
@Module
@InstallIn(ActivityRetainedComponent::class)
class UiModule {
    @Provides
    @ActivityRetainedScoped
    fun provideCart(): MutableList<Product> = mutableListOf()
}
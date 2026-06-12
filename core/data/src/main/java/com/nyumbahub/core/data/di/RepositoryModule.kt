package com.nyumbahub.core.data.di

import com.nyumbahub.core.data.repository.AuthRepositoryImpl
import com.nyumbahub.core.data.repository.ListingRepositoryImpl
import com.nyumbahub.core.domain.repository.AuthRepository
import com.nyumbahub.core.domain.repository.ListingRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindListingRepository(impl: ListingRepositoryImpl): ListingRepository
}
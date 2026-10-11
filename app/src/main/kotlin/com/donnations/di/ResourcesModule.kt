package com.donnations.di

import com.donnations.core.base.StringResolver
import com.donnations.resources.AndroidStringResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ResourcesModule {
    @Binds
    fun bindStringResolver(impl: AndroidStringResolver): StringResolver
}

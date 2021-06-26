package com.thenativecitizens.onlinewallpapereditoradmin.di

import com.thenativecitizens.onlinewallpapereditoradmin.data.FirebaseRepo
import com.thenativecitizens.onlinewallpapereditoradmin.data.FirebaseRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@InstallIn(ViewModelComponent::class)
@Module
abstract class RepositoryModule {

    @ViewModelScoped
    @Binds
    abstract fun bindRepository(firebaseRepoImpl: FirebaseRepoImpl): FirebaseRepo
}
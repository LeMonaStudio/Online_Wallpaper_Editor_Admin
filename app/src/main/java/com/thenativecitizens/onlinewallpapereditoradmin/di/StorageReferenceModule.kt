package com.thenativecitizens.onlinewallpapereditoradmin.di

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped



@InstallIn(ViewModelComponent::class)
@Module 
object StorageReferenceModule {

    @ViewModelScoped
    @Provides
    fun provideStorageReference(): StorageReference{
        return Firebase.storage.reference
    }
}
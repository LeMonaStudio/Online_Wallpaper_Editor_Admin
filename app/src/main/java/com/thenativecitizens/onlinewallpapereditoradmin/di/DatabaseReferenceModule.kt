package com.thenativecitizens.onlinewallpapereditoradmin.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object DatabaseReferenceModule {

    @Singleton
    @Provides
    fun provideDatabaseReference(): DatabaseReference{
        return Firebase.database.reference
    }
}
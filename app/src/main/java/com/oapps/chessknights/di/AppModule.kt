package com.oapps.chessknights.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.Realm
import javax.inject.Singleton
import io.realm.RealmConfiguration


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRealmDatabase(@ApplicationContext context: Context): Realm {
        Realm.init(context)
        val config = RealmConfiguration.Builder()
            .name("db.realm")
            .schemaVersion(1)
            .compactOnLaunch()
            .build()

        Realm.setDefaultConfiguration(config)

        return Realm.getInstance(config)
    }
}
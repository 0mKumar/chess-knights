package com.oapps.chessknights

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import io.realm.RealmConfiguration

@HiltAndroidApp
class MyApplication: Application(){

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name("db.realm")
            .schemaVersion(1)
            .compactOnLaunch()
            .build()

        Realm.setDefaultConfiguration(config)
    }
}
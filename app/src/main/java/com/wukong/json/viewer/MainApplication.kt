package com.wukong.json.viewer

import com.android.wukong.viewer.WuKongApi

class MainApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()

        WuKongApi.application = this
        WuKongApi.topActivity = { MainAppLifeCycle.instance.currentActivity }
        MainAppLifeCycle.instance.application = this

    }
}
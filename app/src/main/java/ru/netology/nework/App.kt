package ru.netology.nework

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import ru.netology.nework.BuildConfig


@HiltAndroidApp
class App : Application(){
init {
    MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
}
}
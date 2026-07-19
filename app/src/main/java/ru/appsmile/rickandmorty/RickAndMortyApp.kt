package ru.appsmile.rickandmorty

import android.app.Application
import ru.appsmile.rickandmorty.data.FavoritesRepository

class RickAndMortyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FavoritesRepository.init(this)
    }
}

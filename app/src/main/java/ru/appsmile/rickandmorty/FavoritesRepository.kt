package ru.appsmile.rickandmorty.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.appsmile.rickandmorty.model.ResultItem

/**
 * Хранит избранных персонажей целиком (а не только id), поэтому экран "Избранное"
 * может показать список без единого сетевого запроса — даже офлайн.
 *
 * Инициализируется один раз в [ru.appsmile.rickandmorty.RickAndMortyApp], поэтому
 * MainActivity, DetailActivity и FavoritesActivity видят одно и то же состояние.
 */
object FavoritesRepository {

    private const val PREFS_NAME = "rick_and_morty_favorites"
    private const val KEY_FAVORITES = "favorites_json"

    private val gson = Gson()
    private lateinit var prefs: SharedPreferences

    private val _favorites = MutableStateFlow<List<ResultItem>>(emptyList())
    val favorites: StateFlow<List<ResultItem>> = _favorites.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _favorites.value = loadFromDisk()
    }

    fun isFavorite(characterId: Int): Boolean =
        _favorites.value.any { it.id == characterId }

    fun toggleFavorite(character: ResultItem) {
        val current = _favorites.value
        val updated = if (current.any { it.id == character.id }) {
            current.filterNot { it.id == character.id }
        } else {
            current + character
        }
        _favorites.value = updated
        persistToDisk(updated)
    }

    private fun loadFromDisk(): List<ResultItem> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ResultItem>>() {}.type
            gson.fromJson<List<ResultItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistToDisk(list: List<ResultItem>) {
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(list)).apply()
    }
}

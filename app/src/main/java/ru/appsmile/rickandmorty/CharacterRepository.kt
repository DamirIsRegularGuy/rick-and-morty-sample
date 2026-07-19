package ru.appsmile.rickandmorty.network

import ru.appsmile.rickandmorty.model.CharacterResponse
import ru.appsmile.rickandmorty.model.EpisodeItem

class CharacterRepository(
    private val api: RickAndMortyApiService = RetrofitApi.service
) {
    suspend fun getCharacters(page: Int, query: String? = null, status: String? = null): CharacterResponse =
        api.getCharacters(
            page = page,
            name = query?.trim()?.takeIf { it.isNotBlank() },
            status = status
        )

    /**
     * [episodeUrls] — значения поля ResultItem.episode, вида
     * "https://rickandmortyapi.com/api/episode/1". Извлекаем id из конца URL.
     * Для одного id API отдаёт одиночный объект, для нескольких — массив,
     * поэтому выбираем нужный endpoint в зависимости от количества.
     */
    suspend fun getEpisodes(episodeUrls: List<String>): List<EpisodeItem> {
        val ids = episodeUrls.mapNotNull { it.trimEnd('/').substringAfterLast("/").toIntOrNull() }
        if (ids.isEmpty()) return emptyList()

        return if (ids.size == 1) {
            listOf(api.getEpisode(ids.first()))
        } else {
            api.getEpisodes(ids.joinToString(","))
        }
    }
}
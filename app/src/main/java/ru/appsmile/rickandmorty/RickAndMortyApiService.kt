package ru.appsmile.rickandmorty.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.appsmile.rickandmorty.model.CharacterResponse
import ru.appsmile.rickandmorty.model.EpisodeItem

interface RickAndMortyApiService {

    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("status") status: String? = null
    ): CharacterResponse

    // Один id -> API отдаёт одиночный объект (не массив).
    @GET("episode/{id}")
    suspend fun getEpisode(@Path("id") id: Int): EpisodeItem

    // Несколько id через запятую -> API отдаёт массив.
    @GET("episode/{ids}")
    suspend fun getEpisodes(@Path("ids") ids: String): List<EpisodeItem>
}
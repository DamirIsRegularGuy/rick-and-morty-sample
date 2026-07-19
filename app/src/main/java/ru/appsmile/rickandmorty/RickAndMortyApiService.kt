package ru.appsmile.rickandmorty.network

import retrofit2.http.GET
import retrofit2.http.Query
import ru.appsmile.rickandmorty.model.CharacterResponse

interface RickAndMortyApiService {

    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("status") status: String? = null
    ): CharacterResponse
}

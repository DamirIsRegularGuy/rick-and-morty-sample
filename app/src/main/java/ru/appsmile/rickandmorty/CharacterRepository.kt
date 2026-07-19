package ru.appsmile.rickandmorty.network

import ru.appsmile.rickandmorty.model.CharacterResponse

class CharacterRepository(
    private val api: RickAndMortyApiService = RetrofitApi.service
) {
    suspend fun getCharacters(page: Int, query: String? = null, status: String? = null): CharacterResponse =
        api.getCharacters(
            page = page,
            name = query?.trim()?.takeIf { it.isNotBlank() },
            status = status
        )
}

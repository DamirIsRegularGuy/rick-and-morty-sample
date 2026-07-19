package ru.appsmile.rickandmorty.adapter

import ru.appsmile.rickandmorty.model.ResultItem

data class CharacterListItem(
    val character: ResultItem,
    val isFavorite: Boolean
)

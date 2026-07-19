package ru.appsmile.rickandmorty.ui.episodes

data class EpisodesUiState(
    val rows: List<EpisodeListRow> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
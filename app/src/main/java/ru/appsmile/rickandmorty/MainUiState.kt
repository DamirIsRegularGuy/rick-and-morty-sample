package ru.appsmile.rickandmorty.ui.main

import ru.appsmile.rickandmorty.model.ResultItem

data class MainUiState(
    val items: List<ResultItem> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val query: String = "",
    val statusFilter: String? = null,     // null | "alive" | "dead" | "unknown"
    val favoriteIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,       // полноэкранный лоадер (первая загрузка / новый поиск)
    val isLoadingMore: Boolean = false,   // маленький лоадер пагинации внизу списка
    val errorMessage: String? = null
)

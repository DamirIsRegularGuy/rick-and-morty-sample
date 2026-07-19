package ru.appsmile.rickandmorty.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.appsmile.rickandmorty.data.FavoritesRepository
import ru.appsmile.rickandmorty.model.ResultItem
import ru.appsmile.rickandmorty.network.CharacterRepository
import java.io.IOException

class MainViewModel(
    private val repository: CharacterRepository = CharacterRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        loadCharacters(reset = true)

        // Первое значение ("") пропускаем — оно уже загружено выше.
        viewModelScope.launch {
            searchQuery
                .drop(1)
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    _uiState.update { it.copy(query = query) }
                    loadCharacters(reset = true)
                }
        }

        // Избранное меняется мгновенно и без похода в сеть — просто обновляем набор id.
        viewModelScope.launch {
            FavoritesRepository.favorites.collectLatest { favorites ->
                _uiState.update { it.copy(favoriteIds = favorites.map { fav -> fav.id }.toSet()) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onStatusFilterChanged(status: String?) {
        if (_uiState.value.statusFilter == status) return
        _uiState.update { it.copy(statusFilter = status) }
        loadCharacters(reset = true)
    }

    fun toggleFavorite(character: ResultItem) {
        FavoritesRepository.toggleFavorite(character)
    }

    fun refresh() = loadCharacters(reset = true)

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.canLoadMore) return
        loadCharacters(reset = false)
    }

    private fun loadCharacters(reset: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val current = _uiState.value
            val pageToLoad = if (reset) 1 else current.currentPage + 1

            _uiState.update {
                it.copy(
                    isLoading = reset,
                    isLoadingMore = !reset,
                    errorMessage = null
                )
            }

            try {
                val response = repository.getCharacters(pageToLoad, current.query, current.statusFilter)
                _uiState.update { state ->
                    state.copy(
                        items = if (reset) response.results else state.items + response.results,
                        currentPage = pageToLoad,
                        canLoadMore = response.info.next != null,
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = null
                    )
                }
            } catch (e: HttpException) {
                // API отдаёт 404, когда поиск не дал результатов — это не ошибка, а пустой список.
                if (e.code() == 404) {
                    _uiState.update {
                        it.copy(
                            items = if (reset) emptyList() else it.items,
                            canLoadMore = false,
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = if (reset) "Персонажи не найдены" else null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = "Ошибка сервера (${e.code()})"
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = "Нет подключения к интернету"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = e.message ?: "Что-то пошло не так"
                    )
                }
            }
        }
    }
}

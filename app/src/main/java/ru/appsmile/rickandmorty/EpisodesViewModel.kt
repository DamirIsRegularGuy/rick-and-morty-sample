package ru.appsmile.rickandmorty.ui.episodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.appsmile.rickandmorty.network.CharacterRepository
import java.io.IOException

class EpisodesViewModel(
    private val episodeUrls: List<String>,
    private val repository: CharacterRepository = CharacterRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpisodesUiState(isLoading = true))
    val uiState: StateFlow<EpisodesUiState> = _uiState.asStateFlow()

    init {
        loadEpisodes()
    }

    private fun loadEpisodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val episodes = repository.getEpisodes(episodeUrls)
                _uiState.update {
                    it.copy(rows = buildEpisodeRows(episodes), isLoading = false, errorMessage = null)
                }
            } catch (e: HttpException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Ошибка сервера (${e.code()})")
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Нет подключения к интернету")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Не удалось загрузить эпизоды")
                }
            }
        }
    }

    class Factory(private val episodeUrls: List<String>) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EpisodesViewModel(episodeUrls) as T
        }
    }
}
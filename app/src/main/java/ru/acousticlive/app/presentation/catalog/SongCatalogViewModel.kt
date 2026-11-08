package ru.acousticlive.app.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.acousticlive.app.domain.CatalogError
import ru.acousticlive.app.domain.CatalogResult
import ru.acousticlive.app.domain.SongRepository

class SongCatalogViewModel(
    private val repository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongCatalogUiState())
    val uiState = _uiState.asStateFlow()

    private var unsubscribe: (() -> Unit)? = null

    fun start() {
        if (unsubscribe != null) return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        unsubscribe = repository.observeSongs { result ->
            _uiState.value = when (result) {
                is CatalogResult.Success -> {
                    _uiState.value.copy(
                        songs = result.songs,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                is CatalogResult.Failure -> {
                    _uiState.value.copy(
                        songs = emptyList(),
                        isLoading = false,
                        errorMessage = when (result.error) {
                            CatalogError.ACCESS_DENIED ->
                                "Нет доступа к каталогу. Войдите как организатор."

                            CatalogError.INVALID_DATA ->
                                "В каталоге есть запись без названия или текста."

                            CatalogError.UNKNOWN ->
                                "Не удалось загрузить каталог. Попробуйте ещё раз."
                        }
                    )
                }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun retry() {
        unsubscribe?.invoke()
        unsubscribe = null
        start()
    }

    fun stop() {
        unsubscribe?.invoke()
        unsubscribe = null

        // Убираем каталог из состояния после закрытия экрана.
        _uiState.value = SongCatalogUiState()
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    class Factory(
        private val repository: SongRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (modelClass.isAssignableFrom(SongCatalogViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SongCatalogViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel: ${modelClass.name}"
            )
        }
    }
}
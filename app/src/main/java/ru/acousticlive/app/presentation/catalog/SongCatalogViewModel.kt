package ru.acousticlive.app.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.acousticlive.app.domain.CatalogError
import ru.acousticlive.app.domain.CatalogResult
import ru.acousticlive.app.domain.SongRepository

class SongCatalogViewModel(
    private val repository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongCatalogUiState())
    val uiState = _uiState.asStateFlow()

    private var unsubscribe: (() -> Unit)? = null
    private var loadingTimeout: Job? = null
    private var observing = false
    private var observationId = 0L

    fun start() {
        if (observing) return

        observing = true
        val currentId = ++observationId

        _uiState.value = _uiState.value.copy(
            isLoading = !_uiState.value.hasLoadedSongs,
            errorMessage = null,
            isConnected = null
        )

        startLoadingTimeout(currentId)

        unsubscribe = repository.observeSongs { result ->
            // Игнорируем запоздалые события старой подписки.
            if (observing && observationId == currentId) {
                when (result) {
                    is CatalogResult.Success -> {
                        cancelLoadingTimeout()

                        _uiState.value = _uiState.value.copy(
                            songs = result.songs,
                            hasLoadedSongs = true,
                            isLoading = false,
                            errorMessage = null
                        )
                    }

                    is CatalogResult.Failure -> {
                        cancelLoadingTimeout()

                        _uiState.value = _uiState.value.copy(
                            songs = emptyList(),
                            hasLoadedSongs = false,
                            isLoading = false,
                            errorMessage = when (result.error) {
                                CatalogError.ACCESS_DENIED ->
                                    "Нет доступа к каталогу. " +
                                            "Войдите как организатор."

                                CatalogError.INVALID_DATA ->
                                    "В каталоге есть запись " +
                                            "без названия или текста."

                                CatalogError.UNKNOWN ->
                                    "Не удалось загрузить каталог. " +
                                            "Попробуйте ещё раз."
                            }
                        )
                    }

                    is CatalogResult.ConnectionChanged -> {
                        val previousConnection =
                            _uiState.value.isConnected

                        _uiState.value = _uiState.value.copy(
                            isConnected = result.isConnected
                        )

                        if (result.isConnected == false) {
                            cancelLoadingTimeout()

                            // Песни и поисковый запрос остаются.
                            _uiState.value = _uiState.value.copy(
                                isLoading = false
                            )
                        } else if (
                            result.isConnected == true &&
                            previousConnection != true &&
                            !_uiState.value.hasLoadedSongs &&
                            _uiState.value.errorMessage == null
                        ) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true
                            )

                            startLoadingTimeout(currentId)
                        }
                    }
                }
            }
        }
    }

    private fun startLoadingTimeout(currentId: Long) {
        cancelLoadingTimeout()

        if (_uiState.value.hasLoadedSongs) return

        loadingTimeout = viewModelScope.launch {
            delay(10_000L)

            if (
                observing &&
                observationId == currentId &&
                !_uiState.value.hasLoadedSongs
            ) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }

            // Подписка остаётся активной.
            // Поздний ответ всё равно отобразит каталог.
        }
    }

    private fun cancelLoadingTimeout() {
        loadingTimeout?.cancel()
        loadingTimeout = null
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    private fun detach() {
        observing = false
        observationId++

        cancelLoadingTimeout()

        unsubscribe?.invoke()
        unsubscribe = null
    }

    fun retry() {
        detach()
        start()
    }

    fun stop() {
        detach()
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
            if (
                modelClass.isAssignableFrom(
                    SongCatalogViewModel::class.java
                )
            ) {
                @Suppress("UNCHECKED_CAST")
                return SongCatalogViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel: ${modelClass.name}"
            )
        }
    }
}
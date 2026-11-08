package ru.acousticlive.app.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.acousticlive.app.domain.SongPresentationResult
import ru.acousticlive.app.domain.SongRepository
import ru.acousticlive.app.domain.model.Song

data class SongPresentationUiState(
    val isSending: Boolean = false,
    val message: String? = null
)

class SongPresentationViewModel(
    private val repository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SongPresentationUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var isCleared = false

    fun presentSong(
        song: Song,
        isConnected: Boolean
    ) {
        if (_uiState.value.isSending) return

        if (!isConnected) {
            _uiState.value = SongPresentationUiState(
                message = "Нет связи с сервером. " +
                        "Подключитесь к интернету перед переключением песни."
            )
            return
        }

        if (song.title.isBlank() || song.lyrics.isBlank()) {
            _uiState.value = SongPresentationUiState(
                message = "У песни должны быть название и текст."
            )
            return
        }

        _uiState.value = SongPresentationUiState(
            isSending = true,
            message = "Отправляем «${song.title}». " +
                    "Ожидаем подтверждения сервера…"
        )

        repository.presentSong(song) { result ->
            if (!isCleared) {
                _uiState.value = SongPresentationUiState(
                    isSending = false,
                    message = when (result) {
                        SongPresentationResult.SUCCESS ->
                            "Сервер подтвердил переключение " +
                                    "на «${song.title}»."

                        SongPresentationResult.ACCESS_DENIED ->
                            "Нет разрешения на переключение песни. " +
                                    "Проверьте учётную запись и правила Firebase."

                        SongPresentationResult.UNKNOWN ->
                            "Не удалось подтвердить переключение. " +
                                    "Проверьте текущую песню на экране зрителя."
                    }
                )
            }
        }
    }

    override fun onCleared() {
        isCleared = true
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
                    SongPresentationViewModel::class.java
                )
            ) {
                @Suppress("UNCHECKED_CAST")
                return SongPresentationViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel: ${modelClass.name}"
            )
        }
    }
}

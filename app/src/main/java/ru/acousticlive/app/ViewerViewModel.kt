package ru.acousticlive.app

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ViewerUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val lyrics: String = "",
    val isConnected: Boolean? = null,
    val error: String? = null
)

class ViewerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ViewerUiState())
    val uiState = _uiState.asStateFlow()

    private val database = FirebaseDatabase.getInstance(
        "https://acoustic-live-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    private val songReference = database.getReference("currentSong")
    private val connectionReference = database.getReference(".info/connected")

    private val songListener = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            if (!snapshot.exists()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    title = "Ожидаем начало выступления",
                    lyrics = "Когда ведущий выберет песню, здесь появится её текст.",
                    error = null
                )
                return
            }

            val title = snapshot.child("title").value as? String
            val lyrics = snapshot.child("lyrics").value as? String

            if (title == null || lyrics == null) {
                _uiState.value = _uiState.value.copy(
                    error = "У песни отсутствует название или текст."
                )
                return
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                title = title,
                lyrics = lyrics,
                error = null
            )
        }

        override fun onCancelled(error: DatabaseError) {
            // Сохраняем ранее полученный текст.
            _uiState.value = _uiState.value.copy(
                error = "Обновления недоступны: ${error.message}"
            )
        }
    }

    private val connectionListener = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            val connected = snapshot.getValue(Boolean::class.java) == true

            _uiState.value = _uiState.value.copy(
                isConnected = connected
            )
        }

        override fun onCancelled(error: DatabaseError) {
            _uiState.value = _uiState.value.copy(
                isConnected = null,
                error = "Не удалось проверить соединение: ${error.message}"
            )
        }
    }

    init {
        connectionReference.addValueEventListener(connectionListener)
        songReference.addValueEventListener(songListener)
    }

    override fun onCleared() {
        songReference.removeEventListener(songListener)
        connectionReference.removeEventListener(connectionListener)
        super.onCleared()
    }
}
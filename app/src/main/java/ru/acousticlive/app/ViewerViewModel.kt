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
    val error: String? = null
)

class ViewerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ViewerUiState())
    val uiState = _uiState.asStateFlow()

    private val songReference = FirebaseDatabase.getInstance(
        "https://acoustic-live-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("currentSong")

    private val songListener = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            if (!snapshot.exists()) {
                _uiState.value = ViewerUiState(
                    isLoading = false,
                    title = "Ожидаем начало выступления",
                    lyrics = "Когда ведущий выберет песню, здесь появится её текст."
                )
                return
            }

            val title = snapshot.child("title").value as? String
            val lyrics = snapshot.child("lyrics").value as? String

            if (title == null || lyrics == null) {
                _uiState.value = ViewerUiState(
                    isLoading = false,
                    error = "У песни отсутствует название или текст."
                )
                return
            }

            _uiState.value = ViewerUiState(
                isLoading = false,
                title = title,
                lyrics = lyrics
            )
        }

        override fun onCancelled(error: DatabaseError) {
            _uiState.value = ViewerUiState(
                isLoading = false,
                error = "Не удалось загрузить песню: ${error.message}"
            )
        }
    }

    init {
        songReference.addValueEventListener(songListener)
    }

    override fun onCleared() {
        songReference.removeEventListener(songListener)
        super.onCleared()
    }
}
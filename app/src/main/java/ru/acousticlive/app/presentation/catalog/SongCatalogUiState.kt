package ru.acousticlive.app.presentation.catalog

import ru.acousticlive.app.domain.model.Song

data class SongCatalogUiState(
    val songs: List<Song> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
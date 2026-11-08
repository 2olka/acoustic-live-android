package ru.acousticlive.app.domain

import ru.acousticlive.app.domain.model.Song

enum class CatalogError {
    ACCESS_DENIED,
    INVALID_DATA,
    UNKNOWN
}

sealed interface CatalogResult {
    data class Success(val songs: List<Song>) : CatalogResult
    data class Failure(val error: CatalogError) : CatalogResult
}

interface SongRepository {
       fun observeSongs(
        onResult: (CatalogResult) -> Unit
    ): () -> Unit
}
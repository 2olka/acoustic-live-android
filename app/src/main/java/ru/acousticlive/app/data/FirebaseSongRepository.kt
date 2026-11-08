package ru.acousticlive.app.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.acousticlive.app.domain.CatalogError
import ru.acousticlive.app.domain.CatalogResult
import ru.acousticlive.app.domain.SongRepository
import ru.acousticlive.app.domain.model.Song

class FirebaseSongRepository(
    database: FirebaseDatabase
) : SongRepository {

    private val songsReference = database.getReference("songs")

    private val connectionReference =
        database.getReference(".info/connected")

    override fun observeSongs(
        onResult: (CatalogResult) -> Unit
    ): () -> Unit {
        var active = true

        val songListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!active) return

                val songs = mutableListOf<Song>()

                for (child in snapshot.children) {
                    val id = child.key
                    val title = child.child("title").value as? String
                    val lyrics = child.child("lyrics").value as? String

                    if (id == null || title == null || lyrics == null) {
                        onResult(
                            CatalogResult.Failure(
                                CatalogError.INVALID_DATA
                            )
                        )
                        return
                    }

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            lyrics = lyrics
                        )
                    )
                }

                onResult(
                    CatalogResult.Success(
                        songs.sortedBy { it.title.lowercase() }
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                if (!active) return

                val catalogError =
                    if (error.code == DatabaseError.PERMISSION_DENIED) {
                        CatalogError.ACCESS_DENIED
                    } else {
                        CatalogError.UNKNOWN
                    }

                onResult(CatalogResult.Failure(catalogError))
            }
        }

        val connectionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!active) return

                onResult(
                    CatalogResult.ConnectionChanged(
                        isConnected = snapshot.value as? Boolean
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                if (!active) return

                onResult(
                    CatalogResult.ConnectionChanged(
                        isConnected = null
                    )
                )
            }
        }

        connectionReference.addValueEventListener(connectionListener)
        songsReference.addValueEventListener(songListener)

        return {
            active = false
            songsReference.removeEventListener(songListener)
            connectionReference.removeEventListener(connectionListener)
        }
    }
}
package ru.acousticlive.app.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.acousticlive.app.domain.model.Song
import ru.acousticlive.app.ui.theme.AcousticLiveTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private val CatalogBackground = Color(0xFF0F0F12)
private val CatalogSurface = Color(0xFF1C1C22)
private val CatalogAccent = Color(0xFFFFB347)
private val CatalogText = Color(0xFFF5F5F5)
private val CatalogSecondary = Color(0xFF9A9AA5)
private val CatalogError = Color(0xFFFF6B6B)

@Composable
fun SongCatalogScreen(
    songs: List<Song>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    hasLoadedSongs: Boolean = true,
    isConnected: Boolean? = true,
    onPresentSong: (Song) -> Unit = {},
    isPresenting: Boolean = false,
    presentationMessage: String? = null
) {
    var selectedSongId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val selectedSong = songs.firstOrNull {
        it.id == selectedSongId
    }

    val filteredSongs = songs.filter { song ->
        song.title.contains(
            other = searchQuery.trim(),
            ignoreCase = true
        )
    }

    if (selectedSongId != null) {
        AlertDialog(
            onDismissRequest = {
                selectedSongId = null
            },
            containerColor = CatalogSurface,
            titleContentColor = CatalogAccent,
            textContentColor = CatalogText,
            title = {
                Text(
                    text = selectedSong?.title ?: "Песня недоступна",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isConnected != true) {
                        Text(
                            text = "Нет подтверждённой связи с сервером. " +
                                    "Текст может быть неактуален.",
                            color = CatalogAccent,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = if (selectedSong != null) {
                            selectedSong.lyrics.ifBlank {
                                "Текст пока не добавлен."
                            }
                        } else {
                            "Песня удалена из каталога " +
                                    "или данные больше недоступны."
                        },
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    )
                    if (presentationMessage != null) {
                        Text(
                            text = presentationMessage,
                            color = CatalogAccent,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    if (isPresenting && isConnected != true) {
                        Text(
                            text = "Связь прервана. Отправка не отменена: " +
                                    "Firebase может завершить её после восстановления связи.",
                            color = CatalogAccent,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedSong?.let { song ->
                            onPresentSong(song)
                        }
                    },
                    enabled = selectedSong != null &&
                            selectedSong.title.isNotBlank() &&
                            selectedSong.lyrics.isNotBlank() &&
                            isConnected == true &&
                            errorMessage == null &&
                            !isPresenting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatalogAccent,
                        contentColor = CatalogBackground
                    )
                ) {
                    Text(
                        if (isPresenting) {
                            "Отправляем…"
                        } else {
                            "Показать зрителям"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedSongId = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = CatalogAccent
                    )
                ) {
                    Text("Закрыть")
                }
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CatalogBackground,
        contentColor = CatalogText
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = CatalogAccent
                    )
                ) {
                    Text("К зрителю")
                }

                TextButton(
                    onClick = onSignOut,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = CatalogAccent
                    )
                ) {
                    Text("Выйти")
                }
            }

            Text(
                text = "Каталог песен",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            if (presentationMessage != null) {
                Text(
                    text = presentationMessage,
                    color = CatalogAccent,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            if (isPresenting && isConnected != true) {
                Text(
                    text = "Связь прервана. Отправка не отменена: " +
                            "Firebase может завершить её после восстановления связи.",
                    color = CatalogAccent,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск по названию") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = { onSearchChange("") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = CatalogAccent
                            )
                        ) {
                            Text("Сброс")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CatalogText,
                    unfocusedTextColor = CatalogText,
                    cursorColor = CatalogAccent,
                    focusedBorderColor = CatalogAccent,
                    unfocusedBorderColor = CatalogSecondary,
                    focusedLabelColor = CatalogAccent,
                    unfocusedLabelColor = CatalogSecondary,
                    focusedContainerColor = CatalogBackground,
                    unfocusedContainerColor = CatalogBackground
                )
            )

            if (isConnected != true && errorMessage == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF302719),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isConnected == false) {
                            "Нет связи с сервером. " +
                                    "Каталог может быть неактуален."
                        } else {
                            "Проверяем связь с сервером…"
                        },
                        modifier = Modifier.padding(12.dp),
                        color = CatalogAccent,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = CatalogAccent
                    )

                    Text(
                        text = "Загружаем песни…",
                        color = CatalogSecondary
                    )
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = CatalogError
                    )

                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CatalogAccent,
                            contentColor = CatalogBackground
                        )
                    ) {
                        Text("Повторить")
                    }
                }

                !hasLoadedSongs -> {
                    Text(
                        text = if (isConnected == false) {
                            "Сохранённый каталог пока недоступен. " +
                                    "Подключитесь к интернету — " +
                                    "песни появятся автоматически."
                        } else {
                            "Не удалось дождаться каталога. " +
                                    "Продолжаем ожидать данные. " +
                                    "Можно повторить загрузку."
                        },
                        color = CatalogSecondary
                    )

                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CatalogAccent,
                            contentColor = CatalogBackground
                        )
                    ) {
                        Text("Повторить")
                    }
                }

                songs.isEmpty() -> {
                    Text(
                        text = "В каталоге пока нет песен.",
                        color = CatalogSecondary
                    )
                }

                filteredSongs.isEmpty() -> {
                    Text(
                        text = "Ничего не найдено. Измените запрос.",
                        color = CatalogSecondary
                    )
                }

                else -> {
                    Text(
                        text = "Показано: ${filteredSongs.size} " +
                                "из ${songs.size}",
                        color = CatalogSecondary,
                        fontSize = 14.sp
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = filteredSongs,
                            key = { song -> song.id }
                        ) { song ->
                            SongCatalogItem(
                                song = song,
                                onClick = {
                                    selectedSongId = song.id
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongCatalogItem(
    song: Song,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CatalogSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = song.title,
                color = CatalogAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = song.lyrics.ifBlank {
                    "Текст пока не добавлен"
                },
                color = CatalogSecondary,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun SongCatalogScreenPreview() {
    AcousticLiveTheme {
        SongCatalogScreen(
            songs = listOf(
                Song(
                    id = "demo-1",
                    title = "Вечер во дворе",
                    lyrics = "Демонстрационный текст первой песни."
                ),
                Song(
                    id = "demo-2",
                    title = "Дорога домой",
                    lyrics = "Демонстрационный текст второй песни."
                )
            ),
            searchQuery = "",
            onSearchChange = {},
            onBack = {},
            onSignOut = {},
            onRetry = {},
            isConnected = false
        )
    }
}
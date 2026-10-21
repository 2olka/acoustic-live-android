package ru.acousticlive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.acousticlive.app.ui.theme.AcousticLiveTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import android.view.WindowManager

private val BackgroundColor = Color(0xFF0F0F12)
private val AccentColor = Color(0xFFFFB347)
private val MainTextColor = Color(0xFFF5F5F5)
private val SecondaryTextColor = Color(0xFF9A9AA5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val viewerViewModel =
            ViewModelProvider(this)[ViewerViewModel::class.java]

        setContent {
            val state by viewerViewModel.uiState.collectAsState()

            AcousticLiveTheme {
                Scaffold(
                    containerColor = BackgroundColor
                ) { innerPadding ->

                    when {
                        state.isLoading -> {
                            ViewerScreen(
                                songTitle = "Подключение…",
                                lyrics = "Загружаем текущую песню.",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        state.error != null -> {
                            ViewerScreen(
                                songTitle = "Не удалось получить данные",
                                lyrics = state.error.orEmpty(),
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        else -> {
                            ViewerScreen(
                                songTitle = state.title,
                                lyrics = state.lyrics,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ViewerScreen(
        songTitle: String,
        lyrics: String,
        modifier: Modifier = Modifier
    ) {
        var fontSize by rememberSaveable { mutableStateOf(26) }
        val scrollState = rememberScrollState()

        var previousTitle by rememberSaveable {
            mutableStateOf(songTitle)
        }
        var previousLyrics by rememberSaveable {mutableStateOf(lyrics)}


        // При изменении песни или её текста переходим к началу.
        LaunchedEffect(songTitle, lyrics) {
            val songChanged =
                songTitle != previousTitle || lyrics != previousLyrics

            if (songChanged) {
                scrollState.scrollTo(0)
                previousTitle = songTitle
                previousLyrics = lyrics
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            // Эта панель остаётся на месте при прокрутке.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        fontSize = (fontSize - 2).coerceAtLeast(18)
                    },
                    enabled = fontSize > 18,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = BackgroundColor
                    )
                ) {
                    Text(
                        text = "A−",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        fontSize = (fontSize + 2).coerceAtMost(42)
                    },
                    enabled = fontSize < 42,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = BackgroundColor
                    )
                ) {
                    Text(
                        text = "A+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Прокручивается только содержимое под панелью.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Acoustic Live",
                    color = AccentColor,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Музыкальный квартирник",
                    color = SecondaryTextColor,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = songTitle,
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = lyrics,
                    modifier = Modifier.fillMaxWidth(),
                    color = MainTextColor,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.5f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun ViewerScreenPreview() {
        AcousticLiveTheme {
            ViewerScreen(
                songTitle = "Давайте споём вместе",
                lyrics = "Сегодня здесь звучат голоса,\nИ музыка нас собирает."
            )
        }
    }
}
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.activity.compose.BackHandler
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.google.firebase.auth.FirebaseAuth
import ru.acousticlive.app.data.FirebaseAuthRepository
import ru.acousticlive.app.presentation.LoginViewModel

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

        val loginViewModel = ViewModelProvider(
            this,
            LoginViewModel.Factory(
                FirebaseAuthRepository(FirebaseAuth.getInstance())
            )
        )[LoginViewModel::class.java]

        setContent {
            val state by viewerViewModel.uiState.collectAsState()

            val loginState by loginViewModel.uiState.collectAsState()

            var showLogin by rememberSaveable {
                mutableStateOf(false)
            }

            // Сохраняет прокрутку и размер шрифта при переходах.
            val screenStateHolder = rememberSaveableStateHolder()

            BackHandler(enabled = showLogin) {
                if (!loginState.isLoading) {
                    showLogin = false
                    loginViewModel.clearError()
                }
            }

            AcousticLiveTheme {
                Scaffold(
                    containerColor = BackgroundColor
                ) { innerPadding ->
                    if (showLogin) {
                        if (loginState.userId != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Вход выполнен",
                                    color = MainTextColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Вы вошли в учётную запись.",
                                    color = SecondaryTextColor
                                )

                                Button(
                                    onClick = {
                                        showLogin = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentColor,
                                        contentColor = BackgroundColor
                                    )
                                ) {
                                    Text("К экрану зрителя")
                                }

                                TextButton(
                                    onClick = {
                                        loginViewModel.signOut()
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = AccentColor
                                    )
                                ) {
                                    Text("Выйти из аккаунта")
                                }
                            }
                        } else {
                            OrganizerLoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBack = {
                                    if (!loginState.isLoading) {
                                        showLogin = false
                                        loginViewModel.clearError()
                                    }
                                },
                                onLogin = loginViewModel::signIn,
                                isLoading = loginState.isLoading,
                                errorMessage = loginState.errorMessage,
                                onInputChanged = loginViewModel::clearError
                            )
                        }
                    } else {
                        screenStateHolder.SaveableStateProvider("viewer") {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            loginViewModel.clearError()
                                            showLogin = true
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = AccentColor
                                        )
                                    ) {
                                        Text("Организатор")
                                    }
                                }

                                val displayedTitle = when {
                                    !state.isLoading -> state.title
                                    state.error != null ->
                                        "Не удалось загрузить песню"
                                    state.isConnected == false ->
                                        "Нет сохранённой песни"
                                    else -> "Подключение…"
                                }

                                val displayedLyrics = when {
                                    !state.isLoading -> state.lyrics
                                    state.error != null ->
                                        "Подробности указаны выше."
                                    state.isConnected == false ->
                                        "Для первой загрузки нужно соединение с сервером."
                                    else -> "Загружаем текущую песню."
                                }

                                val connectionMessage = when {
                                    state.error != null -> state.error

                                    state.isConnected == false ->
                                        "Нет связи с сервером. " +
                                                "Переключения песен временно недоступны. " +
                                                "Сохранённый текст может быть неактуален."

                                    state.isConnected == null ->
                                        "Проверяем связь с сервером…"

                                    else -> null
                                }

                                ViewerScreen(
                                    songTitle = displayedTitle,
                                    lyrics = displayedLyrics,
                                    connectionMessage = connectionMessage,
                                    modifier = Modifier.weight(1f)
                                )
                            }
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
        modifier: Modifier = Modifier,
        connectionMessage: String? = null
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.width(60.dp),
                    contentPadding = PaddingValues(0.dp),
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Acoustic Live",
                        color = AccentColor,
                        fontSize = 32.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Музыкальный квартирник",
                        color = SecondaryTextColor,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    modifier = Modifier.width(60.dp),
                    contentPadding = PaddingValues(0.dp),
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
            if (connectionMessage != null) {
                Text(
                    text = connectionMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF302719))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    color = AccentColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
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
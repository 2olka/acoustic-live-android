package ru.acousticlive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.acousticlive.app.ui.theme.AcousticLiveTheme

private val BackgroundColor = Color(0xFF0F0F12)
private val AccentColor = Color(0xFFFFB347)
private val MainTextColor = Color(0xFFF5F5F5)
private val SecondaryTextColor = Color(0xFF9A9AA5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AcousticLiveTheme {
                Scaffold(
                    containerColor = BackgroundColor
                ) { innerPadding ->
                    ViewerScreen(
                        songTitle = "Давайте споём вместе",
                        lyrics = """
                            Сегодня здесь звучат голоса,
                            И музыка нас собирает.
                            Пусть песня летит в небеса,
                            А каждый вокруг подпевает.

                            Мы этот вечер сохраним,
                            Его тепло возьмём с собою.
                            И снова песню повторим —
                            Все вместе, дружною толпою.
                        """.trimIndent(),
                        modifier = Modifier.padding(innerPadding)
                    )
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
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
            fontSize = 26.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
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
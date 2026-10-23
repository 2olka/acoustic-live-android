package ru.acousticlive.app.presentation

data class LoginUiState(
    val isLoading: Boolean = false,
    val userId: String? = null,
    val errorMessage: String? = null
)
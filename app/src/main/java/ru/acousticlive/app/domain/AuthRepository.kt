package ru.acousticlive.app.domain

enum class LoginError {
    INVALID_CREDENTIALS,
    NETWORK,
    TOO_MANY_REQUESTS,
    UNKNOWN
}

sealed interface LoginResult {
    data class Success(val uid: String) : LoginResult
    data class Failure(val error: LoginError) : LoginResult
}

interface AuthRepository {
    val currentUserId: String?

    fun signIn(
        email: String,
        password: String,
        onResult: (LoginResult) -> Unit
    )

    fun signOut()
}
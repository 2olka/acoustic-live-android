package ru.acousticlive.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.acousticlive.app.domain.AuthRepository
import ru.acousticlive.app.domain.LoginError
import ru.acousticlive.app.domain.LoginResult

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(userId = authRepository.currentUserId)
    )
    val uiState = _uiState.asStateFlow()

    private var isCleared = false

    fun signIn(email: String, password: String) {
        if (_uiState.value.isLoading) return

        if (email.isBlank() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Введите email и пароль."
            )
            return
        }

        _uiState.value = LoginUiState(isLoading = true)

        authRepository.signIn(email.trim(), password) callback@{ result ->
            if (isCleared) return@callback

            _uiState.value = when (result) {
                is LoginResult.Success -> {
                    LoginUiState(userId = result.uid)
                }

                is LoginResult.Failure -> {
                    LoginUiState(
                        errorMessage = when (result.error) {
                            LoginError.INVALID_CREDENTIALS ->
                                "Не удалось войти. Проверьте email и пароль."

                            LoginError.NETWORK ->
                                "Нет связи с сервером. Проверьте интернет."

                            LoginError.TOO_MANY_REQUESTS ->
                                "Слишком много попыток. Попробуйте позже."

                            LoginError.UNKNOWN ->
                                "Не удалось войти. Попробуйте ещё раз."
                        }
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun signOut() {
        if (_uiState.value.isLoading) return

        authRepository.signOut()
        _uiState.value = LoginUiState()
    }

    override fun onCleared() {
        isCleared = true
        super.onCleared()
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(authRepository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel: ${modelClass.name}"
            )
        }
    }
}
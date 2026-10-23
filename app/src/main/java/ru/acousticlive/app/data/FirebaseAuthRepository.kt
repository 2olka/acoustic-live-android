package ru.acousticlive.app.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import ru.acousticlive.app.domain.AuthRepository
import ru.acousticlive.app.domain.LoginError
import ru.acousticlive.app.domain.LoginResult

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun signIn(
        email: String,
        password: String,
        onResult: (LoginResult) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val user = if (task.isSuccessful) {
                    task.result?.user
                } else {
                    null
                }

                if (task.isSuccessful && user != null) {
                    onResult(LoginResult.Success(user.uid))
                } else {
                    val error = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException,
                        is FirebaseAuthInvalidUserException ->
                            LoginError.INVALID_CREDENTIALS

                        is FirebaseNetworkException ->
                            LoginError.NETWORK

                        is FirebaseTooManyRequestsException ->
                            LoginError.TOO_MANY_REQUESTS

                        else -> LoginError.UNKNOWN
                    }

                    onResult(LoginResult.Failure(error))
                }
            }
    }

    override fun signOut() {
        auth.signOut()
    }
}
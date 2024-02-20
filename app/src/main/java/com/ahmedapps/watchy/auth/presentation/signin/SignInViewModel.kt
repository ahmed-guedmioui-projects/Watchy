package com.ahmedapps.watchy.auth.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedapps.watchy.auth.domain.repository.AuthRepository
import com.ahmedapps.watchy.auth.domain.usecase.FormValidatorUseCase
import com.ahmedapps.watchy.auth.util.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val formValidatorUseCase: FormValidatorUseCase
) : ViewModel() {

    private var _signInState = MutableStateFlow(SignInState())
    val signInState = _signInState.asStateFlow()

    private val _authResultChannel = Channel<AuthResult<Unit>>()
    val authResultChannel = _authResultChannel.receiveAsFlow()

    private val _invalidCredentialsToastChannel = Channel<Boolean>()
    val invalidCredentialsToastChannel = _invalidCredentialsToastChannel.receiveAsFlow()


    fun onEvent(event: SignInUiEvent) {
        when (event) {

            is SignInUiEvent.SignInEmailChanged -> {
                _signInState.update {
                    it.copy(signInEmail = event.value)
                }
            }

            is SignInUiEvent.SignInPasswordChanged -> {
                _signInState.update {
                    it.copy(signInPassword = event.value)
                }
            }

            is SignInUiEvent.SignIn -> {
                val isValidEmail = formValidatorUseCase.validEmail(
                    email = signInState.value.signInEmail
                )
                val isValidPassword = formValidatorUseCase.validPassword(
                    password = signInState.value.signInPassword
                )

                if (isValidEmail && isValidPassword) {
                    signIn()
                } else {
                    viewModelScope.launch {
                        _invalidCredentialsToastChannel.send(true)
                    }
                }

            }

        }
    }

    private fun signIn() {
        viewModelScope.launch {
            _signInState.update {
                it.copy(isLoading = true)
            }
            val result = authRepository.singIn(
                email = signInState.value.signInEmail,
                password = signInState.value.signInPassword
            )
            _authResultChannel.send(result)
            _signInState.update {
                it.copy(isLoading = false)
            }
        }
    }
}



















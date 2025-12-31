package com.riohhost.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val authPreferences = com.riohhost.app.data.local.AuthPreferences(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()
    
    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _savedCredentials = MutableStateFlow<Pair<String, String>?>(null)
    val savedCredentials: StateFlow<Pair<String, String>?> = _savedCredentials.asStateFlow()

    init {
        checkSavedUser()
    }

    private fun checkSavedUser() {
        val saved = authPreferences.getSavedUser()
        if (saved != null) {
            _savedCredentials.value = saved
            _rememberMe.value = true
            _biometricEnabled.value = authPreferences.isBiometricEnabled()
        }
    }

    fun toggleRememberMe(enabled: Boolean) {
        _rememberMe.value = enabled
        if (!enabled) {
            authPreferences.clearUser()
            _biometricEnabled.value = false
            authPreferences.setBiometricEnabled(false)
        }
    }
    
    fun toggleBiometric(enabled: Boolean) {
        _biometricEnabled.value = enabled
        authPreferences.setBiometricEnabled(enabled)
    }

    fun attemptLogin(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepository.signIn(email, pass)
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    if (_rememberMe.value) {
                        authPreferences.saveUser(email, pass)
                        authPreferences.setBiometricEnabled(_biometricEnabled.value)
                    }
                    _userRole.value = user.role
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error("Usuário não encontrado.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Erro ao fazer login")
            }
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

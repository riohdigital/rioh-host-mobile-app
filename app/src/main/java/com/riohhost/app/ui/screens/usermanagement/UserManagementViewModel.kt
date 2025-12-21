package com.riohhost.app.ui.screens.usermanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riohhost.app.data.models.UserProfile
import com.riohhost.app.data.repositories.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UserManagementUiState {
    object Loading : UserManagementUiState()
    data class Success(val users: List<UserProfile>) : UserManagementUiState()
    data class Error(val message: String) : UserManagementUiState()
}

class UserManagementViewModel(
    private val repository: UserManagementRepository = UserManagementRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<UserManagementUiState>(UserManagementUiState.Loading)
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UserManagementUiState.Loading
            try {
                val users = repository.getUsers()
                _uiState.value = UserManagementUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = UserManagementUiState.Error(e.message ?: "Erro ao carregar usuários")
            }
        }
    }
}

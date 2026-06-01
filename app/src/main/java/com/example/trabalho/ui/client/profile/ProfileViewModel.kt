package com.example.trabalho.ui.client.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalho.data.AuthRepository
import com.example.trabalho.data.GameRepository
import com.example.trabalho.data.UserRepository
import com.example.trabalho.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _userName = MutableStateFlow("Carregando...")
    val userName: StateFlow<String> = _userName

    private val _userEmail = MutableStateFlow("Carregando...")
    val userEmail: StateFlow<String> = _userEmail

    private val _userRole = MutableStateFlow("Carregando...")
    val userRole: StateFlow<String> = _userRole

    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders

    private val _profileFeedback = MutableStateFlow<String?>(null)
    val profileFeedback: StateFlow<String?> = _profileFeedback

    init {
        loadProfileData()
    }

    fun clearFeedback() {
        _profileFeedback.value = null
    }

    private fun loadProfileData() {
        val uid = authRepository.getCurrentUser()?.uid

        if (uid != null) {
            userRepository.getUserName(uid) { name -> _userName.value = name }
            userRepository.getUserEmail(uid) { email -> _userEmail.value = email }
            userRepository.getUserRole(uid) { role -> _userRole.value = role }

            gameRepository.getUserOrders(uid) { orders ->
                _userOrders.value = orders
            }
        } else {
            _userName.value = "Desconhecido"
            _userEmail.value = "Sem E-mail"
            _userRole.value = "Erro"
        }
    }

    fun changeName(newName: String) {
        val uid = authRepository.getCurrentUser()?.uid
        if (uid == null || newName.isBlank()) {
            _profileFeedback.value = "Nome inválido!"
            return
        }

        userRepository.updateUserName(uid, newName) { success, error ->
            if (success) {
                _userName.value = newName
                _profileFeedback.value = "Nome alterado com sucesso!"
            } else {
                _profileFeedback.value = "Erro ao alterar nome: $error"
            }
        }
    }

    fun sendPasswordReset() {
        val email = _userEmail.value
        if (email.isBlank() || email == "Carregando..." || email == "Sem E-mail") {
            _profileFeedback.value = "E-mail do usuário não identificado!"
            return
        }

        authRepository.sendPasswordResetEmail(email) { success, error ->
            if (success) {
                _profileFeedback.value = "E-mail de redefinição enviado para $email"
            } else {
                _profileFeedback.value = "Erro ao enviar e-mail: $error"
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    private val _selectedOrderFilter = MutableStateFlow("TODOS")
    val selectedOrderFilter: StateFlow<String> = _selectedOrderFilter

    val filteredUserOrders: StateFlow<List<Order>> = combine(_userOrders, _selectedOrderFilter) { orders, filter ->
        if (filter == "TODOS") orders else orders.filter { it.type == filter }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateOrderFilter(filter: String) {
        _selectedOrderFilter.value = filter
    }
}
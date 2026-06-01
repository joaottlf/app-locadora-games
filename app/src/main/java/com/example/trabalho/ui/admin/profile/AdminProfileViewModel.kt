package com.example.trabalho.ui.admin.profile

import androidx.lifecycle.ViewModel
import com.example.trabalho.data.AuthRepository
import com.example.trabalho.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _adminName = MutableStateFlow("Carregando...")
    val adminName: StateFlow<String> = _adminName

    private val _adminEmail = MutableStateFlow("Carregando...")
    val adminEmail: StateFlow<String> = _adminEmail

    private val _adminRole = MutableStateFlow("Carregando...")
    val adminRole: StateFlow<String> = _adminRole

    // Estado para mensagens de sucesso/erro
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
            userRepository.getUserName(uid) { _adminName.value = it }
            userRepository.getUserEmail(uid) { _adminEmail.value = it }
            userRepository.getUserRole(uid) { _adminRole.value = it }
        } else {
            _adminName.value = "Desconhecido"
            _adminEmail.value = "Sem E-mail"
            _adminRole.value = "Erro"
        }
    }

    fun changeName(newName: String) {
        val uid = authRepository.getCurrentUser()?.uid
        if (uid == null || newName.isBlank()) return

        userRepository.updateUserName(uid, newName) { success, error ->
            if (success) {
                _adminName.value = newName
                _profileFeedback.value = "Nome de administrador atualizado!"
            } else {
                _profileFeedback.value = "Erro ao alterar nome: $error"
            }
        }
    }

    fun sendPasswordReset() {
        val email = _adminEmail.value
        if (email.isBlank() || email == "Carregando...") return

        authRepository.sendPasswordResetEmail(email) { success, error ->
            if (success) {
                _profileFeedback.value = "E-mail de recuperação enviado para $email"
            } else {
                _profileFeedback.value = "Erro ao enviar e-mail: $error"
            }
        }
    }

    fun promoteToAdmin(emailToPromote: String) {
        if (emailToPromote.isBlank()) {
            _profileFeedback.value = "Digite um e-mail válido."
            return
        }

        userRepository.promoteUserToAdmin(emailToPromote.trim()) { success, error ->
            if (success) {
                _profileFeedback.value = "Sucesso! $emailToPromote agora é Administrador."
            } else {
                _profileFeedback.value = "Erro: $error"
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
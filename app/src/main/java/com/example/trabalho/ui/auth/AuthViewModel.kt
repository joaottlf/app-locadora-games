package com.example.trabalho.ui.auth

import androidx.lifecycle.ViewModel
import com.example.trabalho.data.AuthRepository
import com.example.trabalho.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log

class AuthViewModel(private val authRepository: AuthRepository, private val userRepository: UserRepository) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun updateEmail(input: String) { _email.value = input }

    fun updatePassword(input: String) { _password.value = input }

    fun updateName(input: String) { _name.value = input }

    fun toggleMode() {
        _isLoginMode.value = !_isLoginMode.value
        _errorMessage.value = null
    }

    fun authenticate(onSuccess: (String) -> Unit) {
        if (_email.value.isBlank() || _password.value.isBlank() || (!_isLoginMode.value && _name.value.isBlank())) {
            _errorMessage.value = "Preencha todos os campos corretamente"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        if (_isLoginMode.value) {
            Log.d("APP_DEBUG", "1. [ViewModel] Iniciando login com email: ${_email.value}")
            authRepository.login(_email.value, _password.value) { success, error ->
                if (success) {
                    Log.d("APP_DEBUG", "2. [ViewModel] Login no Auth deu certo! Pegando UID...")
                    val uid = authRepository.getCurrentUser()?.uid

                    if (uid != null) {
                        Log.d("APP_DEBUG", "3. [ViewModel] UID encontrado: $uid. Chamando Firestore...")
                        userRepository.getUserRole(uid) { role ->
                            Log.d("APP_DEBUG", "6. [ViewModel] Firestore devolveu a role: $role. Avisando a tela!")
                            _isLoading.value = false
                            onSuccess(role)
                        }
                    } else {
                        // PONTO CEGO CORRIGIDO AQUI!
                        Log.e("APP_DEBUG", "ERRO FATAL: O UID veio nulo do Firebase Auth!")
                        _isLoading.value = false
                        _errorMessage.value = "Erro interno: UID nulo."
                    }
                } else {
                    Log.e("APP_DEBUG", "ERRO: O AuthRepository retornou falha: $error")
                    _isLoading.value = false
                    _errorMessage.value = error ?: "Erro ao fazer login"
                }
            }
        } else {
            // ... (A parte do cadastro continua igual, pode manter a sua) ...
            authRepository.register(_email.value, _password.value, _name.value) { success, error ->
                _isLoading.value = false
                if (success) {
                    onSuccess("Conta criada! Faça seu login.")
                    _isLoginMode.value = true
                    _password.value = ""
                    _name.value = ""
                } else {
                    _errorMessage.value = error ?: "Erro ao criar conta"
                }
            }
        }
    }
}
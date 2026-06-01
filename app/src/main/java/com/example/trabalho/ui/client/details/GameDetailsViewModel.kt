package com.example.trabalho.ui.client.details

import androidx.lifecycle.ViewModel
import com.example.trabalho.data.AuthRepository
import com.example.trabalho.data.GameRepository
import com.example.trabalho.data.model.Game
import com.example.trabalho.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class GameDetailsViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository,
    private val gameId: String
) : ViewModel() {

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _purchaseResult = MutableStateFlow<String?>(null)
    val purchaseResult: StateFlow<String?> = _purchaseResult

    private val _isCheckoutStep = MutableStateFlow(false)
    val isCheckoutStep: StateFlow<Boolean> = _isCheckoutStep

    private val _checkoutType = MutableStateFlow("")
    val checkoutType: StateFlow<String> = _checkoutType

    private val _checkoutReturnDate = MutableStateFlow<Date?>(null)
    val checkoutReturnDate: StateFlow<Date?> = _checkoutReturnDate

    init {
        loadGameDetails()
    }

    fun clearPurchaseResult() { _purchaseResult.value = null }

    fun cancelCheckout() { _isCheckoutStep.value = false }

    private fun loadGameDetails() {
        _isLoading.value = true
        gameRepository.getGameById(gameId) { fetchedGame ->
            _game.value = fetchedGame
            _isLoading.value = false
        }
    }

    fun preparePurchase() {
        _checkoutType.value = "COMPRA"
        _checkoutReturnDate.value = null
        _isCheckoutStep.value = true
    }

    fun prepareRental(returnDate: Date) {
        val today = Date()
        val days = com.example.trabalho.data.RentCalculator.calculateDaysBetween(today, returnDate)
        if (days <= 0) {
            _purchaseResult.value = "Erro: A data de devolução deve ser a partir de amanhã!"
            return
        }
        _checkoutType.value = "ALUGUEL"
        _checkoutReturnDate.value = returnDate
        _isCheckoutStep.value = true
    }

    fun confirmOrder(isDelivery: Boolean, cep: String, finalPrice: Double) {
        val currentGame = _game.value ?: return

        val currentUser = authRepository.getCurrentUser()
        val currentUserId = currentUser?.uid
        val currentUserEmail = currentUser?.email ?: "sem-email@loja.com"

        if (currentUserId == null) {
            _purchaseResult.value = "Erro: Usuário não autenticado."
            return
        }

        _isLoading.value = true

        val order = Order(
            userId = currentUserId,
            userEmail = currentUserEmail,
            gameId = currentGame.id,
            gameTitle = currentGame.title,
            pricePaid = finalPrice,
            type = _checkoutType.value,
            rentDays = if (_checkoutType.value == "ALUGUEL") {
                com.example.trabalho.data.RentCalculator.calculateDaysBetween(Date(), _checkoutReturnDate.value!!)
            } else null,
            returnDate = _checkoutReturnDate.value,
            deliveryMethod = if (isDelivery) "ENTREGA" else "RETIRADA",
            deliveryZipCode = if (isDelivery) cep else ""
        )

        gameRepository.buyGame(order) { success, errorMessage ->
            _isLoading.value = false
            if (success) {
                _purchaseResult.value = "Pedido confirmado com sucesso! Verifique seu histórico."
                _isCheckoutStep.value = false
                loadGameDetails()
            } else {
                _purchaseResult.value = errorMessage ?: "Falha ao processar o pedido."
            }
        }
    }
}
package com.example.trabalho.ui.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalho.data.GameRepository
import com.example.trabalho.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Date
import java.util.concurrent.TimeUnit

class AdminOrdersViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow("TODOS")
    val selectedFilter: StateFlow<String> = _selectedFilter

    // --- NOVO: Estado para o Filtro de Data ---
    private val _selectedDateFilter = MutableStateFlow("TUDO") // TUDO, HOJE, SEMANA, MES
    val selectedDateFilter: StateFlow<String> = _selectedDateFilter

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _orderFeedback = MutableStateFlow<String?>(null)
    val orderFeedback: StateFlow<String?> = _orderFeedback

    private val _recalculationDialogData = MutableStateFlow<Pair<Order, com.example.trabalho.data.RecalculationResult>?>(null)
    val recalculationDialogData: StateFlow<Pair<Order, com.example.trabalho.data.RecalculationResult>?> = _recalculationDialogData

    // --- LÓGICA DE COMBINAÇÃO ATUALIZADA (Busca + Tipo + Data) ---
    val filteredOrders: StateFlow<List<Order>> = combine(
        _allOrders,
        _searchQuery,
        _selectedFilter,
        _selectedDateFilter
    ) { orders, query, typeFilter, dateFilter ->
        orders.filter { order ->
            // 1. Pesquisa por Título ou E-MAIL (em vez do ID)
            val matchesSearch = query.isBlank() ||
                    order.gameTitle.contains(query, ignoreCase = true) ||
                    order.userEmail.contains(query, ignoreCase = true)

            // 2. Filtro de Tipo
            val matchesType = when (typeFilter) {
                "COMPRA" -> order.type == "COMPRA"
                "ALUGUEL" -> order.type == "ALUGUEL"
                "PENDENTES" -> order.type == "ALUGUEL" && !order.returned
                else -> true
            }

            // 3. Filtro de Data
            val matchesDate = if (dateFilter == "TUDO" || order.createdAt == null) {
                true
            } else {
                val now = Date()
                val diffInMillis = now.time - order.createdAt.time
                val daysDiff = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                when (dateFilter) {
                    "HOJE" -> daysDiff == 0L
                    "SEMANA" -> daysDiff <= 7L
                    "MES" -> daysDiff <= 30L
                    else -> true
                }
            }

            matchesSearch && matchesType && matchesDate
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    init { loadAllOrders() }

    fun clearFeedback() { _orderFeedback.value = null }
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateFilter(filter: String) { _selectedFilter.value = filter }
    fun updateDateFilter(filter: String) { _selectedDateFilter.value = filter }

    private fun loadAllOrders() {
        _isLoading.value = true
        gameRepository.getAllOrders { orders ->
            _allOrders.value = orders
            _isLoading.value = false
        }
    }

    fun initiateReturn(order: Order) {
        if (order.rentDays == null || order.returnDate == null) return

        _isLoading.value = true
        gameRepository.getGameById(order.gameId) { game ->
            _isLoading.value = false
            if (game != null) {
                val result = com.example.trabalho.data.RentCalculator.recalculateReturnPrice(
                    originalPricePaid = order.pricePaid,
                    baseDailyPrice = game.rentPrice,
                    rentDaysContracted = order.rentDays,
                    returnDateLimit = order.returnDate
                )

                if (result.status == "NO_PRAZO") {
                    confirmReturn(order, order.pricePaid)
                } else {
                    _recalculationDialogData.value = Pair(order, result)
                }
            } else {
                _orderFeedback.value = "Erro: O jogo não existe mais no sistema para recalcular multas."
            }
        }
    }

    fun cancelReturn() { _recalculationDialogData.value = null }

    fun confirmReturn(order: Order, finalPrice: Double) {
        _recalculationDialogData.value = null
        _isLoading.value = true
        gameRepository.returnRentedGame(order, finalPrice) { success, error ->
            if (success) {
                _orderFeedback.value = "Transação concluída e estoque atualizado!"
                loadAllOrders()
            } else {
                _orderFeedback.value = "Erro ao devolver: $error"
                _isLoading.value = false
            }
        }
    }
}
package com.example.trabalho.ui.admin.orders

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    viewModel: AdminOrdersViewModel,
    onBackClick: () -> Unit
) {
    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState() // <-- Novo Estado

    val isLoading by viewModel.isLoading.collectAsState()
    val orderFeedback by viewModel.orderFeedback.collectAsState()
    val recalculationData by viewModel.recalculationDialogData.collectAsState()

    val context = LocalContext.current
    val currentRevenue = filteredOrders.sumOf { it.pricePaid }

    LaunchedEffect(orderFeedback) {
        orderFeedback?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fluxo de Caixa e Pedidos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
            ) {
                // --- PAINEL FINANCEIRO ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) // Verde
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (selectedFilter == "TODOS" && selectedDateFilter == "TUDO" && searchQuery.isBlank()) "Faturação Total (Receita)" else "Faturação Filtrada", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("R$ ${"%.2f".format(currentRevenue)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Pedidos listados: ${filteredOrders.size}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pesquisar por E-mail!
                OutlinedTextField(value = searchQuery, onValueChange = { viewModel.updateSearchQuery(it) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar por jogo ou E-mail do cliente...") }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") }, singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))

                // --- FILTROS DE TIPO ---
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("TODOS", "COMPRA", "ALUGUEL", "PENDENTES")
                    items(filters) { filter ->
                        FilterChip(selected = selectedFilter == filter, onClick = { viewModel.updateFilter(filter) }, label = { Text(filter) })
                    }
                }

                // --- FILTROS DE DATA (Novo!) ---
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val dateFilters = listOf("TUDO" to "Sempre", "HOJE" to "Hoje", "SEMANA" to "7 dias", "MES" to "30 dias")
                    items(dateFilters) { (filterValue, label) ->
                        FilterChip(
                            selected = selectedDateFilter == filterValue,
                            onClick = { viewModel.updateDateFilter(filterValue) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- LISTA ---
                if (filteredOrders.isEmpty()) {
                    Text("Nenhum pedido encontrado para este filtro.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredOrders) { order ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", LocalLocale.current.platformLocale)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${order.type} - R$ ${"%.2f".format(order.pricePaid)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(order.createdAt?.let { sdf.format(it) } ?: "", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Jogo: ${order.gameTitle}", fontWeight = FontWeight.Bold)
                                    // NOVO: Exibe o E-mail em vez do ID longo e feio
                                    Text("Cliente: ${order.userEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                                    Text("Logística: ${order.deliveryMethod} ${if(order.deliveryZipCode.isNotBlank()) "(CEP: ${order.deliveryZipCode})" else ""}")

                                    if (order.type == "ALUGUEL" && order.returnDate != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val dateOnlySdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)

                                        if (order.returned) {
                                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) { // Azul em vez de verde escuro para diferenciar da compra
                                                Text("Devolvido ao Estoque", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                                            }
                                        } else {
                                            // --- CORES DINÂMICAS PARA A DATA USANDO O TEMA ---
                                            val daysLate = com.example.trabalho.data.RentCalculator.calculateDaysLate(order.returnDate)
                                            val dateColor = when {
                                                daysLate > 0 -> MaterialTheme.colorScheme.error // Atrasou!
                                                daysLate in -2..0 -> MaterialTheme.colorScheme.secondary // Laranja Semântico (Faltam 2 dias ou menos)
                                                else -> MaterialTheme.colorScheme.primary // Verde (Tudo bem)
                                            }

                                            Text(
                                                text = "Devolução limite: ${dateOnlySdf.format(order.returnDate)}",
                                                color = dateColor,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = { viewModel.initiateReturn(order) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Confirmar Devolução na Loja")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- POPUP DE RECÁLCULO / MULTA ---
            if (recalculationData != null) {
                val (orderToReturn, result) = recalculationData!!
                AlertDialog(
                    onDismissRequest = { viewModel.cancelReturn() },
                    title = {
                        Text(
                            text = if (result.status == "ATRASADO") "⚠️ Devolução com Atraso" else "ℹ️ Devolução Antecipada",
                            color = if (result.status == "ATRASADO") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    text = {
                        Column {
                            if (result.status == "ATRASADO") {
                                Text("O cliente atrasou a devolução em ${result.actualDays - orderToReturn.rentDays!!} dias.", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Multa aplicada: R$ ${"%.2f".format(result.difference)}")
                                Text("Valor antigo: R$ ${"%.2f".format(orderToReturn.pricePaid)}")
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("Cobrar do cliente: R$ ${"%.2f".format(result.newTotal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("O cliente devolveu adiantado. Ficou apenas ${result.actualDays} dias em vez de ${orderToReturn.rentDays}.", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Descontos originais invalidados.")
                                Text("Valor recalculado: R$ ${"%.2f".format(result.newTotal)}")
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("Crédito a devolver/estornar: R$ ${"%.2f".format(result.difference)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.confirmReturn(orderToReturn, result.newTotal) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Confirmar Atualização")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.cancelReturn() }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
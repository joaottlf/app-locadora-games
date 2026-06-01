package com.example.trabalho.ui.client.details

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    viewModel: GameDetailsViewModel,
    onBackClick: () -> Unit
) {
    val game by viewModel.game.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val purchaseResult by viewModel.purchaseResult.collectAsState()

    val isCheckoutStep by viewModel.isCheckoutStep.collectAsState()
    val checkoutType by viewModel.checkoutType.collectAsState()
    val checkoutReturnDate by viewModel.checkoutReturnDate.collectAsState()

    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(purchaseResult) {
        purchaseResult?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearPurchaseResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCheckoutStep) "Confirmação do Pedido" else "Detalhes do Jogo") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isCheckoutStep) viewModel.cancelCheckout() else onBackClick()
                    }) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (game == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Jogo não encontrado ou indisponível.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val currentGame = game!!

            if (isCheckoutStep) {
                // --- TELA DE CHECKOUT ---
                var isDelivery by remember { mutableStateOf(false) }
                var cep by remember { mutableStateOf("") }

                val deliveryTime = if (cep.length >= 8) "Estimativa: 3 a 5 dias úteis" else "Digite o CEP completo"

                val isRental = checkoutType == "ALUGUEL" && checkoutReturnDate != null
                val rentDays = if (isRental) com.example.trabalho.data.RentCalculator.calculateDaysBetween(Date(), checkoutReturnDate!!) else 0
                val subtotal = if (isRental) currentGame.rentPrice * rentDays else 0.0
                val finalPrice = if (isRental) com.example.trabalho.data.RentCalculator.calculateTotalRentPrice(currentGame.rentPrice, rentDays) else currentGame.salePrice
                val discount = if (isRental) subtotal - finalPrice else 0.0

                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                    Text("Resumo do Pedido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Jogo: ${currentGame.title}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Tipo: $checkoutType", color = MaterialTheme.colorScheme.primary)

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isRental) {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Diária:"); Text("R$ ${"%.2f".format(currentGame.rentPrice)}") }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Período:"); Text("$rentDays dias (Até ${sdf.format(checkoutReturnDate)})") }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Subtotal:"); Text("R$ ${"%.2f".format(subtotal)}") }

                                if (discount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Desconto Progressivo:", color = MaterialTheme.colorScheme.tertiary) // Azul Informativo
                                        Text("- R$ ${"%.2f".format(discount)}", color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("R$ ${"%.2f".format(finalPrice)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                }

                                // --- AVISO DE MULTA DE ALUGUER (Amarelo Semântico) ---
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                                    Text(
                                        text = "Atenção: A devolução em atraso acarretará multa de 2x o valor da diária (R$ ${"%.2f".format(currentGame.rentPrice * 2)}) por cada dia excedente.",
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Preço do Jogo:"); Text("R$ ${"%.2f".format(finalPrice)}") }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("R$ ${"%.2f".format(finalPrice)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Opções de Recebimento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Quero receber em casa (Entrega)")
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = isDelivery, onCheckedChange = { isDelivery = it })
                    }

                    if (isDelivery) {
                        OutlinedTextField(
                            value = cep, onValueChange = { if (it.length <= 8) cep = it },
                            label = { Text("Digite seu CEP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                        )
                        Text(text = deliveryTime, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.confirmOrder(isDelivery, cep, finalPrice) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isDelivery || cep.length >= 8
                    ) { Text("Confirmar Pedido") }
                }

            } else {
                // --- TELA DETALHES ---
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                    Text(currentGame.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(currentGame.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Descrição", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(currentGame.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (currentGame.stockQuantity > 0) "Em Estoque: ${currentGame.stockQuantity} unidades" else "ESGOTADO",
                                color = if (currentGame.stockQuantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Comprar Definitivo:", fontWeight = FontWeight.Bold); Text("R$ ${"%.2f".format(currentGame.salePrice)}") }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Aluguel (Diária):", fontWeight = FontWeight.Bold); Text("R$ ${"%.2f".format(currentGame.rentPrice)} / dia") }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { showDatePicker = true }, modifier = Modifier.weight(1f).height(50.dp), enabled = currentGame.stockQuantity > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) { Text("Alugar") }
                        Button(
                            onClick = { viewModel.preparePurchase() }, modifier = Modifier.weight(1f).height(50.dp), enabled = currentGame.stockQuantity > 0
                        ) { Text("Comprar") }
                    }
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis
                                if (selectedMillis != null) { viewModel.prepareRental(Date(selectedMillis)) }
                                showDatePicker = false
                            }) { Text("Confirmar") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(text = "Cancelar", color = MaterialTheme.colorScheme.error) } }
                    ) { DatePicker(state = datePickerState) }
                }
            }
        }
    }
}
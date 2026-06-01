package com.example.trabalho.ui.client.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import com.example.trabalho.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val profileFeedback by viewModel.profileFeedback.collectAsState()
    val filteredUserOrders by viewModel.filteredUserOrders.collectAsState()
    val selectedOrderFilter by viewModel.selectedOrderFilter.collectAsState()

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var showNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    LaunchedEffect(profileFeedback) {
        profileFeedback?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Icon(Icons.Default.Person, contentDescription = "Foto de Perfil", modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = userEmail, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Azul Semântico
                Text(text = "Conta: ${userRole.uppercase()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { newNameInput = userName; showNameDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary) // Laranja de Edição
                ) { Text("Alterar Meu Nome") }

                OutlinedButton(
                    onClick = { viewModel.sendPasswordReset() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary) // Laranja de Edição
                ) { Text("Redefinir Senha via E-mail") }

                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/jogustto") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary) // Azul Informativo
                ) { Text("Ajuda / Suporte Técnico") }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair da Conta")
                }

                Spacer(modifier = Modifier.height(32.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Histórico de Pedidos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TODOS", "COMPRA", "ALUGUEL").forEach { filter ->
                        FilterChip(selected = selectedOrderFilter == filter, onClick = { viewModel.updateOrderFilter(filter) }, label = { Text(filter) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (filteredUserOrders.isEmpty()) {
                    Text("Nenhum registo encontrado para este filtro.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            items(filteredUserOrders) { order ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)
                val orderDateStr = order.createdAt?.let { sdf.format(it) } ?: "Data pendente"

                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = order.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = orderDateStr, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = order.gameTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Total pago: R$ ${"%.2f".format(order.pricePaid)}")
                        Text(text = "Modalidade: ${order.deliveryMethod} ${if(order.deliveryZipCode.isNotBlank()) "(CEP: ${order.deliveryZipCode})" else ""}")

                        if (order.type == "ALUGUEL" && order.returnDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            if (order.returned) {
                                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                                    Text("Devolvido", color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // --- AVISO DE COR DINÂMICA (LIMPO E SEM CORES CHUMBADAS) ---
                                val daysLate = com.example.trabalho.data.RentCalculator.calculateDaysLate(order.returnDate)
                                val (containerColor, textColor, statusText) = when {
                                    daysLate > 0 -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "Atrasado! Sujeito a multa")
                                    daysLate in -2..0 -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "Devolver até: ${sdf.format(order.returnDate)}")
                                    else -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "Devolver até: ${sdf.format(order.returnDate)}")
                                }

                                Surface(color = containerColor, shape = MaterialTheme.shapes.small) {
                                    Text(
                                        text = statusText,
                                        color = textColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Alterar Nome") },
                text = { OutlinedTextField(value = newNameInput, onValueChange = { newNameInput = it }, label = { Text("Novo Nome Completo") }, modifier = Modifier.fillMaxWidth()) },
                confirmButton = { TextButton(onClick = { if (newNameInput.isNotBlank()) viewModel.changeName(newNameInput); showNameDialog = false }) { Text("Salvar") } },
                dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text(text = "Cancelar", color = MaterialTheme.colorScheme.error) } }
            )
        }
    }
}
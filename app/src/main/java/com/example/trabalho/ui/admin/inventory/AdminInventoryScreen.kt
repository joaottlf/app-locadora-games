package com.example.trabalho.ui.admin.inventory

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.trabalho.data.model.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventoryScreen(
    viewModel: AdminInventoryViewModel,
    onBackClick: () -> Unit
) {
    val gameList by viewModel.gameList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedbackMessage()
        }
    }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estados do Formulário
    var currentId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var rentPrice by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }

    var isTrending by remember { mutableStateOf(false) }
    var isNewRelease by remember { mutableStateOf(false) }

    fun openSheetForAdd() {
        currentId = ""
        title = ""
        category = ""
        description = ""
        salePrice = ""
        rentPrice = ""
        stockQuantity = ""
        isTrending = false
        isNewRelease = false
        showSheet = true
    }

    fun openSheetForEdit(game: Game) {
        currentId = game.id
        title = game.title
        category = game.category
        description = game.description
        salePrice = game.salePrice.toString()
        rentPrice = game.rentPrice.toString()
        stockQuantity = game.stockQuantity.toString()
        isTrending = game.isTrending
        isNewRelease = game.isNewRelease
        showSheet = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Estoque") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openSheetForAdd() },
                containerColor = MaterialTheme.colorScheme.primary, // Verde Padrão
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Jogo")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(gameList) { game ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Título livre para usar o espaço todo
                            Text(text = game.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Estoque: ${game.stockQuantity}", color = if (game.stockQuantity < 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            Text(text = "Venda: R$ ${game.salePrice} | Locação: R$ ${game.rentPrice}", style = MaterialTheme.typography.bodySmall)

                            // --- MOVIDO PARA CÁ: Tags organizadas abaixo das informações de preço ---
                            if (game.isNewRelease || game.isTrending) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (game.isNewRelease) {
                                        Surface(color = MaterialTheme.colorScheme.tertiary, shape = MaterialTheme.shapes.small) {
                                            Text("Novo", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiary)
                                        }
                                    }
                                    if (game.isTrending) {
                                        Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small) {
                                            Text("Em Alta", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            }
                        }

                        // Botões de ação alinhados à direita
                        Column {
                            IconButton(onClick = { openSheetForEdit(game) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                            }
                            IconButton(onClick = { viewModel.deleteGame(game.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) // Vermelho
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FORMULÁRIO COM SCROLL INTERNO ---
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentId.isBlank()) "Adicionar Novo Jogo" else "Editar Jogo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 5
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Preço Venda") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = rentPrice, onValueChange = { rentPrice = it }, label = { Text("Preço Locação") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }

                OutlinedTextField(value = stockQuantity, onValueChange = { stockQuantity = it }, label = { Text("Quantidade Estoque") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marcar como Lançamento (Novidade)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isNewRelease, onCheckedChange = { isNewRelease = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Exibir na lista 'Em Alta'", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isTrending, onCheckedChange = { isTrending = it })
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val parsedSale = salePrice.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val parsedRent = rentPrice.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val parsedStock = stockQuantity.toIntOrNull() ?: 0

                        val gameToSave = Game(
                            id = currentId,
                            title = title,
                            category = category,
                            description = description,
                            salePrice = parsedSale,
                            rentPrice = parsedRent,
                            stockQuantity = parsedStock,
                            isTrending = isTrending,
                            isNewRelease = isNewRelease
                        )

                        viewModel.saveGame(gameToSave)
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading && title.isNotBlank() && category.isNotBlank()
                ) {
                    Text("Guardar Jogo")
                }
            }
        }
    }
}
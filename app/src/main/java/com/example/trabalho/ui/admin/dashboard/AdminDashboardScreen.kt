package com.example.trabalho.ui.admin.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabalho.ui.components.GameItemCard

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onManageInventoryClick: () -> Unit,
    onAdminProfileClick: () -> Unit,
    onManageOrdersClick: () -> Unit
) {
    val adminName by viewModel.adminName.collectAsState()
    val totalGames by viewModel.totalGamesCount.collectAsState()
    val lowStockGames by viewModel.lowStockGames.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- CABEÇALHO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Painel Gerencial",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary // Azul Semântico (Informação)
                )
                Text(
                    text = "Olá, $adminName",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onAdminProfileClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Perfil Admin",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CARDS DE RESUMO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) // Fundo Verde Escuro
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total de Jogos", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "$totalGames", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer) // Fundo Vermelho Escuro
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estoque Baixo", color = MaterialTheme.colorScheme.onErrorContainer)
                    Text(text = "${lowStockGames.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- BOTÕES DE NAVEGAÇÃO ---
        Button(
            onClick = onManageInventoryClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Gerenciar Estoque (Adicionar/Editar)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onManageOrdersClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Fluxo de Caixa e Pedidos")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ALERTAS ---
        Text(
            text = "Alertas de Estoque Crítico",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error // Vermelho Semântico
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (lowStockGames.isEmpty()) {
            Text("Nenhum alerta. Estoque saudável!", modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lowStockGames) { game ->
                    GameItemCard(
                        game = game,
                        onClick = { onManageInventoryClick() }
                    )
                }
            }
        }
    }
}
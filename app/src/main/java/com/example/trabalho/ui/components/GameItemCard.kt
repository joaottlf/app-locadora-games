package com.example.trabalho.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabalho.data.model.Game

@Composable
fun GameItemCard(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = game.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(text = "Categoria: ${game.category}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Venda: R$ ${"%.2f".format(game.salePrice)}",
                    color = MaterialTheme.colorScheme.primary, // Verde
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aluguel: R$ ${"%.2f".format(game.rentPrice)}",
                    color = MaterialTheme.colorScheme.secondary, // Laranja
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
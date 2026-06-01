package com.example.trabalho.ui.admin.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trabalho.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    viewModel: AdminProfileViewModel,
    onLogoutSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val adminName by viewModel.adminName.collectAsState()
    val adminEmail by viewModel.adminEmail.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val profileFeedback by viewModel.profileFeedback.collectAsState()

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var showNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    var showPromoteDialog by remember { mutableStateOf(false) }
    var promoteEmailInput by remember { mutableStateOf("") }

    LaunchedEffect(profileFeedback) {
        profileFeedback?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil Administrativo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
                },
                // Verde Escuro para manter a coesão do painel administrativo
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Admin Shield",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary // Verde (Segurança Ativa)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = adminName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = adminEmail, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Alterado de 'error' para 'tertiary' (Azul Informativo)
            Text(
                text = "Privilégios: ${adminRole.uppercase()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                onClick = { newNameInput = adminName; showNameDialog = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary) // Laranja de Edição
            ) { Text("Alterar Meu Nome") }

            OutlinedButton(
                onClick = { viewModel.sendPasswordReset() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary) // Laranja de Edição
            ) { Text("Redefinir Senha via E-mail") }

            OutlinedButton(
                onClick = { uriHandler.openUri("https://github.com/joaottlf/app-locadora-games") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary) // Azul Informativo
            ) { Text("Ajuda / Suporte Técnico") }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { promoteEmailInput = ""; showPromoteDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Verde
            ) { Text("Conceder Privilégios a um Novo Admin") }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logout()
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Vermelho de Saída
            ) {
                Text("Sair da Conta")
            }
        }

        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Alterar Nome") },
                text = {
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        label = { Text("Novo Nome Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { if (newNameInput.isNotBlank()) viewModel.changeName(newNameInput); showNameDialog = false }) { Text("Salvar") }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) { Text(text = "Cancelar", color = MaterialTheme.colorScheme.error) }
                }
            )
        }

        if (showPromoteDialog) {
            AlertDialog(
                onDismissRequest = { showPromoteDialog = false },
                title = { Text("Conceder Acesso Admin") },
                text = {
                    Column {
                        Text("O novo funcionário deve criar uma conta normal no aplicativo primeiro. Depois, digite o e-mail dele abaixo:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = promoteEmailInput, onValueChange = { promoteEmailInput = it },
                            label = { Text("E-mail do Funcionário") }, modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (promoteEmailInput.isNotBlank()) viewModel.promoteToAdmin(promoteEmailInput)
                            showPromoteDialog = false
                        }
                    ) { Text("Promover") }
                },
                dismissButton = { TextButton(onClick = { showPromoteDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}
package com.example.trabalho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trabalho.data.AuthRepository
import com.example.trabalho.data.GameRepository
import com.example.trabalho.data.UserRepository
import com.example.trabalho.ui.admin.dashboard.AdminDashboardScreen
import com.example.trabalho.ui.admin.dashboard.AdminDashboardViewModel
import com.example.trabalho.ui.auth.AuthScreen
import com.example.trabalho.ui.auth.AuthViewModel
import com.example.trabalho.ui.client.home.HomeScreen
import com.example.trabalho.ui.client.home.HomeViewModel
import com.example.trabalho.ui.client.profile.ProfileScreen
import com.example.trabalho.ui.client.profile.ProfileViewModel
import com.example.trabalho.ui.theme.TrabalhoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trabalho.ui.admin.inventory.AdminInventoryScreen
import com.example.trabalho.ui.admin.inventory.AdminInventoryViewModel
import com.example.trabalho.ui.admin.profile.AdminProfileScreen
import com.example.trabalho.ui.admin.profile.AdminProfileViewModel
import com.example.trabalho.ui.client.details.GameDetailsScreen
import com.example.trabalho.ui.client.details.GameDetailsViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Repositórios instanciados aqui (Correto!)
        authRepository = AuthRepository(auth, db)
        val userRepository = UserRepository(db)
        val gameRepository = GameRepository(db)

        setContent {
            TrabalhoTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "auth",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("auth") {
                            // Criando usando a Fábrica para evitar o erro do Compose!
                            val authViewModel: AuthViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return AuthViewModel(authRepository, userRepository) as T
                                    }
                                }
                            )

                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { userRole ->
                                    val destination = if (userRole == "admin") "admin_dashboard" else "home"
                                    navController.navigate(destination) {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val homeViewModel: HomeViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return HomeViewModel(authRepository, userRepository, gameRepository) as T
                                    }
                                }
                            )


                            HomeScreen(
                                viewModel = homeViewModel,
                                onCategoryClick = { clickedCategory ->
                                    navController.navigate("category/$clickedCategory")
                                },
                                onGameClick = { gameId ->
                                    navController.navigate("game_details/$gameId")
                                },
                                onProfileClick = {
                                    navController.navigate("client_profile")
                                }
                            )
                        }

                        composable("game_details/{gameId}") { backStackEntry ->
                            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""

                            val gameDetailsViewModel: GameDetailsViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return GameDetailsViewModel(authRepository, gameRepository, gameId) as T
                                    }
                                }
                            )

                            GameDetailsScreen(
                                viewModel = gameDetailsViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ... logo a seguir à rota "home" ou "game_details":

                        composable("category/{categoryName}") { backStackEntry ->
                            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""

                            val categoryViewModel: com.example.trabalho.ui.client.category.CategoryViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return com.example.trabalho.ui.client.category.CategoryViewModel(gameRepository, categoryName) as T
                                    }
                                }
                            )

                            com.example.trabalho.ui.client.category.CategoryScreen(
                                categoryName = categoryName,
                                viewModel = categoryViewModel,
                                onGameClick = { gameId ->
                                    navController.navigate("game_details/$gameId")
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("client_profile") {
                            val profileViewModel: ProfileViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return ProfileViewModel(authRepository, userRepository, gameRepository) as T
                                    }
                                }
                            )

                            ProfileScreen(
                                viewModel = profileViewModel,
                                onLogoutSuccess = {
                                    navController.navigate("auth") { popUpTo(0) }
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("admin_dashboard") {
                            val adminDashboardViewModel: AdminDashboardViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return AdminDashboardViewModel(authRepository, userRepository, gameRepository) as T
                                    }
                                }
                            )

                            AdminDashboardScreen(
                                viewModel = adminDashboardViewModel,
                                onManageInventoryClick = {
                                     navController.navigate("admin_inventory")
                                },
                                onAdminProfileClick = {
                                     navController.navigate("admin_profile")
                                },
                                onManageOrdersClick = {
                                    navController.navigate("admin_orders")
                                }
                            )
                        }
                        composable("admin_inventory") {
                            val adminInventoryViewModel: AdminInventoryViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return AdminInventoryViewModel(gameRepository) as T
                                    }
                                }
                            )

                            AdminInventoryScreen(
                                viewModel = adminInventoryViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("admin_profile") {
                            val adminProfileViewModel: AdminProfileViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return AdminProfileViewModel(authRepository, userRepository) as T
                                    }
                                }
                            )

                            AdminProfileScreen(
                                viewModel = adminProfileViewModel,
                                onLogoutSuccess = {
                                    navController.navigate("auth") {
                                        popUpTo(0)
                                    }
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ... (junto das rotas de admin)
                        composable("admin_orders") {
                            val adminOrdersViewModel: com.example.trabalho.ui.admin.orders.AdminOrdersViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                        return com.example.trabalho.ui.admin.orders.AdminOrdersViewModel(gameRepository) as T
                                    }
                                }
                            )

                            com.example.trabalho.ui.admin.orders.AdminOrdersScreen(
                                viewModel = adminOrdersViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

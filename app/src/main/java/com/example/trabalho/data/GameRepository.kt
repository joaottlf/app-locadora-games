package com.example.trabalho.data

import android.util.Log
import com.example.trabalho.data.model.Game
import com.example.trabalho.data.model.Order
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GameRepository(private val db: FirebaseFirestore) {
    fun getGamesRealtime(): Flow<List<Game>> = callbackFlow {
        Log.d("DEBUG_LOGOUT", "1. Iniciando o Listener do Firestore...")
        val listenerRegistration = db.collection("games")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DEBUG_LOGOUT", "2. Erro no Firestore (Provavelmente porque o usuário fez Logout): ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val gamesList = snapshot.documents.mapNotNull { document ->
                        document.toObject(Game::class.java)
                    }
                    trySend(gamesList).isSuccess
                }
            }
        awaitClose {
            Log.d("DEBUG_LOGOUT", "3. Fechando o Listener do Firestore.")
            listenerRegistration.remove()
        }
    }

    fun addGame(game: Game, onResult: (Boolean, String?) -> Unit) {
        db.collection("games").add(game)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun updateGame(game: Game, onResult: (Boolean, String?) -> Unit) {
        if (game.id.isBlank()) {
            onResult(false, "Erro: ID do jogo inválido")
            return
        }

        db.collection("games").document(game.id).set(game)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun deleteGame(gameId: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("games").document(gameId).delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun getGameById(gameId: String, onResult: (Game?) -> Unit) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val game = document.toObject(Game::class.java)
                    onResult(game)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun buyGame(order: Order, onResult: (Boolean, String?) -> Unit) {
        val gameRef = db.collection("games").document(order.gameId)
        val orderRef = db.collection("orders").document()

        db.runTransaction { transaction ->
            val gameSnapshot = transaction.get(gameRef)
            val currentStock = gameSnapshot.getLong("stockQuantity") ?: 0

            if (currentStock <= 0) {
                throw Exception("Desculpe, este jogo acabou de esgotar!")
            }

            transaction.update(gameRef, "stockQuantity", currentStock - 1)

            val orderData = hashMapOf(
                "userId" to order.userId,
                "gameId" to order.gameId,
                "gameTitle" to order.gameTitle,
                "pricePaid" to order.pricePaid,
                "type" to order.type,
                "rentDays" to order.rentDays,
                "returnDate" to order.returnDate,
                "deliveryMethod" to order.deliveryMethod,
                "deliveryZipCode" to order.deliveryZipCode,
                "createdAt" to FieldValue.serverTimestamp()
            )
            transaction.set(orderRef, orderData)
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.message)
        }
    }

    fun getUserOrders(userId: String, onResult: (List<com.example.trabalho.data.model.Order>) -> Unit) {
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.toObjects(com.example.trabalho.data.model.Order::class.java)
                onResult(orders.sortedByDescending { it.createdAt })
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // --- BUSCAR TODOS OS PEDIDOS (Para o Painel Financeiro do Admin) ---
    fun getAllOrders(onResult: (List<com.example.trabalho.data.model.Order>) -> Unit) {
        db.collection("orders")
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.toObjects(com.example.trabalho.data.model.Order::class.java)
                // Ordena do mais recente para o mais antigo
                onResult(orders.sortedByDescending { it.createdAt })
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // --- MARCAR ALUGUEL COMO DEVOLVIDO (ATUALIZADO PARA O BLOCO 3) ---
    fun returnRentedGame(order: com.example.trabalho.data.model.Order, finalPricePaid: Double, onResult: (Boolean, String?) -> Unit) {
        val gameRef = db.collection("games").document(order.gameId)
        val orderRef = db.collection("orders").document(order.id)

        db.runTransaction { transaction ->
            val orderSnapshot = transaction.get(orderRef)
            if (orderSnapshot.getBoolean("returned") == true) {
                throw Exception("Este pedido já foi marcado como devolvido!")
            }

            val gameSnapshot = transaction.get(gameRef)
            val currentStock = gameSnapshot.getLong("stockQuantity") ?: 0

            transaction.update(gameRef, "stockQuantity", currentStock + 1)
            transaction.update(orderRef, "returned", true)
            // NOVO: Atualiza o preço com a multa ou recálculo no fluxo de caixa
            transaction.update(orderRef, "pricePaid", finalPricePaid)

        }.addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
}

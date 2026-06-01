package com.example.trabalho.data

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
class UserRepository(
    private val db: FirebaseFirestore
) {
    fun getUserName(uid: String, onResult: (String) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "Aventureiro"
                onResult(name)
            }
            .addOnFailureListener {
                onResult("Aventureiro")
            }
    }

    fun getUserEmail(uid: String, onResult: (String) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val email = document.getString("email") ?: "E-mail não encontrado."
                onResult(email)
            }
            .addOnFailureListener {
                onResult("E-mail não encontrado!")
            }
    }

    fun getUserRole(uid: String, onResult: (String) -> Unit) {
        Log.d("APP_DEBUG", "4. [UserRepository] Bateu no Repositório! Pedindo documento do Firestore...")

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                Log.d("APP_DEBUG", "5. [UserRepository] Firestore respondeu com Sucesso!")
                val role = document.getString("role") ?: "usuário" // Fallback seguro
                onResult(role)
            }
            .addOnFailureListener { e ->
                Log.e("APP_DEBUG", "5. [UserRepository] ERRO no Firestore: ${e.message}")
                onResult("usuário") // Em caso de erro, devolve usuário comum
            }
    }

    fun updateUserName(uid: String, newName: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("users").document(uid).update("name", newName)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun promoteUserToAdmin(email: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(false, "Nenhum usuário encontrado com este e-mail.")
                } else {
                    val documentId = documents.documents[0].id
                    db.collection("users").document(documentId)
                        .update("role", "admin")
                        .addOnSuccessListener { onResult(true, null) }
                        .addOnFailureListener { e -> onResult(false, e.message) }
                }
            }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
}
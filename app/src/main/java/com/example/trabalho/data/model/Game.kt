package com.example.trabalho.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Game(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val salePrice: Double = 0.0,
    val rentPrice: Double = 0.0,
    val stockQuantity: Int = 0,
    // Forçando o Firebase a ler e escrever exatamente "isTrending"
    @get:PropertyName("isTrending")
    @set:PropertyName("isTrending")
    var isTrending: Boolean = false,

    // Forçando o Firebase a ler e escrever exatamente "isNewRelease"
    @get:PropertyName("isNewRelease")
    @set:PropertyName("isNewRelease")
    var isNewRelease: Boolean = false
)
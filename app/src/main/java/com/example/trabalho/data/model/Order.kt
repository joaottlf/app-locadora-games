package com.example.trabalho.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val gameId: String = "",
    val gameTitle: String = "",
    val pricePaid: Double = 0.0,
    val type: String = "COMPRA",
    val rentDays: Int? = null,
    val returnDate: Date? = null,
    val deliveryMethod: String = "RETIRADA",
    val deliveryZipCode: String = "",
    var returned: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null
)

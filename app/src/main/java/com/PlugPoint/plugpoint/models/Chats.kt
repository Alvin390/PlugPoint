package com.PlugPoint.plugpoint.models

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class ChatUser(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = ""
)
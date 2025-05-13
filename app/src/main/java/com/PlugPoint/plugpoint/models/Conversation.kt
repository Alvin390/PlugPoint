package com.PlugPoint.plugpoint.models

import java.util.Date

data class Conversation(
    val id: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSenderId: String = ""
) : Comparable<Conversation> {
    override fun compareTo(other: Conversation): Int {
        return other.lastMessageTime.compareTo(this.lastMessageTime) // Descending order
    }
}
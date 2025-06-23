package com.PlugPoint.plugpoint.utilis

/**
 * Central place for Firestore collection names so we avoid hard-coding strings across the code-base.
 * NOTE: These constants keep the existing names that the app already writes to; we are not renaming
 * anything – just referencing them consistently.
 */
object FirestoreCollections {
    const val SUPPLIERS = "suppliers"
    const val CONSUMERS = "consumers"
    const val REQUESTS = "requests"
    const val ACCEPTED_REQUESTS = "accepted_requests"
    const val CONVERSATIONS = "conversations"
    const val MESSAGES = "messages" // chat message subcollection
    const val COMMODITIES_TOP = "commodities" // top-level commodity collection
    // Sub-collection name that lives under each supplier document
    const val COMMODITIES_SUB = "commodities"
}

package org.renxo.deeplinkapplication.networking

import kotlinx.serialization.Serializable

@Serializable
data class DetailResponse(
    val text: String? = null,
    val id: Int? = null,
)


@Serializable
data class DetailModel(
    val user_id: String,
)

@Serializable
data object NotImportant : Any()


@Serializable
data class RefreshTokenRequest(val token: String)
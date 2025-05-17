package org.renxo.deeplinkapplication.networking

import kotlinx.serialization.Serializable

@Serializable
data class DetailResponse(
    val fields: FieldsModel? = null,
    val contact_id: Int? = null,
    val templates: List<TemplatesModel?>? = null,
)


@Serializable
data class FieldsModel(
    val address: String? = null,
    val company: String? = null,
    val email: String? = null,
    val job_title: String? = null,
    val name: String? = null,
    val phone_no: String? = null,
    val website: String? = null,
)

@Serializable
data class DetailModel(
    val id: String,
    val username: String,
    val password: String,
)

@Serializable
data class TemplatesModel(
    val template_id: String,
)

@Serializable
data object NotImportant : Any()


@Serializable
data class RefreshTokenRequest(val token: String)
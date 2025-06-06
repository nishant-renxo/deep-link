package org.renxo.deeplinkapplication.models

import kotlinx.serialization.Serializable

@Serializable
data class DetailResponse(
    val fields: FieldsModel? = null,
    val contact_id: Int? = null,
    val templates: List<TemplatesModel?>? = null,
)


@Serializable
data class FieldsModel(
    val address: List<Addresses?>? = null,
    val company_name: String? = null,
    val company_logo: String? = null,
    val designation: String? = null,
    val emails: List<Emails?>? = null,
    val dates: List<String?>? = null,
    val relationships: List<String?>? = null,
    val phone_numbers: List<PhoneNumbers>? = null,
    val urls: List<Urls>? = null,
    val name: String? = null,
    val job_title: String? = null,
    val tag_line: String? = null,
)

@Serializable
data class Addresses(val address: String? = null)

@Serializable
data class Emails(val email: String? = null)

@Serializable
data class PhoneNumbers(val phone_no: String? = null)

@Serializable
data class Urls(val url: String? = null)



@Serializable
data class TemplatesModel(
    val template_id: String,
)


@Serializable
data class GenerateTokenRequest(
    val session_id: String,
)

@Serializable
data class GenerateTokenResponse(
    val expiry: Int? = null,
    val token: String? = null,
)

@Serializable
data object NotImportant : Any()


@Serializable
data class RefreshTokenRequest(val token: String)
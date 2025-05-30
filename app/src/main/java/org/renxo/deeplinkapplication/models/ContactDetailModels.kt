package org.renxo.deeplinkapplication.models

import kotlinx.serialization.Serializable

@Serializable

data class User(
    val display_name: String? = null,
    val language_code: String? = null,
    val language_id: String? = null,
    val roles: List<String?>? = null,
    val session_id: String? = null,
    val type: String? = null,
    val user: String? = null,
)

@Serializable

data class TemplateData(
    val contact_id: String? = null,
    val current_price: Int? = null,
    val list_price: Int? = null,
    val svg: String? = null,
    val template_id: String? = null,
    val template_name: String? = null,
)

@Serializable

data class Template(
    val template_id: String? = null,
)


@Serializable

data class Fields(
    val CompanyName: String? = null,
    val address: String? = null,
    val company: String? = null,
    val email: String? = null,
    val first_name: String? = null,
    val job_title: String? = null,
    val last_name: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val phone_no: String? = null,
    val website: String? = null,
)

@Serializable
data class Contact(
    val contact_id: String? = null,
    val display_name: String? = null,
    val fields: Fields? = null,
    val language_code: String? = null,
    val session: String? = null,
    val template_data: TemplateData? = null,
    val templates: List<Template?>? = null,
    val user_id: String? = null,
    val web_id: String? = null,
)
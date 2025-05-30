package org.renxo.deeplinkapplication.models


import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ParamModel(
//    val type: String,
    val action: String,
//    val transaction: String? = null,
    @Serializable(with = DynamicMapSerializer::class)
    val params: HashMap<String, @Contextual Any?>? = null,
    @Serializable(with = DynamicMapSerializer::class)
    val payload: HashMap<String, @Contextual Any?>? = null,
)

@Serializable
data class Result(
    val code: String? = null,
    val variables: HashMap<String, String?>? = null,
)


@Serializable
data class ResponseModel(
    val type: String,
    val action: String,
    val transaction: String? = null,
    val orig_action: String? = null,
    val result: Result? = null,
    @Serializable(with = DynamicToStringMapSerializer::class)
    val params: HashMap<String, String?>? = null,

)




/////////////////////



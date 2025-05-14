package org.renxo.deeplinkapplication.networking

import io.ktor.http.ContentType
import java.io.File


data class KeyValue(val key: String, val value: String?)

data class MultiPartObj(
    val key: String,
    val file: File,
    val contentType: ContentType? = null,
    val contentDisposition: String? = key,
)

data class ApiException(val code: Int = 0, val errorMessage: String) : Exception()

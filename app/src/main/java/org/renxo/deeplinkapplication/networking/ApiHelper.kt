package org.renxo.deeplinkapplication.networking

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


class ApiHelper( val client: HttpClient,  val baseUrl: String = "http://url/api/") {

    suspend inline fun <reified T> getRequest(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
    ): Result<T> {
        return try {
            val response = client.get(urlString = baseUrl + endPoint) {
                params.forEach {
                    parameter(it.key, it.value)
                }
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    authToken?.let {
                        append(HttpHeaders.Authorization, it)
                    }
                }
            }

            if (response.status.value == 200) {
                Result.success(response.body())
            } else {
                Result.failure(
                    ApiException(
                        response.status.value,
                        response.status.description
                    )
                )
            }
        } catch (e: CancellationException) {
            // Re-throw cancellation exceptions to properly support coroutine cancellation
            throw e
        } catch (e: Exception) {
            Result.failure(ApiException(0, e.message ?: "Unknown error"))
        }
    }

    suspend inline fun <reified T, reified K> postRequest(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
        formData: List<MultiPartObj> = emptyList(),
        body: K? = null,
    ): Result<T> {
        return try {
            val fullUrl = if (endPoint.startsWith("http")) endPoint else baseUrl + endPoint

            val response = client.post(urlString = fullUrl) {
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    authToken?.let {
                        append(HttpHeaders.Authorization, it)
                    }
                }

                when {
                    body != null -> {
                        contentType(ContentType.Application.Json)
                        setBody(body)
                    }
                    params.isNotEmpty() -> {
                        contentType(ContentType.Application.FormUrlEncoded)
                        setBody(FormDataContent(Parameters.build {
                            params.forEach {
                                append(it.key, it.value.toString())
                            }
                        }))
                    }
                    formData.isNotEmpty() -> {
                        setBody(
                            MultiPartFormDataContent(
                                formData {
                                    params.forEach {
                                        append(it.key, it.value.toString())
                                    }
                                    formData.forEach {
                                        append(it.key, it.file.readBytes(), Headers.build {
                                            append(
                                                HttpHeaders.ContentDisposition,
                                                "filename=\"${it.file.name}\""
                                            )
                                            it.contentType?.let { type ->
                                                append(HttpHeaders.ContentType, type.toString())
                                            }
                                        })
                                    }
                                }
                            )
                        )
                    }
                }
            }

            if (response.status.value == 200) {
                Result.success(response.body())
            } else {
                Result.failure(
                    ApiException(
                        response.status.value,
                        response.status.description
                    )
                )
            }
        } catch (e: CancellationException) {
            Result.failure(ApiException(0, e.message ?: "CancellationException"))
        } catch (e: Exception) {
            Result.failure(ApiException(0, e.message ?: "Unknown error"))
        }
    }

    suspend inline fun <reified T, reified K> deleteRequest(
        endPoint: String,
        authToken: String? = null,
        params: List<KeyValue> = emptyList(),
        formData: List<MultiPartObj> = emptyList(),
        body: K? = null,
    ): Result<T> {
        return try {
            val response = client.delete(urlString = baseUrl + endPoint) {
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    authToken?.let {
                        append(HttpHeaders.Authorization, it)
                    }
                }

                when {
                    body != null -> {
                        contentType(ContentType.Application.Json)
                        setBody(body)
                    }
                    params.isNotEmpty() -> {
                        contentType(ContentType.Application.FormUrlEncoded)
                        setBody(FormDataContent(Parameters.build {
                            params.forEach {
                                append(it.key, it.value.toString())
                            }
                        }))
                    }
                    formData.isNotEmpty() -> {
                        setBody(
                            MultiPartFormDataContent(
                                formData {
                                    params.forEach {
                                        append(it.key, it.value.toString())
                                    }
                                    formData.forEach {
                                        append(it.key, it.file.readBytes(), Headers.build {
                                            append(
                                                HttpHeaders.ContentDisposition,
                                                "filename=\"${it.file.name}\""
                                            )
                                            it.contentType?.let { type ->
                                                append(HttpHeaders.ContentType, type.toString())
                                            }
                                        })
                                    }
                                }
                            )
                        )
                    }
                }
            }

            if (response.status.value == 200) {
                Result.success(response.body())
            } else {
                Result.failure(
                    ApiException(
                        response.status.value,
                        response.status.description
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ApiException(0, e.message ?: "Unknown error"))
        }
    }
}



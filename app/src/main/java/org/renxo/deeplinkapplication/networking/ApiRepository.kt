package org.renxo.deeplinkapplication.networking

private const val authUrl = "http://192.168.29.199:8092/"

class ApiRepository(private val helper: ApiHelper) {

    suspend fun getDetail(body: DetailModel) =
        helper.postRequest<DetailResponse, DetailModel>(
            endPoint = authUrl + ApiEndpoints.GET_CONTACT,
            body = body
        )
}
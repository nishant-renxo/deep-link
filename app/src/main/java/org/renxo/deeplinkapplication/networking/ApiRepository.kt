package org.renxo.deeplinkapplication.networking

private const val authUrl = "http://192.168.31.43:8090/"


class ApiRepository(private val helper: ApiHelper) {
    suspend fun getDetail(body: DetailModel) =
        helper.postRequest<DetailResponse, DetailModel>(
            authUrl + ApiEndpoints.FETCH_DATA,
            body = body
        )

}

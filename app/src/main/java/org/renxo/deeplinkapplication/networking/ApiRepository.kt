package org.renxo.deeplinkapplication.networking


class ApiRepository(private val helper: ApiHelper) {
    suspend fun getDetail(body: DetailModel, url: String) =
        helper.postRequest<DetailResponse, DetailModel>(
            url + ApiEndpoints.FETCH_DATA,
            body = body
        )

}

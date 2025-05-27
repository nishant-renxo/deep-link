package org.renxo.deeplinkapplication.networking


class ApiRepository(private val helper: ApiHelper) {

    suspend fun getDetail(body: DetailModel) =
        helper.postRequest<DetailResponse, DetailModel>(
            endPoint = "http://192.168.29.199:8092/" + ApiEndpoints.GET_CONTACT,
            body = body
        )
}
package org.renxo.deeplinkapplication.networking

import org.renxo.deeplinkapplication.models.DetailResponse
import org.renxo.deeplinkapplication.models.GenerateTokenRequest
import org.renxo.deeplinkapplication.models.GenerateTokenResponse
import org.renxo.deeplinkapplication.models.ParamModel
import org.renxo.deeplinkapplication.models.ResponseModel


class ApiRepository(private val helper: ApiHelper) {

    suspend fun getDetail(body: ParamModel) =
        helper.postRequest<ResponseModel, ParamModel>(
            endPoint = "http://192.168.29.199:8092/" + ApiEndpoints.JOIN,
            body = body
        )


    suspend fun getTokenUsingSessionID(body: GenerateTokenRequest) =
        helper.postRequest<GenerateTokenResponse, GenerateTokenRequest>(
            endPoint = "http://192.168.29.199:8092/" + ApiEndpoints.GENERATE_TOKEN,
            body = body
        )
}
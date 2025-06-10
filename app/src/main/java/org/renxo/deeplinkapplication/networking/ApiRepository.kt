package org.renxo.deeplinkapplication.networking

import org.renxo.deeplinkapplication.models.GenerateTokenRequest
import org.renxo.deeplinkapplication.models.GenerateTokenResponse
import org.renxo.deeplinkapplication.models.ParamModel
import org.renxo.deeplinkapplication.models.ResponseModel


class ApiRepository(private val helper: ApiHelper) {

    suspend fun getDetail(body: ParamModel, authToken: String?) =
        helper.postRequest<ResponseModel, ParamModel>(
            authToken = authToken,
            endPoint = "http://192.168.29.64:8082/" + ApiEndpoints.JOIN,
            body = body
        )


    suspend fun getTokenUsingSessionID(body: GenerateTokenRequest) =
        helper.postRequest<GenerateTokenResponse, GenerateTokenRequest>(
            endPoint = "http://192.168.29.64:8085/" + ApiEndpoints.JOIN,
            body = body
        )
}
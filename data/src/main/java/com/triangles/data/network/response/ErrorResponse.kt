package com.triangles.data.network.response

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("errorTypee")
    val errorType: String? = null,
    @SerializedName("statusCode")
    val statusCode: Int
)

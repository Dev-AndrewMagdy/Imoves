package com.triangles.data.network.response

import com.google.gson.annotations.SerializedName

data class BaseResponse<out T>(
    @SerializedName("success")
    val state: Boolean?,
    @SerializedName("message")
    val apiMessage: String? = null,
    @SerializedName("data")
    val data: T? = null
)
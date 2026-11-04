package com.triangles.data.network.response

import com.google.gson.annotations.SerializedName

data class BasePagingResponse<T>(
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("total_pages")
    val totalPages: Int = 0,
    @SerializedName("total_results")
    val totalResults: Int = 0,
    @SerializedName("results")
    val results: List<T> = emptyList()
)
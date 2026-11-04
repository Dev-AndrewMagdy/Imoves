package com.triangles.data.features.moves.source.remote

import com.triangles.data.features.moves.model.MovieResponse
import com.triangles.data.network.response.BaseResponse
import com.triangles.data.network.response.BasePagingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MovesApi {

    @GET("api/movies")
    suspend fun getMoves(@Query("page") page: Int): Response<BaseResponse<BasePagingResponse<MovieResponse>>?>
}
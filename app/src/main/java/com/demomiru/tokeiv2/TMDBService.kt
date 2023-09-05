package com.demomiru.tokeiv2

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBService {
    @GET("tv/popular")
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Response<TVShowResponse>

    @GET("trending/tv/day")
    suspend fun getTrendingTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Response<TVShowResponse>

}

data class TVShowResponse(
    val results: List<TVshow>
)


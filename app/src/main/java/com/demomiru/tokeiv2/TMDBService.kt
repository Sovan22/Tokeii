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

    @GET("")
    suspend fun getTVShowDetails(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Response<TVShowDetailsResponse>

}

data class TVShowResponse(
    val results: List<TVshow>
)

data class TVShowDetailsResponse(
    val backdrop_path : String,
    val overview : String,
    val original_name : String,
    val poster_path : String,
    val episode_count : String,
    val tagline : String
)


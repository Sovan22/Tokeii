package com.demomiru.tokeiv2

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("tv/{series_id}")
    suspend fun getTVShowDetails(
        @Path("series_id") seriesID: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Response<TVShowDetailsResponse>

    @GET("tv/{series_id}/season/{season_number}")
    suspend fun getEpisodeDetails(
        @Path("series_id") seriesID: String,
        @Path("season_number") season: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ) : Response<TVShowEpisodeDetailsResponse>

    @GET("search/tv")
    suspend fun searchShow(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ) : Response<TVShowResponse>

    @GET("tv/top_rated")
    suspend fun getTopRatedTVShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String

    ) : Response<TVShowResponse>



}

data class TVShowResponse(
    val results: List<TVshow>
)

data class TVShowDetailsResponse(
    val backdrop_path : String,
    val overview : String,
    val original_name : String,
    val number_of_seasons: String,
    val poster_path : String,
    val number_of_episodes : Int,
    val tagline : String
)

data class TVShowEpisodeDetailsResponse(
    val episodes : List<Episode>
)
data class Episode(
    val season_number: String,
    val episode_number: String,
    val overview: String,
    val name: String,
    val still_path: String
)

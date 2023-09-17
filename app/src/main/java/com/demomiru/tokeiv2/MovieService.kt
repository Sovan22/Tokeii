package com.demomiru.tokeiv2

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Response<MovieResponse>

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Response<MovieResponse>


    @GET("search/movie")
    suspend fun searchMovie(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ) : Response<MovieResponse>

    @GET("movie/{movie_id}")
    suspend fun getImdbId(
        @Path("movie_id") movie_id : String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ) : Response<IdDB>

    @GET("scrape") // Replace with the actual endpoint path
    fun fetchDataFromServer(
        @Header("ngrok-skip-browser-warning") value: String
    ): Response<ServerResponse>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ) : Response<MovieResponse>



}
data class IdDB(
    val id: String,
    val imdb_id: String
)

data class MovieResponse(
        val results: List<Movie>
        )
data class ServerResponse(
    val videoLink: String
    )




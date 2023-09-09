package com.demomiru.tokeiv2

import retrofit2.Response
import retrofit2.http.GET
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

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ) : Response<MovieResponse>



}

data class MovieResponse(
        val results: List<Movie>
        )




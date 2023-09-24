package com.demomiru.tokeiv2

import java.io.Serializable


data class Movie(
    val id : String,
    val original_language:String,
    val title: String,
    val poster_path : String,
    val release_date : String
) : Serializable

data class MovieFile(
    val file: String, //episode file
    val title: String// language
)

data class MovieIMDB(
    val id: String,
    val imdb_id : String
)



data class Keys(
    val file: String,
    val key : String
)

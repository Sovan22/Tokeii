package com.demomiru.tokeiv2

import java.io.Serializable


data class Movie(
    val id : String,
    val original_language:String,
    val title: String,
    val poster_path : String,
    val release_date : String
) : Serializable


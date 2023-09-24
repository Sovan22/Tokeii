package com.demomiru.tokeiv2.watching

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "continue_watching")
data class ContinueWatching (
        val progress : Int,
        val imgLink : String,
        @PrimaryKey
        val tmdbID: Int,
        val title: String,
        val episode : Int = 0,
        val season : Int = 0,
        val type : String
        ):Serializable


data class VideoData(
        val progress : Int,
        val imgLink : String,
        @PrimaryKey
        val tmdbID: Int,
        val imdbId: String? = null,
        val title: String,
        val episode : Int = 0,
        val season : Int = 0,
        val type : String,
        val videoUrl: String,
        val superId : Int? = null,
        val superSub : List<String>
        ) : Serializable
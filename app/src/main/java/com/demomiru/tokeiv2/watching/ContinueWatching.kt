package com.demomiru.tokeiv2.watching

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.demomiru.tokeiv2.utils.GogoAnime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable


class EpisodeListTypeConverter {

        private val gson = Gson()

        @TypeConverter
        fun episodeListToJson(episodes: List<GogoAnime.Episode>?): String? {
                return gson.toJson(episodes)
        }

        @TypeConverter
        fun jsonToEpisodeList(json: String?): List<GogoAnime.Episode>? {
                if (json == null) return null

                val type = object : TypeToken<List<GogoAnime.Episode>>() {}.type
                return gson.fromJson(json, type)
        }
}

@Entity(tableName = "continue_watching")
data class ContinueWatching (
        val progress : Int,
        val imgLink : String,
        @PrimaryKey(autoGenerate = true)
        val tmdbID: Int? = null,
        val title: String,
        val episode : Int = 0,
        val season : Int = 0,
        val type : String,
        val animeEp : List<GogoAnime.Episode>? = null,
        val origin : String? = null,
        val year : String? = null,
        ) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString()?:"",
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString()?:"",
                parcel.readInt(),
                parcel.readInt(),
                parcel.readString()?:"",
                parcel.createTypedArrayList(GogoAnime.Episode.CREATOR)?: listOf(),
                parcel.readString()?:"",
                parcel.readString()?:""
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeInt(progress)
                parcel.writeString(imgLink)
                parcel.writeValue(tmdbID)
                parcel.writeString(title)
                parcel.writeInt(episode)
                parcel.writeInt(season)
                parcel.writeString(type)
                parcel.writeTypedList(animeEp)
                parcel.writeString(origin)
                parcel.writeString(year)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<ContinueWatching> {
                override fun createFromParcel(parcel: Parcel): ContinueWatching {
                        return ContinueWatching(parcel)
                }

                override fun newArray(size: Int): Array<ContinueWatching?> {
                        return arrayOfNulls(size)
                }
        }
}

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
        val superSub : List<String>,
        val animeEpisode : List<GogoAnime.Episode>? = null,
        val origin: String? = null,
        val year: String? = null
        ) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString() ?: "",
                parcel.readInt(),
                parcel.readString(),
                parcel.readString() ?: "",
                parcel.readInt(),
                parcel.readInt(),
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readInt(),
                parcel.createStringArrayList() ?: ArrayList(),
                parcel.createTypedArrayList(GogoAnime.Episode.CREATOR),
                parcel.readString() ?: "",
                parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeInt(progress)
                parcel.writeString(imgLink)
                parcel.writeInt(tmdbID)
                parcel.writeString(imdbId)
                parcel.writeString(title)
                parcel.writeInt(episode)
                parcel.writeInt(season)
                parcel.writeString(type)
                parcel.writeString(videoUrl)
                parcel.writeInt(superId ?: -1) // Use -1 as the default value for null
                parcel.writeStringList(superSub)
                parcel.writeTypedList(animeEpisode)
                parcel.writeString(origin)
                parcel.writeString(year)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<VideoData> {
                override fun createFromParcel(parcel: Parcel): VideoData {
                        return VideoData(parcel)
                }

                override fun newArray(size: Int): Array<VideoData?> {
                        return arrayOfNulls(size)
                }
        }
}
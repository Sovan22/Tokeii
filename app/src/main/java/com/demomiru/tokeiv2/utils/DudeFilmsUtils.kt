package com.demomiru.tokeiv2.utils

import android.util.Log
import com.demomiru.tokeiv2.Keys
import com.demomiru.tokeiv2.MovieFile
import com.demomiru.tokeiv2.MovieIMDB
import com.demomiru.tokeiv2.Season
import com.demomiru.tokeiv2.TvIMDB
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests
import okio.GzipSource
import okio.buffer
import org.json.JSONArray
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.system.exitProcess


suspend fun getMovieImdb(tmdbID: String) : String{
    val requests = Requests()
    val headers = mapOf(
        "accept" to " application/json",
        "Authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjYWI3MzE4OTFiMjhjNWFkNjFjODVjZDk5Mzg1MWVkNyIsInN1YiI6IjY0YTk1MzUyZDFhODkzMDBhZGJlYTc5YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.8Z_74qWdW5q6iPS7G7_j3NXFFUOUUszmWNZYupFH4Fc"

    )
    val gson = Gson()
    val movieImdb = requests.get("https://api.themoviedb.org/3/movie/$tmdbID?language=en-US",
        headers = headers
    ).okhttpResponse
    val response = movieImdb.body.string()
    val imdbID = gson.fromJson(response, MovieIMDB::class.java)
    return imdbID.imdb_id
//       Log.i("ImdbID", imdbID.imdb_id)
}

suspend fun getTvImdb(tmdbID: String): String{
    val requests = Requests()
    val headers = mapOf(
        "accept" to " application/json",
        "Authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjYWI3MzE4OTFiMjhjNWFkNjFjODVjZDk5Mzg1MWVkNyIsInN1YiI6IjY0YTk1MzUyZDFhODkzMDBhZGJlYTc5YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.8Z_74qWdW5q6iPS7G7_j3NXFFUOUUszmWNZYupFH4Fc"

    )

    val tvImdb = requests.get("https://api.themoviedb.org/3/tv/$tmdbID?append_to_response=external_ids&language=en-US",
        headers = headers
    ).okhttpResponse
    val gson = Gson()
    val response = tvImdb.body.string()
    Log.i("response", response)
    val imdbID = gson.fromJson(response, TvIMDB::class.java)
    return if(imdbID.languages[0] == "hi")
        imdbID.external_ids.imdb_id
    else
        ""
}

suspend fun getMovieLink(imdbId : String): String {
    val origin = "https://log-training-i-254.site"
    val requests = Requests()
    val encoded = Base64.getEncoder().encodeToString(
        (imdbId + "-" + System.currentTimeMillis()).toByteArray(
            StandardCharsets.UTF_8
        )
    )

    val doc2 = requests.get(
        "$origin/pb/$encoded", referer =
        "https://dudefilms.bio/"
    ).document.getElementsByTag("script")

    if (doc2.size < 2) exitProcess(0)

    val script = doc2[5].toString()
    val regex = Regex("""let playerConfigs = (.*?);""")
    val matchResult = regex.find(script)

    if (matchResult != null) {
        val jsonInsideHDVBPlayer = matchResult.groupValues[1]
        Log.i("Keys And Hash", jsonInsideHDVBPlayer)
        val gson = Gson()
        val fileKeys: Keys = gson.fromJson(jsonInsideHDVBPlayer, Keys::class.java)
        Log.i("file:", fileKeys.file)

        val srcUrl = "https://log-training-i-254.site"
        val absoluteUrl = srcUrl + fileKeys.file
        val headers = mapOf(
            "Accept" to "*/*",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "en-US,en;q=0.9",
            "Content-Length" to "0",
            "Content-Type" to "application/x-www-form-urlencoded",
            "Origin" to "https://log-training-i-254.site",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36",
            "X-Csrf-Token" to fileKeys.key
        )

        val referer = "$origin/pb/$encoded"
        val doc3 = requests.post(
            absoluteUrl,
            referer = referer,
            headers = headers
        ).okhttpResponse
        val responseBody = doc3.body
        val contentEncoding = doc3.header("Content-Encoding")

        if (contentEncoding == "gzip") {
            val gzippedSource = GzipSource(responseBody.source())
            val decompressedString = gzippedSource.buffer().readUtf8()
            val jsonArray = JSONArray(decompressedString)
            val jsonObject = jsonArray.getJSONObject(0).toString()
            val movieDetails = gson.fromJson(jsonObject, MovieFile::class.java)
            val movieId = movieDetails.file
            return requests.post(
                "$origin/playlist/$movieId.txt",
                referer = referer,
                headers = headers
            ).toString()
        }

    }
    else{
        return ""
    }
    return ""
}

suspend fun getTvLink(imdbId: String, s : Int, e: Int) : String{
    val origin = "https://log-training-i-254.site"
    val requests = Requests()
    val encoded = Base64.getEncoder().encodeToString(
        (imdbId + "-" + System.currentTimeMillis()).toByteArray(
            StandardCharsets.UTF_8
        )
    )

    val doc2 = requests.get(
        "$origin/pb/$encoded", referer =
        "https://dudefilms.bio/"
    ).document.getElementsByTag("script")

    if (doc2.size < 2) exitProcess(0)
    val script = doc2[7].toString()
    val regex =
        Regex("""HDVBPlayer\((.*?)\);""")
    val matchResult = regex.find(script)

    if (matchResult != null) {
        val jsonInsideHDVBPlayer = matchResult.groupValues[1]
        Log.i("Keys And Hash", jsonInsideHDVBPlayer)
        val gson = Gson()
        val fileKeys: Keys = gson.fromJson(jsonInsideHDVBPlayer, Keys::class.java)
        Log.i("file:", fileKeys.file)

        val srcUrl = "https://log-training-i-254.site"
        val absoluteUrl = srcUrl + fileKeys.file
        val headers = mapOf(
            "Accept" to "*/*",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "en-US,en;q=0.9",
            "Content-Length" to "0",
            "Content-Type" to "application/x-www-form-urlencoded",
            "Origin" to "https://log-training-i-254.site",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36",
            "X-Csrf-Token" to fileKeys.key
        )

        val referer = "$origin/pb/$encoded"
        val doc3 = requests.post(
            absoluteUrl,
            referer = referer,
            headers = headers
        ).okhttpResponse
        val responseBody = doc3.body
        val contentEncoding = doc3.header("Content-Encoding")

        if (contentEncoding == "gzip") {
            // Decompress the gzipped response
            val gzippedSource = GzipSource(responseBody.source())
            val decompressedString = gzippedSource.buffer().readUtf8()
            val jsonArray = JSONArray(decompressedString)
            val jsonObject = jsonArray.getJSONObject(s).toString()
            Log.i("json", jsonObject)
            val episodeDetails = gson.fromJson(
                jsonObject
                    .replace("[]", ""), Season::class.java
            )
            val episode = episodeDetails.folder[e].folder[0].file.replace("~", "")
            Log.i("episode", episode)
            val doc4 = requests.post(
                "$origin/playlist/$episode.txt",
                headers = headers,
                referer = referer
            ).toString()
            Log.i("Video Link", doc4)
            return doc4
        }
    }
    else {
        return ""
    }
    return ""
}
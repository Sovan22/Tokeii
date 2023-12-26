package com.demomiru.tokeiv2.extractors

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.demomiru.tokeiv2.R
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class PrMovies {
    private val app = Requests()

    private var mainUrl = "https://prmovies.best"
    private var name = "Prmovies"



    private val gson = Gson()
    private suspend fun search(query: String): List<SearchResponse>{
        val document = app.get("$mainUrl/?s=$query").document

        val search = document.select("div.ml-item")
        val searchItems = search.map{
            val ty = it.selectFirst(".qtip-title")?.text()
            SearchResponse(
                it.selectFirst("a")?.attr("href"),
                ty,
                ty?.substringAfter("(")?.substringBefore(")"),
                it.selectFirst("img")?.attr("data-original")
            )
        }
        return searchItems
    }


    suspend fun loadLinks(
        data:String
    ): PrFile {
        val vidLink =  app.get(data).document.select("div.movieplay iframe").map { it.attr("src") }
            .map { source ->
                when {
                    source.startsWith("https://minoplres.xyz/") -> app.get(
                        source,
                        referer = "$mainUrl/"
                    ).toString().substringAfter("sources: [").substringBefore("]")

                    else -> ""
                }

            }
        return gson.fromJson(vidLink[0],PrFile::class.java)

    }

    suspend fun getPrMovieLink(query: String): List<SearchResponse>{
//        val query = "ImMATURE"
        return try {
            val searchItems = search(query)
            if(searchItems.size > 5)
                searchItems.dropLast(searchItems.size - 5)
            else
                searchItems
        }catch (e: Exception){
            listOf()
        }
        }
    }

data class SearchResponse(
    val link: String? = null,
    val title: String? = null,
    val year: String? = null,
    val image: String? = null,
)


data class PrFile (val file: String?)
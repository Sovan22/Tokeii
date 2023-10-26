package com.demomiru.tokeiv2.anime

import android.content.Context
import com.demomiru.tokeiv2.BuildConfig
import com.demomiru.tokeiv2.MainActivity
import com.demomiru.tokeiv2.utils.GogoAnime
import com.google.gson.Gson

import com.lagradost.nicehttp.Requests
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class AnimeInfo(context: Context) {
    private val cache = Cache(
        File(context.cacheDir, "http_cache"),
        50L * 1024L * 1024L // 50 MiB
    )

    private val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .build()

    private val app = Requests(okHttpClient)
    private val gson = Gson()
    private val headers = mapOf("X-MAL-CLIENT-ID" to BuildConfig.MAL_API)
    private val proxy = BuildConfig.PROXY_URL
    suspend fun getSeasonalAnimeInfo(choice: Int) : List<GogoAnime.AnimeSearchResponse>{
        return when (choice) {
            1 -> fallAnimeInfo()
            2 -> winterAnimeInfo()
            3 -> springAnimeInfo()
            else -> summerAnimeInfo()
        }
    }

    suspend fun fallAnimeInfo() : List<GogoAnime.AnimeSearchResponse>{
        val fall = app.get("${proxy}https://gogotaku.info/season/fall-2023-anime", cacheTime = 1, cacheUnit = TimeUnit.DAYS).document
            .select("div.main_body div.page_content ul.items.full li")
        val listFall: MutableList<GogoAnime.AnimeSearchResponse> = mutableListOf()
        for (li in fall){
            val imgLink = li.select("div.img a img.lazy").attr("data-original")
            val title = li.select("div.name a").attr("title")
            val gogoLink = li.select("div.name a").attr("href")
            listFall.add(GogoAnime.AnimeSearchResponse(name = title, posterUrl = imgLink,url = "https://gogoanimehd.io$gogoLink", apiName = ""))
//            println("Img: $imgLink")
//            println("Title: $title")
//            println("animeUrl: https://gogoanimehd.io$gogoLink")
        }
        return listFall.toList()
    }

    suspend fun winterAnimeInfo() : List<GogoAnime.AnimeSearchResponse>{
        val winter = app.get("${proxy}https://gogotaku.info/season/winter-2023-anime", cacheTime = 1, cacheUnit = TimeUnit.DAYS).document
            .select("div.main_body div.page_content ul.items.full li")
        val listWinter: MutableList<GogoAnime.AnimeSearchResponse> = mutableListOf()
        for (li in winter){
            val imgLink = li.select("div.img a img.lazy").attr("data-original")
            val title = li.select("div.name a").attr("title")
            val gogoLink = li.select("div.name a").attr("href")
            listWinter.add(GogoAnime.AnimeSearchResponse(name = title, posterUrl = imgLink,url = "https://gogoanimehd.io$gogoLink", apiName = ""))
//            println("Img: $imgLink")
//            println("Title: $title")
//            println("animeUrl: https://gogoanimehd.io$gogoLink")
        }
        return listWinter.toList()
    }

    suspend fun springAnimeInfo() : List<GogoAnime.AnimeSearchResponse>{
        val spring = app.get("${proxy}https://gogotaku.info/season/spring-2023-anime", cacheTime = 1, cacheUnit = TimeUnit.DAYS).document
            .select("div.main_body div.page_content ul.items.full li")
        val listSpring: MutableList<GogoAnime.AnimeSearchResponse> = mutableListOf()
        for (li in spring){
            val imgLink = li.select("div.img a img.lazy").attr("data-original")
            val title = li.select("div.name a").attr("title")
            val gogoLink = li.select("div.name a").attr("href")
            listSpring.add(GogoAnime.AnimeSearchResponse(name = title, posterUrl = imgLink,url = "https://gogoanimehd.io$gogoLink", apiName = ""))
//            println("Img: $imgLink")
//            println("Title: $title")
//            println("animeUrl: https://gogoanimehd.io$gogoLink")
        }
        return listSpring.toList()
    }

    suspend fun summerAnimeInfo() : List<GogoAnime.AnimeSearchResponse>{
        val summer = app.get("${proxy}https://gogotaku.info/season/summer-2023-anime", cacheTime = 1, cacheUnit = TimeUnit.DAYS).document
            .select("div.main_body div.page_content ul.items.full li")
        val listSummer: MutableList<GogoAnime.AnimeSearchResponse> = mutableListOf()
        for (li in summer){
            val imgLink = li.select("div.img a img.lazy").attr("data-original")
            val title = li.select("div.name a").attr("title")
            val gogoLink = li.select("div.name a").attr("href")
            listSummer.add(GogoAnime.AnimeSearchResponse(name = title, posterUrl = imgLink,url = "https://gogoanimehd.io$gogoLink", apiName = ""))
//            println("Img: $imgLink")
//            println("Title: $title")
//            println("animeUrl: https://gogoanimehd.io$gogoLink")
        }
        return listSummer.toList()
    }

    suspend fun getAnimeDetails(query:String): Related{
        val search = app.get("https://api.myanimelist.net/v2/anime?q=$query&limit=1",
            headers = headers).toString()

        val anime = gson.fromJson(search,Data::class.java)
        val id = anime.data[0].node.id

        val animeDetails = app.get("https://api.myanimelist.net/v2/anime/${id}?fields=id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_episodes,start_season,broadcast,source,average_episode_duration,rating,pictures,background,related_anime,related_manga,recommendations,studios,statistics",
            headers = headers).toString()

        return gson.fromJson(animeDetails,Related::class.java)

    }



    data class Related(
        val main_picture: MainPic,
        val related_anime: ArrayList<Anime>,
        val synopsis: String
    )
    data class MainPic(
        val medium: String,
        val large : String
    )
    data class Data(
        val data: ArrayList<Anime>
    )
    data class Anime(
        val node: AnimeDetails,
        val relation_type : String? = null
    )
    data class AnimeDetails(
        val id: Int,
        val title: String,
    )

}
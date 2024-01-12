package com.demomiru.tokeiv2.utils


import android.icu.text.CaseMap.Title
import com.demomiru.tokeiv2.BuildConfig
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests


data class Video1(
    val sourceUrls: ArrayList<String> = arrayListOf(),
    val subtitleUrls: String? = null
)

data class Video3(
    val sourceUrls: ArrayList<Sources> = arrayListOf(),
){
    data class Sources(
        val file: String,
        val title: String,
    )
}
class SmashyStream{

    private val app = Requests(baseClient = getBaseClient())
    private val gson = Gson()
    private val proxy = BuildConfig.PROXY_URL
    suspend fun getLink(isMovie: Boolean = false,id: String,s: Int,e:Int,src:String ="en") : Pair<String?,String?>{
        val getUrl = if(!isMovie) "https://embed.smashystream.com/playere.php?tmdb=$id&season=$s&episode=$e" else "https://embed.smashystream.com/playere.php?tmdb=$id"
        try {
            val sources =
                app.get(
                    "${proxy}$getUrl",
                    referer = "https://smashystream.xyz/"
                ).document
                    .select("div.dropdown-menu a.server.dropdown-item").map {
                        it.attr("data-url")
                    }
            println(sources)

            val srcUrl = if(src == "en")sources.find {
                it.contains("video1")
            }!!
            else sources.find {
                it.contains("video3")
            }!!

            val streamRes = app.get(
                srcUrl,
                referer = getUrl
            ).toString()
//                .document.getElementsByTag("script")
            println(streamRes)

            if(src == "en") {
                val streamParsed = gson.fromJson(streamRes, Video1::class.java)
                if (streamParsed.sourceUrls[0] == "null")
                    return Pair(null, null)

                var subSource: String? = null
                if (streamParsed.subtitleUrls != null) {
                    val subSources = streamParsed.subtitleUrls.split(",").toMutableList()
                    subSources.remove("")
                    val langToUrlMap = mutableMapOf<String, String>()
                    for (subSource in subSources) {
                        val lang =
                            subSource.substring(subSource.indexOf("[") + 1, subSource.indexOf("]"))
                        val url = subSource.substring(subSource.indexOf("]") + 1).replace("\\", "")
                        langToUrlMap[lang] = url
                    }
                    subSource = gson.toJson(langToUrlMap)

//                    langToUrlMap.forEach {
//                        if (it.key.contains("English"))
//                            subSource = it.value
//                    }
                    println("Sub: $subSource")
                }
                return Pair(streamParsed.sourceUrls[0], subSource)
            }else
            {
                val streamParsed = gson.fromJson(streamRes, Video3::class.java)
                return Pair(streamParsed.sourceUrls[0].file,null)
            }
        }catch (e: Exception){
            e.printStackTrace()
            return Pair(null,null)
        }
    }
}
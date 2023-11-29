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

    private val app = Requests()
    private val gson = Gson()
    private val proxy = BuildConfig.PROXY_URL
    suspend fun getLink(isMovie: Boolean = false,id: String,s: Int,e:Int) : Pair<String?,String?>{
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

            val srcUrl = sources.find {
                it.contains("video1")
            }!!
            val streamRes = app.get(
                srcUrl,
                referer = getUrl
            ).toString()
//                .document.getElementsByTag("script")
            println(streamRes)
            val streamParsed = gson.fromJson(streamRes,Video1::class.java)
            if (streamParsed.sourceUrls[0] == "null")
                return Pair(null,null)
//            return Pair(streamParsed.sourceUrls[0],streamParsed.subtitleUrls[0])

//            val content = streamRes
////            [1].html()
//
//            val filePattern = """"file":\s*"([^"]*)"""".toRegex()
//            val subtitlePattern = """"subtitle":\s*"([^"]*)"""".toRegex()
//            val fileMatchResult = filePattern.find(content)
//            val subMatchResult = subtitlePattern.find(content)
//
//
//            val fileValue = fileMatchResult?.groups?.get(1)?.value
//            val subtitleValue = subMatchResult?.groups?.get(1)?.value

//            val subSource =
//                subtitleValue?.substring(subtitleValue.indexOf("]") + 1)?.replace("\\", "")
//                    ?.replace(",", "")
//            println("Sub: $subSource")
            var subSource: String? = null
            if(streamParsed.subtitleUrls!=null){
                val subSources = streamParsed.subtitleUrls.split(",").toMutableList()
                subSources.remove("")
                val langToUrlMap = mutableMapOf<String, String>()
                for (subSource in subSources) {
                    val lang = subSource.substring(subSource.indexOf("[") + 1, subSource.indexOf("]"))
                    val url = subSource.substring(subSource.indexOf("]") + 1).replace("\\","")
                    langToUrlMap[lang] = url
                }


                langToUrlMap.forEach {
                    if(it.key.contains("English"))
                        subSource = it.value
                }
                println("Sub: $subSource")
            }

//            val vidSources = fileValue?.split(",")?.toMutableList()
//            vidSources?.remove("")
//
//            if (sources[0].contains("dud")) return  Pair(vidSources?.get(0)?.replace("\\",""),subSource)
//
//            val qualityToUrlMap = mutableMapOf<String, String>()
//            for (vidSource in vidSources!!) {
//                val quality =
//                    vidSource.substring(vidSource.indexOf("[") + 1, vidSource.indexOf("]"))
//                val url = vidSource.substring(vidSource.indexOf("]") + 1).replace("\\", "")
//                qualityToUrlMap[quality] = url
//            }
//            println(qualityToUrlMap)
//            return Pair(qualityToUrlMap["auto"],subSource)
            return Pair(streamParsed.sourceUrls[0],subSource)
        }catch (e: Exception){
            e.printStackTrace()
            return Pair(null,null)
        }
    }
}
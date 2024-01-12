package com.demomiru.tokeiv2.utils

import com.google.gson.Gson
import com.lagradost.nicehttp.Requests
import org.checkerframework.checker.units.qual.s
import java.util.Base64

class VidSrc {
    private val app = Requests(baseClient = getBaseClient())
    private val gson = Gson()
    private val url = "https://vidsrc.me/"

    private val referer = "https://vidsrc.stream/"

    private val origin = "https://vidsrc.stream"

    private val embedUrl = "${url}embed/"
    private val subtitleUrl = "https://rest.opensubtitles.org/search/imdbid-"

    private fun getHashBasedOnIndex(hash: String, index: String): String {
        var result = ""
        for (i in hash.indices step 2) {
            val j = hash.substring(i, i + 2)
            result += (j.toInt(16) xor index[(i / 2) % index.length].code).toChar()
        }
        return result
    }

    suspend fun getLink(tid:String, isMovie:Boolean, s:Int, ep:Int): Pair<String?,String?>{
        val url = if(isMovie)"${embedUrl}movie?tmdb=$tid" else "${embedUrl}tv?tmdb=$tid&season=$s&episode=$ep"
        var videoUrl : String? = null
        var subUrl: String? = null
        val absoluteUrl = app.get(url).document.select("#player_iframe").attr("src")
        val srcRcpRes = app.get("https:$absoluteUrl",referer = url).document
        val id = srcRcpRes.select("body").attr("data-i")
        val hash = srcRcpRes.select("#hidden").attr("data-h")

        if (id.isNullOrBlank() || hash.isNullOrBlank()) return Pair(null,null)

        val sourceUrl = getHashBasedOnIndex(hash, id)
        val script = app.get("https:${sourceUrl}", referer = url ).toString()
        val pattern = Regex("""file:"(.*?)"""")

        val subs = script.substringAfterLast("default_subtitles = \"").substringBefore("\";")
        if (subs != script) {
            val subSources = subs.split(",").toMutableList()
            subSources.remove("")
            val langToUrlMap = mutableMapOf<String, String>()
            for (subSource in subSources) {
                val lang =
                    subSource.substring(subSource.indexOf("[") + 1, subSource.indexOf("]"))
                val url = subSource.substring(subSource.indexOf("]") + 1).replace("\\", "")
                langToUrlMap[lang] = "https://vidsrc.stream$url"
            }
            subUrl = gson.toJson(langToUrlMap)
            if(subUrl.length<3) subUrl = null
        }


        val matchResult = pattern.find(script)
        val matchedGroup = matchResult?.groups?.get(1)?.value

        val replaced = matchedGroup?.replace(Regex("""\/\/\S+?=""")) { "" }
        val finalResult = replaced?.replace("#2", "")
        val bytes = Base64.getDecoder().decode(finalResult)
        val finalUrl = String(bytes, Charsets.UTF_8)
        if(finalUrl.contains("list.m3u8")) videoUrl = finalUrl
        return Pair(videoUrl,subUrl)
    }
}
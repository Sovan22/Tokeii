package com.demomiru.tokeiv2.extractors

import androidx.lifecycle.lifecycleScope
import com.demomiru.tokeiv2.utils.Video3
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

private val app = Requests()
private val gson = Gson()


class VidplayExtractor {

    private val url = "https://vidplay.site"

    private val referer = "https://vidplay.site/"
    private suspend fun getKeys(): List<String> {
        val url = "https://raw.githubusercontent.com/Claudemirovsky/worstsource-keys/keys/keys.json"
        val res = app.get(url).toString()
        return gson.fromJson(res, object : TypeToken<List<String>>() {}.type)
    }

    private suspend fun getEncodedId(sourceUrl: String): String {
        val id = sourceUrl.split("/e/")[1].split("?")[0]
        val keyList = getKeys()

        val c1 = Cipher.getInstance("RC4")
        c1.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyList[0].toByteArray(), "RC4"))

        val c2 = Cipher.getInstance("RC4")
        c2.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyList[1].toByteArray(), "RC4"))

        var input = id.toByteArray()
        input = c1.doFinal(input)
        input = c2.doFinal(input)

        return Base64.getEncoder().encodeToString(input).replace("/", "_")
    }

    private suspend fun getFuTokenKey(sourceUrl: String): String {
        val id = getEncodedId(sourceUrl)
        val res = app.get("${url}/futoken", referer = withContext(Dispatchers.IO) {
            URLEncoder.encode(sourceUrl, "UTF-8")
        }).toString()
        val fuKey = Regex("var\\s+k\\s*=\\s*'([^']+)'").find(res)?.groupValues?.get(1) ?: ""
        val a = mutableListOf<Int>()
        for (i in id.indices) {
            a.add(fuKey[i % fuKey.length].toInt() + id[i].toInt())
        }
        return "${fuKey},${a.joinToString(",")}"
    }

    private suspend fun getFileUrl(sourceUrl: String): String {
        val futoken = getFuTokenKey(sourceUrl)
        val url = "${this.url}/mediainfo/$futoken?${sourceUrl.split("?")[1]}"
        return url
    }

    suspend fun extractUrl(url: String): String {
        val fileUrl = getFileUrl("${url}&autostart=true")
        println(fileUrl)
        val res = app.get(fileUrl, referer = url).toString()
        val sourceRes = gson.fromJson(res, VidPlaySource::class.java)
        return sourceRes.result.sources[0].file

    }
}

data class VidPlaySource(
    val status: Int,
    val result: Src,
){
    data class Src(
        val sources: ArrayList<File>
    ){
        data class File(
            val file:String,
        )
    }
}

data class Sources(
    val status: Int,
    val result: ArrayList<Source>,
){
    data class Source(
        val id : String,
        val title: String,
    )
}

data class SourceObject(
    val status: Int,
    val result: Url,
){
    data class Url(
        val url: String,
    )
}

data class VidPlaySub(
    val file: String,
    val label: String,
)


class Vidplay {



    private val url = "https://vidsrc.to/embed/"

    private val mainUrl = "https://vidsrc.to/"

//    private vidStreamExtractor = new VidstreamExtractor();
//
//    private fileMoonExtractor = new FileMoonExtractor();
//
    private val vidPlayExtractor = VidplayExtractor()

    private val key = "8z5Ag5wgagfsOuhz"

    private fun decodeBase64UrlSafe(str: String): ByteArray {
        val standardizedInput = str.replace('_', '/').replace('-', '+')
        return Base64.getDecoder().decode(standardizedInput)
    }

    private fun decode(str: ByteArray): ByteArray {
        val keyBytes = key.toByteArray()

        var j = 0
        val s = ByteArray(256) { it.toByte() }

        for (i in s.indices) {
            j = (j + s[i].toInt() + keyBytes[i % keyBytes.size].toInt()) and 0xff
            val temp = s[i]
            s[i] = s[j]
            s[j] = temp
        }

        val decoded = ByteArray(str.size)
        var i = 0
        var k = 0
        for (index in str.indices) {
            i = (i + 1) and 0xff
            k = (k + s[i].toInt()) and 0xff
            val temp = s[i]
            s[i] = s[k]
            s[k] = temp
            val t = (s[i].toInt() + s[k].toInt()) and 0xff
            decoded[index] = (str[index].toInt() xor s[t].toInt()).toByte()
        }

        return decoded
    }

    private fun decryptSourceUrl(sourceUrl: String): String {
        val encoded = decodeBase64UrlSafe(sourceUrl)
        val decoded = decode(encoded)
        val decodedText = String(decoded, StandardCharsets.UTF_8)
        return URLDecoder.decode(URLDecoder.decode(decodedText, "UTF-8"), "UTF-8")
    }

    suspend fun getVidPlayUrl(isMovie: Boolean, season: Int, episode: Int, id: String): Pair<String?,String?>{
        val mainUrl = if(isMovie)
            "${url}movie/${id}"
        else
            "${url}tv/${id}/${season}/${episode}"
        val res = app.get(mainUrl).document
//            println(res)
        val dataId = res.selectFirst("ul.episodes > li > a")?.attr("data-id")
            ?: throw Exception("Data ID not found")
        println(dataId)
        val sourceRes = app.get(
//                "${url}ajax/embed/episode/${dataId}/sources",
            "https://vidsrc.to/ajax/embed/episode/$dataId/sources",
            headers =  mapOf(
                "X-Requested-With" to
                        "XMLHttpRequest"
            ),
            referer = mainUrl
        ).toString()
        val sources = gson.fromJson(sourceRes,Sources::class.java)
        if(sources.status != 200) throw Exception("No sources found")
        var videoUrl : String? = null
        val sourceUrls = sources.result.forEach {
            if(it.title == "Vidplay"){
                val encryptedUrl = app.get(
                    "https://vidsrc.to/ajax/embed/source/${it.id}",
                ).toString()
                val url = gson.fromJson(encryptedUrl,SourceObject::class.java).result.url
                println(url)
                val decryptedUrl = decryptSourceUrl(url)
                videoUrl = vidPlayExtractor.extractUrl(decryptedUrl)
            }
        }
        val subtitlesRes = app.get(
            "https://vidsrc.to/ajax/embed/episode/${dataId}/subtitles",
        ).toString()
        var subs : String? = null
        val subtitles = mutableMapOf<String,String>()
        val subJSONArray = JSONArray(subtitlesRes)
        for( i in 0 until subJSONArray.length())
        {
            val subObject = subJSONArray.getJSONObject(i).toString()
            val sub = gson.fromJson(subObject,VidPlaySub::class.java)
            subtitles[sub.label] = sub.file
        }

        subs = gson.toJson(subtitles)
        if (subs.length <3)subs = null
        return Pair(videoUrl,subs)

    }

}


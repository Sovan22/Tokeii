package com.demomiru.tokeiv2.utils

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

import android.util.Log
import com.demomiru.tokeiv2.watching.VideoData

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.Serializable
import java.net.URI
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil

class GogoAnime{

    private val gson = Gson()
    private val app = Requests()
    private fun getKey(id: String): String? {
        return normalSafeApiCall {
            id.map {
                it.code.toString(16)
            }.joinToString("").substring(0, 32)
        }
    }

    private fun <T> normalSafeApiCall(apiCall: () -> T): T? {
        return try {
            apiCall.invoke()
        } catch (throwable: Throwable) {
            logError(throwable)
            return null
        }
    }

    private fun logError(throwable: Throwable) {
        Log.d("ApiError", "-------------------------------------------------------------------")
        Log.d("ApiError", "safeApiCall: " + throwable.localizedMessage)
        Log.d("ApiError", "safeApiCall: " + throwable.message)
        throwable.printStackTrace()
        Log.d("ApiError", "-------------------------------------------------------------------")
    }


    val qualityRegex = Regex("(\\d+)P")

    // https://github.com/saikou-app/saikou/blob/3e756bd8e876ad7a9318b17110526880525a5cd3/app/src/main/java/ani/saikou/anime/source/extractors/GogoCDN.kt#L60
    // No Licence on the function
    private fun cryptoHandler(
        string: String,
        iv: String,
        secretKeyString: String,
        encrypt: Boolean = true
    ): String {
        //println("IV: $iv, Key: $secretKeyString, encrypt: $encrypt, Message: $string")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())
        val secretKey = SecretKeySpec(secretKeyString.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        return if (!encrypt) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            String(cipher.doFinal(base64DecodeArray(string)))
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            base64Encode(cipher.doFinal(string.toByteArray()))
        }
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun base64Decode(encodedString: String): String {
        return try {
            val decodedBytes = android.util.Base64.decode(encodedString, android.util.Base64.DEFAULT)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            decodedString
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    suspend fun extractVidstream(
        iframeUrl: String,
        mainApiName: String,
        iv: String?,
        secretKey: String?,
        secretDecryptKey: String?,
        isUsingAdaptiveKeys: Boolean,
        isUsingAdaptiveData: Boolean,
        iframeDocument: Document? = null
    ): String{

        if ((iv == null || secretKey == null || secretDecryptKey == null) && !isUsingAdaptiveKeys)
            return ""

        val id = Regex("id=([^&]+)").find(iframeUrl)!!.value.removePrefix("id=")

        var document: Document? = iframeDocument
        val foundIv =
            iv ?: (document ?: app.get(iframeUrl).document.also { document = it })
                .select("""div.wrapper[class*=container]""")
                .attr("class").split("-").lastOrNull() ?: return ""
        val foundKey = secretKey ?: getKey(base64Decode(id) + foundIv) ?: return ""
        val foundDecryptKey = secretDecryptKey ?: foundKey

        val uri = URI(iframeUrl)
        val mainUrl = "https://" + uri.host

        val encryptedId = cryptoHandler(id, foundIv, foundKey)
        val encryptRequestData = if (isUsingAdaptiveData) {
            // Only fetch the document if necessary
            val realDocument = document ?: app.get(iframeUrl).document
            val dataEncrypted =
                realDocument.select("script[data-name='episode']").attr("data-value")
            val headers = cryptoHandler(dataEncrypted, foundIv, foundKey, false)
            "id=$encryptedId&alias=$id&" + headers.substringAfter("&")
        } else {
            "id=$encryptedId&alias=$id"
        }

        val jsonResponse =
            app.get(
                "$mainUrl/encrypt-ajax.php?$encryptRequestData",
                headers = mapOf("X-Requested-With" to "XMLHttpRequest")
            )
        val dataencrypted =
            jsonResponse.text.substringAfter("{\"data\":\"").substringBefore("\"}")
        val datadecrypted = cryptoHandler(dataencrypted, foundIv, foundDecryptKey, false)
        val sources = gson.fromJson(datadecrypted, GogoSources::class.java)

        sources.source?.forEach {
            if(it.file.isBlank())
                return@forEach
             return it.file
//            Log.i("inside extract",it.file)
        }
        sources.sourceBk?.forEach {
//            invokeGogoSource(it, callback)
            if(it.file.isBlank())
                return@forEach
            return it.file
//            Log.i("inside extract sourceBk",it.file)
        }
        return ""
    }

    private var mainUrl = "https://gogoanime3.net"
//        "https://gogoanime.lu"
    private var name = "GogoAnime"
    private val hasQuickSearch = false
    private val hasMainPage = true


    val headers = mapOf(
        "authority" to "ajax.gogo-load.com",
        "sec-ch-ua" to "\"Google Chrome\";v=\"89\", \"Chromium\";v=\"89\", \";Not A Brand\";v=\"99\"",
        "accept" to "text/html, */*; q=0.01",
        "dnt" to "1",
        "sec-ch-ua-mobile" to "?0",
        "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36",
        "origin" to mainUrl,
        "sec-fetch-site" to "cross-site",
        "sec-fetch-mode" to "cors",
        "sec-fetch-dest" to "empty",
        "referer" to "$mainUrl/"
    )
    val parseRegex =
        Regex("""<li>\s*\n.*\n.*<a\s*href=["'](.*?-episode-(\d+))["']\s*title=["'](.*?)["']>\n.*?img src="(.*?)"""")


    suspend fun search(query: String): ArrayList<AnimeSearchResponse> {
        val link = "$mainUrl/search.html?keyword=$query"
        val html = app.get(link).text
        val doc = Jsoup.parse(html)

        val episodes = doc.select(""".last_episodes li""").mapNotNull {
            AnimeSearchResponse(
                it.selectFirst(".name")?.text()?.replace(" (Dub)", "") ?: return@mapNotNull null,
                fixUrl(it.selectFirst(".name > a")?.attr("href") ?: return@mapNotNull null),
                this.name,
                0,
                it.selectFirst("img")?.attr("src"),
                it.selectFirst(".released")?.text()?.split(":")?.getOrNull(1)?.trim()
                    ?.toIntOrNull(),
                it.selectFirst(".name")?.text()?.contains("Dub")
            )
        }

        return ArrayList(episodes)
    }

    private fun fixUrl(url: String): String {
        if (url.startsWith("http") ||
            // Do not fix JSON objects when passed as urls.
            url.startsWith("{\"")
        ) {
            return url
        }
        if (url.isEmpty()) {
            return ""
        }

        val startsWithNoHttp = url.startsWith("//")
        if (startsWithNoHttp) {
            return "https:$url"
        } else {
            if (url.startsWith('/')) {
                return mainUrl + url
            }
            return "$mainUrl/$url"
        }
    }

    private fun getProperAnimeLink(uri: String): String {
        if (uri.contains("-episode")) {
            val split = uri.split("/")
            val slug = split[split.size - 1].split("-episode")[0]
            return "$mainUrl/category/$slug"
        }
        return uri
    }

    data class AnimeDetails(
        val title: String? = null,
        val poster: String? = null,
        val description: String? = null,
        val genre: ArrayList<String>? = null,
        val year: Int? = null,
        val status: String? = null,
        val nativeName: String? = null,
        val type: String? = null,
        val episodes : List<Episode>? = null
    ): Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createStringArrayList(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createTypedArrayList(Episode.CREATOR)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(poster)
            parcel.writeString(description)
            parcel.writeStringList(genre)
            parcel.writeValue(year)
            parcel.writeString(status)
            parcel.writeString(nativeName)
            parcel.writeString(type)
            parcel.writeTypedList(episodes)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<AnimeDetails> {
            override fun createFromParcel(parcel: Parcel): AnimeDetails {
                return AnimeDetails(parcel)
            }

            override fun newArray(size: Int): Array<AnimeDetails?> {
                return arrayOfNulls(size)
            }
        }
    }

    suspend fun load(url: String): AnimeDetails {
        val link = getProperAnimeLink(url)
        val episodeloadApi = "https://ajax.gogo-load.com/ajax/load-list-episode"
        val doc = app.get(link).document

        val animeBody = doc.selectFirst(".anime_info_body_bg")
        val title = animeBody?.selectFirst("h1")!!.text()
        val poster = animeBody.selectFirst("img")?.attr("src")
        var description: String? = null
        val genre = ArrayList<String>()
        var year: Int? = null
        var status: String? = null
        var nativeName: String? = null
        var type: String? = null

        animeBody.select("p.type").forEach { pType ->
            when (pType.selectFirst("span")?.text()?.trim()) {
                "Plot Summary:" -> {
                    description = pType.text().replace("Plot Summary:", "").trim()
                }

                "Genre:" -> {
                    genre.addAll(pType.select("a").map {
                        it.attr("title")
                    })
                }

                "Released:" -> {
                    year = pType.text().replace("Released:", "").trim().toIntOrNull()
                }

                "Status:" -> {
                    status = pType.text().replace("Status:", "").trim()
                }

                "Other name:" -> {
                    nativeName = pType.text().replace("Other name:", "").trim()
                }

                "Type:" -> {
                    type = pType.text().replace("type:", "").trim()
                }
            }
        }


        val animeId = doc.selectFirst("#movie_id")!!.attr("value")
        val params = mapOf("ep_start" to "0", "ep_end" to "2000", "id" to animeId)

        val episodes = app.get(episodeloadApi, params = params).document.select("a").map {
            Episode(
                fixUrl(it.attr("href").trim()),
                "Episode " + it.selectFirst(".name")?.text()?.replace("EP", "")?.trim()
            )
        }.reversed()
        return AnimeDetails(
            title,
            poster,
            description,
            genre,
            year,
            status,
            nativeName,
            type,
            episodes
        )
    }

    private fun fixUrlNull(url: String?): String? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return fixUrl(url)
    }

    suspend fun extractVideos(
        uri: String,
    ) : String {
        val doc = app.get(uri).document
        var url = ""
        val iframe = fixUrlNull(doc.selectFirst("div.play-video > iframe")?.attr("src")) ?: return ""
        val link = iframe.replace("streaming.php", "download")
        val page = app.get(link, headers = mapOf("Referer" to iframe))
        page.document.select(".dowload > a").forEach {
            if (it.hasAttr("download")) {
                val qual = if (it.text()
                        .contains("HDP")
                ) "1080" else qualityRegex.find(it.text())?.destructured?.component1()
                    .toString()
            }else {
                url = it.attr("href")

            }
            Log.i("inside page",it.attr("href"))
        }

        val streamingResponse = app.get(iframe, headers = mapOf("Referer" to iframe))
        val streamingDocument = streamingResponse.document
        streamingDocument.select(".list-server-items > .linkserver")
            .forEach { element ->
                val status = element.attr("data-status") ?: return@forEach
                if (status != "1") return@forEach
                val data = element.attr("data-video") ?: return@forEach
            }
        val iv = "3134003223491201"
        val secretKey = "37911490979715163134003223491201"
        val secretDecryptKey = "54674138327930866480207815084989"
        return extractVidstream(
            iframe,
            this.name,
            iv,
            secretKey,
            secretDecryptKey,
            isUsingAdaptiveKeys = false,
            isUsingAdaptiveData = true
        )


    }

    data class Episode(
        val url : String,
        val name: String
    ): Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(url)
            parcel.writeString(name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Episode> {
            override fun createFromParcel(parcel: Parcel): Episode {
                return Episode(parcel)
            }

            override fun newArray(size: Int): Array<Episode?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class AnimeSearchResponse(
        val name: String,
        val url: String,
        val apiName: String,
        var type: Int? = null,

        var posterUrl: String? = null,
        var year: Int? = null,
        val dub: Boolean? = false,

//        var otherName: String? = null,
//        var episodes: MutableList<String> = mutableListOf(),
//
//        var id: Int? = null,
//        var quality: String? = null,
//        var posterHeaders: Map<String, String>? = null,
    )


    @SuppressLint("NewApi")
    private fun base64DecodeArray(string: String): ByteArray {
        return try {
            android.util.Base64.decode(string, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Base64.getDecoder().decode(string)
        }
    }

    @SuppressLint("NewApi")
    private fun base64Encode(array: ByteArray): String {
        return try {
            String(android.util.Base64.encode(array, android.util.Base64.NO_WRAP), Charsets.ISO_8859_1)
        } catch (e: Exception) {
            String(Base64.getEncoder().encode(array))
        }
    }

    fun onCreate() {
//        lifecycleScope.launch {
//            val searchResults = search("Link Click")
//            println(searchResults)
//            val episodeResults = load(searchResults[0].url)
//            println(episodeResults)
//            val episodeStream = extractVideos(episodeResults.episodes[0].url)
//        }
    }


    data class GogoSources(
        @JsonProperty("source") val source: List<GogoSource>?,
        @JsonProperty("sourceBk") val sourceBk: List<GogoSource>?,
        //val track: List<Any?>,
        //val advertising: List<Any?>,
        //val linkiframe: String
    )

    data class GogoSource(
        @JsonProperty("file") val file: String,
        @JsonProperty("label") val label: String?,
        @JsonProperty("type") val type: String?,
        @JsonProperty("default") val default: String? = null
    )
}

//lifecycleScope.launch {
//    val gogoSrc = GogoAnime()
//    val videoUrl = gogoSrc.extractVideos(it.url)
//
//    withContext(Dispatchers.Main) {
//        val ep = ceil(it.name.replace("Episode ", "").toDouble()).toInt()
//        val data = VideoData(
//            0,
//            animeDetails.poster!!,
//            0,
//            null,
//            animeDetails.title!!,
//            ep,
//            1,
//            "anime",
//            videoUrl,
//            null,
//            listOf(),
//            episodes?.size
//        )
//        val intent = passVideoData(
//            data, requireContext()
//        )
//        intent.putExtra("animeUrl", it.url)
//        startActivity(intent)
//    }
//}
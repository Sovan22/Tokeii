package com.demomiru.tokeiv2.utils


import android.util.Base64

import com.demomiru.tokeiv2.utils.SuperstreamUtils.CipherUtils.getVerify
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import com.lagradost.nicehttp.NiceResponse
import com.lagradost.nicehttp.Requests

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SuperstreamUtils() {
    private val app = Requests(baseClient = getBaseClient())
    private val unixTime = System.currentTimeMillis()/1000L
    private val gson = Gson()
//    private var episode: Int? = null
//    private var season : Int? = null

    private fun base64Decode(encodedString: String): String {
        return try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            decodedString
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private val headers = mapOf(
        "Platform" to "android",
        "Accept" to "charset=utf-8",
    )

    private fun randomToken(): String {
        return (0..31).joinToString("") {
            (('0'..'9') + ('a'..'f')).random().toString()
        }
    }
    private val token = randomToken()

    private val iv = base64Decode("d0VpcGhUbiE=")
    private val key = base64Decode("MTIzZDZjZWRmNjI2ZHk1NDIzM2FhMXc2")

    private val baseApiUrl = base64Decode("aHR0cHM6Ly9zaG93Ym94LnNoZWd1Lm5ldA==")
    private val apiUrl =
        "$baseApiUrl${base64Decode("L2FwaS9hcGlfY2xpZW50L2luZGV4Lw==")}"

    private val secondApiUrl =
        base64Decode("aHR0cHM6Ly9tYnBhcGkuc2hlZ3UubmV0L2FwaS9hcGlfY2xpZW50L2luZGV4Lw==")

    private val appKey = base64Decode("bW92aWVib3g=")
    private val appId = base64Decode("Y29tLnRkby5zaG93Ym94")
    private val appIdSecond = base64Decode("Y29tLm1vdmllYm94cHJvLmFuZHJvaWQ=")
    private val appVersion = "14.7"
    private val appVersionCode = "160"

    private object CipherUtils {
        private const val ALGORITHM = "DESede"
        private const val TRANSFORMATION = "DESede/CBC/PKCS5Padding"
        fun encrypt(str: String, key: String, iv: String): String? {
            return try {
                val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
                val bArr = ByteArray(24)
                val bytes: ByteArray = key.toByteArray()
                var length = if (bytes.size <= 24) bytes.size else 24
                System.arraycopy(bytes, 0, bArr, 0, length)
                while (length < 24) {
                    bArr[length] = 0
                    length++
                }
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    SecretKeySpec(bArr, ALGORITHM),
                    IvParameterSpec(iv.toByteArray())
                )

                String(Base64.encode(cipher.doFinal(str.toByteArray()), 2), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        // Useful for deobfuscation
        fun decrypt(str: String, key: String, iv: String): String? {
            return try {
                val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
                val bArr = ByteArray(24)
                val bytes: ByteArray = key.toByteArray()
                var length = if (bytes.size <= 24) bytes.size else 24
                System.arraycopy(bytes, 0, bArr, 0, length)
                while (length < 24) {
                    bArr[length] = 0
                    length++
                }
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(bArr, ALGORITHM),
                    IvParameterSpec(iv.toByteArray())
                )
                val inputStr = Base64.decode(str.toByteArray(), Base64.DEFAULT)
                cipher.doFinal(inputStr).decodeToString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun md5(str: String): String? {
            return MD5Util.md5(str)?.let { HexDump.toHexString(it).lowercase() }
        }

        fun getVerify(str: String?, str2: String, str3: String): String? {
            if (str != null) {
                return md5(md5(str2) + str3 + str)
            }
            return null
        }
    }

    private object MD5Util {
        fun md5(str: String): ByteArray? {
            return this.md5(str.toByteArray())
        }

        fun md5(bArr: ByteArray?): ByteArray? {
            return try {
                val digest = MessageDigest.getInstance("MD5")
                digest.update(bArr ?: return null)
                digest.digest()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                null
            }
        }
    }

    private object HexDump {
        private val HEX_DIGITS = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        )
        fun toHexString(bArr: ByteArray, i: Int = 0, i2: Int = bArr.size): String {
            val cArr = CharArray(i2 * 2)
            var i3 = 0
            for (i4 in i until i + i2) {
                val b = bArr[i4].toInt()
                val i5 = i3 + 1
                val cArr2 = HEX_DIGITS
                cArr[i3] = cArr2[b ushr 4 and 15]
                i3 = i5 + 1
                cArr[i5] = cArr2[b and 15]
            }
            return String(cArr)
        }
    }

    suspend fun queryApi(query: String, useAlternativeApi: Boolean): NiceResponse {
        val encryptedQuery = CipherUtils.encrypt(query, key, iv)!!
        val appKeyHash = CipherUtils.md5(appKey)!!
        val newBody =
            """{"app_key":"$appKeyHash","verify":"${
                getVerify(
                    encryptedQuery,
                    appKey,
                    key
                )
            }","encrypt_data":"$encryptedQuery"}"""
        val base64Body = String(Base64.encode(newBody.toByteArray(), Base64.DEFAULT))

        val data = mapOf(
            "data" to base64Body,
            "appid" to "27",
            "platform" to "android",
            "version" to appVersionCode,
            // Probably best to randomize this
            "medium" to "Website&token$token"
        )

        val url = if (useAlternativeApi) secondApiUrl else apiUrl
        return app.post(url, headers = headers, data = data, timeout = 120L)
    }

    private fun getExpiryDate(): Long {
        // Current time + 12 hours
        return unixTime + 60 * 60 * 12
    }

    suspend fun search(query: String): DataJSON{
        val hideNsfw = 0
        val apiQuery =
            // Originally 8 pagelimit
            """{"childmode":"$hideNsfw",
                |"app_version":"$appVersion",
                |"appid":"$appIdSecond",
                |"module":"Search4",
                |"channel":"Website",
                |"page":"1",
                |"lang":"en",
                |"type":"all",
                |"keyword":"$query",
                |"pagelimit":"20",
                |"expired_date":
                |"${getExpiryDate()}",
                |"platform":"android"}""".trimMargin()

        val response = queryApi(apiQuery,true).toString()
        return gson.fromJson(response,DataJSON::class.java)

//        return queryApi(apiQuery, true)
    }

    data class Data(
        @JsonProperty("id") val id: Int? = null,
        @JsonProperty("mid") val mid: Int? = null,
        @JsonProperty("box_type") val boxType: Int? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("poster_org") val posterOrg: String? = null,
        @JsonProperty("poster") val poster: String? = null,
        @JsonProperty("cats") val cats: String? = null,
        @JsonProperty("year") val year: Int? = null,
        @JsonProperty("imdb_rating") val imdbRating: String? = null,
        @JsonProperty("quality_tag") val qualityTag: String? = null,
    )

    data class MainData(
        @JsonProperty("data") val data: ArrayList<Data> = arrayListOf()
    )

     data class PostJSON(
        @JsonProperty("id") val id: Int? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("year") val year: Int? = null,
        @JsonProperty("poster") val poster: String? = null,
        @JsonProperty("poster_2") val poster2: String? = null,
        @JsonProperty("box_type") val boxType: Int? = null,
        @JsonProperty("imdb_rating") val imdbRating: String? = null,
        @JsonProperty("quality_tag") val quality_tag: String? = null,
    )

     data class ListJSON(
        @JsonProperty("code") val code: Int? = null,
        @JsonProperty("type") val type: String? = null,
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("box_type") val boxType: Int? = null,
        @JsonProperty("list") val list: ArrayList<PostJSON> = arrayListOf(),
    )

     data class DataJSON(
        @JsonProperty("data") val data: ListJSON
    )
//
//    private suspend inline fun <reified T : Any> queryApiParsed(
//        query: String,
//        useAlternativeApi: Boolean = true
//    ): T {
//        return queryApi(query, useAlternativeApi).parsed()
//    }


    suspend fun load(isMovie:Boolean, id: Int): NiceResponse {
//        val isMovie = loadData.type == ResponseTypes.Movies.value
        val hideNsfw = 0
        val apiQuery =
            if (isMovie) { // 1 = Movie

                """{"childmode":"$hideNsfw","uid":"","app_version":"$appVersion","appid":"$appIdSecond","module":"Movie_detail","channel":"Website","mid":"$id","lang":"en","expired_date":"${getExpiryDate()}","platform":"android","oss":"","group":""}"""

            }else{//2 Series

                """{"childmode":"$hideNsfw","uid":"","app_version":"$appVersion","appid":"$appIdSecond","module":"TV_detail_1","display_all":"1","channel":"Website","lang":"en","expired_date":"${getExpiryDate()}","platform":"android","tid":"$id"}"""
            }
        return queryApi(apiQuery,false)
    }

    suspend fun loadLinks(isMovie: Boolean,id: Int,season: Int = 1,episode: Int = 1): LinkDataProp {
//        val parsed = parseJson<LinkData>(data)

        // No childmode when getting links
        // New api does not return video links :(
        val query = if (isMovie) {
            """{"childmode":"0","uid":"","app_version":"11.5","appid":"$appId","module":"Movie_downloadurl_v3","channel":"Website","mid": "$id","lang":"","expired_date":"${getExpiryDate()}","platform":"android","oss":"1","group":""}"""
        } else {
//            val episode = parsed.episode ?: throw RuntimeException("No episode number!")
//            val season = parsed.season ?: throw RuntimeException("No season number!")
            """{"childmode":"0","app_version":"11.5","module":"TV_downloadurl_v3","channel":"Website","episode":"$episode","expired_date":"${getExpiryDate()}","platform":"android","tid":"$id","oss":"1","uid":"","appid":"$appId","season":"$season","lang":"en","group":""}"""
        }
        return try {
            val tvlinks = queryApi(query, false).toString()
            gson.fromJson(tvlinks, LinkDataProp::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            LinkDataProp()
        }
    //        return queryApi(query, false)
    }

    suspend fun loadSubtile(isMovie: Boolean, fid:Int, id:Int,season: Int = 1, episode: Int = 1) : SubtitleDataProp{
        val subtitleQuery = if (isMovie) {
            """{"childmode":"0","fid":"$fid","uid":"","app_version":"11.5","appid":"$appId","module":"Movie_srt_list_v2","channel":"Website","mid":"$id","lang":"en","expired_date":"${getExpiryDate()}","platform":"android"}"""
        } else {
            """{"childmode":"0","fid":"$fid","app_version":"11.5","module":"TV_srt_list_v2","channel":"Website","episode":"$episode","expired_date":"${getExpiryDate()}","platform":"android","tid":"$id","uid":"","appid":"$appId","season":"$season","lang":"en"}"""
        }
//        return queryApi(subtitleQuery,true)
        val response = queryApi(subtitleQuery,true).toString()
        return gson.fromJson(response,SubtitleDataProp::class.java)

    }

    data class MovieData(
        @JsonProperty("id") val id: Int? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("director") val director: String? = null,
        @JsonProperty("writer") val writer: String? = null,
        @JsonProperty("actors") val actors: String? = null,
        @JsonProperty("runtime") val runtime: Int? = null,
        @JsonProperty("poster") val poster: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("cats") val cats: String? = null,
        @JsonProperty("year") val year: Int? = null,
        @JsonProperty("imdb_id") val imdbId: String? = null,
        @JsonProperty("imdb_rating") val imdbRating: String? = null,
        @JsonProperty("trailer") val trailer: String? = null,
        @JsonProperty("released") val released: String? = null,
        @JsonProperty("content_rating") val contentRating: String? = null,
        @JsonProperty("tmdb_id") val tmdbId: Int? = null,
        @JsonProperty("tomato_meter") val tomatoMeter: Int? = null,
        @JsonProperty("poster_org") val posterOrg: String? = null,
        @JsonProperty("trailer_url") val trailerUrl: String? = null,
        @JsonProperty("imdb_link") val imdbLink: String? = null,
        @JsonProperty("box_type") val boxType: Int? = null,
        @JsonProperty("recommend") val recommend: List<Data> = listOf(),
    )

     data class MovieDataProp(
        @JsonProperty("data") val data: MovieData? = MovieData()
    )

     data class LinkData(
        val id: Int,
        val type: Int,
        val season: Int?,
        val episode: Int?
    )


     data class LinkDataProp(
        @JsonProperty("code") val code: Int? = null,
        @JsonProperty("msg") val msg: String? = null,
        @JsonProperty("data") val data: ParsedLinkData? = ParsedLinkData()
    )

     data class LinkList(
        @JsonProperty("path") val path: String? = null,
        @JsonProperty("quality") val quality: String? = null,
        @JsonProperty("real_quality") val realQuality: String? = null,
        @JsonProperty("format") val format: String? = null,
        @JsonProperty("size") val size: String? = null,
        @JsonProperty("size_bytes") val sizeBytes: Long? = null,
        @JsonProperty("count") val count: Int? = null,
        @JsonProperty("dateline") val dateline: Long? = null,
        @JsonProperty("fid") val fid: Int? = null,
        @JsonProperty("mmfid") val mmfid: Int? = null,
        @JsonProperty("h265") val h265: Int? = null,
        @JsonProperty("hdr") val hdr: Int? = null,
        @JsonProperty("filename") val filename: String? = null,
        @JsonProperty("original") val original: Int? = null,
        @JsonProperty("colorbit") val colorbit: Int? = null,
        @JsonProperty("success") val success: Int? = null,
        @JsonProperty("timeout") val timeout: Int? = null,
        @JsonProperty("vip_link") val vipLink: Int? = null,
        @JsonProperty("fps") val fps: Int? = null,
        @JsonProperty("bitstream") val bitstream: String? = null,
        @JsonProperty("width") val width: Int? = null,
        @JsonProperty("height") val height: Int? = null
    )

  data class ParsedLinkData(
        @JsonProperty("seconds") val seconds: Int? = null,
        @JsonProperty("quality") val quality: ArrayList<String> = arrayListOf(),
        @JsonProperty("list") val list: ArrayList<LinkList> = arrayListOf()
    )


    data class SeriesDataProp(
        @JsonProperty("code") val code: Int? = null,
        @JsonProperty("msg") val msg: String? = null,
        @JsonProperty("data") val data: SeriesData? = SeriesData()
    )

   data class SeriesSeasonProp(
        @JsonProperty("code") val code: Int? = null,
        @JsonProperty("msg") val msg: String? = null,
        @JsonProperty("data") val data: ArrayList<SeriesEpisode>? = arrayListOf()
    )
//    data class PlayProgress (
//
//  @JsonProperty("over"      ) val over     : Int? = null,
//  @JsonProperty("seconds"   ) val seconds  : Int? = null,
//  @JsonProperty("mp4_id"    ) val mp4Id    : Int? = null,
//  @JsonProperty("last_time" ) val lastTime : Int? = null
//
//)

    data class SeriesEpisode(
        @JsonProperty("id") val id: Int? = null,
        @JsonProperty("tid") val tid: Int? = null,
        @JsonProperty("mb_id") val mbId: Int? = null,
        @JsonProperty("imdb_id") val imdbId: String? = null,
        @JsonProperty("imdb_id_status") val imdbIdStatus: Int? = null,
        @JsonProperty("srt_status") val srtStatus: Int? = null,
        @JsonProperty("season") val season: Int? = null,
        @JsonProperty("episode") val episode: Int? = null,
        @JsonProperty("state") val state: Int? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("thumbs") val thumbs: String? = null,
        @JsonProperty("thumbs_bak") val thumbsBak: String? = null,
        @JsonProperty("thumbs_original") val thumbsOriginal: String? = null,
        @JsonProperty("poster_imdb") val posterImdb: Int? = null,
        @JsonProperty("synopsis") val synopsis: String? = null,
        @JsonProperty("runtime") val runtime: Int? = null,
        @JsonProperty("view") val view: Int? = null,
        @JsonProperty("download") val download: Int? = null,
        @JsonProperty("source_file") val sourceFile: Int? = null,
        @JsonProperty("code_file") val codeFile: Int? = null,
        @JsonProperty("add_time") val addTime: Int? = null,
        @JsonProperty("update_time") val updateTime: Int? = null,
        @JsonProperty("released") val released: String? = null,
        @JsonProperty("released_timestamp") val releasedTimestamp: Long? = null,
        @JsonProperty("audio_lang") val audioLang: String? = null,
        @JsonProperty("quality_tag") val qualityTag: String? = null,
        @JsonProperty("3d") val _3d: Int? = null,
        @JsonProperty("remark") val remark: String? = null,
        @JsonProperty("pending") val pending: String? = null,
        @JsonProperty("imdb_rating") val imdbRating: String? = null,
        @JsonProperty("display") val display: Int? = null,
        @JsonProperty("sync") val sync: Int? = null,
        @JsonProperty("tomato_meter") val tomatoMeter: Int? = null,
        @JsonProperty("tomato_meter_count") val tomatoMeterCount: Int? = null,
        @JsonProperty("tomato_audience") val tomatoAudience: Int? = null,
        @JsonProperty("tomato_audience_count") val tomatoAudienceCount: Int? = null,
        @JsonProperty("thumbs_min") val thumbsMin: String? = null,
        @JsonProperty("thumbs_org") val thumbsOrg: String? = null,
        @JsonProperty("imdb_link") val imdbLink: String? = null,
//        @JsonProperty("quality_tags") val qualityTags: ArrayList<String> = arrayListOf(),
//  @JsonProperty("play_progress"         ) val playProgress        : PlayProgress?     = PlayProgress()

    )

     data class SeriesLanguage(
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("lang") val lang: String? = null
    )

     data class SeriesData(
        @JsonProperty("id") val id: Int? = null,
        @JsonProperty("mb_id") val mbId: Int? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("display") val display: Int? = null,
        @JsonProperty("state") val state: Int? = null,
        @JsonProperty("vip_only") val vipOnly: Int? = null,
        @JsonProperty("code_file") val codeFile: Int? = null,
        @JsonProperty("director") val director: String? = null,
        @JsonProperty("writer") val writer: String? = null,
        @JsonProperty("actors") val actors: String? = null,
        @JsonProperty("add_time") val addTime: Int? = null,
        @JsonProperty("poster") val poster: String? = null,
        @JsonProperty("poster_imdb") val posterImdb: Int? = null,
        @JsonProperty("banner_mini") val bannerMini: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("imdb_id") val imdbId: String? = null,
        @JsonProperty("cats") val cats: String? = null,
        @JsonProperty("year") val year: Int? = null,
        @JsonProperty("collect") val collect: Int? = null,
        @JsonProperty("view") val view: Int? = null,
        @JsonProperty("download") val download: Int? = null,
        @JsonProperty("update_time") val updateTime: String? = null,
        @JsonProperty("released") val released: String? = null,
        @JsonProperty("released_timestamp") val releasedTimestamp: Int? = null,
        @JsonProperty("episode_released") val episodeReleased: String? = null,
        @JsonProperty("episode_released_timestamp") val episodeReleasedTimestamp: Int? = null,
        @JsonProperty("max_season") val maxSeason: Int? = null,
        @JsonProperty("max_episode") val maxEpisode: Int? = null,
        @JsonProperty("remark") val remark: String? = null,
        @JsonProperty("imdb_rating") val imdbRating: String? = null,
        @JsonProperty("content_rating") val contentRating: String? = null,
        @JsonProperty("tmdb_id") val tmdbId: Int? = null,
        @JsonProperty("tomato_url") val tomatoUrl: String? = null,
        @JsonProperty("tomato_meter") val tomatoMeter: Int? = null,
        @JsonProperty("tomato_meter_count") val tomatoMeterCount: Int? = null,
        @JsonProperty("tomato_meter_state") val tomatoMeterState: String? = null,
        @JsonProperty("reelgood_url") val reelgoodUrl: String? = null,
        @JsonProperty("audience_score") val audienceScore: Int? = null,
        @JsonProperty("audience_score_count") val audienceScoreCount: Int? = null,
        @JsonProperty("no_tomato_url") val noTomatoUrl: Int? = null,
        @JsonProperty("order_year") val orderYear: Int? = null,
        @JsonProperty("episodate_id") val episodateId: String? = null,
        @JsonProperty("weights_day") val weightsDay: Double? = null,
        @JsonProperty("poster_min") val posterMin: String? = null,
        @JsonProperty("poster_org") val posterOrg: String? = null,
        @JsonProperty("banner_mini_min") val bannerMiniMin: String? = null,
        @JsonProperty("banner_mini_org") val bannerMiniOrg: String? = null,
        @JsonProperty("trailer_url") val trailerUrl: String? = null,
        @JsonProperty("years") val years: ArrayList<Int> = arrayListOf(),
        @JsonProperty("season") val season: ArrayList<Int> = arrayListOf(),
        @JsonProperty("history") val history: ArrayList<String> = arrayListOf(),
        @JsonProperty("imdb_link") val imdbLink: String? = null,
        @JsonProperty("episode") val episode: ArrayList<SeriesEpisode> = arrayListOf(),
//        @JsonProperty("is_collect") val isCollect: Int? = null,
        @JsonProperty("language") val language: ArrayList<SeriesLanguage> = arrayListOf(),
        @JsonProperty("box_type") val boxType: Int? = null,
        @JsonProperty("year_year") val yearYear: String? = null,
        @JsonProperty("season_episode") val seasonEpisode: String? = null
    )

    data class SubtitleDataProp(
        val code: Int? = null,
        val msg: String? = null,
        val data: PrivateSubtitleData? = PrivateSubtitleData()
    )

    data class Subtitles(
        val sid: Int? = null,
        val tid: String? = null,
        val file_path: String? = null,
        val lang: String? = null,
        val language: String? = null,
        val delay: Int? = null,
        val point: String? = null,
        val order: Int? = null,
        val admin_order: Int? = null,
        val myselect: Int? = null,
        val add_time: Long? = null,
        val count: Int? = null
    )

    data class SubtitleList(
        val language: String? = null,
        val subtitles: ArrayList<Subtitles> = arrayListOf()
    )

    data class PrivateSubtitleData(
        val select: ArrayList<String> = arrayListOf(),
        val list: ArrayList<SubtitleList> = arrayListOf()
    )

}
package com.demomiru.tokeiv2.utils

import android.util.Base64
import com.demomiru.tokeiv2.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lagradost.nicehttp.Requests
import org.json.JSONArray
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class GoMovies{

    class AES {

        fun decrypt(encrypted: String, password: String): String {
            val keySize = 8
            val ivSize = 4
            val cipherText = Base64.decode(encrypted, Base64.DEFAULT)
            val prefix = ByteArray(8)
            System.arraycopy(cipherText, 0, prefix, 0, 8)
            val salt = ByteArray(8)
            System.arraycopy(cipherText, 8, salt, 0, 8)
            val trueCipherText = ByteArray(cipherText.size - 16)
            System.arraycopy(cipherText, 16, trueCipherText, 0, cipherText.size - 16)
            val javaKey = ByteArray(keySize * 4)
            val javaIv = ByteArray(ivSize * 4)
            evpKDF(
                password.toByteArray(StandardCharsets.UTF_8),
                keySize,
                ivSize,
                salt,
                javaKey,
                javaIv
            )
            val aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(javaIv)
            aesCipherForEncryption.init(Cipher.DECRYPT_MODE, SecretKeySpec(javaKey, "AES"), ivSpec)
            val byteMsg = aesCipherForEncryption.doFinal(trueCipherText)
            return String(byteMsg, StandardCharsets.UTF_8)
        }

        private fun evpKDF(
            password: ByteArray,
            keySize: Int,
            ivSize: Int,
            salt: ByteArray,
            resultKey: ByteArray,
            resultIv: ByteArray
        ): ByteArray {
            return evpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv)
        }

        private fun evpKDF(
            password: ByteArray,
            keySize: Int,
            ivSize: Int,
            salt: ByteArray,
            iterations: Int,
            hashAlgorithm: String,
            resultKey: ByteArray,
            resultIv: ByteArray
        ): ByteArray {
            val targetKeySize = keySize + ivSize
            val derivedBytes = ByteArray(targetKeySize * 4)
            var numberOfDerivedWords = 0
            var block: ByteArray? = null
            val hasher = MessageDigest.getInstance(hashAlgorithm)
            while (numberOfDerivedWords < targetKeySize) {
                if (block != null) {
                    hasher.update(block)
                }
                hasher.update(password)
                block = hasher.digest(salt)
                hasher.reset()
                for (i in 1 until iterations) {
                    block = hasher.digest(block)
                    hasher.reset()
                }
                System.arraycopy(
                    block, 0, derivedBytes, numberOfDerivedWords * 4,
                    minOf(block?.size!!, (targetKeySize - numberOfDerivedWords) * 4)
                )
                numberOfDerivedWords += block.size / 4
            }
            System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4)
            System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4)
            return derivedBytes
        }

    }

    private val aes = AES()
    private val gson = Gson()
    private val app = Requests()
    private val proxy = BuildConfig.PROXY_URL
    private val headers = mapOf("X-Requested-With" to "XMLHttpRequest")
    private val baseUrl = "https://gomovies.sx"
    suspend fun search(s: Int, ep:Int ,query: String) : Pair<String?,ArrayList<String>?>{
        val squery = query.lowercase().replace(" ","-")
        val search = app.get(
            "${proxy}https://gomovies.sx/search/${squery}",
            headers = headers
        ).document.select("div.flw-item h2 a")

        var mediaId = ""

        for (se in search) {
            if (se.attr("title") == query) {
                mediaId = se.attr("href").substringAfter("gomovies-")
                println(mediaId)
                break
            }
        }

        if (mediaId == "") return Pair( null, null)

        val seasons = app.get(
            "${proxy}https://gomovies.sx/ajax/v2/tv/seasons/${mediaId}", headers = headers
        ).document.select(".ss-item")

//        for (s in seasons) {
//            println(s)
//        }
        val dataId = seasons[s-1].attr("data-id")

        val episodes = app.get(
            "${proxy}$baseUrl/ajax/season/episodes/${dataId}", headers = headers
        ).document.select(".eps-item")

//            for (ep in episodes)
//                println(ep)

        val epDataId = episodes[ep-1].attr("data-id")

        val sources = app.get(
            "${proxy}$baseUrl/ajax/episode/servers/$epDataId",
            headers = headers
        ).document.select("li.nav-item a")
        var upCloudId = ""
        for (source in sources) {
            if (source.attr("title").contains("UpCloud")) upCloudId = source.attr("data-id")
        }
//        println("upcloudid below")
//        println(upCloudId)

        val upCloudSource = app.get("${proxy}$baseUrl/ajax/sources/$upCloudId").toString()
        val upcloudSrc = gson.fromJson(upCloudSource, UpCloud::class.java)
//        println(upcloudSrc.link)
        upcloudSrc.link = upcloudSrc.link?.replace("?z=", "")
        val upDataID = upcloudSrc.link?.substringAfter("embed-4/")
        val streamRes = app.get(
            "https://rabbitstream.net/ajax/embed-4/getSources?id=${upDataID}",
            headers = headers,
            referer = "https://rabbitstream.net"
        ).toString()

        val stream = gson.fromJson(streamRes, StreamRes::class.java)
//        println(stream)

        val sub = getSubs(stream.tracks)

        val decryptionKeyResponse =
            app.get("https://raw.githubusercontent.com/enimax-anime/key/e4/key.txt").toString()

//        println(decryptionKeyResponse)

        val listType = object : TypeToken<List<List<Int>>>() {}.type
        val decryptionKey: List<List<Int>> = gson.fromJson(decryptionKeyResponse, listType)
//        println(decryptionKey)

        var extractedKey = ""
        val sourcesArray = stream.sources.split("").toMutableList()
        sourcesArray.removeAt(0)
        sourcesArray.removeAt(sourcesArray.size - 1)
        println(sourcesArray)
        decryptionKey.forEach { index ->
            for (i in index[0] until index[1]) {
                extractedKey += stream.sources[i]
                sourcesArray[i] = ""
            }
        }
        val key = extractedKey
        val data = sourcesArray.joinToString("")
        val decryptedStream = aes.decrypt(data, key)
//        println(decryptedStream)
        val jsonArray = JSONArray(decryptedStream)
        val videoSrc = gson.fromJson(jsonArray.getJSONObject(0).toString(),VidFile::class.java)
        println(videoSrc)
        return Pair(videoSrc.file, sub)

    }


    data class StreamRes(

        val server: Int,
        val sources: String,
        val tracks: ArrayList<Track>
    )

    data class Track(
        val file: String,
        val kind: String,
        val label: String
    )

    data class UpCloud(
        val type: String?,
        var link: String?,
        val sources: ArrayList<String>?,
        val tracks: ArrayList<String>?,
        val title: String?
    )

    data class VidFile(
        val file: String?,
        val type: String?
    )

    private fun getSubs(tracks: ArrayList<Track>?): ArrayList<String>? {
        if(tracks == null) return null
        return if (tracks.size == 1){
            arrayListOf(tracks[0].file)
        } else{
            val subs = arrayListOf<String>()
            for(track in tracks){
                if(track.label.contains("English"))
                    subs.add(track.file)
            }
            subs
        }
    }
}
package com.demomiru.tokeiv2.utils

import JsUnpacker
import android.net.Uri
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import com.demomiru.tokeiv2.extractors.Vidplay
import com.demomiru.tokeiv2.subtitles.SubtitleConfig
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests
import com.lagradost.nicehttp.addGenericDns
import com.lagradost.nicehttp.ignoreAllSSLErrors
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.URI

private val appCache = Cache(File("cacheDir", "okhttpcache"), 10 * 1024 * 1024)
private val proxy = "https://hello-world-aged-resonance-fc8f.bokaflix.workers.dev/?apiUrl="
private fun OkHttpClient.Builder.addCloudFlareDns() = (
        addGenericDns(
            "https://cloudflare-dns.com/dns-query",
            // https://www.cloudflare.com/ips/
            listOf(
                "1.1.1.1",
                "1.0.0.1",
                "2606:4700:4700::1111",
                "2606:4700:4700::1001"
            )
        ))
private val baseClient = OkHttpClient.Builder()
    .followRedirects(true)
    .followSslRedirects(true)
    .ignoreAllSSLErrors()
    .cache(
        appCache
    ).addCloudFlareDns().build()

data class ExtractedData(
    val videoUrl: String? = null,
    val subs: List<String> = listOf(),
    val source: String,
    val isSuper: Boolean = false,
)

fun String.createSlug(): String {
    return this.replace(Regex("[^\\w ]+"), "").replace(" ", "-").lowercase()
}

fun getBaseUrl(url: String): String {
    return URI(url).let {
        "${it.scheme}://${it.host}"
    }
}

private val packedRegex = Regex("""eval\(function\(p,a,c,k,e,.*\)\)""")
fun getPacked(string: String): String? {
    return packedRegex.find(string)?.value}
fun getAndUnpack(string: String): String {
    val packedText = getPacked(string)
    return JsUnpacker(packedText).unpack() ?: string
}

class Extractor (private val origin: String){


    private val gson = Gson()
    private val extractorPriority = mapOf(
//        "hi" to listOf(1,5,3,4),
//        "en" to listOf(1,5,2,3),
//        "" to listOf(1,5,2,3)
        "hi" to listOf(6,5,4,1,3),
        "en" to listOf(6,5,4,1,3),
        "" to listOf(6,5,4,1,3)
    )
    private val eList = if(origin in extractorPriority.keys) extractorPriority[origin] else extractorPriority[""]
    var i = 0

    suspend fun loadExtractor(title: String, id: String, year: String = "1970", s: Int, ep: Int, isMovie: Boolean, next:Int = 6): ExtractedData{
        println(origin)
        return when(next){
//            2 -> goMovieExtractor(title,s,ep,id,year,isMovie)
//            2-> zoeChipExtractor(title,s,ep,id,year,isMovie)
//            2-> nowTvExtractor(title,s,ep,id,year,isMovie)
            1 -> superStreamExtractor(title,s,ep,id,year,isMovie)
            3 -> smashyExtractor(title,s,ep,id,year,isMovie)
//            4 -> dudeFilmExtractor(title,s,ep,id,year,isMovie)
            4-> vidSrcExtractor(title,s,ep,id,year,isMovie)
            5 -> vidSrcExtractor(title,s,ep,id,year,isMovie)
            6 -> vidPlayExtractor(title,year,s,ep,id,isMovie)
            else -> ExtractedData(source = "")
        }
    }

    private suspend fun zoeChipExtractor(title: String, s: Int, ep: Int, id: String, year: String, isMovie: Boolean,srcChange: Boolean = false): ExtractedData {
        val slug = title.createSlug()
        val app = Requests()
        val zoechipAPI ="https://zoechip.org"
        var m3u8url = ""
        try {

            val url = if (isMovie) {
                "$zoechipAPI/film/${title.createSlug()}-$year"
            } else {
                "$zoechipAPI/episode/$slug-season-$s-episode-$ep"
            }

            val mid = app.get("$proxy$url").document.selectFirst("div#show_player_ajax")?.attr("movie-id")
                ?: throw Exception("no zoechip found")

            val server = app.post(
                "$proxy$zoechipAPI/wp-admin/admin-ajax.php", data = mapOf(
                    "action" to "lazy_player",
                    "movieID" to mid,
                ), referer = url, headers = mapOf(
                    "X-Requested-With" to "XMLHttpRequest"
                )
            ).document.selectFirst("ul.nav a:contains(Filemoon)")?.attr("data-server")
                ?: throw Exception("no zoechip found")

            val res = app.get("$proxy$server", referer = "$zoechipAPI/")
            val host = getBaseUrl(res.url)
            val script =
                res.document.select("script:containsData(function(p,a,c,k,e,d))").last()?.data()
            val unpacked = getAndUnpack(script ?: throw Exception("no zoechip found"))

            val m3u8 = Regex("file:\\s*\"(.*?m3u8.*?)\"").find(unpacked)?.groupValues?.getOrNull(1)
            m3u8url = m3u8 ?: throw Exception("no zoechip found")
        }catch (e: Exception){
            return if(!srcChange)  loadExtractor(title, id, year, s, ep, isMovie,eList!![++i]) else ExtractedData(source = "")
        }
        println(m3u8url)
        return ExtractedData(m3u8url, listOf(),"zoechip",false)
    }


    suspend fun loadExtractorNext(title: String, id: String, s: Int, ep: Int, source: String?) : ExtractedData{
        return when(source){
            "superstream" -> superStreamExtractor(title,s,ep,id,"",false)
            "gomovies" -> goMovieExtractor(title,s,ep,id,"",false)
             "smashy" -> smashyExtractor(title,s,ep,id,"",false)
             "dudefilms" ->  dudeFilmExtractor(title,s,ep,id,"",false)
            "vidsrc"   -> vidSrcExtractor(title,s,ep,id,"",false)
            "vidplay"   -> vidPlayExtractor(title,"",s,ep,id,false)
//            "nowtv" -> nowTvExtractor(title,s,ep,id,"",false)
            else-> ExtractedData(source = "")
        }
    }

    suspend fun loadSourceChange(title: String, id:String,s:Int, ep: Int,year: String,isMovie: Boolean, source: String? = null) : List<ExtractedData>{
        val listSources = mutableListOf<ExtractedData>()
        listSources.add(vidPlayExtractor(title,year,s,ep,id,isMovie,true))
        listSources.add(superStreamExtractor(title,s,ep,id,year,isMovie,true))
        listSources.add(vidSrcExtractor(title,s,ep,id,year,isMovie,true))
        listSources.add(goMovieExtractor(title,s,ep,id,year,isMovie,true))
        listSources.add(smashyExtractor(title,s,ep,id,year,isMovie,true))
        listSources.add(dudeFilmExtractor(title,s,ep,id,year,isMovie))
//        listSources.add(zoeChipExtractor(title,s,ep,id,year,isMovie,true))


        listSources.removeIf{
            it.videoUrl.isNullOrBlank()
        }
        if(origin == "hi")
            listSources.removeIf{
                it.source == "gomovies"
            }
        if(source!=null)
            listSources.removeIf {
                it.source == source
            }
        println(listSources)
        return listSources.toList()
    }



    private suspend fun superStreamExtractor(title: String,s:Int, ep: Int, id: String, year: String, isMovie: Boolean,srcChange: Boolean = false) : ExtractedData{
        val superStream = SuperstreamUtils()
        var videoUrl: String? = null
        val subUrl : MutableList<String> = mutableListOf()
        try {
            val mainData = superStream.search(title)
            val item = mainData.data.list[0]
            val superId =
                if (item.title == title && item.year.toString() == year) item.id else if(!isMovie && item.title == title) item.id else throw Exception(
                    "No super stream found"
                )
            val movieLinks = superStream.loadLinks(isMovie, superId!!,s,ep)
            println(movieLinks)
            val urlMaps: MutableMap<String, String> = mutableMapOf()
            movieLinks.data?.list?.forEach {
                if (!it.path.isNullOrBlank()) {
                    urlMaps[it.quality!!] = it.path
                    if (it.quality == "720p") {
                        val subtitle = superStream.loadSubtile(isMovie, it.fid!!, superId, s,ep).data
                        subUrl.add(getSub2(subtitle))
                        return@forEach
                    }
                }
            }

            if(urlMaps.isNotEmpty())
                videoUrl = gson.toJson(urlMaps)
            if(videoUrl.isNullOrBlank()){

//                    isSuper = false
                throw Exception("No super stream found")
//                   return loadExtractor(title,id,year,s,ep,isMovie, eList!![++i])
            }
        }catch(e:Exception) {
            e.printStackTrace()
            return if(!srcChange)loadExtractor(title, id, year,s,ep,isMovie,eList!![++i]) else ExtractedData(source = "")
        }
        println("Superstream")
        return ExtractedData(videoUrl,subUrl,"superstream",true)
    }


    private fun getSub(subtitle: SuperstreamUtils.PrivateSubtitleData?): List<String>{
        val subUrl: MutableList<String> = mutableListOf()
        subtitle?.list?.forEach { subList->
            if(subList.language == "English"){
                subList.subtitles.forEach { sub->

                    if (subUrl.size == 3) {
                        return subUrl
                    }
                    if (sub.lang == "en" && !sub.file_path.isNullOrBlank()) {
                        subUrl.add(sub.file_path)
//                            println("${sub.language} : ${sub.file_path}")
                    }
                }
                return subUrl
            }
        }
        return listOf()
    }

    private fun getSub2(subtitle: SuperstreamUtils.PrivateSubtitleData?): String{
        val subUrl: MutableMap<String,String> = mutableMapOf()
        subtitle?.list?.forEach { subList ->
            if(subList.language == null) return@forEach
            var subsString = ""
            subList.subtitles.forEach { sub ->
                if (!sub.file_path.isNullOrBlank()) subsString += "${sub.file_path},"
            }
            subsString = subsString.substringBeforeLast(",")
            subUrl[subList.language] = subsString
        }
        return gson.toJson(subUrl)
    }

    suspend fun nowTvExtractor(title: String, s: Int, ep: Int, id: String, year: String, movie: Boolean,srcChange: Boolean = false): ExtractedData {

        val app = Requests()
        val referer = "https://bflix.gs/"
        val nowTvAPI = "https://myfilestorage.xyz"
        suspend fun String.isSuccess(): Boolean {
            return app.get(this, referer = referer).isSuccessful
        }

        var url =
            if (movie) "$nowTvAPI/$id.mp4" else "$nowTvAPI/tv/$id/s${s}e${ep}.mp4"
        print("NowTv")
        if (!url.isSuccess()) {
            url = if (movie) {
                val imdb = getMovieImdb(id)
                val temp = "$nowTvAPI/$imdb.mp4"
                if (temp.isSuccess()) temp else "$nowTvAPI/$id-1.mp4"
            } else {
                val imdb = getTvIMDB(id)
                "$nowTvAPI/tv/$imdb/s${s}e${ep}.mp4"
            }
            if (!app.get(url, referer = referer).isSuccessful) {return if(!srcChange)   loadExtractor(title, id, year, s, ep, movie,eList!![++i]) else ExtractedData(source = "")}
            else return ExtractedData(url, listOf(),"nowtv",false)

        } else
           return ExtractedData(url, listOf(),"nowtv",false)
    }



    private suspend fun vidSrcExtractor(title: String, s: Int,ep:Int, id: String,year:String,isMovie: Boolean,srcChange: Boolean = false): ExtractedData{
        val vidSrc = VidSrc()
        val videoUrl: String?
        val subUrl: ArrayList<String> = arrayListOf()
        try {
            val links =  vidSrc.getLink(id,isMovie,s,ep)
            val vidLink = links.first
            val subLink = links.second
            if(vidLink.isNullOrBlank()) throw Exception("No vidsrc found")
            else {
                if (!subLink.isNullOrBlank())subUrl.add(subLink)
                videoUrl = vidLink
            }
        }catch (e:Exception){
            e.printStackTrace()
            return if(!srcChange)  loadExtractor(title, id, year, s, ep, isMovie,eList!![++i]) else ExtractedData(source = "")
        }
        return ExtractedData(videoUrl,subUrl,"vidsrc",false)

    }

    private suspend fun goMovieExtractor(title: String,s: Int, ep: Int, id: String,year: String,isMovie: Boolean,srcChange: Boolean = false): ExtractedData{
        val goMovie = GoMovies()
        var videoUrl: String? = null
        val subUrl : ArrayList<String> = arrayListOf()

        try{
            val data = goMovie.search(s,ep,title,isMovie,year)
            val vidLink = data.first
            val subLinks = data.second
            if(vidLink.isNullOrBlank()){
//                return loadExtractor(title, id, year, s, ep, isMovie,eList!![++i])
                throw Exception("No go movies found")
            }
            else{
                if (!subLinks.isNullOrEmpty())subUrl.add(subLinks)
                videoUrl = vidLink
            }
        }catch (e:Exception){
            return if(!srcChange)   loadExtractor(title, id, year, s, ep, isMovie,eList!![++i]) else ExtractedData(source = "")
        }
        println("GoMovies")
        return ExtractedData(videoUrl,subUrl,"gomovies",false)
    }

    private suspend fun smashyExtractor(title: String,s: Int, ep: Int, id: String,year: String,isMovie: Boolean,srcChange: Boolean = false): ExtractedData{
        var videoUrl : String? = null
        val subUrl : ArrayList<String> = arrayListOf()
        val smashSrc = SmashyStream()
        try{
            val links = smashSrc.getLink(isMovie,id, s, ep,origin)
            val vidLink = links.first
            val subLink = links.second
            if(vidLink.isNullOrBlank()){
//                return if (origin == "hi")
//                    loadExtractor(title,id,year,s,ep,isMovie,eList!![++i])
//                else
//                    loadExtractor(title,id,year,s,ep,isMovie,0)
                throw Exception("No smashy stream found")
            }
            else{
                if (!subLink.isNullOrBlank())subUrl.add(subLink)
                videoUrl = vidLink
            }


        }catch (e: Exception){
            return if(!srcChange) {
                if (origin == "hi")
                    loadExtractor(title,id,year,s,ep,isMovie,eList!![++i])
                else
                    loadExtractor(title,id,year,s,ep,isMovie,0)
            }else ExtractedData(source = "")
        }
        println("Smashy")
        return ExtractedData(videoUrl,subUrl,"smashy",false)
    }

    private suspend fun vidPlayExtractor(title: String, year :String,s: Int, ep: Int, id: String, isMovie: Boolean, srcChange: Boolean = false): ExtractedData{
        var videoUrl: String? = null
        val subUrl : ArrayList<String> = arrayListOf()
        val vidPlay = Vidplay()
        try {
            val links = vidPlay.getVidPlayUrl(isMovie,s,ep,id)
            val vidLink = links.first
            val subLink = links.second
            if(vidLink.isNullOrBlank()){
                throw Exception("No vidplay found")
            }
            else{
                if (!subLink.isNullOrBlank())subUrl.add(subLink)
                videoUrl = vidLink
            }

        }catch (e: Exception){
            return if(!srcChange)   loadExtractor(title, id, year, s, ep, isMovie,eList!![++i]) else ExtractedData(source = "")
        }
        return ExtractedData(videoUrl,subUrl,"vidplay",false)
    }

    //TODO
    private suspend fun dudeFilmExtractor(title: String, s: Int, ep: Int, id: String, year: String, movie: Boolean): ExtractedData {
        var videoUrl : String? = null
        try {
            videoUrl = if (movie) {
                val imdb = getMovieImdb(id)
                getMovieLink(imdb)
            } else {
                val imdb = getTvImdb(id)
                getTvLink(imdb, s-1, ep-1)
            }
        }  catch (e: Exception){
            return ExtractedData(source = "")
        }
        println("DudeFilms")
        return ExtractedData(videoUrl, listOf(),"dudefilms",false)
    }
}
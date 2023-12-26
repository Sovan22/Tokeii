package com.demomiru.tokeiv2.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.subtitles.SubtitleConfig
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.GzipSource
import okio.buffer
import org.checkerframework.checker.units.qual.s
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

data class Languages(
    val data: ArrayList<Data> = arrayListOf()
){
    data class Data(
        val language_code: String,
        val language_name: String
    )
}

data class DownloadSub(
    val link: String,
    val file_name: String,
    val remaining: String
)


data class SubRest(
    val SubDownloadLink: String,
    val SubFileName:String,
)



class OpenSubtitle(private val applicationContext: Context) {
    private val app = Requests()
    private val gson = Gson()

    private val headers = mapOf(
        "Api-Key" to "XeM3ngDLQIPF6ySf37z6PIIzTbAMIb8x",
        "Content-Type" to "application/json",
        "Accept" to "application/json",
        "User-Agent" to "Tokeiv2"
//        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0"
    )

    val sHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0",
        "X-User-Agent" to "trailers.to-UA"
    )

    val subUrl : MutableList<String> = mutableListOf()

    suspend fun getLang() : Map<String,String>{
           val langs =
               app.get("https://api.opensubtitles.com/api/v1/infos/languages", headers = headers)
                   .toString()
           val res = gson.fromJson(langs, Languages::class.java)

           val langMap = mutableMapOf<String, String>()
           res.data.forEach {
               langMap[it.language_name] = it.language_name.substring(0, 3)
                   .lowercase(Locale.getDefault())
           }
           return langMap
    }


    private fun makeSRT(content:String, lang: String, fileName:String) {
        val context: Context = applicationContext

        try {
            // Create a temporary file in the app's internal cache directory
            val tempFile = File(context.cacheDir, "$fileName.srt")
            FileOutputStream(tempFile).use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            // You now have a temporary file in the app's internal cache directory
            // You can use tempFile to reference it further
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getSRT(file: String) : Uri?{
        val context: Context = applicationContext

        try {
            // Specify the name of the temporary file

            // Create a File object for the temporary file
            val tempFile = File(context.cacheDir, "$file.srt")

            // Check if the file exists before attempting to read it
            if (tempFile.exists()) {
                // Read the contents of the file
                val content = FileInputStream(tempFile).bufferedReader().use { it.readText() }

                // Display the content (e.g., in a TextView)
                return Uri.fromFile(tempFile)
//                println(content)
            } else {
                // The file does not exist
                // Handle this case as needed

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
//
//    suspend fun getSampleSource(uri: Uri?){
//        val subtitleMediaSource =
//            SingleSampleMediaSource.Factory(DefaultDataSourceFactory(this@VideoPlayActivity,"user-agent")).createMediaSource(
//                MediaItem.SubtitleConfiguration.Builder(oS.getSRT(it[0])!!)
//                    .setMimeType(MimeTypes.APPLICATION_SUBRIP)
//                    .setLanguage("en")
//                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
//                    .build(),
//                C.TIME_UNSET
//            )
//        subtitleConfig.add(SubtitleConfig(subtitleMediaSource, language = it[0]))
//    }

    suspend fun searchSubs(id:String, langCode: String, s: Int = 1, e: Int = 1 ,isMovie: Boolean = true): List<SubRest>{
//        val searchResults = app.get("https://api.opensubtitles.com/api/v1/subtitles?tmdb_id=$id&type=$type&languages=$langCode&order_by=download_count",
//            headers = headers).toString()

        val subs = arrayListOf<SubRest>()
        val getUrl = if(isMovie)"https://rest.opensubtitles.org/search/imdbid-$id/sublanguageid-$langCode" else  "https://rest.opensubtitles.org/search/episode-$e/imdbid-$id/season-$s/sublanguageid-$langCode"
        val searchResults = app.get(getUrl, headers = sHeaders).toString()
        val jsonArray = JSONArray(searchResults)
        for( i in 0 until jsonArray.length())
        {
            subs.add(gson.fromJson(jsonArray.getJSONObject(i).toString(), SubRest::class.java))
        }
        println(subs)
        return subs.toList()
    }

//    suspend fun getSub(fileId: String): String{
//
//        val mediaType = "application/json".toMediaType()
//        val body = "{\r\n  \"file_id\": ${fileId}\r\n}".toRequestBody(mediaType)
//        val res = app.post("https://api.opensubtitles.com/api/v1/download",headers = headers ,requestBody = body).toString()
//        val download = gson.fromJson(res,DownloadSub::class.java)
//        println(download.remaining)
//        return download.link
//
//    }

    suspend fun getSub2(link:String, lang: String,fileName: String){
        val res = app.get(link).okhttpResponse
        val gzippedSource = GzipSource(res.body.source())
        val decompressedString = gzippedSource.buffer().readUtf8()
        makeSRT(decompressedString,lang, fileName)
    }
}

class SubAdapter(private val onClick :(String)->Unit):
    androidx.recyclerview.widget.ListAdapter<SubRest, SubAdapter.ViewHolder>(SearchDiffCallBack) {

    private var selectedPosition = -1

    private fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val subTv: CheckedTextView = itemView.findViewById(R.id.track_quality)
    }
    object SearchDiffCallBack : DiffUtil.ItemCallback<SubRest>() {
        override fun areItemsTheSame(oldItem: SubRest, newItem: SubRest): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: SubRest, newItem: SubRest): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sub = getItem(position)
        holder.subTv.isChecked = position == selectedPosition
        holder.subTv.text = sub.SubFileName
        holder.itemView.setOnClickListener {
            setSelectedPosition(holder.bindingAdapterPosition)
            onClick(sub.SubDownloadLink)
        }
    }
}


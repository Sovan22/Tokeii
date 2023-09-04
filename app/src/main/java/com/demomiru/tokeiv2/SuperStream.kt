package com.demomiru.tokeiv2

import android.os.Bundle
import android.util.Log
import com.gargoylesoftware.htmlunit.javascript.host.WindowOrWorkerGlobalScopeMixin.atob
import java.util.Base64

class SuperStream {

    private fun atob(encodedData : String):String{
        val decodedBytes = Base64.getDecoder().decode(encodedData)
        return String(decodedBytes)

    }

//     fun onCreate(savedInstanceState: Bundle?) {
//
//        val iv = atob("d0VpcGhUbiE=")
//        val key = atob("MTIzZDZjZWRmNjI2ZHk1NDIzM2FhMXc2")
//        val apiUrls = arrayListOf(
//            atob("aHR0cHM6Ly9zaG93Ym94LnNoZWd1Lm5ldC9hcGkvYXBpX2NsaWVudC9pbmRleC8="),
//            atob("aHR0cHM6Ly9tYnBhcGkuc2hlZ3UubmV0L2FwaS9hcGlfY2xpZW50L2luZGV4Lw=="))
//
//        val appKey = atob("bW92aWVib3g=")
//        val appId = atob("Y29tLnRkby5zaG93Ym94")
//        Log.i("api ", apiUrls[1])
//
////        GlobalScope.launch(Dispatchers.IO) {
////            val url = "https://movie-web.app/media/tmdb-movie-670"
////            val document: Document = Jsoup.connect(url).timeout(10000).get()
////            val video: Element? = document.select("video.z-0.h-full.w-full").first()
////            val videoUrl = video?.absUrl("src")
////
////            withContext(Dispatchers.Main) {
////            }
////        }
}
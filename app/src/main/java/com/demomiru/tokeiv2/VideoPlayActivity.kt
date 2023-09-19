@file:Suppress("DEPRECATION")
@file:OptIn(DelicateCoroutinesApi::class)

package com.demomiru.tokeiv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.demomiru.tokeiv2.watching.VideoData
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C

import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.subtitles.SubTrackAdapter
import com.demomiru.tokeiv2.subtitles.Subtitle
import com.demomiru.tokeiv2.subtitles.SubtitleConfig
import com.demomiru.tokeiv2.utils.setSeekBarTime
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.lang.reflect.Type
import kotlin.math.abs


//TODO Implement TVShowPlay
class VideoPlayActivity : AppCompatActivity(),AudioManager.OnAudioFocusChangeListener, GestureDetector.OnGestureListener {


    private lateinit var id:String
    private var season: Int = 1
    private var episode: Int = 1
//    private var origin : String = ""
    private var progress : Int = 0
    private var imgLink : String? = null
    private lateinit var title:String
    private var type : String? = null

    private lateinit var unlockIv : ImageView
    private var isLocked = false
    private lateinit var lockLL : LinearLayout
    private val database by lazy { ContinueWatchingDatabase.getInstance(this) }
    private val watchHistoryDao by lazy { database.watchDao() }
    private lateinit var gestureDetectorCompat : GestureDetectorCompat
    private var subList = MutableLiveData<List<String>>()
    private lateinit var player : ExoPlayer
    private lateinit var goBack : ImageView
    private lateinit var videoUri: Uri
    private var maxVolume: Int = 0
    private var brightness: Int = 0
    private lateinit var brightnessLL: LinearLayout
    private lateinit var brightnessSeek : SeekBar
    private lateinit var volumeLL: LinearLayout
    private lateinit var mainPlayer:FrameLayout
    private lateinit var volumeSeek : SeekBar
    private var audioManager: AudioManager? = null
    private lateinit var seekBar:SeekBar
    private lateinit var mediaSource: MediaSource
    private lateinit var playPause: ImageButton
    private lateinit var titleTv : TextView
    private lateinit var screenScale: LinearLayout
    private var volume : Int = 0
    private var minSwipeY: Float = 0f
    private var fit = true

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            seekBar.progress = player.currentPosition.toInt()
            handler.postDelayed(this, 1000)
        }
    }

    @SuppressLint("UnsafeOptInUsageError", "ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video_play)

        val bundle = intent.extras
        val data = bundle?.getSerializable("VidData") as? VideoData
        type = data!!.type
        id = data.tmdbID.toString()
        videoUri = Uri.parse(data.videoUrl)
        progress = data.progress
        title = data.title
        imgLink = data.imgLink
        if (type == "tvshow"){
            season = data.season
            episode = data.episode
        }

        // This code goes in your activity's onCreate method
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
        gestureDetectorCompat = GestureDetectorCompat(this,this)

        var play = true
        val videoLoading = findViewById<ProgressBar>(R.id.video_loading)
        val remTimeTv = findViewById<TextView>(R.id.videoView_endtime)
        val seekForward: ImageButton = findViewById(R.id.videoView_forward)
        val seekBack : ImageButton = findViewById(R.id.videoView_rewind)
        val subTracks : LinearLayout = findViewById(R.id.videoView_track)
        val subSelectionView: RecyclerView = findViewById(R.id.sub_tracks_rc)
        val subSelectBg : ConstraintLayout = findViewById(R.id.subtitle_select)
        val applySub : Button = findViewById(R.id.apply_sub)
        val screenResizeTv : TextView = findViewById(R.id.screen_resize_text)
        val screenResizeIv : ImageView = findViewById(R.id.screen_resize_img)
        mainPlayer = findViewById(R.id.main_player)
        goBack = findViewById(R.id.videoView_go_back)
        screenScale = findViewById(R.id.videoView_screen_size)
        lockLL  = findViewById(R.id.videoView_lock_screen)
        unlockIv = findViewById(R.id.unlock_controls)



        brightnessLL = findViewById(R.id.videoView_two_layout)
        brightnessSeek = findViewById(R.id.videoView_brightness)
        brightnessSeek.max = 30
        brightness = 15

        if(audioManager == null) audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        Log.i("maxVolume",maxVolume.toString())
        volumeLL = findViewById(R.id.volume_ll)
        volumeSeek = findViewById(R.id.volume_seek)
        volumeSeek.max = maxVolume
        volume = maxVolume/2

        subSelectionView.layoutManager = LinearLayoutManager(this)


        val playerView = findViewById<PlayerView>(R.id.video_view)
        player = ExoPlayer.Builder(this).build()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(50000)
            .setReadTimeoutMs(50000)
            .setTransferListener(DefaultBandwidthMeter.Builder(this).build())

        playPause = findViewById(R.id.videoView_play_pause_btn)
        seekBar = findViewById(R.id.videoView_seekbar)
        titleTv = findViewById(R.id.videoView_title)

        val listener = object : Player.Listener {
            @SuppressLint("UnsafeOptInUsageError")
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (player.duration != C.TIME_UNSET) {
                    // Duration is available
                    seekBar.max = player.duration.toInt()
                    val seek = player.duration / 100 * progress
                    player.seekTo(seek)
                    seekBar.progress = seek.toInt()
                    playerView.setOnTouchListener { _, motionEvent ->
                        if(!isLocked) {
//                            playerView.useController = true
                            gestureDetectorCompat.onTouchEvent(motionEvent)
                            if (motionEvent.action == MotionEvent.ACTION_UP) {
                                if (brightnessLL.visibility == View.VISIBLE || volumeLL.visibility == View.VISIBLE) {
                                    playerView.useController = false
                                    brightnessLL.visibility = View.GONE
                                    volumeLL.visibility = View.GONE

                                }

                            }
                            Handler(Looper.getMainLooper()).postDelayed({
                                playerView.useController = true
                            }, 1000)
                        }
//                        TODO implement lock controls
                        else{

                                if(unlockIv.visibility == View.VISIBLE){
                                    unlockIv.visibility = View.GONE
                                }
                                else{
                                    unlockIv.visibility = View.VISIBLE
                                    Handler(Looper.getMainLooper()).postDelayed({
                                       unlockIv.visibility = View.GONE
                                    }, 2000)
                                }

                        }
                        return@setOnTouchListener false
                    }
                }
                super.onTimelineChanged(timeline, reason)
            }
        }
        player.addListener(listener)

        lifecycleScope.launch (Dispatchers.IO){
            val fileID : List<String> = if (type == "movie") {
                getSubtitles(id)
            } else {
                getSubtitles(id,season,episode)
            }

            subList.postValue(getAuthToken(fileID))
            withContext(Dispatchers.IO){
                    Log.i("DownloadSubCount", subList.value!!.size.toString())
            }
        }

        // When Subtitles are available
        subList.observe(this) {
            if (!it.isNullOrEmpty()) {
                val subtitleConfig: MutableList<SubtitleConfig> = mutableListOf()
                for (i in 0 until it.size) {
                    val subtitleMediaSource =
                        SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(
                            MediaItem.SubtitleConfiguration.Builder(Uri.parse(it[i]))
                                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                                .setLanguage("en")
                                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                                .build(),
                            C.TIME_UNSET
                        )
                    subtitleConfig.add(SubtitleConfig(subtitleMediaSource))
                }

                val videoMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUri))
//                player.setMediaItem(mediaItem)

                subSelectionView.adapter = SubTrackAdapter(subtitleConfig, title) { sub ->
                    val newMediaSource = MergingMediaSource(videoMediaSource, sub.subConfig)
                    val progress = player.currentPosition
                    player.setMediaSource(newMediaSource)
                    player.prepare()
                    player.seekTo(progress)
                }
                mediaSource = MergingMediaSource(videoMediaSource, subtitleConfig[0].subConfig)
//            }
//            else {
//                mediaSource = HlsMediaSource.Factory(dataSourceFactory)
//                    .createMediaSource(MediaItem.fromUri(videoUri))
//            }

                player.setMediaSource(mediaSource)
                if (type == "movie")
                    titleTv.text = title
                else
                    titleTv.text = "$title S$season E$episode"

                mainPlayer.visibility = View.VISIBLE
                playerView.player = player
                videoLoading.visibility = View.GONE

                player.prepare()
                player.playWhenReady = true

                subTracks.setOnClickListener {
                    playerPause()
                    mainPlayer.visibility = View.GONE
                    subSelectBg.visibility = View.VISIBLE
                }

                applySub.setOnClickListener {
                    subSelectBg.visibility = View.GONE
                    mainPlayer.visibility = View.VISIBLE
                    playerPlay()
                }


                playPause.setOnClickListener {
                    play = if (play) {
                        player.pause()
                        playPause.setImageResource(R.drawable.icon_play)
                        false
                    } else {
                        player.play()
                        playPause.setImageResource(R.drawable.netflix_pause_button)
                        true
                    }
                }

                seekForward.setOnClickListener {
                    val progress = (player.currentPosition + 10000)
                    player.seekTo(progress)
                    seekBar.progress = progress.toInt()
                    seekForward.rotation = 90f
                    Handler(Looper.getMainLooper()).postDelayed({
                        seekForward.rotation = 0f
                    }, 1000)
                }

                seekBack.setOnClickListener {
                    val progress = (player.currentPosition - 10000)
                    player.seekTo(progress)
                    seekBar.progress = progress.toInt()
                    seekBack.rotation = -90f
                    Handler(Looper.getMainLooper()).postDelayed({
                        seekBack.rotation = 0f
                    }, 1000)  // Delay of 1 second

                }

                screenScale.setOnClickListener {
                    if (fit) {
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        screenResizeTv.text = "Fill"
                        screenResizeIv.setImageResource(R.drawable.fill_screen)
                        fit = false
                    } else {
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        screenResizeTv.text = "Fit"
                        screenResizeIv.setImageResource(R.drawable.fit_screen)
                        fit = true
                    }
                }

                lockLL.setOnClickListener {
                    isLocked = true
                    playerView.useController = false
//                    unlockIv.visibility = View.VISIBLE
                }


                goBack.setOnClickListener {
                    finish()
                }

                unlockIv.setOnClickListener {
                    isLocked = false
                    playerView.useController = true
                    unlockIv.visibility = View.GONE
                }
            }
        }




        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(player.duration != C.TIME_UNSET) {
                    val rem = player.duration - progress.toLong()
                    if (fromUser) {
                        player.seekTo(progress.toLong())
                        remTimeTv.text = setSeekBarTime(rem)
                    }
                    remTimeTv.text = setSeekBarTime(rem)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: stop updating the SeekBar while the user is dragging it
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: resume updating the SeekBar after the user finished dragging it
                handler.post(updateSeekBarRunnable)
            }
        })




        // Start updating the SeekBar
        handler.post(updateSeekBarRunnable)

    }




    private fun getAuthToken(fileId: List<String>): List<String>{
        val client = OkHttpClient()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body =
            "{\n  \"username\": \"bokaboy_20\",\n  \"password\": \"d.omh.err.y61.7@gmail.com\"\n}".toRequestBody(mediaType)
        try {


            val request = Request.Builder()
                .url("https://api.opensubtitles.com/api/v1/login")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "")
                .addHeader("Accept", "application/json")
                .addHeader("Api-Key", "6YnKoBPSsK8VXYwwrPoZBzVmki0ooTcf")
                .build()
            val response = client.newCall(request).execute()



            if (response.isSuccessful) {
                val gson = Gson()

                val responseBody = response.body

                val type: Type = object : TypeToken<Map<String?, Any?>?>() {}.type
                val map: Map<String, Any> = gson.fromJson(responseBody!!.string(), type)
                val token = map["token"].toString()
                Log.i("response", token)

                responseBody.close()
                val downloadUrlList = mutableListOf<String>()
                for (element in fileId) {
                    downloadUrlList.add(getSubtitleURl(token, element))
                }

                return downloadUrlList.toList()
            } else
                Log.i("response", "unsuccessful")
        }catch (e :Exception){
            e.printStackTrace()
            return listOf()
        }
        return listOf()
    }

    private fun getSubtitleURl(token:String, fileId: String):String{
        val client = OkHttpClient()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = "{\n  \"file_id\": $fileId\n}".toRequestBody(mediaType)
        try{
            val request = Request.Builder()
                .url("https://api.opensubtitles.com/api/v1/download")
                .post(body)
                .addHeader("User-Agent", "")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Api-Key", "6YnKoBPSsK8VXYwwrPoZBzVmki0ooTcf")
                .addHeader("Authorization", token)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful){
                val gson = Gson()

                val responseBody = response.body

                val type: Type = object : TypeToken<Map<String?, Any?>?>() {}.type
                val map: Map<String, Any> = gson.fromJson(responseBody!!.string(), type)
                responseBody.close()
                return map["link"].toString()

            }
        }catch (e :Exception){
            e.printStackTrace()
            return ""
        }
        return ""
    }

    private fun getSubtitles(tmdbId:String, season:Int = 0, episode:Int = 0) : List<String>{
        val client = OkHttpClient()

        try{
            val request : Request
            if(type == "movie") {
                request = Request.Builder()
                    .url("https://api.opensubtitles.com/api/v1/subtitles?tmdb_id=$tmdbId&type=movie&languages=en&order_by=ratings&page=1")
                    .get()
                    .addHeader("User-Agent", "")
                    .addHeader("Api-Key", "6YnKoBPSsK8VXYwwrPoZBzVmki0ooTcf")
                    .build()
            }
            else{
                request = Request.Builder()
                    .url("https://api.opensubtitles.com/api/v1/subtitles?languages=en&order_by=ratings&parent_tmdb_id=$tmdbId&season_number=$season&episode_number=$episode&page=1")
                    .get()
                    .addHeader("User-Agent", "")
                    .addHeader("Api-Key", "6YnKoBPSsK8VXYwwrPoZBzVmki0ooTcf")
                    .build()
            }

            val response = client.newCall(request).execute()
            if (response.isSuccessful){
                val gson = Gson()
                val responseBody = response.body
                val subtitle: Subtitle =
                    gson.fromJson(responseBody!!.string(), Subtitle::class.java)


                responseBody.close()
                val fileIDs = mutableListOf<String>()
                val count = if(subtitle.data.size> 3) 3
                else subtitle.data.size

                for (i in 0 until count){
                    fileIDs.add(subtitle.data[i].attributes.files[0].file_id)
                }
                return fileIDs.toList()
            }
        }catch (e :Exception){
            e.printStackTrace()
            return listOf()
        }
        return listOf()
    }


    private fun playerPause(){
        player.pause()
        playPause.setImageResource(R.drawable.icon_play)
    }
    private fun playerPlay(){
        player.play()
        playPause.setImageResource(R.drawable.netflix_pause_button)
    }

    override fun onPause(){
        player.pause()
        playPause.setImageResource(R.drawable.icon_play)
        super.onPause()

    }

    override fun onDestroy() {

        val currentPosition = player.currentPosition
        val duration = player.duration
        val progress = (currentPosition * 100 / duration).toInt()

        if (progress > 2) {
            GlobalScope.launch(Dispatchers.IO) {
                if (type == "movie") {
                    watchHistoryDao.insert(
                        ContinueWatching(
                            progress = progress,
                            imgLink = imgLink!!,
                            tmdbID = id.toInt(),
                            title = title,
                            type = type!!
                        )
                    )
                } else if (type == "tvshow") {
                    watchHistoryDao.insert(
                        ContinueWatching(
                            progress = progress,
                            imgLink = imgLink!!,
                            tmdbID = id.toInt(),
                            title = title,
                            season = season,
                            episode = episode,
                            type = type!!
                        )
                    )
                }
            }
        }
        Log.i("Progress", progress.toString())
        player.playWhenReady = false
        player.stop()
        player.seekTo(0)
        player.release() // or pause, depending on your requirements
        handler.removeCallbacks(updateSeekBarRunnable)
        Log.i("Finish", "Called finish() go back pressed")
        super.onDestroy()
    }

    override fun onDown(p0: MotionEvent): Boolean = false

    override fun onShowPress(p0: MotionEvent) = Unit

    override fun onSingleTapUp(p0: MotionEvent): Boolean = false
    override fun onScroll(event: MotionEvent?, event1: MotionEvent, dX: Float, dY: Float): Boolean {
        minSwipeY += dY

        val sWidth = Resources.getSystem().displayMetrics.widthPixels

        if(abs(dX)< abs(dY) && abs(minSwipeY) > 50){
            if(event!!.x < sWidth/2){
                brightnessLL.visibility = View.VISIBLE
                volumeLL.visibility = View.GONE
                val increase = dY > 0
                val newValue = if(increase) brightness + 1 else brightness - 1

                if(newValue in 0..30) brightness = newValue
                brightnessSeek.progress = brightness
                setScreenBrightness(brightness)
            }
            else{
                brightnessLL.visibility = View.GONE
                volumeLL.visibility = View.VISIBLE

                val increase = dY > 0
                val newValue = if(increase) volume + 5 else volume - 5
                if(newValue in 0..maxVolume) volume = newValue
                volumeSeek.progress = volume
                audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            }
            minSwipeY = 0f
        }
        return true
    }

    private fun setScreenBrightness(value: Int){
        val d = 1.0f/30
        val lp = this.window.attributes
        lp.screenBrightness = d * value
        this.window.attributes = lp
    }

    override fun onResume() {
        super.onResume()
        if(audioManager == null) audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if(brightness != 0) setScreenBrightness(brightness)
    }


//    @Deprecated("Deprecated in Java")
//    override fun onBackPressed() {
//        onDestroy()
//        super.onBackPressed()
//    }

    override fun onLongPress(p0: MotionEvent) = Unit
    override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean  = false
    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange <= 0) playerPause()
    }
}
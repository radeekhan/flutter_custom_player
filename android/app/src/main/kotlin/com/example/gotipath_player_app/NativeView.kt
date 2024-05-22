package com.example.gotipath_player_app

import android.content.Context
import android.os.Handler
import android.net.Uri
import android.content.pm.ActivityInfo
import android.view.View
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
//import com.google.android.exoplayer2.trackselection.TrackSelectionOverrides
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.AdsConfiguration
import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration
import com.google.android.exoplayer2.MediaItem.Subtitle
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ads.AdPlaybackState
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.common.collect.ImmutableList
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import com.mux.stats.sdk.core.MuxSDKViewOrientation
import com.mux.stats.sdk.core.model.CustomData
import com.mux.stats.sdk.core.model.CustomerData
import com.mux.stats.sdk.core.model.CustomerPlayerData
import com.mux.stats.sdk.core.model.CustomerVideoData
import com.mux.stats.sdk.core.model.CustomerViewData
import com.mux.stats.sdk.core.model.CustomerViewerData
import com.mux.stats.sdk.muxstats.MuxStatsExoPlayer
import java.util.UUID
import org.matomo.sdk.TrackMe
import org.matomo.sdk.QueryParams
import org.matomo.sdk.extra.TrackHelper


internal class NativeView(context: Context, id: Int, creationParams: Map<String?, Any?>?,messenger: BinaryMessenger,
                          mainActivity: com.example.gotipath_player_app.MainActivity) : PlatformView,
    MethodChannel.MethodCallHandler {
    private val playerView: PlayerView
    private val trackSelector: DefaultTrackSelector = DefaultTrackSelector(context)
    private var adsLoader: ImaAdsLoader? = null
    private var eventListener : AdsLoader.EventListener? = null
    var player: ExoPlayer? = null
    var muxStatsExoPlayer: MuxStatsExoPlayer? = null
    var initial: Boolean = true
    val concatenatingMediaSource = ConcatenatingMediaSource()
    private val methodChannel: MethodChannel
    override fun getView(): View {
        return playerView
    }


    override fun dispose() {
        adsLoader!!.setPlayer(null)
     //   player!!.removeAnalyticsListener(analyticsListener)
     //   player!!.removeListener(playBackStateListener())
        playerView.player = null
        player!!.release()
        player = null

        //ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    companion object{
        var mediaEvent = TrackMe()
    }

   // lateinit var analyticsListener : PlayerAnalyticsListener


//    private fun playBackStateListener(video_url: String,video_title: String,device_category: String, device_model: String, device_os_version: String, video_id: String,video_series: String, user_id: String,player: ExoPlayer?,user_info: Map<String?, Any?>,matomo_url: String, matomo_site_id: Int) = object : Player.Listener{
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            if (playbackState == ExoPlayer.STATE_BUFFERING) {
//
//            }
//            else if (playbackState == ExoPlayer.STATE_READY || playbackState == 9 ||  playbackState == 10){
//
////                mediaEvent.set(PlayerAnalyticsUnit.ma_id.toString(), video_id)
////                mediaEvent.set(PlayerAnalyticsUnit.ma_re.toString(), video_url)
////                mediaEvent.set(PlayerAnalyticsUnit.ma_mt.toString(), "video")
////                mediaEvent.set(PlayerAnalyticsUnit.ma_ti.toString(), video_title)
////                mediaEvent.set(PlayerAnalyticsUnit.ma_pn.toString(), "Flutter_Exoplayer")
////                mediaEvent.set(PlayerAnalyticsUnit.ma_le.toString(), (player!!.duration.toInt()/1000).toString())
////                mediaEvent.set(PlayerAnalyticsUnit.ma_ps.toString(), (player!!.currentPosition.toInt()/1000).toString())
////
////                mediaEvent.set(PlayerAnalyticsUnit.ma_st.toString(), "0")
////                mediaEvent.set(PlayerAnalyticsUnit.ma_ttp.toString(), "3")
////                mediaEvent.set(PlayerAnalyticsUnit.ma_w.toString(), player!!.videoSize.width.toString())
////                mediaEvent.set(PlayerAnalyticsUnit.ma_h.toString(), player!!.videoSize.height.toString())
////                mediaEvent.set(PlayerAnalyticsUnit.ma_fs.toString(), "0")
////                mediaEvent.set(PlayerAnalyticsUnit.ma_se.toString(), "")
////
////            //    PlayerUtils.getTracker(playerView.context)!!.track(mediaEvent)
////               // PlayerUtils.getTracker(playerView.context)!!.dispatch()
////                TrackHelper.track(mediaEvent)
////                    .dimension(1, user_info["cd_userId"].toString())
////                    .dimension(2, user_info["cd_SubscriptionStatus"].toString())
////                    .screen("")
////                    .with(PlayerUtils.getTracker(playerView.context,matomo_url,matomo_site_id))
//
//            }
//        }
//    }


    fun setTrackParameters(width: Int, height: Int, bitrate: Int) {
        val parametersBuilder = trackSelector.buildUponParameters()

        if (width != 0 && height != 0) {
            parametersBuilder.setMaxVideoSize(width, height)
        }
        if (bitrate != 0) {
            parametersBuilder.setMaxVideoBitrate(bitrate)
        }
        if (width == 0 && height == 0 && bitrate == 0) {
            parametersBuilder.clearVideoSizeConstraints()
            parametersBuilder.setMaxVideoBitrate(Int.MAX_VALUE)
        }
        trackSelector.setParameters(parametersBuilder)
    }


    fun addNewUrl(video_url: String, ad_url: String){
        val mediaSources = ArrayList<MediaSource>()
        val contentUri = Uri.parse(video_url)
        val adTagUri = Uri.parse(ad_url)
        val mediaItem = MediaItem.Builder().setUri(contentUri).setAdsConfiguration(AdsConfiguration.Builder(adTagUri).build()).build()
        mediaSources.add(mediaSourceFactory.createMediaSource(mediaItem))

        concatenatingMediaSource.addMediaSources(mediaSources)


        Handler().postDelayed({
            player!!.seekToNextMediaItem()
            player!!.setPlayWhenReady(true)
        }, 1000)
    }


//    fun muxStatCustomerData(video_url: String,video_title: String,device_category: String, device_model: String, device_os_version: String, video_id: String,video_series: String, user_id: String){
//        val customerData = CustomerData().apply {
//            customerVideoData = CustomerVideoData().apply {
//                // Data about this video
//                // Add or change properties here to customize video metadata such as title,
//                //   language, etc
//                videoTitle = video_title
//
//                videoId= video_id
//
//                if(video_series!=null || video_series!="")
//                    videoSeries=video_series
//
//
//                // ExoPlayer doesn't provide an API to obtain this, so it must be set manually
//                videoSourceUrl = video_url
//            }
//            customerViewData = CustomerViewData().apply {
//                // Data about this viewing session
//                if(user_id==null || user_id=="")
//                    viewSessionId = UUID.randomUUID().toString()
//                else
//                    viewSessionId = user_id
//            }
//            customerViewerData = CustomerViewerData().apply {
//                // Data about the Viewer and the device they are using
//                muxViewerDeviceCategory = device_category
//                muxViewerDeviceManufacturer = device_model
//                muxViewerOsVersion = device_os_version
//            }
//        }
//
//        muxStatsExoPlayer = MuxStatsExoPlayer(
//            playerView.context,
//            "",
//            player!!,
//            customerData
//        )
//
//    }
//



    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "loadUrl" -> {
                val url: String = call.arguments.toString()
            }
            "pauseVideo" -> {
                player!!.pause()
            }
            "setBitrate" -> {
                setTrackParameters(
                    call.argument("TRACK_WIDTH")!!,
                    call.argument("TRACK_HEIGHT")!!,
                    call.argument("TRACK_BITRATE")!!
                )
            }
            "resumeVideo" -> {
                player!!.play()
            }
            "playNewVideo" -> {

                player!!.pause()
                val content_url: String? = call.argument("video_url")
                val ad_url: String? = call.argument("ad_url")
                val video_title: String? = call.argument("video_title")
//                val device_category: String? = call.argument("device_category")
//                val device_model: String? = call.argument("device_model")
//                val device_os_version: String? = call.argument("device_os_version")
                val video_id: String? = call.argument("video_id")
                val video_series: String? = call.argument("video_series")
                //   val user_id: String? = call.argument("user_id")

//                val pre_content_url: String = call.argument("pre_video_url")!!

                val customerVideoData = CustomerVideoData().apply {
                    // Data about this video
                    // Add or change properties here to customize video metadata such as title,
                    //   language, etc
                    videoTitle = video_title

                    videoId= video_id

                    if(video_series!=null || video_series!="")
                        videoSeries=video_series


                    // ExoPlayer doesn't provide an API to obtain this, so it must be set manually
                    videoSourceUrl = content_url
                }


                addNewUrl(content_url!!,ad_url!!)
                muxStatsExoPlayer!!.videoChange(customerVideoData)

                // muxStatCustomerData(content_url!!,video_title!!,device_category!!,device_model!!,device_os_version!!,video_id!!,video_series!!,user_id!!,true)
                //  muxStatsExoPlayer!!.setPlayerView(playerView)


                //   val mediaSource = buildMediaSource(content_url!!,ad_url!!)

                //   player!!.repeatMode = Player.REPEAT_MODE_ALL

                //  player!!.prepare(mediaSource, false, false)
                //   player!!.setPlayWhenReady(true)

//                player!!.seekToNextMediaItem()
//                player!!.setPlayWhenReady(true)

            }
            "player#currentPosition" -> {
                val arguments = HashMap<String, Any>()
                arguments["currentPosition"] = player!!.currentPosition
                result.success(arguments)
            }
            "player#seekTo" -> {
                val position: Int? = call.argument("position")
                if (position != null)
                    player!!.seekTo(position.toLong())
                result.success(null)
            }
            "player#getDuration" -> {
                val arguments = HashMap<String, Any>()
                arguments["getDuration"] = player!!.getDuration()
                result.success(arguments)
            }
            "player#isPlaying" -> {
                val arguments = HashMap<String, Any>()
                arguments["isPlaying"] = player!!.isPlaying
                result.success(arguments)
            }
            "player#AdStatus" -> {
                val arguments = HashMap<String, Any>()
                arguments["AdStatus"] = player!!.isPlayingAd()
                result.success(arguments)
            }

            else -> result.notImplemented()
        }
    }

    lateinit var dataSourceFactory: DataSource.Factory

    lateinit var mediaSourceFactory: MediaSourceFactory


    init {
        methodChannel = MethodChannel(messenger, "video_player_view")
        methodChannel.setMethodCallHandler(this)
        playerView = PlayerView(context)

        adsLoader = ImaAdsLoader.Builder( /* context= */context)
            //  .setAdEventListener(muxStatsExoPlayer?.getAdsImaSdkListener()!!)
            .build()
        if (Util.SDK_INT > 23) {
            initializePlayer(id,mainActivity,creationParams,methodChannel)
        }
        val url = creationParams as Map<String?, Any?>?
     //   val matomo_user_info=url!!.get("user_info") as Map<String?, Any?>
    //    PlayerUtils.getTracker(context,url?.get("matomo_url") as String,url?.get("matomo_site_id") as Int)!!.defaultTrackMe.set(QueryParams.USER_ID,matomo_user_info["userId"].toString())
    }

    private fun initializePlayer(
        id: Int,
        mainActivity: MainActivity,
        creationParams: Map<String?, Any?>?,
        methodChannel: MethodChannel
    ) {


        // Set up the factory for media sources, passing the ads loader and ad view providers.
        val dataSourceFactory1: DataSource.Factory =
            DefaultDataSourceFactory(view.context, Util.getUserAgent(playerView.context, "flios"))
        val mediaSourceFactory1: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory1)
            .setAdsLoaderProvider {  unusedAdTagUri: AdsConfiguration? -> adsLoader }
            .setAdViewProvider(playerView)

        dataSourceFactory=dataSourceFactory1
        mediaSourceFactory=mediaSourceFactory1

        val url = creationParams as Map<String?, Any?>?
       // var user_info=url?.get("user_info") as Map<String?,Any?>
        //  muxStatCustomerData(url?.get("videoURL") as String);
        val params = DefaultTrackSelector.ParametersBuilder().apply {
            var resolution=1080
           // if(user_info["cd_SubscriptionStatus"].toString()=="Non Subscribed" && resolution>0)
                setMaxVideoSize((resolution/9)*16, resolution)
        }.build()
        trackSelector.parameters = params
        player = ExoPlayer.Builder(view.context).setTrackSelector(trackSelector).setMediaSourceFactory(mediaSourceFactory).build()
     //   muxStatCustomerData(url?.get("videoURL") as String,url?.get("video_title") as String,url?.get("device_category") as String,url?.get("device_model") as String,url?.get("device_os_version") as String,url?.get("video_id") as String,url?.get("video_series") as String,url?.get("user_id") as String)
        player!!.preparePlayer(playerView, true,mainActivity,methodChannel)
        playerView.player = player
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
        adsLoader!!.setPlayer(player)
        playerView.setKeepScreenOn(true);
        playerView.useController = false
        //  playerView.isControllerVisible
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
    //    player!!.addListener(playBackStateListener(url?.get("videoURL") as String,url?.get("video_title") as String,url?.get("device_category") as String,url?.get("device_model") as String,url?.get("device_os_version") as String,url?.get("video_id") as String,url?.get("video_series") as String,url?.get("user_id") as String,player!!,url?.get("user_info") as Map<String?,Any?>,url?.get("matomo_url") as String,url?.get("matomo_site_id") as Int))
   //     analyticsListener = PlayerAnalyticsListener(playerView.context,player!!,url?.get("matomo_url") as String,url?.get("matomo_site_id") as Int,url?.get("videoURL") as String,url?.get("video_title") as String,url?.get("video_id") as String,url?.get("video_series") as String,url?.get("user_id") as String,url?.get("user_info") as Map<String?,Any?>)
 //       player!!.addAnalyticsListener(analyticsListener)
//        playerView.fullScreen()
        //  playerView.showController()
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
//        player.videoScalingMode=C.VIDEO_SCALING_MODE_TO_FIT_WITH_CROPPING
        playerView.controllerHideOnTouch=false
        playerView.getSubtitleView()
    //    muxStatsExoPlayer!!.setPlayerView(playerView)
        // Create the MediaItem to play, specifying the content URI and ad tag URI.
        // val contentUri = Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4")


        val contentUri = Uri.parse(url?.get("videoURL") as String?)
        var preContentUri= Uri.parse("")
        var assetSrtUri = Uri.parse("")
        var adTagUri = Uri.parse("")
        val mediaSources = ArrayList<MediaSource>()

        if(url?.get("subtitleURL")!=null){
            assetSrtUri = Uri.parse(url?.get("subtitleURL") as String?)
        }

        if(url?.get("vastURL")!=null){
            adTagUri = Uri.parse(url?.get("vastURL") as String?)
        }

        if(url?.get("preVideoUrl")!=null){
            preContentUri = Uri.parse(url?.get("preVideoUrl") as String?)
        }




        val subtitle = SubtitleConfiguration.Builder(assetSrtUri)
            .setMimeType(MimeTypes.TEXT_VTT)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()



        var adPlaybackState = AdPlaybackState(0, 500 * C.MICROS_PER_SECOND)
        adPlaybackState = adPlaybackState.withAdUri(0, 0, adTagUri)

        eventListener?.onAdPlaybackState(adPlaybackState);


        val contentStart = MediaItem.Builder().setUri(contentUri)
            .setAdsConfiguration(
                AdsConfiguration.Builder(adTagUri).build())
            .build()

        mediaSources.add(mediaSourceFactory.createMediaSource(contentStart))



        //  player!!.setMediaItem(contentStart)
        if(url?.get("preVideoUrl")!=null) {
            val mediaItem = MediaItem.Builder().setUri(preContentUri).build()
            mediaSources.add(mediaSourceFactory.createMediaSource(mediaItem))
        }


//        player!!.repeatMode = Player.REPEAT_MODE_ALL


        concatenatingMediaSource.addMediaSources(mediaSources)

        player!!.prepare(concatenatingMediaSource, false, false)
        player!!.setPlayWhenReady(true)
        player!!.seekTo(0)


    }


}

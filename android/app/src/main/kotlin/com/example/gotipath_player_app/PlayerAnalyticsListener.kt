package com.example.gotipath_player_app

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.matomo.sdk.extra.TrackHelper
import com.google.android.exoplayer2.MediaItem
import android.widget.Toast
import org.matomo.sdk.TrackMe

class PlayerAnalyticsListener(private val context: Context, private val player: ExoPlayer?,private val matomo_url: String,private val matomo_site_id: Int, private val video_url: String,private val video_title: String,private val video_id: String,private val video_series: String,private val user_id: String,private val user_info: Map<String?, Any?>) :
    AnalyticsListener {
    private var currentPositionTracker: Runnable? = null
    private var lastKnownPlaybackPercent: Long = -1
    private var lastKnownPlaybackPos: Long = -1
    private val timer = TotalPlayTime()
    private var mediaEvent = TrackMe()

    private var currentPositionTrackerJob: Job? = null

    private fun registerCurrentPositionTracker() {
        unRegisterCurrentPositionTracker()

//        currentPositionTrackerJob = CoroutineScope(Dispatchers.Main).launch {
//            while (isActive) {
//                val currentPosition = player!!.currentPosition
//                val percent = currentPosition * 100 / player.duration
//                if (lastKnownPlaybackPos != currentPosition) {
//                    lastKnownPlaybackPos = currentPosition
//                    lastKnownPlaybackPercent = percent
//
//                    NativeView.mediaEvent.set(PlayerAnalyticsUnit.ma_st.toString(),(player.currentPosition.toInt() / 1000).toString())
//
//                    TrackHelper.track(NativeView.mediaEvent)
//                        .screen("")
//                        .with(PlayerUtils.getTracker(context,matomo_url,matomo_site_id))
//
//                }
//                delay(10000) // Delay for 10 seconds
//            }
//        }


        currentPositionTrackerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val currentPosition = player!!.currentPosition
                val percent = currentPosition * 100 / player.duration
                if (lastKnownPlaybackPos != currentPosition) {
                    lastKnownPlaybackPos = currentPosition
                    lastKnownPlaybackPercent = percent

                    fireMedia()

                }
                delay(10000) // Delay for 10 seconds
            }
        }
    }

    private fun unRegisterCurrentPositionTracker() {
        currentPositionTrackerJob?.cancel()
        currentPositionTrackerJob = null
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == ExoPlayer.STATE_READY || state == 9|| state == 8) {
            fireMedia()
        }
        if (state == ExoPlayer.STATE_READY && player!!.playWhenReady) {
            if (currentPositionTracker == null) {
                registerCurrentPositionTracker()
            }
        } else if (player!!.playWhenReady) {
            unRegisterCurrentPositionTracker()
            if (player.duration != 0L && player.duration <= player.currentPosition) {

            } else {
            }
        } else {
            unRegisterCurrentPositionTracker()
        }
    }

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        super.onIsPlayingChanged(eventTime, isPlaying)

        //start-resume and pause-stop time
        timer.update()
        if (isPlaying)timer.start()
        else timer.stop()
    }

    override fun onMediaItemTransition(
        eventTime: AnalyticsListener.EventTime,
        mediaItem: MediaItem?,
        reason: Int
    ) {
        super.onMediaItemTransition(eventTime, mediaItem, reason)
      //  Toast.makeText(context, "Video Changed", Toast.LENGTH_SHORT).show()
        timer.reset()
        timer.stop()
        timer.start()
    }

    private fun fireMedia(){
        val elapsedMillis = timer.update()

        mediaEvent.set(PlayerAnalyticsUnit.ma_id.toString(), video_id)
        mediaEvent.set(PlayerAnalyticsUnit.ma_re.toString(), video_url)
        mediaEvent.set(PlayerAnalyticsUnit.ma_mt.toString(), "video")
        mediaEvent.set(PlayerAnalyticsUnit.ma_ti.toString(), video_title)
        mediaEvent.set(PlayerAnalyticsUnit.ma_pn.toString(), "Flutter_Exoplayer")

        mediaEvent.set(PlayerAnalyticsUnit.ma_st.toString(), (elapsedMillis.toInt() / 1000).toString())

        mediaEvent.set(PlayerAnalyticsUnit.ma_le.toString(), (player!!.duration.toInt()/1000).toString())
        mediaEvent.set(PlayerAnalyticsUnit.ma_ps.toString(), (player!!.currentPosition.toInt()/1000).toString())
        mediaEvent.set(PlayerAnalyticsUnit.ma_ttp.toString(), "3")
        mediaEvent.set(PlayerAnalyticsUnit.ma_w.toString(), player!!.videoSize.width.toString())

        mediaEvent.set(PlayerAnalyticsUnit.ma_h.toString(), player!!.videoSize.height.toString())
        mediaEvent.set(PlayerAnalyticsUnit.ma_fs.toString(), "0")
        mediaEvent.set(PlayerAnalyticsUnit.ma_se.toString(), "")

        TrackHelper.track(mediaEvent)
            .dimension(1, user_info["cd_userId"].toString())
            .dimension(2, user_info["cd_SubscriptionStatus"].toString())
            .screen("")
            .with(PlayerUtils.getTracker(context,matomo_url,matomo_site_id))

        Log.d("MATOMO_ANALYTICS", mediaEvent.toMap().toString())
    }
}
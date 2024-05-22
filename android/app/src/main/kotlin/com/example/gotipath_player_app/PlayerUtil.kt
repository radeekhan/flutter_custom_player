package com.example.gotipath_player_app

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import com.google.common.collect.ImmutableList
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

class PlayerUtils {
    companion object{
//        const val SAMPLE_VIDEO_URL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
//        const val SAMPLE_VIDEO_URL_M3U8 = "https://vhx9nfhlsy.gpcdn.net/transcoded/2023/08/15/1008537/1/3/1835/manifest.m3u8"
//        const val SAMPLE_VIDEO_URL_DKnight = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
//        const val SAMPLE_VIDEO_URL_Running = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4"
//        const val SAMPLE_VIDEO_URL_BigBuckBunny = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
//        const val SAMPLE_VIDEO_URL_Elephant = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
//
//        const val SAMPLE_VAST_TAG_URL_NULL = ""
//        const val VMAP_ADS_TAG = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpost&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&cmsid=496&vid=short_onecue&correlator="
//        const val SAMPLE_VAST_TAG_URL = ("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/" + "single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast" + "&unviewed_position_start=1&env=vp&impl=s&correlator=")
//
//        //MEDIA ITEMS
//        private val firstMedia: Uri = Uri.parse(SAMPLE_VIDEO_URL_M3U8)
//        private val secondMedia: Uri = Uri.parse(SAMPLE_VIDEO_URL_M3U8)
//        private val thirdMedia: Uri = Uri.parse(SAMPLE_VIDEO_URL_M3U8)
//        private val fourthMedia: Uri = Uri.parse(SAMPLE_VIDEO_URL_M3U8)
//        var mediaItemTitle = arrayListOf("BigBuckBunny", "Dark Knight", "Jogging", "Elephants Dream")
//        var mediaItemList: List<MediaItem> = ImmutableList.of(
//            MediaItem.fromUri(firstMedia),
//            MediaItem.fromUri(secondMedia),
//            MediaItem.fromUri(thirdMedia),
//            MediaItem.fromUri(fourthMedia)
//        )
//        var mediaItemLinks = arrayListOf(
//            PlayerUtils.SAMPLE_VIDEO_URL_M3U8,
//            PlayerUtils.SAMPLE_VIDEO_URL_M3U8,
//            PlayerUtils.SAMPLE_VIDEO_URL_M3U8,
//            PlayerUtils.SAMPLE_VIDEO_URL_M3U8
//        )

        private var tracker: Tracker? = null
        @Synchronized
        fun getTracker(context: Context, url: String, siteId: Int): Tracker? {
            if (tracker == null) {
                tracker = TrackerBuilder.createDefault(url+"/matomo.php", siteId)
                    .build(Matomo.getInstance(context))
            }
            return tracker
        }
    }

}
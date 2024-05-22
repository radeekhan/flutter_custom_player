import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

class PlayerScreen extends StatefulWidget {
  const PlayerScreen({super.key});

  @override
  State<PlayerScreen> createState() => _PlayerScreenState();
}

class _PlayerScreenState extends State<PlayerScreen> {

   String viewType = 'video_player_view';
   Map<String, dynamic> creationParams = <String, dynamic>{
      "ad_url": "",
       "width": "",
       "height": "",
       "videoURL": "https://vhx9nfhlsy.gpcdn.net/transcoded/2022/12/17/995052/1/3/1835/manifest.m3u8",
       "preVideoUrl": null,
       "vastURL": "",
       "subtitleUrl":"",
       "TRACK_WIDTH":"",
       "TRACK_HEIGHT":"",
       "TRACK_BITRATE":"",
       "video_title" : "",
       "device_model": "",
       "device_os_version": "",
       "device_category": "",
       "video_id": "",
       "video_series": "",
       "user_id": "",
       "user_info": {},
       "max_resolution": "1080",
       "matomo_url": "",
       "matomo_site_id": "1"

   };
   MethodChannel _channel=MethodChannel('video_player_view');

   Future<void> playNewVideo(String url,String adUrl) async {
     Map<String, dynamic> args = {
       "video_url": url,
       "ad_url": adUrl
     };
     print(args);
     final result = await _channel.invokeMethod("playNewVideo", args);
     print(result);
   }

   Future<void> releasePlayer() async {
     final result = await _channel.invokeMethod("releasePlayer");
     print(result);
   }

   Future<void> play() async {
     final result = await _channel.invokeMethod("play");
     print(result);
   }

   Future<void> pause() async {
     final result = await _channel.invokeMethod("pause");
     print(result);
   }

   AppLifecycleState lastAppState = AppLifecycleState.resumed;

   @override
   void didChangeAppLifecycleState(AppLifecycleState state) {

    print("app life cycle state: ${state.name}");

     if (state == AppLifecycleState.paused) {

        pause();

     } else if (state == AppLifecycleState.resumed && lastAppState == AppLifecycleState.paused) {
       play();
     }
     lastAppState = state;
   }

   @override
  void dispose() {
     releasePlayer();
    // TODO: implement dispose
    super.dispose();
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [

          Container(
            height: MediaQuery.of(context).orientation== Orientation.portrait ? 500 : MediaQuery.of(context).size.height,
            width: MediaQuery.of(context).size.width,
            child: Platform.isAndroid ? PlatformViewLink(
              viewType: viewType,
              surfaceFactory:
                  (context, controller) {
                return AndroidViewSurface(
                  controller: controller as AndroidViewController,
                  gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
                  hitTestBehavior: PlatformViewHitTestBehavior.opaque,
                );
              },
              onCreatePlatformView: (params) {
                return PlatformViewsService.initSurfaceAndroidView(
                  id: params.id,
                  viewType: viewType,
                  layoutDirection: TextDirection.ltr,
                  creationParams: creationParams,
                  creationParamsCodec: const StandardMessageCodec(),
                  onFocus: () {
                    params.onFocusChanged(true);
                  },
                )
                  ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
                  ..create();
              },
            ) :
              UiKitView(
              viewType: viewType,
              layoutDirection: TextDirection.ltr,
              creationParams: creationParams,
              gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
              hitTestBehavior: PlatformViewHitTestBehavior.opaque,
              creationParamsCodec: StandardMessageCodec(),
              )
                  ),



        ],
      ),
    );
  }
}

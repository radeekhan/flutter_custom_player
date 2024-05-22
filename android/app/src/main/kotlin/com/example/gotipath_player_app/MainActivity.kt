package com.example.gotipath_player_app

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("video_player_view", NativeViewFactory(flutterEngine.dartExecutor.binaryMessenger,this))

    }
}

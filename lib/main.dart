import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

import 'player_screen.dart';


void main() {
  runApp(const MyApp());
}

class Myapp extends StatefulWidget {
  const Myapp({super.key});

  @override
  State<Myapp> createState() => _MyappState();
}

class _MyappState extends State<Myapp> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Video Player'),
      ),
      body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children:[
            InkWell(
              onTap: () {
                Navigator.push (
                  context,
                  MaterialPageRoute (
                    builder: (BuildContext context) => PlayerScreen(),
                  ),
                );
              },
              child: Center(
                child: Container(
                  height: 30,
                  color: Colors.amber,
                  width: MediaQuery.of(context).size.width*.5,
                  child: Center(child: Text('Play video')),
                ),
              ),
            )
          ]
      ),
    );
  }
}


class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {

    return MaterialApp(
      home: Myapp(),
    );

  }
}
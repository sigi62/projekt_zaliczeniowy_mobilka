import 'package:flutter/material.dart';
import 'dart:io' show Platform;
import 'package:flutter/services.dart';

class ARScreen extends StatelessWidget {
  const ARScreen({super.key});

  static const platform = MethodChannel("com.example.ar/ar");


  Future<void> _startAR() async {
    try {
      if (Platform.isAndroid) {
        await platform.invokeMethod("startAR"); // launches ARActivity
      } else if (Platform.isIOS) {
        // TODO: launch ARKit screen via channel
      }
    } on PlatformException catch (e) {
      debugPrint("Failed to open AR: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("AR Screen")),
      body: Center(
        child: ElevatedButton(
          onPressed: _startAR,
          child: const Text("Launch AR"),
        ),
      ),
    );
  }
}

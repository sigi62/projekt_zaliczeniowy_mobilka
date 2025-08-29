import 'package:flutter/material.dart';
import 'dart:io' show Platform;
import 'package:flutter/services.dart';
import 'models_screen.dart';
import '../globals.dart'; // for selectedModel

class StartScreen extends StatelessWidget {
  final String? modelName; // nullable
  const StartScreen({super.key, this.modelName});

  static const platform = MethodChannel("com.example.ar/ar");

  Future<void> _startAR() async {
    try {
      if (Platform.isAndroid) {
        final args = modelName != null ? {"model": modelName} : null;
        await platform.invokeMethod("startAR", args);
      } else if (Platform.isIOS) {
        // TODO: launch ARKit screen via channel
      }
    } on PlatformException catch (e) {
      debugPrint("Failed to open AR: ${e.message}");
    }
  }

  void _openModelsScreen(BuildContext context) async {
    await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const ModelsScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {

    final displayModelName = selectedModelGlobal != null
        ? selectedModelGlobal!.replaceAll(RegExp(r'\.glb$', caseSensitive: false), '')
        : null;
    return Scaffold(
      appBar: AppBar(title: const Text("Start Screen")),
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ElevatedButton(
              onPressed: () => _openModelsScreen(context),
              child: Text(
                displayModelName != null ? "Model: $displayModelName" : "Select Model",
              ),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _startAR,
              child: const Text("Launch AR"),
            ),
          ],
        ),
      ),
    );
  }
}

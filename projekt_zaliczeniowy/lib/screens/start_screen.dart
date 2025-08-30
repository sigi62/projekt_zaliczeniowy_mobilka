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
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              GridView.builder(
                shrinkWrap: true,
                physics: NeverScrollableScrollPhysics(),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2, // 2 buttons per row
                  mainAxisSpacing: 16,
                  crossAxisSpacing: 16,
                  childAspectRatio: 2.5, // Make buttons larger
                ),
                itemCount: 2,
                itemBuilder: (context, index) {
                  return ElevatedButton(
                    onPressed: index == 0 ? () => _openModelsScreen(context) : _startAR,
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size.fromHeight(60), // increase button height
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    child: Text(
                      index == 0
                          ? (displayModelName != null ? "Model: $displayModelName" : "Select Model")
                          : "Launch AR",
                      style: const TextStyle(fontSize: 18),
                    ),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

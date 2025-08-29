import 'dart:convert';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../globals.dart';
import 'start_screen.dart';

class ModelsScreen extends StatefulWidget {
  const ModelsScreen({super.key});

  @override
  State<ModelsScreen> createState() => _ModelsScreenState();
}

class _ModelsScreenState extends State<ModelsScreen> {
  List<String> models = [];
  String? selectedModel;
  final Random _random = Random();
  final Map<String, Color> _buttonColors = {};

  @override
  void initState() {
    super.initState();
    loadModels();
    selectedModel = selectedModelGlobal; // initialize from global
  }

  Future<void> loadModels() async {
    final jsonStr = await rootBundle.loadString('assets/models/models.json');
    final List<dynamic> modelsList = json.decode(jsonStr)['models'];
    setState(() {
      models = modelsList.cast<String>();
      // Assign a consistent random light color to each model
      for (var model in models) {
        _buttonColors[model] = Color.fromARGB(
          255,
          150 + _random.nextInt(106),
          150 + _random.nextInt(106),
          150 + _random.nextInt(106),
        );
      }
    });
  }

  Future<void> _confirmSelection() async {
    selectedModelGlobal = selectedModel; // update global variable
    const platform = MethodChannel("com.example.ar/ar");

    try {
      final args = selectedModel != null ? {"model": selectedModel} : null;
      await platform.invokeMethod("startAR", args); // launch AR directly
    } on PlatformException catch (e) {
      debugPrint("Failed to open AR: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Select Model"),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            selectedModelGlobal = selectedModel; // persist selection
            Navigator.pushReplacement(
              context,
              MaterialPageRoute(
                builder: (_) => StartScreen(modelName: selectedModel),
              ),
            );
          },
        ),
      ),
      body: models.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : Column(
        children: [
          Expanded(
            child: GridView.builder(
              padding: const EdgeInsets.all(16),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                mainAxisSpacing: 16,
                crossAxisSpacing: 16,
                childAspectRatio: 0.9,
              ),
              itemCount: models.length,
              itemBuilder: (context, index) {
                final model = models[index];
                final isSelected = selectedModel == model;
                final color = _buttonColors[model] ?? Colors.grey[800]!;

                // Display name without .glb
                final displayName = model.replaceAll(RegExp(r'\.glb$', caseSensitive: false), '');

                return GestureDetector(
                  onTap: () {
                    setState(() {
                      selectedModel = isSelected ? null : model; // toggle
                    });
                  },
                  child: Container(
                    decoration: BoxDecoration(
                      color: isSelected ? Colors.green[400] : color,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.3),
                          offset: const Offset(0, 3),
                          blurRadius: 5,
                        ),
                      ],
                    ),
                    padding: const EdgeInsets.all(8),
                    child: Center(
                      child: Text(
                        displayName,
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Colors.black87,
                          fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: ElevatedButton(
              onPressed: _confirmSelection,
              style: ElevatedButton.styleFrom(
                minimumSize: const Size.fromHeight(50),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              child: Text(
                selectedModel == null
                    ? "Launch AR without model"
                    : "Launch AR with ${selectedModel!.replaceAll(RegExp(r'\.glb$', caseSensitive: false), '')}",
                style: const TextStyle(fontSize: 18),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

import 'dart:convert';
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

  @override
  void initState() {
    super.initState();
    loadModels();
  }

  Future<void> loadModels() async {
    final jsonStr = await rootBundle.loadString('assets/models/models.json');
    final Map<String, dynamic> data = json.decode(jsonStr);
    final List<dynamic> modelsList = data['models'];
    setState(() {
      models = modelsList.cast<String>();
    });
  }

  void _launchAR({String? model}) {
    selectedModel = model; // update global variable if selected
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (_) => StartScreen(modelName: model)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Select Model")),
      body: models.isEmpty
          ? const Center(child: CircularProgressIndicator())
          : Column(
        children: [
          Expanded(
            child: ListView.builder(
              itemCount: models.length,
              itemBuilder: (context, index) {
                final model = models[index];
                return ListTile(
                  title: Text(model),
                  trailing: selectedModel == model
                      ? const Icon(Icons.check, color: Colors.green)
                      : null,
                  onTap: () => _launchAR(model: model),
                );
              },
            ),
          ),
          // Optional button to launch AR without a model
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: ElevatedButton(
              onPressed: () => _launchAR(model: null),
              child: const Text("Launch AR without model"),
            ),
          ),
        ],
      ),
    );
  }
}

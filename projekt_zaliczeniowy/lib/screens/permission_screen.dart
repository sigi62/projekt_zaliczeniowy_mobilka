import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'dart:io' show Platform;
import 'package:flutter/services.dart';
import 'start_screen.dart';

class PermissionScreen extends StatefulWidget {
  const PermissionScreen({super.key});

  @override
  State<PermissionScreen> createState() => _PermissionScreenState();
}
class _PermissionScreenState extends State<PermissionScreen> {
  bool _cameraGranted = false;

  @override
  void initState() {
    super.initState();
    _checkPermissionOnStart();
  }

  Future<void> _checkPermissionOnStart() async {
    final status = await Permission.camera.status;
    if (status.isGranted) {
      setState(() {
        _cameraGranted = true;
      });
      // Auto-navigate if you want
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (_) => const StartScreen()),
        );
      }
    }
  }

  Future<void> _askPermissions() async {
    final cameraStatus = await Permission.camera.request();

    setState(() {
      _cameraGranted = cameraStatus.isGranted;
    });

    if (_cameraGranted) {
      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => const StartScreen()),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Camera permission is required")),
      );
    }
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.videocam, size: 100, color: _cameraGranted ? Colors.green : Colors.red),
            const SizedBox(height: 16),
            Text(
              _cameraGranted ? "Camera Granted" : "Camera Needed",
              style: const TextStyle(color: Colors.white),
            ),
            const SizedBox(height: 40),
            ElevatedButton(
              onPressed: _askPermissions,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
              ),
              child: const Text("Grant Permissions"),
            )
          ],
        ),
      ),
    );
  }
}

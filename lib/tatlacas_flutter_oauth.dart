
import 'dart:async';

import 'package:flutter/services.dart';

class TatlacasFlutterOauth {
  static const MethodChannel _channel =
      const MethodChannel('tatlacas_flutter_oauth');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

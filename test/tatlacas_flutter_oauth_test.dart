import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:tatlacas_flutter_oauth/tatlacas_flutter_oauth.dart';

void main() {
  const MethodChannel channel = MethodChannel('tatlacas_flutter_oauth');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await TatlacasFlutterOauth.platformVersion, '42');
  });
}

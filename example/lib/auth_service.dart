import 'dart:async';

import 'package:tatlacas_flutter_oauth/app_auth_export.dart';
import 'package:tatlacas_flutter_oauth/authorization_token_request.dart';
import 'package:tatlacas_flutter_oauth/authorization_token_response.dart';
import 'package:tatlacas_flutter_oauth/flutter_appauth.dart';

class AuthService {
  final _accessTokenValidStreamController = StreamController<bool>();

  Stream<bool> get accessTokenValidStream =>
      _accessTokenValidStreamController.stream;
  static const String AccessTokenKey = "ndaza-manager-access-token";
  static String? _accessToken;

  static String? get accessToken => _accessToken;

  Future logout() async {}
  static const String clientId = '639679b5-5e0a-4198-a444-ee28ddea01f6';
  static const String redirectUrl =
      'com.tatlacas.app.droid.ndaza.manager://oauthredirect';
  static const String issuer =
      'https://login.microsoftonline.com/b6e01ed4-0b7a-47a6-965e-817997ec2436/v2.0';
  static const List<String> scopes = [
    'openid',
    'email',
  ];

  Future<String?> authenticate() async {
    FlutterAppAuth appAuth = FlutterAppAuth();

    final AuthorizationTokenResponse? result =
        await appAuth.authorizeAndExchangeCode(
      AuthorizationTokenRequest(
        clientId: clientId,
        redirectUrl: redirectUrl,
        allowInsecureConnections: true,
        promptValues: ['login'],
        issuer: issuer,
        scopes: scopes,
      ),
    );
    return result?.accessToken;
  }

  Future confirmAuth() async {}

  @override
  void dispose() {
    _accessTokenValidStreamController.close();
  }
}

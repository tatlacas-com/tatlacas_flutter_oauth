import 'dart:async';
import 'dart:io';

import 'package:tatlacas_flutter_oauth/app_auth_export.dart';
import 'package:tatlacas_flutter_oauth/authorization_token_request.dart';
import 'package:tatlacas_flutter_oauth/authorization_token_response.dart';
import 'package:tatlacas_flutter_oauth/flutter_appauth.dart';


class AuthService  {
  final _accessTokenValidStreamController = StreamController<bool>();

  Stream<bool> get accessTokenValidStream =>
      _accessTokenValidStreamController.stream;
  static const String AccessTokenKey = "ndaza-manager-access-token";
  static String _accessToken;

  static String get accessToken => _accessToken;


  Future logout() async {
  }
  static const String clientId = 'tatlacas-stay-manager';
  static const String redirectUrl = 'com.tatlacas.app.ios.stay.manager:/oauthredirect';
  static const String issuer = 'https://192.168.100.18:5002';
  static const List<String> scopes = [
    'openid',
    'profile',
    'offline_access',
    'manager-api',
    'user_roles'
  ];

  Future<String> authenticate() async {
    FlutterAppAuth appAuth = FlutterAppAuth();

    final AuthorizationTokenResponse result =
        await appAuth.authorizeAndExchangeCode(
      AuthorizationTokenRequest(
       clientId,
        redirectUrl,
        allowInsecureConnections: true,
        promptValues: ['login'],
        issuer: issuer,
        scopes: scopes,
      ),
    );
    return result?.accessToken;
  }

  Future confirmAuth() async {

  }

  @override
  void dispose() {
    _accessTokenValidStreamController.close();
  }
}

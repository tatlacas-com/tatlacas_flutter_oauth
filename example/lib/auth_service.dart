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
  static const String clientId = 'e7fa4c20-33b1-4da6-a5c8-8b638c834079';
  static const String redirectUrl = 'com.tatlacas.bullraise://oauthredirect';
  static const String discoveryUrl =
      'https://bullraise.b2clogin.com/bullraise.onmicrosoft.com/v2.0/.well-known/openid-configuration?p=B2C_1A_SIGNUP_SIGNIN';
  static const List<String> scopes = ['openid', 'email', 'profile'];

  Future<String?> authenticate() async {
    FlutterAppAuth appAuth = FlutterAppAuth();

    final AuthorizationTokenResponse? result =
        await appAuth.authorizeAndExchangeCode(
      AuthorizationTokenRequest(
        clientId: 'e7fa4c20-33b1-4da6-a5c8-8b638c834079',
        //todo save this somewhere, not here
        redirectUrl: 'com.tatlacas.bullraise://oauthredirect',
        promptValues: ['login'],
        serviceConfiguration: AuthorizationServiceConfiguration(
            "https://bullraise.b2clogin.com/bullraise.onmicrosoft.com/oauth2/v2.0/authorize",
            "https://bullraise.b2clogin.com/bullraise.onmicrosoft.com/oauth2/v2.0/token?p=b2c_1a_signup_signin"),
        additionalParameters: {"p": "b2c_1a_signup_signin"},
        // discoveryUrl:
        //     'https://bullraise.b2clogin.com/bullraise.onmicrosoft.com/v2.0/.well-known/openid-configuration?p=B2C_1A_SIGNUP_SIGNIN',
        scopes: ['openid', 'email', 'profile'],
      ),
      /* AuthorizationTokenRequest(
        clientId: clientId,
        redirectUrl: redirectUrl,
        allowInsecureConnections: true,
        promptValues: ['login'],
        discoveryUrl: discoveryUrl,
        scopes: scopes,
      ),*/
    );
    return result?.accessToken;
  }

  Future confirmAuth() async {}

  @override
  void dispose() {
    _accessTokenValidStreamController.close();
  }
}

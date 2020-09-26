import 'package:tatlacas_flutter_oauth/authorization_request.dart';
import 'package:tatlacas_flutter_oauth/authorization_response.dart';
import 'package:tatlacas_flutter_oauth/authorization_token_request.dart';
import 'package:tatlacas_flutter_oauth/authorization_token_response.dart';
import 'package:tatlacas_flutter_oauth/flutter_appauth_platform.dart';
import 'package:tatlacas_flutter_oauth/token_request.dart';
import 'package:tatlacas_flutter_oauth/token_response.dart';

class FlutterAppAuth {
  /// Convenience method for authorizing and then exchanges code
  Future<AuthorizationTokenResponse> authorizeAndExchangeCode(
      AuthorizationTokenRequest request) {
    return FlutterAppAuthPlatform.instance.authorizeAndExchangeCode(request);
  }

  /// Sends an authorization request
  Future<AuthorizationResponse> authorize(AuthorizationRequest request) {
    return FlutterAppAuthPlatform.instance.authorize(request);
  }

  /// For exchanging tokens
  Future<TokenResponse> token(TokenRequest request) {
    return FlutterAppAuthPlatform.instance.token(request);
  }
}

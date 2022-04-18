import 'authorization_parameters.dart';
import 'authorization_service_configuration.dart';
import 'grant_types.dart';
import 'token_request.dart';

/// Details required for a combined authorization and code exchange request
class AuthorizationTokenRequest extends TokenRequest
    with AuthorizationParameters {
  AuthorizationTokenRequest({
    required String clientId,
    required String redirectUrl,
    String? loginHint,
    String? clientSecret,
    List<String>? scopes,
    AuthorizationServiceConfiguration? serviceConfiguration,
    Map<String, String>? additionalParameters,
    String? issuer,
    String? refreshToken,
    String? codeVerifier,
    String? authorizationCode,
    String? discoveryUrl,
    List<String>? promptValues,
    String grantType = GrantType.authorizationCode,
    bool allowInsecureConnections = false,
    bool preferEphemeralSession = false,
  }) : super(
          clientId: clientId,
          redirectUrl: redirectUrl,
          clientSecret: clientSecret,
          refreshToken: refreshToken,
          codeVerifier: codeVerifier,
          authorizationCode: authorizationCode,
          discoveryUrl: discoveryUrl,
          issuer: issuer,
          scopes: scopes,
          grantType: grantType,
          serviceConfiguration: serviceConfiguration,
          additionalParameters: additionalParameters,
          allowInsecureConnections: allowInsecureConnections,
        ) {
    this.loginHint = loginHint;
    this.promptValues = promptValues;
    this.preferEphemeralSession = preferEphemeralSession;
  }

  AuthorizationTokenRequest copyWith({
    String? clientId,
    String? redirectUrl,
    String? loginHint,
    String? clientSecret,
    String? codeVerifier,
    List<String>? scopes,
    AuthorizationServiceConfiguration? serviceConfiguration,
    Map<String, String>? additionalParameters,
    String? issuer,
    String? discoveryUrl,
    String? authorizationCode,
    String? refreshToken,
    List<String>? promptValues,
    String? grantType,
    bool? allowInsecureConnections,
    bool? preferEphemeralSession,
  }) {
    return AuthorizationTokenRequest(
      clientId: clientId ?? this.clientId!,
      redirectUrl: redirectUrl ?? this.redirectUrl!,
      loginHint: loginHint ?? this.loginHint,
      clientSecret: clientSecret ?? this.clientSecret,
      codeVerifier: codeVerifier ?? this.codeVerifier,
      scopes: scopes ?? this.scopes,
      serviceConfiguration: serviceConfiguration ?? this.serviceConfiguration,
      additionalParameters: additionalParameters ?? this.additionalParameters,
      issuer: issuer ?? this.issuer,
      discoveryUrl: discoveryUrl ?? this.discoveryUrl,
      authorizationCode: authorizationCode ?? this.authorizationCode,
      refreshToken: refreshToken ?? this.refreshToken,
      promptValues: promptValues ?? this.promptValues,
      grantType: grantType ?? this.grantType!,
      allowInsecureConnections:
          allowInsecureConnections ?? this.allowInsecureConnections!,
      preferEphemeralSession:
          preferEphemeralSession ?? this.preferEphemeralSession!,
    );
  }
}

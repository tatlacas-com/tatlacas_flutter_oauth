package com.tatlacas.plugins.tatlacas.flutter.tatlacas_flutter_oauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.PluginRegistry.Registrar
import net.openid.appauth.*
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import net.openid.appauth.AuthorizationServiceConfiguration.*
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import java.util.*

/** TatlacasFlutterOauthPlugin */
public class TatlacasFlutterOauthPlugin: FlutterPlugin, MethodCallHandler,ActivityResultListener, ActivityAware {
  private val AUTHORIZE_AND_EXCHANGE_CODE_METHOD = "authorizeAndExchangeCode"
  private val AUTHORIZE_METHOD = "authorize"
  private val TOKEN_METHOD = "token"

  private val DISCOVERY_ERROR_CODE = "discovery_failed"
  private val AUTHORIZE_AND_EXCHANGE_CODE_ERROR_CODE = "authorize_and_exchange_code_failed"
  private val AUTHORIZE_ERROR_CODE = "authorize_failed"
  private val TOKEN_ERROR_CODE = "token_failed"

  private val DISCOVERY_ERROR_MESSAGE_FORMAT = "Error retrieving discovery document: [error: %s, description: %s]"
  private val TOKEN_ERROR_MESSAGE_FORMAT = "Failed to get token: [error: %s, description: %s]"
  private val AUTHORIZE_ERROR_MESSAGE_FORMAT = "Failed to authorize: [error: %s, description: %s]"

  private val RC_AUTH_EXCHANGE_CODE = 65030
  private val RC_AUTH = 65031
  private var applicationContext: Context? = null
  private var mainActivity: Activity? = null
  private var pendingOperation: PendingOperation? = null
  private var clientSecret: String? = null
  private var allowInsecureConnections = false
  private var defaultAuthorizationService: AuthorizationService? = null
  private var insecureAuthorizationService: AuthorizationService? = null
  private val  authorizationService: AuthorizationService  get() = if (allowInsecureConnections) insecureAuthorizationService!! else defaultAuthorizationService!!
  private val  connectionBuilder: ConnectionBuilder  get() = if (allowInsecureConnections) ConnectionBuilderForTesting.INSTANCE else DefaultConnectionBuilder.INSTANCE

  /**
   * Plugin registration.
   */
  fun registerWith(registrar: Registrar) {
    val plugin = TatlacasFlutterOauthPlugin()
    plugin.setActivity(registrar.activity())
    plugin.onAttachedToEngine(registrar.context(), registrar.messenger())
    registrar.addActivityResultListener(plugin)
    registrar.addViewDestroyListener {
      plugin.disposeAuthorizationServices()
      false
    }
  }


  private fun setActivity(flutterActivity: Activity) {
    mainActivity = flutterActivity
  }

  private fun onAttachedToEngine(context: Context, binaryMessenger: BinaryMessenger) {
    applicationContext = context
    defaultAuthorizationService = AuthorizationService(applicationContext!!)
    val authConfigBuilder = AppAuthConfiguration.Builder()
    authConfigBuilder.setConnectionBuilder(ConnectionBuilderForTesting.INSTANCE)
    insecureAuthorizationService = AuthorizationService(applicationContext!!, authConfigBuilder.build())
    val channel = MethodChannel(binaryMessenger, "tatlacas.lib/tatlacas_flutter_oauth")
    channel.setMethodCallHandler(this)
  }

  override fun onAttachedToEngine(binding: FlutterPluginBinding) {
    onAttachedToEngine(binding.applicationContext, binding.binaryMessenger)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    disposeAuthorizationServices()
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    binding.addActivityResultListener(this)
    mainActivity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    mainActivity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    binding.addActivityResultListener(this)
    mainActivity = binding.activity
  }

  override fun onDetachedFromActivity() {
    mainActivity = null
  }

  private fun disposeAuthorizationServices() {
    defaultAuthorizationService!!.dispose()
    insecureAuthorizationService!!.dispose()
    defaultAuthorizationService = null
    insecureAuthorizationService = null
  }

  private fun checkAndSetPendingOperation(method: String, result: Result) {
    check(pendingOperation == null) { "Concurrent operations detected: " + pendingOperation?.method + ", " + method }
    pendingOperation = PendingOperation(method, result)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    val arguments = call.arguments<Map<String, Any?>>()
    when (call.method) {
      AUTHORIZE_AND_EXCHANGE_CODE_METHOD -> try {
        checkAndSetPendingOperation(call.method, result)
        handleAuthorizeMethodCall(arguments, true)
      } catch (ex: Exception) {
        finishWithError(AUTHORIZE_AND_EXCHANGE_CODE_ERROR_CODE, ex.localizedMessage)
      }
      AUTHORIZE_METHOD -> try {
        checkAndSetPendingOperation(call.method, result)
        handleAuthorizeMethodCall(arguments, false)
      } catch (ex: Exception) {
        finishWithError(AUTHORIZE_ERROR_CODE, ex.localizedMessage)
      }
      TOKEN_METHOD -> try {
        checkAndSetPendingOperation(call.method, result)
        handleTokenMethodCall(arguments)
      } catch (ex: Exception) {
        finishWithError(TOKEN_ERROR_CODE, ex.localizedMessage)
      }
      else -> result.notImplemented()
    }
  }

  private fun processAuthorizationTokenRequestArguments(arguments: Map<String, Any?>): AuthorizationTokenRequestParameters {
    val clientId = arguments["clientId"] as String?
    val issuer = arguments["issuer"] as String?
    val discoveryUrl = arguments["discoveryUrl"] as String?
    val redirectUrl = arguments["redirectUrl"] as String?
    val loginHint = arguments["loginHint"] as String?
    clientSecret = arguments["clientSecret"] as String?
    val scopes = arguments["scopes"] as ArrayList<String>?
    val promptValues = arguments["promptValues"] as ArrayList<String>?
    val serviceConfigurationParameters = arguments["serviceConfiguration"] as Map<String, String>?
    val additionalParameters = arguments["additionalParameters"] as Map<String, String>?
    allowInsecureConnections = arguments["allowInsecureConnections"] as Boolean
    return AuthorizationTokenRequestParameters(clientId, issuer, discoveryUrl, scopes, redirectUrl, serviceConfigurationParameters, additionalParameters, loginHint, promptValues)
  }

  private fun processTokenRequestArguments(arguments: Map<String, Any?>): TokenRequestParameters {
    val clientId = arguments["clientId"] as String?
    val issuer = arguments["issuer"] as String?
    val discoveryUrl = arguments["discoveryUrl"] as String?
    val redirectUrl = arguments["redirectUrl"] as String?
    val grantType = arguments["grantType"] as String?
    clientSecret = arguments["clientSecret"] as String?
    var refreshToken: String? = null
    if (arguments.containsKey("refreshToken")) {
      refreshToken = arguments["refreshToken"] as String?
    }
    var authorizationCode: String? = null
    if (arguments.containsKey("authorizationCode")) {
      authorizationCode = arguments["authorizationCode"] as String?
    }
    var codeVerifier: String? = null
    if (arguments.containsKey("codeVerifier")) {
      codeVerifier = arguments["codeVerifier"] as String?
    }
    val scopes = arguments["scopes"] as ArrayList<String>?
    val serviceConfigurationParameters = arguments["serviceConfiguration"] as Map<String, String>?
    val additionalParameters = arguments["additionalParameters"] as Map<String, String>?
    allowInsecureConnections = arguments["allowInsecureConnections"] as Boolean
    return TokenRequestParameters(clientId, issuer, discoveryUrl, scopes, redirectUrl, refreshToken, authorizationCode, codeVerifier, grantType, serviceConfigurationParameters, additionalParameters)
  }

  private fun handleAuthorizeMethodCall(arguments: Map<String, Any?>, exchangeCode: Boolean) {
    val tokenRequestParameters = processAuthorizationTokenRequestArguments(arguments)
    if (tokenRequestParameters.serviceConfigurationParameters != null) {
      val serviceConfiguration = requestParametersToServiceConfiguration(tokenRequestParameters)
      tokenRequestParameters.clientId?.let { tokenRequestParameters.redirectUrl?.let { it1 -> performAuthorization(serviceConfiguration, it, it1, tokenRequestParameters.scopes, tokenRequestParameters.loginHint, tokenRequestParameters.additionalParameters, exchangeCode, tokenRequestParameters.promptValues) } }
    } else {
      val callback = RetrieveConfigurationCallback { serviceConfiguration, ex ->
        if (ex == null) {
          tokenRequestParameters.clientId?.let { tokenRequestParameters.redirectUrl?.let { it1 -> performAuthorization(serviceConfiguration, it, it1, tokenRequestParameters.scopes, tokenRequestParameters.loginHint, tokenRequestParameters.additionalParameters, exchangeCode, tokenRequestParameters.promptValues) } }
        } else {
          finishWithDiscoveryError(ex)
        }
      }
      if (tokenRequestParameters.discoveryUrl != null) {
        fetchFromUrl(Uri.parse(tokenRequestParameters.discoveryUrl), callback, connectionBuilder)
      } else {
        fetchFromUrl(Uri.parse(tokenRequestParameters.issuer).buildUpon().appendPath(WELL_KNOWN_PATH)
                .appendPath(OPENID_CONFIGURATION_RESOURCE)
                .build(), callback, connectionBuilder)
      }
    }
  }

  private fun handleSignOut(){

  }

  private fun requestParametersToServiceConfiguration(tokenRequestParameters: TokenRequestParameters): AuthorizationServiceConfiguration {
    return AuthorizationServiceConfiguration(Uri.parse(tokenRequestParameters.serviceConfigurationParameters!!["authorizationEndpoint"]), Uri.parse(tokenRequestParameters.serviceConfigurationParameters["tokenEndpoint"]))
  }

  private fun handleTokenMethodCall(arguments: Map<String, Any?>) {
    val tokenRequestParameters = processTokenRequestArguments(arguments)
    if (tokenRequestParameters.serviceConfigurationParameters != null) {
      val serviceConfiguration = requestParametersToServiceConfiguration(tokenRequestParameters)
      performTokenRequest(serviceConfiguration, tokenRequestParameters)
    } else {
      if (tokenRequestParameters.discoveryUrl != null) {
        val callback = RetrieveConfigurationCallback{ serviceConfiguration, ex ->
          if (ex == null) {
            performTokenRequest(serviceConfiguration, tokenRequestParameters)
          } else {
            finishWithDiscoveryError(ex)
          }
        }
        fetchFromUrl(Uri.parse(tokenRequestParameters.discoveryUrl),callback ,connectionBuilder)
      } else {
        val callback = RetrieveConfigurationCallback{ serviceConfiguration, ex ->
          if (ex == null) {
            performTokenRequest(serviceConfiguration, tokenRequestParameters)
          } else {
            finishWithDiscoveryError(ex)
          }
        }
        fetchFromUrl(Uri.parse(tokenRequestParameters.issuer).buildUpon().appendPath(WELL_KNOWN_PATH)
                .appendPath(OPENID_CONFIGURATION_RESOURCE)
                .build(), callback, connectionBuilder)
      }
    }
  }


  private fun performAuthorization(serviceConfiguration: AuthorizationServiceConfiguration?, clientId: String, redirectUrl: String, scopes: ArrayList<String>?, loginHint: String?, additionalParameters: Map<String, String>?, exchangeCode: Boolean, promptValues: ArrayList<String>?) {
    val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfiguration!!,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse(redirectUrl))
    if (scopes != null && !scopes.isEmpty()) {
      authRequestBuilder.setScopes(scopes)
    }
    if (loginHint != null) {
      authRequestBuilder.setLoginHint(loginHint)
    }
    if (promptValues != null && !promptValues.isEmpty()) {
      authRequestBuilder.setPromptValues(promptValues)
    }
    if (additionalParameters != null && !additionalParameters.isEmpty()) {
      authRequestBuilder.setAdditionalParameters(additionalParameters)
    }
    val authIntent = authorizationService.getAuthorizationRequestIntent(authRequestBuilder.build())
    mainActivity!!.startActivityForResult(authIntent, if (exchangeCode) RC_AUTH_EXCHANGE_CODE else RC_AUTH)
  }

  private fun performTokenRequest(serviceConfiguration: AuthorizationServiceConfiguration?, tokenRequestParameters: TokenRequestParameters) {
    val builder = TokenRequest.Builder(serviceConfiguration!!, tokenRequestParameters.clientId!!)
            .setRefreshToken(tokenRequestParameters.refreshToken)
            .setAuthorizationCode(tokenRequestParameters.authorizationCode)
            .setCodeVerifier(tokenRequestParameters.codeVerifier)
            .setRedirectUri(Uri.parse(tokenRequestParameters.redirectUrl))
    if (tokenRequestParameters.grantType != null) {
      builder.setGrantType(tokenRequestParameters.grantType)
    }
    if (tokenRequestParameters.scopes != null) {
      builder.setScopes(tokenRequestParameters.scopes)
    }
    if (tokenRequestParameters.additionalParameters != null && !tokenRequestParameters.additionalParameters.isEmpty()) {
      builder.setAdditionalParameters(tokenRequestParameters.additionalParameters)
    }
    val tokenResponseCallback = TokenResponseCallback { resp, ex ->
      if (resp != null) {
        val responseMap = tokenResponseToMap(resp, null)
        finishWithSuccess(responseMap)
      } else {
        finishWithTokenError(ex)
      }
    }
    val tokenRequest = builder.build()
    if (clientSecret == null) {
      authorizationService.performTokenRequest(tokenRequest, tokenResponseCallback)
    } else {
      authorizationService.performTokenRequest(tokenRequest, ClientSecretBasic(clientSecret!!), tokenResponseCallback)
    }
  }

  private fun finishWithTokenError(ex: AuthorizationException?) {
    finishWithError(TOKEN_ERROR_CODE, String.format(TOKEN_ERROR_MESSAGE_FORMAT, ex!!.error, ex.errorDescription))
  }


  private fun finishWithSuccess(data: Any) {
    if (pendingOperation != null) {
      pendingOperation!!.result.success(data)
      pendingOperation = null
    }
  }

  private fun finishWithError(errorCode: String, errorMessage: String) {
    if (pendingOperation != null) {
      pendingOperation!!.result.error(errorCode, errorMessage, null)
      pendingOperation = null
    }
  }

  private fun finishWithDiscoveryError(ex: AuthorizationException) {
    finishWithError(DISCOVERY_ERROR_CODE, String.format(DISCOVERY_ERROR_MESSAGE_FORMAT, ex.error, ex.errorDescription))
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
    if (pendingOperation == null) {
      return false
    }
    if (requestCode == RC_AUTH_EXCHANGE_CODE || requestCode == RC_AUTH) {
      val authResponse = AuthorizationResponse.fromIntent(intent!!)
      val ex = AuthorizationException.fromIntent(intent)
      processAuthorizationData(authResponse, ex, requestCode == RC_AUTH_EXCHANGE_CODE)
      return true
    }
    return false
  }

  private fun processAuthorizationData(authResponse: AuthorizationResponse?, authException: AuthorizationException?, exchangeCode: Boolean) {
    if (authException == null) {
      if (exchangeCode) {
        val authConfigBuilder = AppAuthConfiguration.Builder()
        if (allowInsecureConnections) {
          authConfigBuilder.setConnectionBuilder(ConnectionBuilderForTesting.INSTANCE)
        }
        val authService = AuthorizationService(applicationContext!!, authConfigBuilder.build())
        val tokenResponseCallback = TokenResponseCallback { resp, ex ->
          if (resp != null) {
            finishWithSuccess(tokenResponseToMap(resp, authResponse))
          } else {
            finishWithError(AUTHORIZE_AND_EXCHANGE_CODE_ERROR_CODE, String.format(AUTHORIZE_ERROR_MESSAGE_FORMAT, ex!!.error, ex.errorDescription))
          }
        }
        if (clientSecret == null) {
          authService.performTokenRequest(authResponse!!.createTokenExchangeRequest(), tokenResponseCallback)
        } else {
          authService.performTokenRequest(authResponse!!.createTokenExchangeRequest(), ClientSecretBasic(clientSecret!!), tokenResponseCallback)
        }
      } else {
        finishWithSuccess(authorizationResponseToMap(authResponse))
      }
    } else {
      finishWithError(if (exchangeCode) AUTHORIZE_AND_EXCHANGE_CODE_ERROR_CODE else AUTHORIZE_ERROR_CODE, String.format(AUTHORIZE_ERROR_MESSAGE_FORMAT, authException.error, authException.errorDescription))
    }
  }

  private fun tokenResponseToMap(tokenResponse: TokenResponse, authResponse: AuthorizationResponse?): Map<String, Any?> {
    val responseMap: MutableMap<String, Any?> = HashMap()
    responseMap["accessToken"] = tokenResponse.accessToken
    responseMap["accessTokenExpirationTime"] = if (tokenResponse.accessTokenExpirationTime != null) tokenResponse.accessTokenExpirationTime!!.toDouble() else null
    responseMap["refreshToken"] = tokenResponse.refreshToken
    responseMap["idToken"] = tokenResponse.idToken
    responseMap["tokenType"] = tokenResponse.tokenType
    if (authResponse != null) {
      responseMap["authorizationAdditionalParameters"] = authResponse.additionalParameters
    }
    responseMap["tokenAdditionalParameters"] = tokenResponse.additionalParameters
    return responseMap
  }

  private fun authorizationResponseToMap(authResponse: AuthorizationResponse?): Map<String, Any?> {
    val responseMap: MutableMap<String, Any?> = HashMap()
    responseMap["codeVerifier"] = authResponse!!.request.codeVerifier
    responseMap["authorizationCode"] = authResponse.authorizationCode
    responseMap["authorizationAdditionalParameters"] = authResponse.additionalParameters
    return responseMap
  }

  internal class PendingOperation(val method: String, val result: Result)


  private open class TokenRequestParameters(val clientId: String?, val issuer: String?, val discoveryUrl: String?, val scopes: ArrayList<String>?, val redirectUrl: String?, val refreshToken: String?, val authorizationCode: String?, val codeVerifier: String?, val grantType: String?, val serviceConfigurationParameters: Map<String, String>?, val additionalParameters: Map<String, String>?)

  private class AuthorizationTokenRequestParameters(clientId: String?, issuer: String?, discoveryUrl: String?, scopes: ArrayList<String>?, redirectUrl: String?, serviceConfigurationParameters: Map<String, String>?, additionalParameters: Map<String, String>?, val loginHint: String?, val promptValues: ArrayList<String>?) : TokenRequestParameters(clientId, issuer, discoveryUrl, scopes, redirectUrl, null, null, null, null, serviceConfigurationParameters, additionalParameters)
}

import Flutter
import UIKit

public class SwiftTatlacasFlutterOauthPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "tatlacas_flutter_oauth", binaryMessenger: registrar.messenger())
    let instance = SwiftTatlacasFlutterOauthPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}

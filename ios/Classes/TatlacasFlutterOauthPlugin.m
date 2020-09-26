#import "TatlacasFlutterOauthPlugin.h"
#if __has_include(<tatlacas_flutter_oauth/tatlacas_flutter_oauth-Swift.h>)
#import <tatlacas_flutter_oauth/tatlacas_flutter_oauth-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tatlacas_flutter_oauth-Swift.h"
#endif

@implementation TatlacasFlutterOauthPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTatlacasFlutterOauthPlugin registerWithRegistrar:registrar];
}
@end

#import <Flutter/Flutter.h>
#import <AppAuth/AppAuth.h>

@interface TatlacasFlutterOauthPlugin : NSObject<FlutterPlugin>

@property(nonatomic, strong, nullable) id<OIDExternalUserAgentSession> currentAuthorizationFlow;

@end

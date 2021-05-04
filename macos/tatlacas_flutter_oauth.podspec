#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint tatlacas_flutter_oauth.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'tatlacas_flutter_oauth'
  s.version          = '0.0.1'
  s.summary          = 'TatlaCas Flutter OAuth2'
  s.description      = <<-DESC
TatlaCas Flutter OAuth2
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files     = 'Classes/**/*'
  s.dependency 'FlutterMacOS'
  s.dependency 'AppAuth', '1.3.1'

  s.platform = :osx, '10.11'
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }
  s.swift_version = '5.0'
end

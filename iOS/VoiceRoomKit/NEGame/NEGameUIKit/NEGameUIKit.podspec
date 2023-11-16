Pod::Spec.new do |s|
  s.name = 'NEGameUIKit'
  s.version = '1.0.0'
  s.summary = 'A short description of NEGameUIKit.'
  s.homepage = 'http://netease.im'
  s.license = {"type"=> "Copyright", "text"=> " Copyright 2022 Netease "}
  s.author = 'yunxin engineering department'
  s.platforms = {"ios"=> "11.0"}
  s.swift_versions = '5.0'
  s.source = {"git"=> "https://github.com/netease-kit/"}
  s.source_files = 'NEGameUIKit/Classes/**/*'
  s.resources = 'NEGameUIKit/Assets/**/*'
  s.dependency 'NEGameKit'
  s.dependency 'NEVoiceRoomBaseUIKit'
  s.dependency 'SnapKit'
  s.dependency 'NEVoiceRoomKit'
  s.dependency 'NEUIKit'
  s.dependency 'SudMGPWrapper'
  s.pod_target_xcconfig = {"EXCLUDED_ARCHS[sdk=iphonesimulator*]"=> "arm64", "BUILD_LIBRARY_FOR_DISTRIBUTION"=> "YES", "APPLICATION_EXTENSION_API_ONLY"=> "NO"}
  s.swift_version = '5.0'
end

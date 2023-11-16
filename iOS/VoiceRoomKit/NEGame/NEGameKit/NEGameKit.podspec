Pod::Spec.new do |s|
  s.name = 'NEGameKit'
  s.version = '1.0.0'
  s.summary = 'A short description of NEGameKit.'
  s.homepage = 'http://netease.im'
  s.license = {"type"=> "Copyright", "text"=> " Copyright 2022 Netease "}
  s.author = 'yunxin engineering department'
  s.platforms = {"ios"=> "11.0"}
  s.swift_versions = '5.0'
  s.source = {"git"=> "https://github.com/netease-kit/"}
  s.source_files = 'NEGameKit/Classes/**/*'
  s.dependency 'NERoomKit/Base_Special'
  s.dependency 'SudMGPWrapper', '1.3.5'
  s.pod_target_xcconfig = {"EXCLUDED_ARCHS[sdk=iphonesimulator*]"=> "arm64", "BUILD_LIBRARY_FOR_DISTRIBUTION"=> "YES", "APPLICATION_EXTENSION_API_ONLY"=> "NO"}
  s.swift_version = '5.0'
end

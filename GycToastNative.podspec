Pod::Spec.new do |spec|
  spec.name = 'GycToastNative'
  spec.version = '0.1.4'
  spec.summary = 'Native iOS toast presenter shared by CMP and Kuikly hosts.'
  spec.homepage = 'https://github.com/gycrosskit/toast'
  spec.license = { :type => 'Apache-2.0', :file => 'LICENSE' }
  spec.author = { 'gycrosskit' => 'guoyanggit@gmail.com' }
  spec.source = { :git => 'https://github.com/gycrosskit/toast.git', :tag => spec.version.to_s }
  spec.source_files = 'iosApp/Sources/GycToastNative/**/*.swift'
  spec.ios.deployment_target = '15.0'
  spec.swift_version = '5.9'
end

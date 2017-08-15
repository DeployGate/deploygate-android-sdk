# DeployGate SDK for Android

[![Build Status](https://travis-ci.org/DeployGate/deploygate-android-sdk.svg?branch=master)](https://travis-ci.org/DeployGate/deploygate-android-sdk)
[ ![Download](https://api.bintray.com/packages/deploygate/maven/com.deploygate%3Asdk/images/download.svg) ](https://bintray.com/deploygate/maven/com.deploygate%3Asdk/_latestVersion)

You can integrate DeployGate's realtime remote logging & crash reporting without code modification on your apps in development.

## Install

In your build.gradle of `app` module:

```gradle
repositories {
    mavenCentral()
}

dependencies {
    // use `compile` instead of `implementation` for Android Build Tools < 3.0
    implementation 'com.deploygate:sdk:4.0.0'
}
```

Then synchronize, build and upload your app to DeployGate. With Gradle DeployGate Plugin, it can be done by running `uploadDeployGateDebug` task.

Since 4.0.0, you don't need to add `DeployGate.install(this)` in your `Application#onCreate` except you have multiple processes. It is automatically called when your application process starts.

## Usage

You can retrieve detailed information on current running build and status change events via functions and callback listeners.

For example, you can prevent your application running on unauthorized devices by putting the following code in your main Activity's `Activity#onCreate`.

```java
DeployGate.registerCallback(new DeployGateCallback() {
    @Override
    public void onInitialized(boolean isServiceAvailable) {
        if (!isServiceAvailable) {
            Toast.makeText(this, "DeployGate is not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
        if (!isAuthorized) {
            Toast.makeText(this, "This device is not authorized to use this app", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onUpdateAvailable(int revision, String versionName, int versionCode) {}
}, true);
```


See [SDK Sample](https://github.com/deploygate/deploygate-android-sdk-sample) for more examples and [Javadoc](https://deploygate.com/javadoc) for all available methods.

## Mock

You may want to remove DeployGate SDK and related code in production build
to reduce your app's footprint.

For your convenience, we provide "Mock" SDK that replaces every function call
to empty implementation so you don't have to modify your code to switch the builds.

To use it, simply replace the dependency from `sdk` to `sdk-mock`.
You can use it with a conjunction of `productFlavors` and `buildConfig` of Gradle
like the following example:

```gradle
dependencies {
    // use full implementation for debug builds
    debugImplementation 'com.deploygate:sdk:4.0.0'

    // use mocked implementation for release builds
    releaseImplementation 'com.deploygate:sdk-mock:4.0.0'
}
```


## Links

 * [SDK Document](https://deploygate.com/docs/sdk)
 * [SDK Sample](https://github.com/deploygate/deploygate-android-sdk-sample)
 * [Reference (Javadoc)](https://deploygate.com/javadoc)
 * [Previous releases, download JAR/AARs (Bintray)](https://bintray.com/deploygate/maven/com.deploygate%3Asdk)
 * [Issues](https://github.com/deploygate/deploygate-android-sdk/issues)


## License

Copyright © 2017 DeployGate

Licensed under [the Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

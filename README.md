# DeployGate SDK for Android

[![Build Status](https://travis-ci.org/DeployGate/deploygate-android-sdk.svg?branch=master)](https://travis-ci.org/DeployGate/deploygate-android-sdk)
[![Download](https://img.shields.io/maven-central/v/com.deploygate/sdk)](https://ossindex.sonatype.org/component/pkg:maven/com.deploygate/sdk)

You can integrate DeployGate's realtime remote logging & crash reporting without code modification on your apps in development.

> 4.3.0 and later require Android Studio whose versions are the latest patch version of 3.3 or above.
> For the more details, please see https://developer.android.com/studio/releases/gradle-plugin#4.0.1

## Install

In your build.gradle of `app` module:

```gradle
repositories {
    mavenCentral()
}

dependencies {
    // Use SDK dependency for variants like debug.
    debugImplementation 'com.deploygate:sdk:<latest version>'
    
    // Use no-op implementation for variants you would like to disable DeployGate SDK.
    // see also "Mock" section below for more details
    releaseImplementation 'com.deploygate:sdk-mock:<latest version>'
}
```

Then synchronize, build and upload your app to DeployGate. 

> Since 4.0.0, you don't need to add `DeployGate.install(this)` to your `Application#onCreate` except you have multiple processes. It is automatically called when your application process starts through the ContentProvider initialization.

### For Jetpack App Startup users or those who would like to initialize SDK manually

DeployGate SDK uses `ContentProvider` to initialize itself so you need to remove the provider from your manifest file.

```AndroidManifest.xml
<application>
    <provider
        android:name="com.deploygate.sdk.DeployGateProvider"
        android:authorities="${applicationId}.deploygateprovider"
        tools:node="remove"
        />
</application>
```

And also, you need to call `DeployGate#install` in your Application class, ContentProvider or AndroidX Startup Initializer.
For example, add to your custom application class, content provider, or else.

```java
DeployGate.install(context, /** forceApplyOnReleaseBuild */ false);
```


## Usage

You can retrieve detailed information on current running build and status change events through functions and callback listeners.

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

See [SDK Sample](./sample) for more examples.

## Mock

Do you want to disable DeployGate SDK on production builds? If so, please use `sdk-mock` dependency for production builds instead of `sdk`. `sdk-mock` dependency has public interfaces that are same as of `sdk` but their implementations are empty, so you don't have to modify your app code for specific build variants.

To use it, simply replace the dependency from `sdk` to `sdk-mock`.
You can use it with a conjunction of `productFlavors` and `buildConfig` of Gradle
like the following example:

```gradle
dependencies {
    // use full implementation for debug builds
    debugImplementation 'com.deploygate:sdk:<latest version>'

    // use mocked implementation for release builds
    releaseImplementation 'com.deploygate:sdk-mock:<latest version>'
}
```

> Proper variants depends on your use-case. If you are using R8-applied applications during the QA process, you may need to use non-mock SDK even in release build type.

## Upload your app to DeployGate

[Gradle DeployGate Plugin](https://github.com/DeployGate/gradle-deploygate-plugin/) will be your help. Please note that the SDK works without the Gradle plugin and vice versa. 

## Links

 * [SDK Document](https://deploygate.com/docs/sdk)
 * [Reference (Javadoc)](https://deploygate.github.io/deploygate-android-sdk/)
   * Javadoc hosting is currently in beta.
 * [Previous releases, download JAR/AARs](https://search.maven.org/artifact/com.deploygate/sdk)
 * [Issues](https://github.com/deploygate/deploygate-android-sdk/issues)

## License

Copyright © 2017- DeployGate

Licensed under [the Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Development

- Clone this repo and open by Android Studio.
  - See the `/build.gradle` for the version of AGP.
- Import `/CodeStyle.xml`
- Modify java or required files
- Add new tests for your changes
- Make sure all tests are passed

```bash
# help: build and install artifacts into your local maven repo
./gradlew clean \
    sdk:verifyBytecodeVersionRelease sdkMock:verifyBytecodeVersionRelease \
    sdk:publishReleasePublicationToMavenLocal sdkMock:publishReleasePublicationToMavenLocal \
    --stacktrace
```

### sdk

- Consider if we should use external libraries carefully
- Allow users to opt out non-required features
- Minimize the impact of proguard rules

### sdkMock

sdkMock must be No-Op implementation.

- Do not have any permission
- In the same version, the public API of sdkMock and sdk must have no difference

## Deployment

Use GitHub Actions.

- [release a new version](.github/workflows/release.yml)
- [distribute a new sample with the latest version](.github/workflows/test.yml)
- [deploy the Javadocs](.github/workflows/deploy-javadoc.yml)
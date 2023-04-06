# Tokeninc Gradle Build Tool Plugin

<!-- TOC -->
* [Tokeninc Gradle Build Tool Plugin](#tokeninc-gradle-build-tool-plugin)
    * [What is Tokeninc Gradle Build Tool Plugin](#what-is-tokeninc-gradle-build-tool-plugin)
    * [Release Notes](#release-notes)
      * [Version 0.1 Notes](#version-01-notes)
  * [Getting Started](#getting-started)
    * [Using Build Plugin, in Project `build.gradle`](#using-build-plugin-in-project-buildgradle)
      * [Project Level `build.gradle`](#project-level-buildgradle)
      * [Java GUI for Build Plugin, Preferred on Android Studio IDE](#java-gui-for-build-plugin-preferred-on-android-studio-ide)
      * [Providing Gradle properties for Build Plugin, Preferred on CI/CD](#providing-gradle-properties-for-build-plugin-preferred-on-cicd)
    * [Using Settings Plugin, in `settings.gradle`](#using-settings-plugin-in-settingsgradle)
      * [Project Level `settings.gradle`](#project-level-settingsgradle)
      * [Java GUI for Settings Plugin, Preferred on Android Studio IDE](#java-gui-for-settings-plugin-preferred-on-android-studio-ide)
      * [Providing Gradle properties for Settings Plugin, Preferred on CI/CD](#providing-gradle-properties-for-settings-plugin-preferred-on-cicd)
    * [Using Publisher Plugin, in library `build.gradle`](#using-publisher-plugin-in-library-buildgradle)
      * [Module `build.gradle`](#module-buildgradle)
      * [Java GUI for Publisher Plugin, Preferred on Android Studio IDE](#java-gui-for-publisher-plugin-preferred-on-android-studio-ide)
      * [Providing Gradle properties for Publisher Plugin, Preferred on CI/CD](#providing-gradle-properties-for-publisher-plugin-preferred-on-cicd)
    * [Modifying consumer or publisher credentials](#modifying-consumer-or-publisher-credentials)
<!-- TOC -->

### What is Tokeninc Gradle Build Tool Plugin

This plugin allows you to configure your Android projects to consume/publish artifacts
from/to private repositories.

These credentials are either asked by the plugin in a Java GUI or
supplied to the plugin by adding properties while building with Gradle.

The provided credentials are then later saved under the $HOME/.gradle/tokeninc folder
where all other projects that use this plugin can easily read from, i.e. this procedure only has to be done once.

### Release Notes

#### Version 0.1 Notes

    Added maven consumer and publisher plugins for Android Projects

## Getting Started

### Using Build Plugin, in Project `build.gradle`

If the repository dependencies are managed in *project level* `build.gradle` file, enable the build plugin as follows:

#### Project Level `build.gradle`
```groovy
buildscript {
    // ...
}

plugins {
    id 'com.tokeninc.tools.build' version '0.1' apply true
}

// ..
```

#### Java GUI for Build Plugin, Preferred on Android Studio IDE
On first sync, you will be prompted a window to enter repository consumer credentials:

<img src="src/main/resources/ReadMeResources/consumer.png" width=50% height=50%>

Upon filling the provided fields, the plugin verifies the credentials and saves them under $HOME/.gradle/tokeninc/consume folder



#### Providing Gradle properties for Build Plugin, Preferred on CI/CD

You need to provide the following 3 properties to consume from a Maven Repository

1) consume-repo-url-1
2) consume-repo-usr-1
3) consume-repo-pwd-1

If you need to add multiple repositories, just increment the trailing number to add the next repository
e.g.: consume-repo-url-2, consume-repo-usr-2, consume-repo-pwd-2

Example

`gradle :app:assembleDebug -Pconsume-repo-url-1="https://link/to/maven/repository" -Pconsume-repo-usr-1="your_usr_1" -Pconsume-repo-pwd-1="your_pwd_1"`

### Using Settings Plugin, in `settings.gradle`

If the repository dependencies are managed in the `settings.gradle` file, enable the settings plugin as follows:

#### Project Level `settings.gradle`
```groovy
pluginManagement {
    // ..
}

plugins {
    id 'com.tokeninc.tools.settings' version '0.1' apply true
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // ..
    }
}
```

**Warning**

Due to the restrictions of *plugins* block, 
you need to remove the version information from the *plugins* block in the project level `build.gradle` if you also use the settings plugin

#### Java GUI for Settings Plugin, Preferred on Android Studio IDE
On first sync, you will be prompted a window to enter repository consumer credentials:

<img src="src/main/resources/ReadMeResources/consumer.png" width=50% height=50%>

Upon filling the provided fields, the plugin verifies the credentials and saves them under $HOME/.gradle/tokeninc/consume folder

#### Providing Gradle properties for Settings Plugin, Preferred on CI/CD

You need to provide the following 3 properties to consume from a Maven Repository

1) consume-repo-url-1
2) consume-repo-usr-1
3) consume-repo-pwd-1

If you need to add multiple repositories, just increment the trailing number to add the next repository
e.g.: consume-repo-url-2, consume-repo-usr-2, consume-repo-pwd-2

Example

`gradle :app:assembleDebug -Pconsume-repo-url-1="https://link/to/maven/repository" -Pconsume-repo-usr-1="your_usr_1" -Pconsume-repo-pwd-1="your_pwd_1"`

### Using Publisher Plugin, in library `build.gradle`

If you would like publish your Android library to a snapshot maven repository, enable the publisher plugin as follows:

#### Module `build.gradle`
```groovy
plugins {
    id 'com.android.library'
    // ...
    id 'com.tokeninc.tools.publish' version '0.1'
}
```

#### Java GUI for Publisher Plugin, Preferred on Android Studio IDE
On first sync, you will be prompted a window to enter repository publisher credentials:

<img src="src/main/resources/ReadMeResources/publisher.png" width=50% height=50%>

Upon filling the provided fields, the plugin verifies the credentials and saves them under $HOME/.gradle/tokeninc/publish folder

#### Providing Gradle properties for Publisher Plugin, Preferred on CI/CD

You need to provide the following 3 properties to publish to a Maven Repository

1) publish-repo-url-1
2) publish-repo-usr-1
3) publish-repo-pwd-1

If you need to add multiple repositories, just increment the trailing number to add the next repository
e.g.: publish-repo-url-2, publish-repo-usr-2, publish-repo-pwd-2

Example

`gradle :my-android-lib:publishWithTokenPlugin -Ppublish-repo-url-1="https://link/to/maven/repository" -Ppublish-repo-usr-1="your_usr_1" -Ppublish-repo-pwd-1="your_pwd_1"`

### Modifying consumer or publisher credentials

If you need to later add or remove credentials, you can do it so by in Android Studio as follows:

1. Click on *gradle* from the sidebar menu
2. Under the root project, click on *Tasks*
3. Under *Tasks*, click on *tokeninc*

You can click on *modifyConsumerCredentials* or *modifyPublisherCredentials* 
task to modify the corresponding credentials

<img src="src/main/resources/ReadMeResources/modifyTasks.png" width=75% height=75%>

**Please reach support@tokeninc.com if you have further questions.**

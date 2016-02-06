[![Travis CI](http://travis-ci.org/eivindveg/HotSUploader.svg?branch=develop)](http://travis-ci.org/eivindveg/HotSUploader)
[![VersionEye](https://www.versioneye.com/user/projects/563d0ed44d415e001b000073/badge.svg?style=flat)](https://www.versioneye.com/user/projects/563d0ed44d415e001b000073)
[![Join the chat at https://gitter.im/eivindveg/HotSUploader](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/eivindveg/HotSUploader)
# HotS Replay Uploader
JavaFX-based Replay Uploader for Heroes of the Storm

## About
HotS Replay Uploader is a JavaFX-based uploader for HotsLogs.com that aims to make uploading replays and looking up relevant statistics as effortless as possible.  
As Heroes of the Storm supports Windows and OSX, JavaFX was chosen due to the immediate simplicity of creating native installers for an event-driven cross platform application. Although there is no official game client for Linux distributions, the uploader is capable of running on most of them as well.

## Installation
Links to install the Hots Replay Uploader can be found on the [release page for this project](https://github.com/eivindveg/HotSUploader/releases) at the bottom of each release.

You can also install with HomeBrew:

``brew cask install hots-replay-uploader``

## Licenses
[![YourKit, LLC](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/)

This application's performance is profiled by YourKit Java Profiler. YourKit, LLC provides open source projects with licenses for professional Java and .NET profilers. Read more at [YourKit's Java Profiler product page](https://www.yourkit.com/features/).

## Contributing
Before contributing new features, please make sure the feature is discussed in an issue. Once an issue is confirmed and tagged as "Help wanted", feel free to fork and do as much work as you want. Unfinished features are also welcome, as long as they don't push to master. The application is intended to be simple in behaviour, requiring as little user input as possible. With this said, this puts certain requirements on all features being as powerful as possible, and the application must handle most/all of all variations in user setup.

If you've had a pull request accepted, or otherwise helped solve an issue, feel free to submit a pull request adding your own entry to pom.xml. I will not add anyone to this list without their consent.

## Building
This is a Maven project. You must have the Java 8 SDK installed in order to build from source. To build native artifacts for your platform in addition to jar files, run the following:

``mvn clean package``

## Maintainers
[eivindveg](/../../../../eivindveg) - Windows, Linux and general development

[zhedar](/../../../../../zhedar) - OSX and general development

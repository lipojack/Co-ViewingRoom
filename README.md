# CastVideos-android (reference Android sender app)

CastVideos-android application shows how to cast videos from an android device in a way that is fully compliant with the Design Checklist.

**This is a reference sender app to be used as the starting point for your Android sender app**

Here is the list of other reference apps:
* [iOS Sender: CastVideos-ios](https://github.com/googlecast/CastVideos-ios)
* [Chrome Sender: CastVideos-chrome](https://github.com/googlecast/CastVideos-chrome)
* [Receiver: Cast-Player-Sample](https://github.com/googlecast/Cast-Player-Sample)

## Dependencies
* CastCompanionLibrary-android: can be downloaded here at https://github.com/googlecast/CastCompanionLibrary-android
* ShowcaseView: can be downloaded at https://github.com/amlcurran/ShowcaseView (see change list 1.2 for details)
* android-advancedrecyclerview: See https://github.com/h6ah4i/android-advancedrecyclerview

## Setup Instructions
* Get a Chromecast device and set it up
* [Optional] Register an application on the Developers Console (http://cast.google.com/publish). The easiest would be to use the Styled Media Receiver option there.
You will get an App ID when you finish registering your application. This project uses a published Application ID that
can be used to run the app without using your own ID but if you need to do any console debugging, you would need
to have your own ID.
* Setup the project dependencies
  * For Eclipse: you need to setup the above dependencies manually. For ShowcaseView Library, see the additional comments
  below in the Change List 1.2.
  * For Android Studio: make sure you are running the latest version (it may work on earlier versions). Dependencies are
   setup in the build.gradle file and the settings.gradle; it assumes you have cloned the CastCompanionLibrary-android project
   into a parallel project named "CastCompanionLibrary". If you want to change the name, make sure it gets updated in both
   settings.gradle and build.gradle. Quick steps to clone and compile the app from command line (on a Linux or Mac) are:
     * $ git clone https://github.com/googlecast/CastVideos-android.git CastVideos
     * $ git clone https://github.com/googlecast/CastCompanionLibrary-android.git CastCompanionLibrary
     * $ cd CastVideos
     * $ ./gradlew build
* Compile and deploy to your Android device.
* This sample includes a published app id in the res/values/strings.xml file so the project can be built and run without a need
   to register an app id. If you want to use your own receiver (which is required if you need to debug the receiver),
    update "app_id" in that file with your own app id.

## References and How to report bugs
* [Cast Developer Documentation](http://developers.google.com/cast/)
* [Design Checklist](http://developers.google.com/cast/docs/design_checklist)
* If you find any issues with this library, please open a bug here on GitHub
* Question are answered on [StackOverflow](http://stackoverflow.com/questions/tagged/google-cast)

## How to make contributions?
Please read and follow the steps in the CONTRIBUTING.md

## License
See LICENSE

## Google+
Google Cast Developers Community on Google+ [http://goo.gl/TPLDxj](http://goo.gl/TPLDxj)

## Change List
2.2
 * Updating media assets to include three different types: mp4, HLS and DASH. Developers can decide which type of content
 they want to use by updating the VideoProvider.TARGET_FORMAT field; it is currently set to HLS. The HLS content has been
 recreated to provide a better set of segments.
2.1
 * Moving media assets to HLS format: all the videos have been reformatted to HLS to enable more exotic features such as pre-loading. The receiver
 has also been upated so that the included App ID points to a receiver that includes the pre-loading UI appropriate for the pre-loading feature.
 If the HLS format doesn't play locally on your phone or tablet, you may decide to switch back to the old media or you may even set things up so
 that local playback uses the old mp4 media and remote playback uses the HLS content.
 * A couple of bug fixes, including addressing a crash in the queue list page for GB devices.

2.0
 * Adapting to CCL 2.1 and adding support for Autoplay and Queueing. Note: in order to see the full capabilities for the
 autoplay, both on the sender and on the receiver, you would need to use the [reference receiver] (https://github.com/googlecast/Cast-Player-Sample)
  that is part of the samples.
 * A new page for handling queue has been added where you can reorder, delete, pause or play any of the queue items.
 * Some bug fixes.

1.6
 * Adapting to the CCL v2.0
 * Fixing some typos and minor issues

1.5
 * Adapting to the CCL v1.12
 * Fixing some typos

1.4
 * Added support for Android Lollipop. Toolbar has now replaced ActionBar in the project.
 * Updated android Studio support and gradle scripts and wrapper to gradle 2.1, gradle tools plugin 0.14 and Android Studio 0.9.1

1.3
 * Added support for Closed Captions based on enhancements made in CCL. Three new videos have been
   added to the beginning of the list of videos; these three include captions.

1.2
 * Updated the build.gradle to work with the latest version of the Android Studio Beta (0.8.1). You may need to upgrade your
 Android Studio to be abe to work with this project.
 * Added a first-time introductory UI for the Cast Button. This is accomplished by using
 [ShowcaseView project] (https://github.com/amlcurran/ShowcaseView).
 If you are using Eclipse, you would need to get the project and include that as a library project yourself.
 For Android Studio, an AAR package of the library is included.

1.1
 * Updated the Cast button images to match the new style
 * Addressed a couple of potential leaks

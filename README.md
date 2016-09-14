# orion360-sdk-basic-examples-android

Table of Contents
-----------------
1. [Prerequisities](#prerequisities)
2. [Cloning the project](#cloning-the-project)
3. [Studying the examples](#studying-the-examples)
4. [Example: Minimal Video Stream Player](#example-minimal-video-stream-player)
5. [Example: Minimal Video Download Player](#example-minimal-video-download-player)
6. [Example: Minimal Video File Player](#example-minimal-video-file-player)
7. [Example: Minimal Image Download Player](#example-minimal-image-download-player)
8. [Example: Minimal Image File Player](#example-minimal-image-file-player)
9. [Example: Minimal VR Video File Player](#example-minimal-vr-video-file-player)
10. [Example: Sensor Fusion](#example-sensor-fusion)
11. [Example: Nadir Patch](#example-nadir-patch)

Prerequisities
--------------

Cloning the project
-------------------

Studying the examples
---------------------

To get you started quickly, we have created a set of examples that show the very minimal player code for the most typical use cases (look for word "minimal" in the example name). You should start studying from these short and to-the-point examples, preferably in the presented order as the examples get gradually more complex.

For more advanced usage, we have created a set of examples that focus on a particular Orion360 feature, such as VR mode or hotspots. When you have mastered the basics, these examples will become valuable sources for adding new features to your 360 player app.

Example: Minimal Video Stream Player
------------------------------------

An example of a minimal Orion360 video player, for streaming a video file over the network.

Plays a low-quality 360 video stream over the network, and shows how to create a simple buffering indicator by listening to *OrionVideoView* buffering events.

Example: Minimal Video Download Player
--------------------------------------

An example of a minimal Orion360 video player, for downloading a video file before playback.

Notice that saving a copy of a video file while streaming it is not possible with Android MediaPlayer as a video backend. To obtain a local copy of a video file that resides in the network you need to download it separately, as shown in this example.

Since downloading a file will take some time, the example uses an AsyncTask to download the file in the background and updates download progress on screen. The video playback begins automatically when the whole video file has been downloaded.

Example: Minimal Video File Player
----------------------------------

An example of a minimal Orion360 video player, for playing a video file from local file system.

Showcases all supported file system locations and access methods (you need to select one from code). The supported locations are:

1. Application installation package's /assets folder

   Private asset folder allows playing content embedded to the apps's own installation package (.apk) (notice 100MB apk size limit in Google Play store). This is the recommended location when the application embeds video files to the installation package and is NOT distributed via Google Play store (single large .apk file delivery).

2. Application installation package's /res/raw folder

   Private raw resource folder allows playing content embedded to the app's own installation package (.apk) (notice 100MB apk size limit in Google Play). Use lowercase characters in filename, and access it without extension. This location is generally not recommended; use /assets folder instead for embedding media content.

3. Application's private path on device's internal memory

   Private internal folder is useful mainly when the app downloads a video file, as only the app itself can access that location (exception: rooted devices). This location is recommended only if downloaded content files need to be protected from ordinary users - although the protection is easy to circumvent with a rooted device.

4. Application's private path on device's external memory

   Private external folder allows copying videos via file manager app or a USB cable, which can be useful for users who know their way in the file system and the package name of the app (e.g. developers). This location is recommended for caching downloaded content.

5. Application's public path on device's external memory

   Public external folder allows easy content sharing between apps and copying content from PC to a familiar location such as the /Movies folder, but video playback requires READ_EXTERNAL_STORAGE permission, which needs to be explicitly requested from user (starting from Android 6.0). This location is recommended for playing content that is sideloaded by end users.

6. Application expansion package

   Private expansion package allows playing content embedded to the app's extra installation package (.obb) (up to 2 GB per package, max 2 packages). This is the recommended location when the application embeds video files to the installation package and is distributed via Google Play store.

Example: Minimal Image Download Player
--------------------------------------

An example of a minimal Orion360 image player, for downloading an image file before playback.

Notice that there is no "stream player" for 360 images; an equirectangular 360 image needs to be fully downloaded before it can be shown (viewing tiled images is not supported).

Example: Minimal Image File Player
----------------------------------

An example of a minimal Orion360 video player, for playing a video file from local file system.

Showcases all supported file system locations and access methods (you need to select one from code). The supported locations are:

1. Application's private path on device's internal memory

   Private internal folder is useful mainly when the app downloads an image file, as only the app itself can access that location (exception: rooted devices). This location is recommended only if downloaded content files need to be protected from ordinary users - although the protection is easy to circumvent with a rooted device.

2. Application's private path on device's external memory

   Private external folder allows copying images via file manager app or a USB cable, which can be useful for users who know their way in the file system and the package name of the app (e.g. developers). This location is recommended for caching downloaded content.

3. Application's public path on device's external memory

   Public external folder allows easy content sharing between apps and copying content from PC to a familiar location such as the /Pictures folder, but image viewing requires READ_EXTERNAL_STORAGE permission, which needs to be explicitly requested from user (starting from Android 6.0). This location is recommended for playing content that is sideloaded by end users.

Example: Minimal VR Video File Player
-------------------------------------

An example of a minimal Orion360 video player, with VR mode enabled.

Shows how to enable VR mode for viewing 360 videos with Google Cardboard or other VR frame where a smartphone can be slided in.

In short, the example shows how to:
* Configure horizontally split video view with landscape orientation
* Configure VR frame lens distortion compensation
* Configure field-of-view
* Hide system navigation bar
* Create a gesture detector for toggling VR mode on/off with long taps and a hint about it with ordinary taps
* Initialize the view orientation to World orientation (make video horizon perpendicular to gravity vector)
* Disable magnetometer from sensor fusion so that Cardboard's magnetic switch does not confuse it

Example: Sensor Fusion
----------------------

An example of a minimal Orion360 video player, with sensor fusion control.

By default, the 360 view is automatically rotated based on device orientation. Hence, to look at a desired direction, user can turn the device towards it, or when viewing through a VR frame, simply turn her head.

This feature requires movement sensors - most importantly, a gyroscope. Not all Android devices have one. The feature is automatically disabled on devices that do not have the necessary sensor hardware, with a fallback to touch-only control.

The supported movement sensors include 
* Accelerometer, which tells where is 'Down' direction, and linear acceleration
* Magnetometer, which tells where is 'North' direction, and slow rotation
* Gyroscope, which tells the rotation about the device's own axis very rapidly

Using data from all the three sensors, a sophisticated sensor fusion algorithm calculates device orientation several hundreds of times per second. The sensor fusion algorithm is also responsible for merging end user's touch input drag events with the orientation calculated from movement sensor data (touch tapping events are handled separately).

In short, the example shows how to:
* Manually disable sensor control, to enable touch-only mode
* Manually disable magnetometer input, to prevent issues with nearby magnetic objects and bad sensor calibration
* Manually disable pinch rotate gesture
* Manually configure pinch zoom gesture limits, or disable pinch zoom gesture
* Listen for device orientation changes (sensor fusion events), for custom features

Example: Nadir Patch
--------------------

An example of a minimal Orion360 video player, with a nadir patch image.

Nadir patch is frequently used when 360 photo or video is captured with a camera setup that does not cover the full sphere (360x180). The purpose is to cover the hole in the natural direction (down) with content producer or customer brand logo.

Orion360 allows adding 2D image panes to the 3D world; these are called tags. Adding a tag only requires a path to the image file, a direction vector, and a scale factor - the image pane is automatically set at the proper distance from the origin and kept facing to user at all times.

This example uses the tag feature to add a nadir patch image on top of the video layer. While a tag image pane is always a rectangular area, the PNG image file format with alpha channel allows drawing non-rectangular shapes, here a circular patch.

Orion360 tags must be created during view initialization, but they can be manipulated later. Here a standard Android object animator is used for fading in the patch image when the video playback begins. It is also possible to use current viewing direction as an input for tag manipulation, as shown here by keeping the patch upright at all times.


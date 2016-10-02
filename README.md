![alt tag](https://cloud.githubusercontent.com/assets/12032146/18542079/8d2c1c86-7b31-11e6-856a-7b3fd5d9c1a7.png)

# Examples for Orion360 SDK (Basic) for Android

This repository contains a set of examples for creating a 360 photo/video player using Orion360 SDK (Basic) for Android and Android Studio IDE.

Preface
-------

Thank you for choosing Orion360, and welcome to learning Orion360 SDK (Basic) for Android! You have made a good choice: Orion360 is the leading purpose-built SDK for 360/VR video apps, with over 400 licensed apps and 10+ millions of downloads in total - and growing fast.

We encourage you to begin studying from the _minimal_ examples. These are short and to-the-point; they will help you to create a simple 360 player in no-time. When you have mastered the basics, proceed to the more advanced examples that focus on a particular topic, such as touch input, VR mode, custom controls, and hotspots. The examples in this repository will become a valuable resource when you start adding features to your new 360 photo/video app, and will save you lots of development time!

In order to make studying the examples as easy as possible, each example has been implemented as an individual activity with very few dependencies outside of its own source code file - everything you need for a particular feature can be found from one place. To keep the examples short and easy to grasp, different topics are covered in separate examples. You don't need to go through them in order; just keep building your own app by adding features one-by-one based on your needs and priorities.

The aim of the examples is to show how the most common features can be implemented easily with Orion360, but also to help you understand _why_ something is done. Therefore, the examples are thoroughly commented and also briefly explained in this README. Sometimes, a particular feature can be implemented much better with the Pro version of the SDK, in such case this is mentioned in the example.

Finally, all the examples are collected under a single application that can be compiled from this project, installed and run on your own Android device. This way you can try out the reference implementation with ease, and also experiment by modifying the examples. Notice that you are allowed to utilize the example code and resources in your own app project as described in the copyright section of the source code files.

Happy coding! 

Table of Contents
-----------------
1. [Preface](#preface)
2. [Table of Contents](#table-of-contents)
3. [Prerequisities](#prerequisities)
4. [Cloning and Running the Project](#cloning-and-running-the-project)
5. [Example: Minimal Video Stream Player](#example-minimal-video-stream-player)
6. [Example: Minimal Video Download Player](#example-minimal-video-download-player)
7. [Example: Minimal Video File Player](#example-minimal-video-file-player)
8. [Example: Minimal Video Controls](#example-minimal-video-controls)
9. [Example: Minimal VR Video File Player](#example-minimal-vr-video-file-player)
10. [Example: Minimal Image Download Player](#example-minimal-image-download-player)
11. [Example: Minimal Image File Player](#example-minimal-image-file-player)
12. [Example: Buffering Indicator](#example-buffering-indicator)
13. [Example: Preview Image](#example-preview-image)
14. [Example: Sensor Fusion](#example-sensor-fusion)
15. [Example: Touch Input](#example-touch-input)
16. [Example: Nadir Patch](#example-nadir-patch)
17. [Example: Director's Cut](#example-directors-cut)
18. [Example: Interactive Hotspots](#example-interactive-hotspots)

Prerequisities
--------------

Basic Android software development skills are enough for understanding, modifying and running the examples.

As a first step, you should install Android Studio IDE (recommended version is 2.2 or newer):
https://developer.android.com/studio/install.html

Then, using the SDK Manager tool that comes with the IDE, install one or more Android SDKs. Notice that for Orion360 SDK Basic the minimum is **Android API level 14: Android 4.0 IceCreamSandwitch**.

> If you haven't already studied the Hello World project for Orion360 SDK (Basic), you should do that first and then continue with this example project. https://github.com/FinweLtd/orion360-sdk-basic-hello-android

Cloning and Running the Project
-------------------------------

To clone the project from GitHub, start Android Studio, select *Check out project from Version Control* and *Git* from the popup dialog.

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18541651/2264faaa-7b2f-11e6-9a21-75182dcf9666.png)

Set repository URL, parent directory, and project directory.

>Notice that the repository URL is easy to copy-paste from a web browser to Android Studio: click the green *Clone or download* button on the project's GitHub page, copy the URL from the dialog that appears, and paste it to Android Studio's dialog as shown below. 

Hit *Clone* button to retrieve the repository contents to your local machine.

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18541713/a98e6ebc-7b2f-11e6-8c89-fa32266131e1.png)

Cloning the project will take a moment. Android Studio then asks if you want to open the project, answer *Yes*.

When the project opens Android Studio performs Gradle sync that will take some time (please wait). After Gradle sync finishes, you can find the project files by opening the *Project* view on the left edge of the IDE window.

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18541727/b5e46a72-7b2f-11e6-99df-3fe4ee471547.png)

Next, connect an Android device to your computer via a USB cable, and then compile the project and run the app on your device by simply clicking the green *Play* button in the top toolbar. This will take a moment.

> While 360 photo/video apps can in theory be developed using an emulator, real Android hardware is highly recommended. The Android emulator does not support video playback. Moreover, to work with sensor fusion, touch control and VR mode, the developer frequently needs to run the app on target device.

When the app starts on your device, a menu of examples similar to the image below will be shown. Tap any example from the list to run it, and return to the examples menu by tapping the *Back* button from your device's Navigation Bar. In order to really understand what each example is about, you should always read the source code and comments.

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18544086/c8a52096-7b3b-11e6-8feb-18a8569250b6.png)

> Most examples use demo content that requires an Android device that can decode and play FullHD (1920x1080p) video, or less. However, a few examples may require UHD (3840x1920) resolution playback. If your development device does not support 4k UHD video, simply change the content URI to another one with smaller resolution (you can find plenty of demo content links from the *MainMenu* source code file).

Example: Minimal Video Stream Player
------------------------------------

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18544135/0068a3b8-7b3c-11e6-9bac-983f05fa144c.png)

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/MinimalVideoStreamPlayer.java)

An example of a minimal Orion360 video player, for streaming a video file over the network.

This example shows how to add an Orion360 video view to an XML layout, get a handle to it from Java code, request the video view to prepare an MP4 video file (that resides somewhere in the network) for playback, and start playback when an asynchronous callback tells that enough video frames have been downloaded and buffered.

The example also shows how to create a simple buffering indicator by listening to video view buffering events, and how to propagate activity life cycle events to the video view so that it can automatically respond to them (for example pause video playback if user navigates to another app).

Orion360 views have lots of features built-in; you will have all the following without writing any additional code:
- Support for rendering full spherical (360x180) equirectangular video content with rectilinear projection
- Panning, zooming and tilting the view with touch and movement sensors, which work seamlessly together
- Auto Horizon Aligner (AHL) keeps the horizon straight by gently re-orienting it when necessary

> Android device's hardware video decoder sets a limit for the maximum resolution / bitrate of a video file that can be decoded, but to be rendered on screen, the decoded video frame also needs to fit inside a single OpenGL texture. In 2016, new mid-range devices support FullHD video and high-end devices 4k UHD video, while some popular older models cannot decode even FullHD. The maximum texture size in new devices ranges from 4096x4096 to 16386x16384, while some popular older models have 2048x2048 texture size. To be on the safe side, recommendation is to use 1920x960 video resolution and a moderate bitrate. If necessary, offer another 3840x1920 stream for high-end devices.

Example: Minimal Video Download Player
--------------------------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/MinimalVideoDownloadPlayer.java)

An example of a minimal Orion360 video player, for downloading a video file before playback.

Available network bandwith often becomes an issue when streaming video over the network (especially true with high-resolution 4k content). Unfortunately, saving a copy of a video file while streaming it is not possible with Android MediaPlayer as a video backend. Hence, if you need to obtain a local copy of a video file that resides in the network either for offline use or to be cached, download it separately as shown in this example.

Since downloading a large file will take a considerable amount of time, the example uses an AsyncTask to download the file in the background and updates download progress on screen. In this simple example, user needs to wait for the download to complete and the playback to begin as there is nothing else to do. However, you should consider placing a small download indicator somewhere in your app and allowing the user to continue using the app while the download is in progress. A high quality app has a download queue for downloading multiple files sequentially, is able to continue a download if it gets terminated early for example because of a network issue, allows user to cancel ongoing downloads, and uses platform notifications for indicating download progress and completion of a download. These features go beyond this example.

Video files are large and device models with small amounts of storage space tend to be popular as they are priced competitively. Consider saving the downloaded video file to external memory if it is currently present. It is also a good idea to offer a method for deleting downloaded content without uninstalling the whole app; this way users can still keep your app installed when they need to restore some storage space.

Example: Minimal Video File Player
----------------------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/MinimalVideoFilePlayer.java)

An example of a minimal Orion360 video player, for playing a video file from local file system.

This example showcases all supported file system locations and file access methods for video sources: the locations embedded to the app distribution packages, the app's private locations that become available after installation, and the locations that are more or less external to the app. To keep the example simple, only one location is active at a time and the others are commented out (you can easily select the active location from the source code). The supported locations are:

1. Application installation package's _/assets_ folder

   Private assets folder allows playing content embedded to the apps's own installation package (.apk). Notice 100MB .apk size limit in Google Play store. This is the recommended location when the application embeds video files to the installation package and _is NOT_ distributed via Google Play store (single large .apk file delivery).

2. Application installation package's _/res/raw_ folder

   Private raw resource folder allows playing content embedded to the app's own installation package (.apk). Notice 100MB .apk size limit in Google Play. Must use lowercase characters in filenames and access them without filename extension. This location is generally not recommended; use _/assets_ folder instead.

3. Application expansion packages

   Private expansion package allows playing content embedded to the app's extra installation package (.obb). Up to 2 GB per package, max 2 packages. This is the recommended location when the application embeds video files to the installation package and _is_ distributed via Google Play store. Fairly complex but very useful solution. For more information, see https://developer.android.com/google/play/expansion-files.html

4. Application's private path on device's internal memory

   Private internal folder is useful mainly when the app _downloads_ a video file for offline mode or to be cached, as only the app itself can access that location (exception: rooted devices). This location is recommended only if downloaded content files need to be protected from ordinary users - although the protection is easy to circumvent with a rooted device.

5. Application's private path on device's external memory

   Private external folder allows copying videos back and forth via file manager app or a USB cable, which can be useful for users who know their way in the file system and the package name of the app (e.g. developers). This location is recommended for caching downloaded content, as many devices have more external memory than internal memory.

6. Any public path on device's external memory

   Public external folders allow easy content sharing between apps and copying content from PC to a familiar location such as the /Movies folder, but reading from there requires READ_EXTERNAL_STORAGE permission (WRITE_EXTERNAL_STORAGE for writing) that needs to be explicitly requested from user, starting from Android 6.0. This location is recommended for playing content that is sideloaded by end users either by copying to device via a USB cable or read directly from a removable memory card.

> In case your app is intended for playing a couple of short fixed 360 videos or a fixed set of 360 photos, then you should consider embedding the content into the app. This approach provides several benefits:
> - Simpler content deployment without a streaming server and a content-delivery network (CDN)
> - Lower and more predictable content deployment cost - even FREE delivery via Google Play store
> - Built-in offline mode without making the UI more complex with content download and delete features
> - Guaranteed to have no buffering pauses during video playback
> 
> However, there are also some major drawbacks:
> - App installation package becomes large and potential users may skip the app based on its size
> - After watching the embedded content the whole app needs to be uninstalled to remove the content
> - Adding/updating content not possible without updating the app (many users will never update)
> - Only a limited amount of content can be embedded to the app
> 
> Typically one-shot apps that are intended for a particular event, product campaign, or offline use have embedded content. However, also apps that mostly use streamed content may include a few embedded items that are frequently needed and rarely updated, such as brand introduction, user tutorials, and menu backgrounds.

Example: Minimal Video Controls
-------------------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/MinimalVideoControls.java)

An example of a minimal Orion360 video player, with minimal video controls.

This example uses _MediaController_ class as a simple way to add controls into a 360 video player. This approach requires only a few lines of code: first a new media controller is instantiated, and then Orion360 video view is added to it as a media player to control, and as a UI anchor view where to position the control widget. Finally, a gesture detector is used for showing and hiding the controls when the video view is tapped (by default, the media controller automatically hides itself after a moment of inactivity).

The control widget includes play/pause button, rewind and fast forward buttons, a seek bar, and labels for elapsed and total playing time. If you want to customize the look&feel of the control widget or add your own buttons, see _CustomControls_ example where video controls are created from scratch.

> When seeking within a video, notice that it is only possible to seek to keyframes - the player will automatically jump to the nearest one. The number of keyframes and their positions depend on video content, used video encoder, and encoder settings. In general, the more keyframes are added the larger the video file will be. The Orion360 example video is mostly static and thus has very few keyframes, allowing the user to seek only to a few positions.

Example: Minimal VR Video File Player
-------------------------------------

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18544208/43e3850e-7b3c-11e6-947c-2fe01130b52d.png)

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/MinimalVRVideoFilePlayer.java)

An example of a minimal Orion360 video player, with VR mode enabled.

The most impressive way to experience 360 photos and videos is through virtual reality (VR). Unfortunately, most people do not have the necessary equipment yet. However, there is a _very_ cost efficient method: users can simply slide their existing smartphone inside a VR frame that essentially consists of a plastic or cardboard frame and a pair of convex lenses, and enable VR mode from an app that supports split-screen viewing.

Currently the most popular VR frame by far is Google Cardboard (https://vr.google.com/cardboard); millions of them have been distributed to users already. There are also plenty of Cardboard clones available from different manufacturers. It is a fairly common practice to create a custom-printed Cardboard-style VR frame for a dollar or two per piece, and give them out to users for free along with a 360/VR video app and content. That combo makes a really great marketing tool.

This example shows how to enable VR mode from an Orion360 video view for viewing content with Google Cardboard or other similar VR frame where smartphone can be slided in. In short, the example shows how to:
- Configure horizontally split video view in landscape orientation
- Configure (and lock) the field-of-view into a realistic setting
- Configure VR frame lens distortion compensation for improved image quality
- Initialize the view orientation to World orientation ie. keep video horizon perpendicular to gravity vector
- Hide the system navigation bar for occlusion free viewing in devices where it is made by software
- Disable magnetometer from sensor fusion so that Cardboard's magnetic switch does not interfere with it
- Create a gesture detector for toggling VR mode on/off with long taps and a hint about it with a single tap

> For high-quality VR experiences, consider using a high-end Samsung smartphone and an active GearVR frame (you will also need to use the Pro version of the Orion360 SDK). The equipment cost will be significantly higher, but also the improvement in quality is remarkable and well worth it. GearVR frame has great optics, high speed sensors and touch controls built-in. They only work with specific Samsung models that have a number of performance tunings built-in and drivers for the GearVR frame. In general, Cardboard-style VR is recommended when you want to provide the VR viewing experience for a large audience by giving out free VR frames, while GearVR-style VR is best for trade shows, shop desks and one-to-one marketing where quality counts the most!

Example: Minimal Image Download Player
--------------------------------------

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18792594/c6499950-81bf-11e6-85d1-8164f2517799.png)

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/MinimalImageDownloadPlayer.java)

An example of a minimal Orion360 image player, for downloading an image file before playback.

> Notice that there is no example of a streaming player for 360 images, as an image always needs to be downloaded completely before it can be shown (tiled 360 images are not supported in the Basic version of the Orion360 SDK).

This example is similar to _MinimalVideoDownloadPlayer_, but showcases how to use _OrionImageView_ component instead of _OrionVideoView_ for showing a 360 image.

Since downloading a large file will take a considerable amount of time, the example uses an AsyncTask to download the file in the background and updates download progress on screen. In this simple example, user needs to wait for the download to complete and the playback to begin as there is nothing else to do. However, you should consider placing a small download indicator somewhere in your app and allowing the user to continue using the app while the download is in progress. A high quality app has a download queue for downloading multiple files sequentially, is able to continue a download if it gets terminated early for example because of a network issue, allows user to cancel ongoing downloads, and uses platform notifications for indicating download progress and completion of a download. These features go beyond this example.

Image files are large and device models with small amounts of storage space tend to be popular as they are priced competitively. Consider saving the downloaded image file to external memory if it is currently present. It is also a good idea to offer a method for deleting downloaded content without uninstalling the whole app; this way users can still keep your app installed when they need to restore some storage space.

> The hardware limits for 360 image resolution come from available memory for decoding the image file and maximum texture size for storing and rendering it. Notice that Orion360 automatically scales the image to fit to device's maximum texture size if necessary. In 2016, some popular older devices have 2048x2048 pixel texture size (4 megapixels), while new devices range from 4096x4096 (16 megapixels) to 16384x16384 pixels (256 megapixels). Obviously, depending on target device, the difference in rendered image quality can be remarkable with a high-resolution source image.

Example: Minimal Image File Player
----------------------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/MinimalImageFilePlayer.java)

An example of a minimal Orion360 video player, for playing a video file from local file system.

Showcases all supported file system locations and access methods (you need to select one from code). The supported locations are:

1. Application's private path on device's internal memory

   Private internal folder is useful mainly when the app downloads an image file, as only the app itself can access that location (exception: rooted devices). This location is recommended only if downloaded content files need to be protected from ordinary users - although the protection is easy to circumvent with a rooted device.

2. Application's private path on device's external memory

   Private external folder allows copying images via file manager app or a USB cable, which can be useful for users who know their way in the file system and the package name of the app (e.g. developers). This location is recommended for caching downloaded content.

3. Application's public path on device's external memory

   Public external folder allows easy content sharing between apps and copying content from PC to a familiar location such as the /Pictures folder, but image viewing requires READ_EXTERNAL_STORAGE permission, which needs to be explicitly requested from user (starting from Android 6.0). This location is recommended for playing content that is sideloaded by end users.

Example: Buffering Indicator
----------------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/BufferingIndicator.java)

An example of a minimal Orion360 video player, with a buffering indicator.

A buffering indicator tells end user that the video player is currently loading content and should start/continue soon. This example shows some tips on how to implement it properly.

Buffering before and during video playback are covered, as well as pausing and resuming player activity, and toggling between normal and VR mode.

Example: Preview Image
----------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/PreviewImage.java)

An example of a minimal Orion360 video player, with a preview image.

The preview image is a full-size equirectangular image overlay on top of the video layer. Notice the difference to tags, which are origin-facing rectilinear images that cover only a part of the video layer. The preview image should be of the same resolution than the main video or image that it is applied to, while tags can be of any resolution.

Similar to tags, the alpha value of the preview image can be freely adjusted. Therefore it is possible to completely cover the video layer, add a semi-transparent layer, and cross-fade between image and video (or two images when using OrionImageView instead of OrionVideoView). With a PNG image that has transparent areas, only selected parts of the video can be covered.

The typical use case is to add a preview image that is shown in the beginning while the video is still being buffered from the network. For example, the image could contain a brand logo, instructions for panning and zooming within 360 view, or a reminder about placing the device inside a VR frame.

However, the feature is actually much more versatile than that. Here are a few ideas:
* Show an image also when the video completes to thank users for watching and to instruct what to do next.
* If you have a playlist, show a hero image while buffering next video.
* Show an image when user pauses the video, when the player stops for buffering, or when network connection issues or other problems occur.
* Dim video easily by adjusting preview image alpha and NOT setting a preview image at all.
* Add a color overlay FX with a single-color preview image and a small alpha value.
* Show dynamically loaded ads during video playback.
* Create a slideshow with cross-fade effect using OrionImageView and an audio track.

Example: Sensor Fusion
----------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/SensorFusion.java)

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

Example: Touch Input
----------------------

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18792680/192f7b12-81c0-11e6-8fa3-81ae09e6c5c0.png)

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/TouchInput.java)

An example of a minimal Orion360 video player, with touch input.

This example uses single tapping for toggling between normal and full screen view, double tapping for toggling between video playback and pause states, and long tapping for toggling between normal and VR mode rendering. These are tried-and-true mappings that are recommended for all 360/VR video apps.

Left and right edge of the video view have hidden tapping areas that seek the video 10 seconds backward and forward, respectively. This is just an example of mapping different actions to tapping events based on touch position on screen, not a general recommendation.

To showcase tapping inside the 3D scene, a hotspot is added to the video view and tapping the hotspot area will trigger roll animation. Notice that with Orion360 SDK Basic, the developer must manually combine hotspot and tapping near to its location, whereas Orion360 SDK Pro has built-in 3D objects and callbacks for their tapping and gaze selection events.

For panning, zooming and rotating the view via swipe and pinch, see SensorFusion example.

Example: Nadir Patch
--------------------

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18544249/6fae14f6-7b3c-11e6-8c0b-ee322e53cdb2.png)

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/NadirPatch.java)

An example of a minimal Orion360 video player, with a nadir patch image.

Nadir patch is frequently used when 360 photo or video is captured with a camera setup that does not cover the full sphere (360x180). The purpose is to cover the hole in the natural direction (down) with content producer or customer brand logo.

Orion360 allows adding 2D image panes to the 3D world; these are called tags. Adding a tag only requires a path to the image file, a direction vector, and a scale factor - the image pane is automatically set at the proper distance from the origin and kept facing to user at all times.

This example uses the tag feature to add a nadir patch image on top of the video layer. While a tag image pane is always a rectangular area, the PNG image file format with alpha channel allows drawing non-rectangular shapes, here a circular patch.

Orion360 tags must be created during view initialization, but they can be manipulated later. Here a standard Android object animator is used for fading in the patch image when the video playback begins. It is also possible to use current viewing direction as an input for tag manipulation, as shown here by keeping the patch upright at all times.

Example: Director's Cut
-----------------------

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/DirectorsCut.java)

An example of a minimal Orion360 video player, with forced view rotation control.

It is characteristic to 360 content that the end user is able to decide where to look at, and that there is (hopefully interesting) content available at all directions. While this freedom of exploration is one of the best treats of consuming content in 360 degrees, it is also contrary to traditional video and photo production where a director or a photographer decides the direction where to aim the camera and how to frame the shot, and thus carefully leads the user through the story by ensuring that she sees the relevant cues.

In 360, most of the time it is desirable to let the end user be in charge of the virtual camera ie. turn the device or pan the content with a finger. Yet there are occasions where a decision needs to be made on behalf of the user. The primary concern is that in case of video content the playback progresses at a constant pace, and in order to keep up the rhythm the story telling must proceed as well - but at the moment of a cut or a major event, the user may be looking at a 'wrong' direction and hence miss important cues, making the storyline feel very confusing!

The solution is to force the view to certain direction at a certain moment of time. This is, of course, a tool that should not be used without a very good reason, and that requires skill to do well.

The first decision to be made is the very first frame of the video. There are a few typical use cases that are covered in the example:

1. Case A: The user is holding the device in hand at some random angle, but presumably at an orientation that feels comfortable to her. In this case, we want to rotate the view so that viewing begins from the center of the video, ie. from the 'front' direction of the content. The experience would be the same for a user who is sitting in a bus and looking down-forward to a device that lies on her hand that is resting on her knees, and for a user who is lying on a sofa and looking up-forward to a device held with a raised arm. This is also the default configuration for Orion360, and the developer needs to do nothing to accomplish this.
2. Case B: In case the director wants to make an artistic decision in the opening scene, she might want to force the view away from the 'front' direction of the content, to make the viewer first slightly confused and to find the 'front' direction where the action mostly takes place by panning the view there herself. This would be a rather rarely used effect and a variation of Case A.
3. Case C: If the user makes use of a VR frame to view the content, the solution presented in Case A is not appropriate. It is crucial that the nadir is aligned with user's perception of 'down' direction, and also the horizon line appears to be in its natural place.
4. Case D: Similar to Case B, the director may want to start from a certain viewing direction. However, when using a VR frame, only the yaw angle (azimuth/compass angle) should be enforced, to keep the content aligned with the user's perception of 'down' direction at all times.

After the question of the initial viewing rotation is settled, the director may want to add some additional forced viewing directions. The most suitable places are when there is a cut and the viewer is taken to another place and time anyway - it is not that disturbing if also the viewing direction is re-oriented at the same exact moment.

In order to perform such operations during video playback, we need to listen to the video position and check when a predefined moment of time has been reached. Unfortunately, the Android media player backend does not provide frame numbers, and even video position must be queried via polling. The example shows how to rotate the camera at certain positions of time (with respect to video player position).

Finally, the director may want to perform animated camera operations, such as panning and zooming. These are somewhat controversial, but feel fairly good when the user is not taken completely out of control. Hence, we perform the panning and zooming as small animated steps by always modifying the latest value towards the target, thus allowing simultaneous control by user. The example shows how to do this.

Example: Interactive Hotspots
-----------------------------

![alt tag](https://cloud.githubusercontent.com/assets/12032146/18544261/84e2ad82-7b3c-11e6-87d6-0cc91eabfffe.png)

[View code](app/src/main/java/fi/finwe/orion360/sdk/basic/examples/examples/InteractiveHotspots.java)

An example of a minimal Orion360 video player, with interactive hotspots.

This example initializes 4 hotspots ("Start" buttons) at front, back, left & right directions. It dims and pauses the video, and waits for user to trigger start event by interacting with one of the hotspots. To get user's attention, the "Start" buttons are continuously animated (floating).

There is a fifth hotspot that acts as a reticle, continuously showing the position where the user is looking/pointing at. When the reticle is moved close enough to one of the "Start" hotspots, a pre-selection animation begins (roll). If the user keeps looking at the "Start" button long enough, it is triggered and a post-selection animation begins (escape), video dimming is removed, and playback starts. However, if the user moves away from the "Start" button before the pre-selection animation ends, selection is canceled.

This is a fairly complex example. To structure information into easily digestable pieces, a simple Hotspot class is represented, and then improved by adding more and more features to it by the means of inheritance.

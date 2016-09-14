# orion360-sdk-basic-examples-android

Table of Contents
-----------------
1. [Prerequisities](#prerequisities)
2. [Cloning the project](#cloning-the-project)
3. [Studying the examples](#studying-the-examples)
4. [Example: Minimal Video Stream Player](#example-minimal-video-stream-player)
5. [Example: Minimal Video Download Player](#example-minimal-video-download-player)
6. [Example: Minimal Video File Player](#example-minimal-video-file-player)

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

   Private asset folder allows playing content embedded to the apps's own installation package (.apk) (notice 100MB apk size limit in Google Play store).
2. Application installation package's /res/raw folder

   Private raw resource folder allows playing content embedded to the app's own installation package (.apk) (notice 100MB apk size limit in Google Play). Use lowercase characters in filename, and access it without extension!
3. Application's private path on device's internal memory

   Private internal folder is useful mainly when the app downloads a video file, as only the app itself can access that location (exception: rooted devices).
4. Application's private path on device's external memory

   Private external folder allows copying videos via file manager app or a USB cable, which can be useful for users who know their way in the file system and the package name of the app (e.g. developers).
5. Application's public path on device's external memory

   Public external folder allows easy content sharing between apps and copying content from PC to a familiar location such as the /Movies folder, but video playback requires READ_EXTERNAL_STORAGE permission.
6. Application expansion package

   Private expansion package allows playing content embedded to the app's extra installation package (.obb) (up to 2 GB per package, max 2 packages).


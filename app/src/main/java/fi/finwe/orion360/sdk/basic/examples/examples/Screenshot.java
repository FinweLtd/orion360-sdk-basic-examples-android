/**
 * Copyright (c) 2016, Finwe Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fi.finwe.orion360.sdk.basic.examples.examples;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fi.finwe.orion360.OrionSurfaceView;
import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with screenshot capture by tapping.
 * <p/>
 * Features:
 * <ul>
 * <li>Plays one hard-coded full spherical (360x180) equirectangular video
 * <li>Creates a fullscreen view locked to landscape orientation
 * <li>Auto-starts playback on load and stops when playback is completed
 * <li>Renders the video using standard rectilinear projection
 * <li>Allows navigation with touch & movement sensors (if supported by HW) as follows:
 * <ul>
 * <li>Panning (gyro or swipe)
 * <li>Zooming (pinch)
 * <li>Tilting (pinch rotate)
 * </ul>
 * <li>Auto Horizon Aligner (AHL) feature straightens the horizon</li>
 * </ul>
 */
public class Screenshot extends Activity {

    /** Tag for logging. */
    public static final String TAG = Screenshot.class.getSimpleName();

    /** Screenshot filename format string. */
    private static final String FILE_NAME_FORMAT = "yyyyMMddhhmmss'_Orion360_Screenshot.png'";

    private static final String CAMERA_SOUND_FILE ="file:///system/media/audio/ui/camera_click.ogg";

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** Gesture detector for tapping events. */
    private GestureDetector mGestureDetector;

    /** Media player for playing camera shoot sound. */
    private MediaPlayer mCameraShootPlayer;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {
                mOrionVideoView.start();
            }
        });

        // Initialize Orion360 video view with a URI to an .mp4 video file.
        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.prepare(MainMenu.PRIVATE_EXTERNAL_FILES_PATH
                    + MainMenu.TEST_VIDEO_FILE_MQ);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Propagate all touch events from the video view to a gesture detector.
        mOrionVideoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }

        });

        // Toggle media controls by tapping the screen.
        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {

                        //captureScreenshot();
                        captureScreenshotAsync();

                        return true;
                    }

                });

        // Listener for async version of the screenshot capturing.
        mOrionVideoView.setOnScreenshotReadyListener(
                new OrionSurfaceView.OnScreenshotReadyListener() {

                    @Override
                    public void onScreenshotReady(OrionSurfaceView orionSurfaceView,
                                                  Bitmap bitmap) {
                        saveScreenshot(bitmap, createScreenshotFilePath());
                        bitmap.recycle();
                    }

                });
	}

    @Override
    public void onStart() {
        super.onStart();

        // Propagate activity lifecycle events to Orion360 video view.
        mOrionVideoView.onStart();
    }

	@Override
	public void onResume() {
		super.onResume();

        // Propagate activity lifecycle events to Orion360 video view.
		mOrionVideoView.onResume();

        Toast.makeText(this, "Tap the screen to capture screenshot", Toast.LENGTH_SHORT).show();
    }

	@Override
	public void onPause() {
        // Propagate activity lifecycle events to Orion360 video view.
		mOrionVideoView.onPause();

		super.onPause();
	}

	@Override
	public void onStop() {
        // Propagate activity lifecycle events to Orion360 video view.
		mOrionVideoView.onStop();

		super.onStop();
	}

	@Override
	public void onDestroy() {
        // Propagate activity lifecycle events to Orion360 video view.
		mOrionVideoView.onDestroy();

		super.onDestroy();
	}

    /**
     * Capture screenshot synchronously.
     */
    private void captureScreenshot() {

        // Capture screenshot from the Orion video view. Reading pixels from OpenGL
        // surface takes some time and this is a blocking call that freezes rendering!

        // Request to play camera shoot sound first, then start capturing the screenshot.
        playCameraShootSound();

        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        Bitmap bitmap = null;
        try {
            bitmap = mOrionVideoView.captureScreenshot();
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // Save screenshot to file.
        saveScreenshot(bitmap, createScreenshotFilePath());

        // Release memory.
        bitmap.recycle();
    }

    /**
     * Capture screenshot asynchronously.
     */
    private void captureScreenshotAsync() {

        // Capture screenshot from the Orion video view. Reading pixels from OpenGL
        // surface takes some time, this is an async call - you will get notified
        // when the screenshot is ready via OnScreenshotReadyListener.

        // Request to play camera shoot sound first, then start capturing the screenshot.
        playCameraShootSound();

        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.captureScreenshotAsync();
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates a new screenshot file name and path, using time stamp in the file name.
     *
     * @return The newly created screenshot file path.
     */
    private String createScreenshotFilePath() {
        String fileName = new SimpleDateFormat(FILE_NAME_FORMAT).format(new Date());
        String filePath = MainMenu.PUBLIC_EXTERNAL_PICTURES_ORION_PATH + fileName;
        return filePath;
    }

    /**
     * Play camera shoot sound.
     */
    private void playCameraShootSound()
    {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
        if (volume > 0) {
            if (mCameraShootPlayer == null) {
                mCameraShootPlayer = MediaPlayer.create(this, Uri.parse(CAMERA_SOUND_FILE));
            }
            if (mCameraShootPlayer != null) {
                mCameraShootPlayer.start();
            }
        }
    }

    /**
     * Save a screenshot bitmap as a PNG image to given file path.
     *
     * Notice: depending on target file path, WRITE_EXTERNAL_STORAGE permission may be required!
     *
     * @param screenshot The bitmap to be saved.
     * @param filePath The file path where to save the bitmap.
     */
    private void saveScreenshot(Bitmap screenshot, String filePath) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.i(TAG, "Screenshot saved to " + filePath);
            Toast.makeText(this, "Screenshot saved to " + filePath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save screenshot", e);
            Toast.makeText(this, "Screenshot FAILED!", Toast.LENGTH_SHORT).show();
        } finally {
            try { if (out != null) { out.close();} } catch (IOException e) {}
        }
    }
}

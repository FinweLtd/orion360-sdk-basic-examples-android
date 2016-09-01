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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.OrionViewConfig;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with VR mode enabled.
 * <p>
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
public class MinimalVRVideoFilePlayer extends Activity {

    /** Tag for logging. */
    public static final String TAG = MinimalVRVideoFilePlayer.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** Gesture detector for touch events. */
    private GestureDetector mGestureDetector;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_minimal_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {

                // Some VR frames, such as Google Cardboard, may contain a magnetic switch
                // whose magnet will confuse sensor fusion -> we should disable magnetometer.
                // The best place to do that is here when video view has just been prepared;
                // this way sensor fusion has time to get a few samples from magnetometer
                // and stabilizes faster.
                mOrionVideoView.setOrientationMagnetometer(false);

                // Configure video view for VR mode. This will split the screen horizontally,
                // render the image separately for left and right eye, and apply lens distortion
                // compensation and field-of-view (FOV) locking to configured values.
                setVRMode(true);

                // Start video playback.
                mOrionVideoView.start();
            }
        });

        // Initialize Orion360 video view with a URI to a local .mp4 video file.
        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.prepare(MainMenu.PRIVATE_EXTERNAL_FILES_PATH
                    + MainMenu.TEST_VIDEO_FILE_MQ);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // In normal mode it is preferable to automatically rotate the content so that
        // when the rendering begins the front direction of the video is brought in view,
        // despite of the current device orientation (for example when lying on a sofa,
        // you probably want to start from front direction, not top). However, in VR mode
        // this is not acceptable as the horizon needs to be in its natural place,
        // hence we clear the default view rotation simply by setting it to null *before*
        // preparing the video view for playback.
        OrionViewConfig cfg = mOrionVideoView.getCurrentConfigCopy();
        cfg.setDefaultViewRotation(null);
        try {
            mOrionVideoView.applyConfig(cfg);
        } catch (OrionViewConfig.NotSupportedException e) {
            Log.e(TAG, "Selected configuration is not supported");
        }

        // The user should always have an easy-to-find method for returning from VR mode to
        // normal mode. Here we use touch events, as it is natural to try tapping the screen
        // if you don't know what else to do. Start by propagating touch events from the
        // video view to a gesture detector.
        mOrionVideoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }

        });

        // Then, handle tap and long press events based on VR mode state. Typically you
        // want to associate long tap for entering/exiting VR mode and inform the user
        // that this hidden feature exists (at least when the user is stuck in VR mode).
        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {

                        // Notify user how enter/exit VR mode with long press.
                        String message;
                        if (mOrionVideoView.getCurrentConfigCopy().getTargetLayout()
                                != OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
                            message = getString(R.string.player_long_tap_hint_enter_vr_mode);
                        } else {
                            message = getString(R.string.player_long_tap_hint_exit_vr_mode);
                        }
                        Toast.makeText(MinimalVRVideoFilePlayer.this, message,
                                Toast.LENGTH_SHORT).show();

                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {

                        // Enter or exit VR mode.
                        if (mOrionVideoView.getCurrentConfigCopy().getTargetLayout()
                                != OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
                            setVRMode(true);
                        } else {
                            setVRMode(false);
                        }

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
     * Set VR mode enabled or disabled.
     *
     * @param enabled Set true to enable VR mode, or false to return to normal mode.
     */
    public void setVRMode(boolean enabled) {

        // To modify view configuration, we get a copy of the current configuration, change it,
        // and then apply the new configuration.
        OrionViewConfig cfg = mOrionVideoView.getCurrentConfigCopy();

        if (enabled) {

            // To view the content through a VR frame, we need to split the screen horizontally and
            // render a separate image for left and right eye. This can be achieved by configuring
            // the target layout as horizontally split.
            cfg.setTargetLayout(OrionViewConfig.Layout.SPLIT_HORIZONTAL);

            // We should always compensate the image distortion produced by the VR frame's convex
            // lenses. The type of the distortion created by a convex lens is pincushion distortion,
            // which can be compensated by properly configured barrel distortion. Distortion
            // coefficients A, B, C, and D set the strength of the barrel distortion in relation
            // to distance from the lens center point.
            cfg.setBarrelDistortionCoeffs(1.0f, 0.06f, -0.03f, 0.02f);

            // The size of the image can be adjusted with fill scale setting. Notice that barrel
            // distortion coefficients need to be reconfigured if fill scale is changed.
            cfg.setBarrelFillScale(1.0f);

            // Users should not be able to change the field-of-view (FOV) while in VR mode, so
            // we prevent zooming by locking the FOV to a predefined level. This can be achieved
            // by setting the min and max values to the same as the initial value.
            cfg.setFov(
                    110, // Initial value
                    110, // Minimum value
                    110  // Maximum value
            );

            // We need to hide the navigation bar, else this will be visible for the right eye.
            hideNavigationBar();

            // VR is still new for many users, hence they should be educated what this feature is
            // and how to use it, e.g. an animation about putting the device inside a VR frame.
            // Here we simply show a notification.
            Toast.makeText(this, "Please put the device inside a VR frame",
                    Toast.LENGTH_LONG).show();

        } else {

            // To view the content directly from screen again (after visiting VR mode), we need
            // to fill the whole screen with a single image. This can be achieved by configuring
            // the target layout as full.
            cfg.setTargetLayout(OrionViewConfig.Layout.FULL);

            // Allow field-of-view (FOV) adjustment for users.
            cfg.setFov(
                    95, // Initial value
                    45, // Minimum value
                    100  // Maximum value
            );

            // Show the navigation bar again.
            showNavigationBar();

        }

        // Apply the changes.
        try {
            mOrionVideoView.applyConfig(cfg);
        } catch (OrionViewConfig.NotSupportedException e) {
            Log.e(TAG, "Selected configuration is not supported!");
        }
    }

    /**
     * Hide navigation bar.
     */
    public void hideNavigationBar() {
        View v = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT < 19) {
            v.setSystemUiVisibility(View.GONE);
        } else {
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            v.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * Show navigation bar.
     */
    public void showNavigationBar() {
        View v = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT < 19) {
            v.setSystemUiVisibility(View.VISIBLE);
        } else {
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            v.setSystemUiVisibility(uiOptions);
        }
    }
}

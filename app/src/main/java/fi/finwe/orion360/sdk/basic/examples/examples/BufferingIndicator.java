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
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.OrionViewConfig;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with a buffering indicator.
 * <p/>
 * A buffering indicator tells end user that the video player is currently loading
 * content and should start/continue soon. This example shows some tips on how to
 * implement it properly.
 * <p/>
 * To show and hide a buffering indicator when buffering occurs during video playback,
 * set OnBufferingStatusListener and show/hide an indeterminate progress bar accordingly.
 * <p/>
 * It can take a long time before Android MediaPlayer reports that buffering has started.
 * Hence, it is a good idea to show the buffering indicator immediately after calling
 * prepare() for a video view.
 * <p/>
 * Since the activity can get paused and resumed at any time, and the video playback is
 * usually auto-started when the player activity is resumed, it is often simplest
 * to show the buffering indicator in onResume().
 * <p/>
 * Some Android devices have a buggy implementation of buffering events and the 'buffering
 * stopped' event might never come in the case we are buffering the very beginning
 * of the video. To prevent buffering indicator for staying on screen forever, you can use a
 * handler that polls when the video playback has progressed and ensure that buffering
 * indicator gets removed.
 * <p/>
 * In VR mode, both eyes need a separate buffering indicator. Simple implementation is to
 * have both normal and VR mode indicators configured in the layout, and select which one
 * to use by toggling their visibilities. Remember to update the indicators when user
 * toggles between normal and VR mode.
 *
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
public class BufferingIndicator extends Activity {

    /** Tag for logging. */
    public static final String TAG = BufferingIndicator.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** Buffering indicator, for normal mode. */
    private ProgressBar mBufferingIndicator;

    /** Buffering indicator, for VR mode. */
    private LinearLayout mBufferingIndicatorVR;

    /** Handler for buffering indicators. */
    private Handler mBufferingIndicatorHandler;

    /** Polling interval for buffering indicator handler, in ms. */
    int mBufferingIndicatorInterval = 500;

    /** Gesture detector for tapping events. */
    private GestureDetector mGestureDetector;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Get buffering indicator and setup a handler for it.
        mBufferingIndicator = (ProgressBar) findViewById(R.id.buffering_indicator);
        mBufferingIndicatorVR = (LinearLayout) findViewById(R.id.buffering_indicator_vr);
        mBufferingIndicatorHandler = new Handler();

        // Listen for buffering events, and show/hide the buffering indicator accordingly.
        mOrionVideoView.setOnBufferingStatusListener(new OrionVideoView.OnBufferingStatusListener() {
            @Override
            public void onBufferingStarted(OrionVideoView orionVideoView) {
                showBufferingIndicator();
            }

            @Override
            public void onBufferFillRateChanged(OrionVideoView orionVideoView, int percentage) {
                Log.v(TAG, "Buffer: " + percentage + "%");
            }

            @Override
            public void onBufferingStopped(OrionVideoView orionVideoView) {
                hideBufferingIndicator();
            }
        });

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {
                mOrionVideoView.start();
            }
        });

        // Initialize Orion360 video view with a URI to an .mp4 video-on-demand stream.
        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.prepare(MainMenu.TEST_VIDEO_URI_1280x640);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Don't wait for 'buffering started' event; show buffering indicator right away.
        showBufferingIndicator();

        // Propagate all touch events from the video view to a gesture detector.
        mOrionVideoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }

        });

        // Toggle VR mode with long tapping.
        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {

                        // Show hint in VR mode.
                        if (mOrionVideoView.getCurrentConfigCopy().getTargetLayout()
                                == OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
                            String message = getString(R.string.player_long_tap_hint_exit_vr_mode);
                            Toast.makeText(BufferingIndicator.this, message, Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {

                        // Toggle between normal mode and VR mode.
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

        // We will automatically start/continue video playback when resumed,
        // hence we make also the buffering indicator visible now.
        showBufferingIndicator();

        // Hide buffering indicator when playback starts, even if device doesn't
        // properly notify that buffering has ended.
        mBufferingIndicatorRunnable.run();
    }

	@Override
	public void onPause() {
        // Propagate activity lifecycle events to Orion360 video view.
		mOrionVideoView.onPause();

        // Cancel buffering indicator handler (polling).
        mBufferingIndicatorHandler.removeCallbacks(mBufferingIndicatorRunnable);

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
     * Runnable for polling if video playback has already begun, and to hide buffering indicator.
     */
    Runnable mBufferingIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Checking if playback has started...");
            int newPosition = mOrionVideoView.getCurrentPosition();
            if (newPosition > 0) {
                Log.d(TAG, "Now playing video.");
                hideBufferingIndicator();
            } else if (mOrionVideoView.getState() != OrionVideoView.PlayingState.UNDEFINED){
                Log.d(TAG, "Still buffering.");
                mBufferingIndicatorHandler.postDelayed(mBufferingIndicatorRunnable,
                        mBufferingIndicatorInterval);
            }
        }
    };

    /**
     * Show buffering indicator, or toggle between normal and VR mode indicator.
     */
    private void showBufferingIndicator() {
        OrionViewConfig cfg = mOrionVideoView.getCurrentConfigCopy();
        if (cfg.getTargetLayout() == OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
            mBufferingIndicatorVR.setVisibility(View.VISIBLE);
            mBufferingIndicator.setVisibility(View.GONE);
        } else {
            mBufferingIndicatorVR.setVisibility(View.GONE);
            mBufferingIndicator.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide buffering indicator.
     */
    private void hideBufferingIndicator() {
        mBufferingIndicatorVR.setVisibility(View.GONE);
        mBufferingIndicator.setVisibility(View.GONE);
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

            // We need to hide the navigation bar.
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

        // Update buffering indicator type (normal or VR mode), if it is currently visible.
        if (mBufferingIndicator.getVisibility() == View.VISIBLE ||
                mBufferingIndicatorVR.getVisibility() == View.VISIBLE) {
            showBufferingIndicator();
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
}

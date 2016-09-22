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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.OrionViewConfig;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with touch input.
 * <p/>
 * Uses single tapping for toggling between normal and full screen view,
 * double tapping for toggling between video playback start and pause,
 * and long tapping for toggling between normal and VR mode.
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
public class TouchInput extends Activity {

    /** Tag for logging. */
    public static final String TAG = TouchInput.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

	/** Gesture detector for touch events. */
	private GestureDetector mGestureDetector;

	/** Play animation. */
	private Animation mPlayAnimation;

	/** Pause animation. */
	private Animation mPauseAnimation;

	/** Play overlay image. */
	private ImageView mPlayOverlay;

	/** Pause overlay image. */
	private ImageView mPauseOverlay;

	/** Flag for indicating if title bar is currently showing, or not. */
	private boolean mIsTitleBarShowing = false;

	/** Flag for indicating if navigation bar is currently showing, or not. */
	private boolean mIsNavigationBarShowing = false;

	/** Flag for indicating if video has been prepared already, or not. */
	private boolean mIsVideoPrepared = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_video_player);

		// Make sure title bar and navigation bar are shown initially.
		showTitleBar();
		showNavigationBar();

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {
				mIsVideoPrepared = true;

                // Start video playback.
                mOrionVideoView.start();

            }
		});

        // Initialize Orion360 video view with a URI to a local .mp4 video file.
        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
			mIsVideoPrepared = false;
            mOrionVideoView.prepare(MainMenu.PRIVATE_EXTERNAL_FILES_PATH
                    + MainMenu.TEST_VIDEO_FILE_MQ);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

		// To capture tapping events (from the whole video view area) with a gesture detector,
		// first propagate touch events from the video view to a gesture detector.
		mOrionVideoView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}

		});

		// Then, handle tapping and long press events. Recommended setup is as follows:
		// - Single tap in normal mode shows/hides other UI components (normal/fullscreen),
		//   and in VR mode shows a Toast hinting how to return to normal mode with a long press.
		// - Long tap in normal and VR mode toggles between normal and VR mode.
		// - Double tap in normal mode toggles play/pause (use animation to indicate event).
		mGestureDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {

						// Toggle title bar and navigation bar in normal mode, show hint in VR mode.
						String message;
						if (mOrionVideoView.getCurrentConfigCopy().getTargetLayout()
								!= OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
							toggleTitleBar();
							toggleNavigationBar();
						} else {
							message = getString(R.string.player_long_tap_hint_exit_vr_mode);
							Toast.makeText(TouchInput.this, message, Toast.LENGTH_SHORT).show();
						}

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

					@Override
					public boolean onDoubleTap(MotionEvent e) {

						// Play/pause video in normal mode.
						if (mOrionVideoView.getCurrentConfigCopy().getTargetLayout()
								!= OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
							if (mOrionVideoView.isPlaying()) {
								mOrionVideoView.pause();
								runPauseAnimation();
							} else if (mIsVideoPrepared) {
								mOrionVideoView.start();
								runPlayAnimation();
							}
						}

						return true;
					}

				});

		// Play overlay image and animation.
		mPlayOverlay = (ImageView) findViewById(R.id.play_overlay);
		mPlayAnimation = AnimationUtils.loadAnimation(this, R.anim.fast_fadeinout_animation);
		mPlayAnimation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				mPlayOverlay.setVisibility(View.GONE);
			}

		});

		// Pause overlay image and animation.
		mPauseOverlay = (ImageView) findViewById(R.id.pause_overlay);
		mPauseAnimation = AnimationUtils.loadAnimation(this, R.anim.fast_fadeinout_animation);
		mPauseAnimation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				mPauseOverlay.setVisibility(View.GONE);
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

			// We need to hide the title bar and navigation bar.
			hideTitleBar();
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

			// Show the title bar and navigation bar again.
			showTitleBar();
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
	 * Toggle title bar.
	 */
	public void toggleTitleBar() {
		if (mIsTitleBarShowing) {
			hideTitleBar();
		} else {
			showTitleBar();
		}
	}

	/**
	 * Show title bar.
	 */
	public void showTitleBar() {
		try {
			((View) findViewById(android.R.id.title).getParent())
					.setVisibility(View.VISIBLE);
		} catch (Exception e) {}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mIsTitleBarShowing = true;
	}

	/**
	 * Hide title bar.
	 */
	public void hideTitleBar() {
		try {
			((View) findViewById(android.R.id.title).getParent())
					.setVisibility(View.GONE);
		} catch (Exception e) {}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		mIsTitleBarShowing = false;
	}

	/**
	 * Toggle navigation bar.
	 */
	public void toggleNavigationBar() {
		if (mIsNavigationBarShowing) {
			hideNavigationBar();
		} else {
			showNavigationBar();
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
		mIsNavigationBarShowing = true;
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
		mIsNavigationBarShowing = false;
	}

	/**
	 * Run Play animation.
	 */
	public void runPlayAnimation() {
		mPlayOverlay.setVisibility(View.VISIBLE);
		mPlayOverlay.startAnimation(mPlayAnimation);
	}

	/**
	 * Run Pause animation.
	 */
	public void runPauseAnimation() {
		mPauseOverlay.setVisibility(View.VISIBLE);
		mPauseOverlay.startAnimation(mPauseAnimation);
	}
}

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

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
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
import android.widget.MediaController;
import android.widget.Toast;

import fi.finwe.math.QuatF;
import fi.finwe.math.Vec3F;
import fi.finwe.orion360.OrionSurfaceView;
import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.OrionViewConfig;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with touch input.
 * <p/>
 * For panning, zooming and rotating the view via swipe and pinch, see SensorFusion example.
 * <p/>
 * This example uses single tapping for toggling between normal and full screen view,
 * double tapping for toggling between video playback and pause states, and long tapping
 * for toggling between normal and VR mode rendering. These are tried-and-true mappings
 * that are recommended for all 360/VR video apps.
 * <p/>
 * Left and right edge of the video view have hidden tapping areas that seek the video 10
 * seconds backward and forward, respectively. This is just an example of mapping different
 * actions to tapping events based on touch position on screen, not a general recommendation.
 * <p/>
 * To showcase tapping inside the 3D scene, a hotspot is added to the video view and tapping
 * the hotspot area will trigger roll animation. Notice that with Orion360 SDK Basic, the
 * developer must manually combine hotspot and tapping near to its location, whereas Orion360
 * SDK Pro has built-in 3D objects and callbacks for their tapping and gaze selection events.
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

	/** Orion360 tag index for the hotspot object. */
	private static final int IDX_HOTSPOT = 0;

    /** Orion360 tag location vector for the hotspot object. */
    private static final Vec3F LOC_HOTSPOT = Vec3F.AXIS_FRONT;

	/** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

	/** Media controller. */
	private MediaController mMediaController;

	/** A value animator for rolling the hotspot object when touched. */
	private ValueAnimator mRotateAnimator;

	/** Gesture detector for tapping events. */
	private GestureDetector mGestureDetector;

	/** Play indicator animation. */
	private Animation mPlayAnimation;

	/** Pause indicator animation. */
	private Animation mPauseAnimation;

	/** Play indicator overlay image. */
	private ImageView mPlayOverlay;

	/** Pause indicator overlay image. */
	private ImageView mPauseOverlay;

	/** Flag for indicating if the title bar is currently showing, or not. */
	private boolean mIsTitleBarShowing = false;

	/** Flag for indicating if the navigation bar is currently showing, or not. */
	private boolean mIsNavigationBarShowing = false;

	/** Flag for indicating if video has been prepared already, or not. */
	private boolean mIsVideoPrepared = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Make sure title bar and navigation bar are shown initially.
        showTitleBar();
        showNavigationBar();

        // Set layout.
		setContentView(R.layout.activity_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

		// Create a media controller.
		mMediaController = new MediaController(this);

		// Set video view as anchor view; media controller positions itself on screen on top of it.
		mMediaController.setAnchorView(mOrionVideoView);

		// Set video view as media player; media controller interacts with it.
		mMediaController.setMediaPlayer(mOrionVideoView);

		// Add a hotspot to the video view; this must be done before video is prepared.
		addHotspot(IDX_HOTSPOT, LOC_HOTSPOT, MainMenu.PUBLIC_EXTERNAL_PICTURES_ORION_PATH +
				MainMenu.TEST_TAG_IMAGE_FILE_HQ, 0.25f, 0.90f);

        // Set up animator for rolling the hotspot 360 degrees.
        mRotateAnimator = ValueAnimator.ofFloat(0, (float)(2 * Math.PI));
        mRotateAnimator.setDuration(1000);
        mRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {

                QuatF rotation = QuatF.fromRotationAxisZ((float)animation.getAnimatedValue());
                mOrionVideoView.setTagRotation(IDX_HOTSPOT, rotation);

            }
        });

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {
                mIsVideoPrepared = true;

                // Start video playback.
                mOrionVideoView.start();

                // Show media controls, for a moment.
                showMediaControls();

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

		// To handle tapping events from the whole video view area with a gesture detector
        // (without caring about the position that user touched), propagate all touch events
		// from the video view to a gesture detector.
		mOrionVideoView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}

		});

		// In the gesture detector we handle tapping, double tapping and long press events.
        // The recommended mapping goes as follows:
		// - Single tap in normal mode shows/hides other UI components (normal/fullscreen mode),
		//   and in VR mode shows a Toast hinting how to return to normal mode with a long press.
		// - Long tap in normal and VR mode toggles between VR mode and normal mode, respectively.
		// - Double tap in normal mode toggles play/pause state (use animation to indicate event!)
		mGestureDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {

						// Toggle title bar, navigation bar and media controls in normal mode.
                        // Show hint in VR mode.
						if (mOrionVideoView.getCurrentConfigCopy().getTargetLayout()
								!= OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
							toggleTitleBar();
							toggleNavigationBar();
                            if (mIsTitleBarShowing) {
                                showMediaControls();
                            } else {
                                hideMediaControls();
                            }
						} else {
							String message = getString(R.string.player_long_tap_hint_exit_vr_mode);
							Toast.makeText(TouchInput.this, message, Toast.LENGTH_SHORT).show();
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

					@Override
					public boolean onDoubleTap(MotionEvent e) {

						// Toggle between play and pause states in normal mode.
						if (mOrionVideoView.getCurrentConfigCopy().getTargetLayout()
								!= OrionViewConfig.Layout.SPLIT_HORIZONTAL) {
							if (mOrionVideoView.isPlaying()) {
								mOrionVideoView.pause();
								runPauseAnimation();
							} else if (mIsVideoPrepared) {
								mOrionVideoView.start();
								runPlayAnimation();
							} else {
                                Log.i(TAG, "Attempt to start video before preparing it!");
                            }
						}

						return true;
					}

				});

		// To handle tapping events based on touch position on the video view widget
		// (i.e. 2D pixel coordinates relative to video view top left corner), use
        // OnClickListener for receiving ordinary motion events from the view.
		mOrionVideoView.setOnClickListener(new OrionSurfaceView.OnClickListener() {

			@Override
			public void onClick(OrionSurfaceView view, MotionEvent event) {

				// 2D touch position (X,Y) coordinates in pixels from inside the view,
				// excluding possible margin and padding ie. visible video area only.
				//Log.d(TAG, "onClick at " + event.getX() + ":" + event.getY());

				int hiddenTouchAreaWidth = (int)(0.10 * mOrionVideoView.getWidth());
				int leftBoundary = hiddenTouchAreaWidth;
				int rightBoundary = mOrionVideoView.getWidth() - hiddenTouchAreaWidth;
				if (event.getX() < leftBoundary) {

					// Try to seek 10 seconds backward. Notice that in reality we can
                    // only seek to nearest keyframe in the video!
					mOrionVideoView.seekTo(mOrionVideoView.getCurrentPosition() - 10000);

					Toast.makeText(TouchInput.this, "Seek -10 sec", Toast.LENGTH_SHORT).show();

				} else if (event.getX() > rightBoundary) {

					// Try to seek 10 seconds forward. Notice that in reality we can
                    // only seek to nearest keyframe in the video.
					mOrionVideoView.seekTo(mOrionVideoView.getCurrentPosition() + 10000);

					Toast.makeText(TouchInput.this, "Seek +10 sec", Toast.LENGTH_SHORT).show();

				}
			}
		});

		// To handle tapping events based on pointing direction inside the 3D scene
        // (e.g. 3D scene coordinates relative to 360 content) use OnPointingListener.
		mOrionVideoView.setOnPointingListener(new OrionSurfaceView.OnPointingListener() {

			@Override
			public void onPointingBegins(OrionSurfaceView view, PointF planeCoord,
										 Vec3F camCoord, Vec3F sceneCoord) {

				// 2D touch position (X,Y) coordinates in normalized coordinates
				// from inside the view, excluding possible margin and padding ie.
				// visible video area only. Normalized coordinates map longer side
				// of the view to range [-1,1] and shorter side the same but scaled
				// with aspect ratio. As an example, the edges of a 1000x500px view
				// yield left=-1, right=1, top=0.5, and bottom=-0.5.
				//Log.d(TAG, "onPointingBegins at " + planeCoord.x + ":" + planeCoord.y);

				// 3D touch position (x,y,z) coordinates in perspective (3D camera)
				// coordinates. Consider that the 3D camera is always at the origin
				// looking forward towards -Z axis, and right hand rule applies.
				// Therefore, touching the center of the viewport yields x=0,y=0,z=-1.
				// Touching 90 degrees to left means -X axis and yields x=-1,y=0,z=0,
				// and 90 degrees to right is naturally +X and yields x=1,y=0,z=0.
				// Then, 90 degrees to top means +Y axis and yields x=0,y=1,z=0
				// and 90 degrees to down x=0,y=-1,z=0. Notice that zooming the view
				// affects to 3D camera field-of-view and thus modifies the reach,
				// whereas rotating the content around the camera e.g. by panning
				// does not have any effect as 3D camera is stationary.
				//Log.d(TAG, "onPointingBegins at " + camCoord.x + ":" +
				//		camCoord.y + ":" + camCoord.z);

				// 3D touch position (x,y,z) coordinates in scene (content) coordinates.
				// Consider looking at a traditional classroom globe from the inside.
				// When you put your finger on top of a city, its coordinates
				// remain the same even though you roll the ball or tilt/move your
				// head. Scene coordinates work in a similar manner: the same point
				// in the content always returns the same coordinates when touched.
                // Some examples using equirectangular 360 content:
				// The center point of the original video/image yields x=0,y=0,z=-1.
				// Left and right edge on the center line wrap around and meet each
				// other at x=0,y=0,z=1. x=-1,y=0,z=0 can be found 1/4 to left from the
				// center point, and x=1,y=0,z=0 is 1/4 to right, respectively.
				// The top row of pixels diminishes to a single point, the zenith at
				// x=0,y=1,z=0 and the bottom row to nadir at x=0,y=-1,z=0.
				//Log.d(TAG, "onPointingBegins at " + sceneCoord.x + ":" +
				//		sceneCoord.y + ":" + sceneCoord.z);

				// If you wish to debug 3D scene touch position as latitude-longitude degrees,
				// you can use the following formula where image center = 0,0 and
				// latitude range [-90,90] and longitude range [-180,180]:
				// lat = atan(y/sqrt(x*x+z*z)) * 180 / pi
				// lon = atan2(x,-z) * 180 / pi
				Log.d(TAG, "onPointingBegins at " +
						Math.atan(sceneCoord.y / (0.0001 + Math.sqrt(sceneCoord.x *
								sceneCoord.x + sceneCoord.z * sceneCoord.z))) * 57.2957795
						+ "° lat, " + Math.atan2(sceneCoord.x, -sceneCoord.z) *
						57.2957795 + "° lon");

				// Rotate hotspot when pointed at (using a simple approximation for hit detection).
				float dist = 0.24f;
				if ((       Math.abs(LOC_HOTSPOT.x - sceneCoord.x) < dist)
						&& (Math.abs(LOC_HOTSPOT.y - sceneCoord.y) < dist)
						&& (Math.abs(LOC_HOTSPOT.z - sceneCoord.z) < dist) ) {

					if (!mRotateAnimator.isRunning()) {
						mRotateAnimator.start();
					}

				}
			}

			@Override
			public void onPointingEnds(OrionSurfaceView view, PointF planeCoord,
									   Vec3F camCoord, Vec3F sceneCoord) {

                // As both pointing begin and end events are offered, you can manipulate
                // hotspots e.g. via dragging them.

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
		// Cancel all animations before pausing the video view.
		mRotateAnimator.cancel();

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
	 * Adds a hotspot to the video view.
	 *
     * @param index The (positive) index for the hotspot to be created.
     * @param location The location of the hotspot given as a unit vector.
	 * @param imagePath The path to the image file to be used as the hotspot visualization.
	 * @param scale Scaling parameter for adjusting the size of the hotspot image.
	 * @param alpha Sets the transparency for the hotspot image.
	 */
	private void addHotspot(int index, Vec3F location, String imagePath, float scale, float alpha) {

		// Create a new tag to the video view, and give it a positive index as a parameter.
		// The index will be used later whenever the tag object needs to be manipulated.
		mOrionVideoView.createTag(index);

		// Set the location on the 360 image sphere where the tag will be drawn.
		mOrionVideoView.setTagLocation(index, location);

		// Set the PNG image file that will be drawn to the tag location.
		// Here we load the image from the file system as a bitmap (RGBA_8888 format).
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		if (null != bitmap) {
			mOrionVideoView.setTagBitmap(index, bitmap);
		} else {
			Log.e(TAG, "Could not decode bitmap " + imagePath);
		}

		// Set the size of the tag by scaling the image horizontally and vertically.
		mOrionVideoView.setTagScale(index, scale, scale);

		// Set the transparency of the tag by adjusting the image alpha between [0.0f-1.0f]
		mOrionVideoView.setTagAlpha(index, 1.0f);
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
	 * Toggle media controls.
	 */
	public void toggleMediaControls() {
		if (mMediaController.isShowing()) {
            hideMediaControls();
		} else {
            showMediaControls();
		}
	}

	/**
	 * Show media controls.
	 */
	public void showMediaControls() {
		mMediaController.show();
	}

	/**
	 * Hide media controls.
	 */
	public void hideMediaControls() {
		mMediaController.hide();
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

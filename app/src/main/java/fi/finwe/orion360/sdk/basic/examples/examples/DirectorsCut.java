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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.Map;

import fi.finwe.math.QuatF;
import fi.finwe.math.Vec3F;
import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.OrionViewConfig;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with forced view rotation control.
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
public class DirectorsCut extends Activity {

    /** Tag for logging. */
    public static final String TAG = DirectorsCut.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** Handler for video position checks. */
    private Handler mVideoPositionHandler = new Handler();

    /** Runnable for video position checks. */
    private Runnable mVideoPositionRunnable;

    /** Camera animator. */
    private ValueAnimator mCameraAnimator;

    /** Handler for camera animation. */
    private Handler mCameraAnimationHandler = new Handler();

    /** Runnable for camera animation. */
    private Runnable mCameraAnimationRunnable;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_minimal_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);


        /**
         * It is characteristic to 360 content that the end user is able to decide
         * where to look at, and that there is (hopefully interesting) content available
         * at all directions. While this freedom of exploration is one of the best treats
         * of consuming content in 360 degrees, it is also contrary to traditional video and
         * photo production where a director or a photographer decides the direction where
         * to aim the camera and how to frame the shot, and thus carefully leads the user
         * through the story by ensuring that she sees the relevant cues.
         * <p/>
         * In 360, most of the time it is desirable to let the end user be in charge of the
         * virtual camera ie. turn the device or pan the content with a finger. Yet there are
         * occasions where a decision needs to be made on behalf of the user. The primary
         * concern is that in case of video content the playback progresses at a constant pace,
         * and in order to keep up the rhythm the story telling must proceed as well - but at
         * the moment of a cut or a major event, the user may be looking at a 'wrong'
         * direction and hence miss important cues, making the storyline feel very confusing!
         * <p/>
         * The solution is to force the view to certain direction at a certain moment of time.
         * This is, of course, a tool that should not be used without a very good reason, and
         * that requires skill to do well.
         * <p/>
         * The first decision to be made is the very first frame of the video. There are
         * a few typical use cases:
         * <li>
         *     <ul/> Case A: The user is holding the device in hand at some random angle,
         *     but presumably at an orientation that feels comfortable to her. In this case,
         *     we want to rotate the view so that viewing begins from the center of the video,
         *     ie. from the 'front' direction of the content. The experience would be the same
         *     for a user who is sitting in a bus and looking down-forward to a device that lies
         *     on her hand that is resting on her knees, and for a user who is lying on a
         *     sofa and looking up-forward to a device held with a raised arm.
         *     This is also the default configuration for Orion360, and the developer needs
         *     to do nothing to accomplish this.
         *     <ul/> Case B: In case the director wants to make an artistic decision in the
         *     opening scene, she might want to force the view away from the 'front' direction
         *     of the content, to make the viewer first slightly confused and to find the 'front'
         *     direction where the action mostly takes place by panning the view there herself.
         *     This would be a rather rarely used effect and a variation of Case A.
         *     <ul/> Case C: If the user makes use of a VR frame to view the content, the
         *     solution presented in Case A is not appropriate. It is crucial that the nadir
         *     is aligned with user's perception of 'down' direction, and also the horizon
         *     line appears to be in its natural place.
         *     <ul/> Case D: Similar to Case B, the director may want to start from a certain
         *     viewing direction. However, when using a VR frame, only the yaw angle
         *     (azimuth/compass angle) should be enforced, to keep the content aligned with
         *     the user's perception of 'down' direction at all times.
         * </li>
         */

        OrionViewConfig cfg = mOrionVideoView.getCurrentConfigCopy();

        // Case A is the default, no need to do anything. When the player starts, the 'front'
        // direction ie. the center of the 360 video frame appears in view, despite of the
        // device's orientation. This is the same as setting the front direction by using
        // zeros for yaw, pitch and roll angles.
        //cfg.setDefaultViewRotationFromYawPitchRoll(0.0f, 0.0f, 0.0f);

        // Case B can be accomplished by setting the desired yaw and pitch (roll is
        // typically kept at 0). Here we begin by looking at back-down. Notice that
        // the backward order of the rotations must be taken into account when setting
        // the values (here: first 45 degrees up, then 180 degrees around the horizon).
        //cfg.setDefaultViewRotationFromYawPitchRoll(180.0f, 45.0f, 0.0f);

        // Case C can be accomplished by clearing the default value by setting it to null,
        // this way the view will be rotated based on sensor data only. If the device is
        // pointed down, the nadir of the image will show up. If the device is pointed
        // towards East, the 'right' direction of the image will show up (as long as
        // the magnetometer sensor has been properly calibrated on the target hardware).
        cfg.setDefaultViewRotation(null);

        try {
            mOrionVideoView.applyConfig(cfg);
        } catch (OrionViewConfig.NotSupportedException e) {
            Log.e(TAG, "Selected configuration is not supported!", e);
        }

        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {

                // Case D can be accomplished by first performing Case C (ie. clearing the
                // default view orientation with sensor data), and then here in onPrepared(),
                // when the sensors have stabilized, applying the new desired yaw offset.
                Toast.makeText(DirectorsCut.this, "Initial orientation",
                        Toast.LENGTH_SHORT).show();
                setYaw(180.0f);

                // Start playback when the player has initialized itself and buffered
                // enough video frames.
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

        /**
         * After the question of the initial viewing rotation is settled, the director may
         * want to add some additional forced viewing directions. The most suitable places
         * are when there is a cut and the viewer is taken to another place and time anyway -
         * it is not that disturbing if also the viewing direction is re-oriented at the same
         * exact moment.
         * <p/>
         * In order to perform such operations during video playback, we need to listen to the
         * video position and check when a predefined moment of time has been reached.
         * Unfortunately, the Android media player backend does not provide frame numbers,
         * and even video position must be queried via polling.
         */
        final LinkedHashMap<Integer, Float> cutList = new LinkedHashMap<>();
        cutList.put(5000,  0.0f);      // At  5s, look at front
        cutList.put(10000, 90.0f);     // At 10s, look at right
        cutList.put(15000, 180.0f);    // At 15s, look at back
        cutList.put(20000, 270.0f);    // At 20s, look at left

        final int videoFramesPerSecond = 25; // Used for controlling the polling delay.

        mVideoPositionRunnable = new Runnable() {
            @Override
            public void run() {

                // Get video player position. Notice that this method has some delay.
                int position = mOrionVideoView.getCurrentPosition();

                // Compare position to the defined set of cuts.
                Map.Entry<Integer, Float> cut = null;
                for (Map.Entry<Integer, Float> entry : cutList.entrySet()) {
                    if (position >= entry.getKey()) {
                        cut = entry;
                        break;
                    }
                }
                if (null != cut) {

                    // Cut found, rotate view to the defined yaw angle.
                    Toast.makeText(DirectorsCut.this, "Cut Position=" + position
                                    + " Time=" + cut.getKey() + " Angle=" + cut.getValue(),
                            Toast.LENGTH_SHORT).show();
                    setYaw(cut.getValue());

                    // This cut has now been handled, remove it from the cut list.
                    cutList.remove(cut.getKey());
                }

                // Continue polling video position as long as there are cuts in the list.
                if (cutList.size() > 0) {

                    // Check video position again after the next frame.
                    mVideoPositionHandler.postDelayed(mVideoPositionRunnable,
                            1000/videoFramesPerSecond);

                }
            }
        };

        // Start video position checks.
        mVideoPositionHandler.postDelayed(mVideoPositionRunnable, 1000/videoFramesPerSecond);

        /**
         * Finally, the director may want to perform animated camera operations,
         * such as panning and zooming. These are somewhat controversial, but feel
         * fairly good when the user is not taken completely out of control. Hence,
         * we perform the panning and zooming as small animated steps by always
         * modifying the latest value towards the target, thus allowing simultaneous
         * control by user.
         */

        // Wait until the hard cuts demo is over, then proceed to camera animation.
        mCameraAnimationRunnable = new Runnable() {
            @Override
            public void run() {

                // Proceed to camera animation, see down below.
                Toast.makeText(DirectorsCut.this, "Camera Animation",
                        Toast.LENGTH_SHORT).show();
                animateYaw(0.0f, 2000);

            }
        };

        mCameraAnimationHandler.postDelayed(mCameraAnimationRunnable, 25000);
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

        // Stop video position checks.
        mVideoPositionHandler.removeCallbacks(mVideoPositionRunnable);

        // Stop camera animation.
        mCameraAnimationHandler.removeCallbacks(mCameraAnimationRunnable);
        if (null != mCameraAnimator) {
            mCameraAnimator.cancel();
        }

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
     * Rotate the view to a given yaw angle, preserving current pitch and roll angles.
     *
     * @param yawDeg The target yaw angle, in degrees.
     */
    private void setYaw(float yawDeg) {

        // Get current orientation.
        QuatF currentOrientation = mOrionVideoView.getViewingRotation();

        // Create a vector showing where in the sphere the user is currently looking at.
        Vec3F lookingAt = Vec3F.AXIS_FRONT.rotate(currentOrientation.conjugate());

        // Get current yaw offset with respective to content 'front' direction in rads.
        float currentYawRad = lookingAt.getYaw();

        // Convert target yaw angle from degrees to radians.
        float newYawRad = (float) (Math.PI / 180.0f * yawDeg);

        // Create a rotation that replaces the current yaw offset with the desired yaw offset.
        QuatF rotation = QuatF.fromRotationAxisY(newYawRad - currentYawRad);

        // Rotate the view to a new orientation.
        QuatF newOrientation = currentOrientation.multiply(rotation);
        mOrionVideoView.setViewingRotation(newOrientation);

    }

    /**
     * Animate the view to a given yaw angle, preserving current pitch and roll angles.
     *
     * @param yawDeg The target yaw angle, in degrees.
     * @param durationMs The length of the animation in milliseconds.
     */
    private void animateYaw(float yawDeg, long durationMs) {

        // Get current orientation.
        QuatF currentOrientation = mOrionVideoView.getViewingRotation();

        // Create a vector showing where in the sphere the user is currently looking at.
        Vec3F lookingAt = Vec3F.AXIS_FRONT.rotate(currentOrientation.conjugate());

        // Get current yaw offset with respective to content 'front' direction in rads.
        float currentYawRad = lookingAt.getYaw();

        // Convert target yaw angle from degrees to radians.
        float newYawRad = (float) (Math.PI / 180.0f * yawDeg);

        // Setup camera animator.
        mCameraAnimator = ValueAnimator.ofFloat(currentYawRad, newYawRad);
        mCameraAnimator.setRepeatCount(0);
        mCameraAnimator.setRepeatMode(ValueAnimator.RESTART);
        mCameraAnimator.setDuration(durationMs);
        mCameraAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {

                // Rotate the camera to the desired yaw angle. Notice that each
                // animation step will get the yaw angle from the camera animator,
                // but pitch and roll will be checked again at each animation step,
                // hence allowing user control for these angles during the animation.
                float animatedValue = (Float) animation.getAnimatedValue();
                float yawDeg = (float) (animatedValue * 180.0f / Math.PI);
                setYaw(yawDeg);

            }
        });
        mCameraAnimator.start();
    }

}

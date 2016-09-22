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
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import fi.finwe.math.QuatF;
import fi.finwe.math.Vec3F;
import fi.finwe.orion360.OrionSensorFusion;
import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.OrionViewConfig;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with sensor fusion control.
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
public class SensorFusion extends Activity implements OrionSensorFusion.Listener {

    /** Tag for logging. */
    public static final String TAG = SensorFusion.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_minimal_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

		/**
		 * By default, the 360 view is automatically rotated based on device orientation.
		 * Hence, to look at a desired direction, user can turn the device towards it,
		 * or when viewing through a VR frame, simply turn her head.
		 * <p/>
		 * This feature requires movement sensors - most importantly, a gyroscope. Not all
		 * Android devices have one. The feature is automatically disabled on devices that
		 * do not have the necessary sensor hardware, with a fallback to touch-only control.
		 * Hence, the application developer does not need to worry about this. An API to
         * disable automatic view rotation is still provided - for example, if you wish
		 * to use a custom sensor fusion algorithm.
		 */
		//mOrionVideoView.setOrientationSensorControlEnabled(false);

        /**
         * The supported movement sensors include
         * <li>
         * <ul/>an accelerometer, which tells where is 'Down' direction, and linear acceleration
         * <ul/>a magnetometer, which tells where is 'North' direction, and slow rotation
         * <ul/>a gyroscope, which tells the rotation about the device's own axis very rapidly
         * </li>
         * Using data from all the three sensors, a sophisticated sensor fusion algorithm
         * calculates device orientation several hundreds of times per second.
         * <p/>
         * While gyroscope and accelerometer are absolutely required for proper operation,
         * the magnetometer sensor is optional. Frequently, it is not necessary to align
         * the 360 content with the point of the compass - instead, the view is rotated
         * so that viewing begins from the content 'front' direction, despite of the end
         * user's initial orientation with respect to North.
         * <p/>
         * Some VR frames (especially the 1st generation Google Cardboards) have a magnetic
         * switch that confuses the magnetometer reading just like a magnetic object confuses
         * a compass. Furthermore, some devices have poor magnetometer calibration; the typical
         * symptom is that the view begins to rotate horizontally at a constant slow rate
         * even though the device is held still, or refuses to turn to some directions.
         * While manual calibration often helps (draw an 8-figure in the air a few times using
         * the device as a pen, simultaneously rotating it along its axis), end users are not
         * aware of this. Moreover, the device may need to be re-calibrated when the magnetic
         * environment changes - for example, the device is placed near metallic objects or
         * strong electrical currents.
         * <p/>
         * As a conclusion, it is often better to disable the magnetometer. The recommended
         * place to do that is inside onPrepared() callback; this is to ensure that the
         * sensor fusion gets a few samples from the magnetometer before it is disabled,
         * and thus stabilizes faster.
         */
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {

                // You can disable the magnetometer here if you don't need to align the 360
                // content horizontally with the end user surroundings (to overcome some
                // magnetometer sensor issues).
                mOrionVideoView.setOrientationMagnetometer(false);

                // Start playback when the player has initialized itself and buffered
                // enough video frames.
                mOrionVideoView.start();

            }
        });

        /**
         * The sensor fusion algorithm is also responsible for merging end user's touch
         * input drag events with the orientation calculated from movement sensor data
         * (touch tapping events are handled separately, see TouchInput example).
         * <p/>
         * Many sensor fusion algorithms on the market have severe shortcomings in this
         * particular task; most notably they prevent panning the 360 view over the nadir
         * and zenith directions, thus creating an artificial limit in navigation. This
         * is annoying to users especially when viewing drone shots where most of the
         * action occurs down below and panning to different directions near the nadir
         * is very common. Also the basic principle of panning with touch - keeping the
         * image position where user initially touched under the finger all the time -
         * is broken near nadir and zenith directions in most of the algorithms.
         * <p/>
         * The underlying reason is their failure to solve the problem of tilted horizon
         * properly. Consider a case where user is looking straight ahead towards the
         * horizon line, then pans straight down to nadir, and finally straight to left
         * or right, up to the horizon level. The horizon now appears 90 degrees tilted.
         * The easy solution is to forget the mapping between the fingertip and touched
         * image position, apply left-right finger movement directly to panning the yaw
         * angle full 360 degrees, and then apply the up-down finger movement to panning
         * the pitch angle ONLY from -90 (nadir) to +90 (zenith), clamping any further
         * panning attempts to these limits - and thus creating the before said issues.
         * <p/>
         * Orion360 sensor fusion solves this with a special auto-horizon aligner
         * algorithm, which silently steps in and gently rotates the view so that the
         * horizon gets aligned again. This unique approach is mathematically much more
         * complex, but provides a very good user experience. The feature fuses touch
         * and movement sensor controls perfectly together, and allows panning freely
         * within the whole image sphere without any artificial limits - the way 360
         * content should be experienced.
         * <p/>
         * This feature is always enabled, along with basic panning with touch.
         */

        /**
         * Most of the touch screens are able to detect at least two fingers on screen
         * simultaneously, allowing two additional drag-type gestures to be recognized:
         * two-finger pinch, and two-finger rotate. Still following the basic principle
         * of touch control, the sensor fusion algorithm aims for keeping the image
         * position where the fingers initially touched under the fingers all the time,
         * by means of panning, zooming and rotating the image.
         * <p/>
         * As usual, the pinch gesture is used for zooming, which in case of 360 content
         * means changing the virtual camera field-of-view (FOV). The developer should set
         * reasonable limits to zooming based on his content: consider your resolution
         * and set a limit for zooming in so that image does not get too blurry, and
         * based on your content type, set a limit for zooming out so that image
         * distortion at the corners of the screen does not get too distractive.
         * <p/>
         * Configure the FOV by setting the default value, the minimum value, and
         * the maximum value. The default value must be between the minimum and
         * the maximum. The user is then able to use pinch gesture to change the
         * FOV between the defined minimum and maximum.
         * <p/>
         * In order to prevent zooming altogether, simply set all three values to
         * the same value that you wish to enforce. Typically in VR mode the FOV
         * is enforced.
         */
        OrionViewConfig cfg = mOrionVideoView.getCurrentConfigCopy();
        cfg.setFov(90.0f, 45.0f, 110.0f); // default, minimum, maximum
        try {
            mOrionVideoView.applyConfig(cfg);
        } catch (OrionViewConfig.NotSupportedException e) {
            Log.e(TAG, "Configuration is not supported!", e);
        }

        /**
         * The two-finger rotate gesture is mapped to rotating the content along the roll
         * axis. This is handy especially when viewing content at the nadir direction
         * and if applied temporarily. However, it can be confusing if applied elsewhere
         * or left in use - for example, to tilt the horizon permanently (overrides
         * auto-horizon aligner).
         * <p/>
         * For consistency, the feature works everywhere within the 360 content, not just
         * near nadir or zenith. The reasoning is that if user has found the gesture and
         * rolled the view to one direction, she has already learned to operate it and
         * can easily roll the view back and forth as she pleases.
         * <p/>
         * Consider your content and user group. If the feature is not needed, it can be
         * disabled with a simple API call.
         */
        //mOrionVideoView.setPinchRotationEnabled(false);

        /**
         * It is possible to listen for sensor fusion data, ie. device orientation
         * changes (a quaternion) and device display rotation changes (portrait,
         * landscape, reverse portrait, reverse landscape in terms of degrees).
         * <p/>
         * A reasonable place to register a listener is at onResume(). Remember to
         * unregister your listener at onPause(), and ensure that your implementation
         * of the callbacks return quickly, as sensor fusion runs at a high data rate,
         * typically about 200 Hz (depends on hardware).
         * <p/>
         * In this example, we simply print the orientation change values to logcat
         * (see the callbacks down below). For a couple of practical examples of using
         * sensor fusion data in your application, see NadirPatch and InteractiveHotspots
         * examples.
         */

        // Initialize Orion360 video view with a URI to a local .mp4 video file.
        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.prepare(MainMenu.PRIVATE_EXTERNAL_FILES_PATH
                    + MainMenu.TEST_VIDEO_FILE_MQ);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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

        // Start listening for sensor fusion events.
        mOrionVideoView.registerOrientationChangeListener(this);

	}

	@Override
	public void onPause() {

        // Stop listening for sensor fusion events.
        mOrionVideoView.unregisterOrientationChangeListener(this);

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

    @Override
    public void onDeviceOrientationChanged(QuatF orientation) {

        // Rotate front vector to the direction where the user is currently looking at.
        Vec3F lookAt = Vec3F.AXIS_FRONT.rotate(orientation.conjugate());

        // Get the yaw offset with respective to the 360 image center.
        float lookAtYaw = lookAt.getYaw();

        // Get the pitch offset with respective to the 360 image center.
        float lookAtPitch = lookAt.getPitch();

        float toDegree = (float) (180.0f / Math.PI);

        Log.d(TAG, "Looking at yaw=" + lookAtYaw * toDegree
                + " pitch=" + lookAtPitch * toDegree);
    }

    @Override
    public void onDeviceDisplayRotationChanged(int rotationDegrees) {

        /**
         * Notice that in the manifest we have not defined orientation for this activity,
         * to allow display rotation changes. Normally it is a good practice to lock the
         * 360 view to landscape orientation, especially if VR frames are to be supported.
         * <p/>
         * Since the content within the 360 view gets rotated automatically continuously,
         * there is little reason to rotate the whole activity at certain specific angles.
         * On the contrary, the platform rotation animations that occur when the activity
         * gets re-created for orientation change, can feel disturbing.
         * <p/>
         * There is one exception - if you have controls drawn on the screen simultaneously
         * with the 360 view, these will get rotated accordingly only if you allow the
         * whole activity to turn. The decision is yours and depends on the application.
         */
        Log.d(TAG, "Display rotation: " + rotationDegrees);

    }
}

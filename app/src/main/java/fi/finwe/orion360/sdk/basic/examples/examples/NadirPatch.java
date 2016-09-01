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
import android.widget.Toast;

import fi.finwe.math.QuatF;
import fi.finwe.math.Vec3F;
import fi.finwe.orion360.OrionSensorFusion;
import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with a nadir patch image.
 * <p/>
 * Nadir patch is frequently used when 360 photo or video is captured with a camera setup
 * that does not cover the full sphere (360x180). The purpose is to cover the hole in the
 * natural direction with content producer or customer brand logo.
 * <p/>
 * Orion360 allows adding 2D image panes to the 3D world; these are called tags. Adding a
 * tag only requires a path to the image file, a direction vector, and a scale factor -
 * the image pane is automatically set at the proper distance from the origin and kept
 * facing to user at all times.
 * <p/>
 * This example uses the tag feature to add a nadir patch image on top of the video layer.
 * While a tag image pane is always a rectangular area, the PNG image file format with
 * alpha channel allows drawing non-rectangular shapes, here a circular patch.
 * <p/>
 * Orion360 tags must be created during view initialization, but they can be manipulated
 * later. Here a standard Android object animator is used for fading in the patch image
 * when the video playback begins. It is also possible to use current viewing direction as
 * an input for tag manipulation, as shown here by keeping the patch upright at all times.
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
public class NadirPatch extends Activity {

    /** Tag for logging. */
    public static final String TAG = NadirPatch.class.getSimpleName();

    /** Orion360 tag index for the nadir patch object. */
    private static final int IDX_NADIR_PATCH = 0;

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** A value animator for fading in the nadir patch when video playback begins. */
    private ValueAnimator mFadeInOutAnimator;

    /** Flag for configuring nadir auto rotate on, or off. */
    private boolean mKeepNadirPatchUpright = true;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_minimal_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Add a nadir patch to the video view. All tags must be created before calling prepare()!
        addNadirPatch(getString(R.string.asset_nadir_patch), 0.55f, 0.90f);

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {

                // Start video playback.
                mOrionVideoView.start();

                // Fade in the nadir patch now.
                mFadeInOutAnimator.start();

                // If nadir patch auto-rotate is enabled, start it now.
                if (mKeepNadirPatchUpright) {
                    startNadirPatchAutoRotate();
                }

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
        mFadeInOutAnimator.cancel();

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
     * Adds a nadir patch image to the video view.
     *
     * @param imagePath The path to the image file to be used as nadir patch.
     * @param scale Scaling parameter for adjusting the size of the patch.
     * @param alpha Sets the transparency for the patch.
     */
    private void addNadirPatch(String imagePath, float scale, float alpha) {

        // Create a new tag to the video view, and give it a positive index as a parameter.
        // The index will be used later whenever the tag object needs to be manipulated.
        mOrionVideoView.createTag(IDX_NADIR_PATCH);

        // Set the location on the 360 image sphere where the tag will be drawn (nadir=down).
        mOrionVideoView.setTagLocation(IDX_NADIR_PATCH, Vec3F.AXIS_DOWN);

        // Set the JPG/PNG image file that will be drawn to the tag location.
        mOrionVideoView.setTagAssetFilename(IDX_NADIR_PATCH, imagePath);

        // Set the size of the tag by scaling the image horizontally and vertically.
        mOrionVideoView.setTagScale(IDX_NADIR_PATCH, scale, scale);

        // Set the transparency of the tag by adjusting the image alpha between [0.0f-1.0f]
        // Here we initialize it to zero to initially hide the tag...
        mOrionVideoView.setTagAlpha(IDX_NADIR_PATCH, 0.0f);

        // ... and then use an object animator to fade in the tag later when animator is started.
        initFadeInOutAnimator(IDX_NADIR_PATCH, 0.0f, alpha, 3000);
    }

    /**
     * Fades in/out the tag referenced by its index.
     *
     * @param tag The index of the tag to be faded in/out.
     * @param from The alpha value where to start from.
     * @param to The alpha value where to end to.
     * @param durationMs The duration of the animation in milliseconds.
     */
    private void initFadeInOutAnimator(final int tag, float from, float to, long durationMs) {
        mFadeInOutAnimator = ValueAnimator.ofFloat(from, to);
        mFadeInOutAnimator.setDuration(durationMs);
        mFadeInOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {

                mOrionVideoView.setTagAlpha(tag, (Float) animation.getAnimatedValue());

            }
        });
    }

    /**
     * Auto-rotates the nadir patch to keep it always upright.
     */
    private void startNadirPatchAutoRotate() {
        mOrionVideoView.registerOrientationChangeListener(new OrionSensorFusion.Listener() {

            @Override
            public void onDeviceOrientationChanged(QuatF orientation) {

                // Rotate front vector to the direction where the user is currently looking at.
                Vec3F gazeAt = Vec3F.AXIS_FRONT.rotate(orientation.conjugate());

                // Get the yaw offset with respective to the 360 image front direction.
                float gazeYaw = gazeAt.getYaw();

                // Negate it and convert to quaternion; this rotation keeps the patch upright.
                QuatF yawCompensation = QuatF.fromRotationAxisY(-gazeYaw);

                // Tag rotation naturally affects to tag location as well, hence we must rotate
                // the nadir patch from the default front location to the bottom again.
                QuatF toNadir = QuatF.fromRotationAxisX(Vec3F.AXIS_DOWN.getPitch());

                // Combine the two rotations and mind the multiplication (rotation) order!
                QuatF total = yawCompensation.multiply(toNadir);

                // Apply to the nadir patch tag.
                mOrionVideoView.setTagRotation(IDX_NADIR_PATCH, total);

            }

            @Override
            public void onDeviceDisplayRotationChanged(int rotationDegrees) {}
        });
    }
}

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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.OrionViewConfig;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with doughnut video configuration.
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
public class Doughnut extends Activity {

    /** Tag for logging. */
    public static final String TAG = Doughnut.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;


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

            // This video is not full spherical, but a doughnut shape:
            // 360x135 degrees where 22.5 degrees is cropped from top and bottom...
            mOrionVideoView.prepare(MainMenu.TEST_VIDEO_URI_1920x720);

            // Hence, we setup a corresponding video surface.
            setupDoughnutMesh();

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
     * Configure doughnut shape video surface mesh for a doughnut shape video source.
     */
    private void setupDoughnutMesh() {
        OrionViewConfig cfg = mOrionVideoView.getCurrentConfigCopy();

        // In case captured video is not full spherical i.e. cover 360x180 degrees,
        // it is necessary to limit the video rendering surface to a matching part
        // of a sphere.

        // First we set mesh horizontal and vertical field-of-view (FOV).
        // Here we define the horizontal limit to the usual 360 degrees,
        // but vertical limit to just 135 degrees, centered around the horizon.
        // This will produce a doughnut shape polygon instead of a sphere.
        cfg.setSphereMeshHorizontalLimits(-180.0f, 180.0f);
        cfg.setSphereMeshVerticalLimits(67.5f, -67.5f);

        // Mesh density defines the video surface complexity as the number of vertices,
        // here given simply as the desired number of rows and columns. Good rule of
        // thumb is to have one column / row for each 6 degrees of FOV. In case of
        // 360x135 doughnut, that yields 60 columns and 23 rows.
        cfg.setSphereMeshDensity(60, 23);

        // Texture mapping defines how the source texture will be mapped to the video surface.
        // First you choose the area of the polygon mesh that you want to cover with texture,
        // by giving the indices of the column and row where mapping begins and then its span
        // in terms of number of columns and rows. Next you choose what part of the source
        // texture shall be mapped there, by giving UV coordinate values [0,1] that define a
        // rectangular area from a video frame.
        cfg.resetSphereMeshParts(); // Remember to clear default mapping first.
        cfg.setSourceLayout(OrionViewConfig.Layout.FULL);
        cfg.addSphereMeshPart(
                0, 0, 60, 23,				// cover full mesh...
                0.0f, 0.0f, 1.0f, 1.0f);	// ... with full video frame

        // Limit viewing vertically so that user can't navigate to areas that don't have content.
        cfg.resetViewableSceneLimits();
        cfg.setViewableSceneLimitTop(67.5f);
        cfg.setViewableSceneLimitBottom(-67.5f);

        // Lock zooming to selected FOV. When device is rolled between portrait and landscape,
        // zooming is automatically adjusted to keep black areas out of view (mostly at least,
        // there are currently 16 control points at the edges of the viewport). You can improve
        // this by setting vertical viewing limits slightly smaller than vertical FOV used in
        // mesh configuration.
        cfg.setFov(100, 100, 100);

        // Optionally, you can lock the roll angle by disabling the part of the sensor fusion
        // algorithm that observes which direction is down. This approach should be considered
        // as an alternative to default operation where roll angle is not locked and black area
        // is kept hidden by automatically zooming a little when device is rolled.
        // This mode is not suitable for VR.
        //mOrionVideoView.setViewportDownRotationUpdatesEnabled(false);

        try {
            mOrionVideoView.applyConfig(cfg);
        } catch (OrionViewConfig.NotSupportedException e) {
            Log.e(TAG, "Custom mesh configuration is invalid");
        }
    }
}

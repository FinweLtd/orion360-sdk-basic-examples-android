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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with a preview image.
 * <p/>
 * The preview image is a full-size equirectangular image overlay on top of the video layer.
 * Notice the difference to tags, which are origin-facing rectilinear images that cover only
 * a part of the video layer. The preview image should be of the same resolution than the main
 * video or image that it is applied to, while tags can be of any resolution.
 * <p/>
 * Similar to tags, the alpha value of the preview image can be freely adjusted. Therefore it
 * is possible to completely cover the video layer, add a semi-transparent layer, and cross-fade
 * between image and video (or two images when using OrionImageView instead of OrionVideoView).
 * With a PNG image that has transparent areas, only selected parts of the video can be covered.
 * <p/>
 * The typical use case is to add a preview image that is shown in the beginning while the
 * video is still being buffered from the network. For example, the image could contain a
 * brand logo, instructions for panning and zooming within 360 view, or a reminder about
 * placing the device inside a VR frame.
 * <p/>
 * However, the feature is actually much more versatile than that. Here are a few ideas:
 * - Show an image also when the video completes to thank users for watching and to
 *   instruct what to do next.
 * - If you have a playlist, show a hero image while buffering next video.
 * - Show an image when user pauses the video, when the player stops for buffering, or when
 *   network connection issues or other problems occur.
 * - Dim video easily by adjusting preview image alpha and NOT setting a preview image at all.
 * - Add a color overlay FX with a single-color preview image and a small alpha value.
 * - Show dynamically loaded ads during video playback.
 * - Create a slideshow with cross-fade effect using OrionImageView and an audio track.
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
public class PreviewImage extends Activity {

    /** Tag for logging. */
    public static final String TAG = PreviewImage.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** Buffering indicator, to be shown while buffering video from the network. */
    private ProgressBar mBufferingIndicator;

    /** Handler for buffering indicators. */
    private Handler mBufferingIndicatorHandler;

    /** Polling interval for buffering indicator handler, in ms. */
    int mBufferingIndicatorInterval = 500;

    /** Cross-fade animator. */
    private ValueAnimator mCrossfadeAnimator;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Get buffering indicator and setup a handler for it.
        mBufferingIndicator = (ProgressBar) findViewById(R.id.buffering_indicator);
        mBufferingIndicatorHandler = new Handler();

        // Listen for buffering events, and show/hide the buffering indicator accordingly.
        // For a better example, see BufferingIndicator example.
        mOrionVideoView.setOnBufferingStatusListener(new OrionVideoView.OnBufferingStatusListener() {
            @Override
            public void onBufferingStarted(OrionVideoView orionVideoView) {
                mBufferingIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBufferFillRateChanged(OrionVideoView orionVideoView, int percentage) {
                Log.v(TAG, "Buffer: " + percentage + "%");
            }

            @Override
            public void onBufferingStopped(OrionVideoView orionVideoView) {
                mBufferingIndicator.setVisibility(View.GONE);
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
            mOrionVideoView.prepare(MainMenu.TEST_VIDEO_URI_1920x960);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Initialize Orion360 preview image with a URI to a .jpg image file.
        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.setPreviewImagePath(MainMenu.PRIVATE_EXTERNAL_FILES_PATH
                    + MainMenu.TEST_PREVIEW_IMAGE_FILE_MQ);
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

        // Preview image is an overlay on top of the video layer. Set preview image fully
        // opaque (and completely cover the video layer) with alpha value 1.0, hide the
        // preview image with alpha value 0.0, or blend with video using alpha value in
        // range (0.0-1.0). Here we start by covering the video layer with a preview image.
        // Notice that the video layer is black until the first video frame gets buffered,
        // decoded and renderer. When video playback begins, cross-fade animation is run.
        mOrionVideoView.setPreviewAlpha(1.0f);

        // Hide buffering indicator when playback starts, even if device doesn't
        // properly notify that buffering has ended.
        mBufferingIndicatorRunnable.run();
	}

	@Override
	public void onPause() {

        // Cancel animations.
        if (null != mCrossfadeAnimator) {
            mCrossfadeAnimator.cancel();
        }

        // Cancel buffering indicator handler (polling).
        mBufferingIndicatorHandler.removeCallbacks(mBufferingIndicatorRunnable);

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
     * Runnable for polling if video playback has already begun.
     */
    Runnable mBufferingIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Checking if playback has started...");
            int newPosition = mOrionVideoView.getCurrentPosition();
            if (newPosition > 0) {
                Log.d(TAG, "Now playing video.");
                mBufferingIndicator.setVisibility(View.GONE);
                crossFade(1.0f, 0.0f, 1000);
            } else if (mOrionVideoView.getState() != OrionVideoView.PlayingState.UNDEFINED){
                Log.d(TAG, "Still buffering.");
                mBufferingIndicatorHandler.postDelayed(mBufferingIndicatorRunnable,
                        mBufferingIndicatorInterval);
            }
        }
    };

    /**
     * Cross-fade between preview image (alpha=1.0) and video layer (alpha=0.0).
     *
     * @param beginAlpha Alpha value where to start.
     * @param endAlpha Alpha value where to end.
     * @param durationMs The duration of the animation, in milliseconds.
     */
    private void crossFade(float beginAlpha, float endAlpha, long durationMs) {
        mCrossfadeAnimator = ValueAnimator.ofFloat(beginAlpha, endAlpha);
        mCrossfadeAnimator.setRepeatCount(0);
        mCrossfadeAnimator.setRepeatMode(ValueAnimator.RESTART);
        mCrossfadeAnimator.setDuration(durationMs);
        mCrossfadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {

                float animatedValue = (Float) animation.getAnimatedValue();
                mOrionVideoView.setPreviewAlpha(animatedValue);

            }
        });
        mCrossfadeAnimator.start();
    }
}

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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import fi.finwe.math.QuatF;
import fi.finwe.math.Vec2F;
import fi.finwe.math.Vec3F;
import fi.finwe.orion360.OrionSensorFusion;
import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with interactive hotspots.
 * <p>
 * Hotspots are implemented using Orion360 tag feature (see Nadir Patch example).
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
public class InteractiveHotspots extends Activity {

    /** Tag for logging. */
    public static final String TAG = InteractiveHotspots.class.getSimpleName();

    /** Orion360 tag index for the reticle. */
    public static final int IDX_HOTSPOT_RETICLE = 99;

    /** Orion360 tag index for the start hotspot object at the FRONT direction. */
    public static final int IDX_HOTSPOT_START_FRONT = 1;

    /** Orion360 tag index for the start hotspot object at the RIGHT direction. */
    public static final int IDX_HOTSPOT_START_RIGHT = 2;

    /** Orion360 tag index for the start hotspot object at the BACK direction. */
    public static final int IDX_HOTSPOT_START_BACK = 3;

    /** Orion360 tag index for the start hotspot object at the LEFT direction. */
    public static final int IDX_HOTSPOT_START_LEFT = 4;

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

	/** An array for the hotspot objects. */
	private ArrayList<Hotspot> mHotspots = new ArrayList<Hotspot>();

    /** Hotspot used as a gaze-selectable start button. */
    private class StartHotspot extends AnimatedSelectableHotspot {

        /**
         * Constructor.
         *
         * @param index The Orion360 tag index to be set for this hotspot instance.
         * @param location The location where to place this hotspot instance.
         */
        public StartHotspot(int index, Vec3F location) {
            super(index);

            // Configure basic hotspot features.
            this.location = location;
            this.imageFilename = getString(R.string.asset_hotspot_start);
            this.scale = new Vec2F(0.10f, 0.10f);
            this.alpha = 0.90f;

            // Configure interactive features.
            this.gazeInSensitivity = 0.08f;
            this.gazeOutSensitivity = 0.12f;

            // Configure animations.
            initAttentionAnimation(0.8f, 1.2f, 2000);
            initPreSelectionAnimation(1000);
            initPostSelectionAnimation(500);
        }

        @Override
        protected void onSelected() {
            // Start video playback and remove preview image from view.
            mOrionVideoView.start();
            mOrionVideoView.setPreviewAlpha(0.0f);

            // Hotspots are not needed anymore, remove them.
            hideHotspots();
            deactivateHotspots();
            stopHotspotAnimations();
        }

    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_minimal_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Initialize the hotspot objects and add them to the video view.
        initializeHotspots();

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {

                // Use empty preview image (~black color) as a transparent mask for video
                // before playback starts (via user triggering one of the start hotspots).
                mOrionVideoView.setPreviewAlpha(0.8f);

				// Show hotspots.
				showHotspots();

                // Activate hotspots.
                activateHotspots();

                // Activate reticle.
                activateReticle();

                // Start hotspot animations.
                startHotspotAnimations();

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

		// Hide the hotspots until the video gets prepared.
		hideHotspots();
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

        // Hide all hotspots.
        hideHotspots();

        // Deactivate all hotspots.
        deactivateHotspots();

        // Deactivate reticle.
        deactivateReticle();

        // Stop all hotspot animations.
        stopHotspotAnimations();

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
     * Initialize the hotspot objects and add them to the video view.
     */
	private void initializeHotspots() {

        // Create the reticle.
        mHotspots.add(new Reticle(IDX_HOTSPOT_RETICLE));

        // Create one "start" hotspot to each of the four cardinal direction, but add a
        // little bit of elevation to prevent selecting them by accident too easily.
        float pitchOffset = (float) (10.0f * Math.PI / 180.0f);
        mHotspots.add(new StartHotspot(IDX_HOTSPOT_START_FRONT,
                Vec3F.AXIS_FRONT.rotateX(pitchOffset)));
        mHotspots.add(new StartHotspot(IDX_HOTSPOT_START_RIGHT,
                Vec3F.AXIS_RIGHT.rotateZ(pitchOffset)));
        mHotspots.add(new StartHotspot(IDX_HOTSPOT_START_BACK,
                Vec3F.AXIS_BACK.rotateX(-pitchOffset)));
        mHotspots.add(new StartHotspot(IDX_HOTSPOT_START_LEFT,
                Vec3F.AXIS_LEFT.rotateZ(-pitchOffset)));

        // Add all hotspots to the video view as Orion360 tags.
        try {
            for (Hotspot hotspot : mHotspots) {
                mOrionVideoView.createTag(hotspot.index);
                mOrionVideoView.setTagAssetFilename(hotspot.index, hotspot.imageFilename);
                mOrionVideoView.setTagLocation(hotspot.index, hotspot.location.x,
                        hotspot.location.y, hotspot.location.z);
                mOrionVideoView.setTagScale(hotspot.index, hotspot.scale.x, hotspot.scale.y);
                mOrionVideoView.setTagAlpha(hotspot.index, hotspot.alpha);
            }
        } catch (OrionVideoView.LicenseVerificationException e) {
            Log.e(TAG, "License does not cover selected feature", e);
        }
    }

    /**
     * Show the hotspots.
     */
	private void showHotspots() {
		for (Hotspot hotspot : mHotspots) {
			mOrionVideoView.setTagAlpha(hotspot.index, hotspot.alpha);
		}
	}

    /**
     * Hide the hotspots.
     */
	private void hideHotspots() {
        for (Hotspot hotspot : mHotspots) {
			mOrionVideoView.setTagAlpha(hotspot.index, 0.0f);
        }
	}

    /** Start hotspot animations. */
    private void startHotspotAnimations() {
        for (Hotspot hotspot : mHotspots) {
            if (hotspot instanceof AnimatedSelectableHotspot) {
                AnimatedSelectableHotspot animated = (AnimatedSelectableHotspot) hotspot;
                animated.startAttentionAnimation();
            }
        }
    }

    /**
     * Stop all animations from all animated hotspots.
     */
    private void stopHotspotAnimations() {
        for (Hotspot hotspot : mHotspots) {
            if (hotspot instanceof AnimatedSelectableHotspot) {
                AnimatedSelectableHotspot animated = (AnimatedSelectableHotspot) hotspot;
                animated.stopAllAnimations();
            }
        }
    }

    /**
     * Activate all interactive hotspots.
     */
    private void activateHotspots() {
        for (Hotspot hotspot : mHotspots) {
            if (hotspot instanceof InteractiveHotspot) {
                InteractiveHotspot interactive = (InteractiveHotspot) hotspot;
                interactive.activate();
            }
        }
    }

    /**
     * Deactivate all interactive hotspots.
     */
    private void deactivateHotspots() {
        for (Hotspot hotspot : mHotspots) {
            if (hotspot instanceof InteractiveHotspot) {
                InteractiveHotspot interactive = (InteractiveHotspot) hotspot;
                interactive.deactivate();
            }
        }
    }

    /**
     * Activate reticle.
     */
    private void activateReticle() {
        for (Hotspot hotspot : mHotspots) {
            if (hotspot instanceof Reticle) {
                Reticle reticle = (Reticle) hotspot;
                reticle.activate();
            }
        }
    }

    /**
     * Deactivate reticle.
     */
    private void deactivateReticle() {
        for (Hotspot hotspot : mHotspots) {
            if (hotspot instanceof Reticle) {
                Reticle reticle = (Reticle) hotspot;
                reticle.deactivate();
            }
        }
    }

    /** Simple class that represents a hotspot object implemented as an Orion360 tag. */
    private class Hotspot {

        /** Each hotspot has a unique (positive) index that is used for referencing it. */
        int index;

        /** Hotspot position on the 360 sphere surface is given as a 3D unit rotation vector. */
        Vec3F location;

        /** Hotspot is visualized with a 2D image pane that always faces the origin (user). */
        String imageFilename;

        /** Hotspot size on the 360 sphere surface is given as a relative [X, Y] scale factor. */
        Vec2F scale;

        /** Hotspot transparency can be configured by giving it a global alpha value. */
        float alpha;


        /**
         * Constructor.
         *
         * @param index The index to be set for this hotspot instance.
         */
        public Hotspot(int index) {
            this.index = index;
        }

    }

    /** Reticle implemented as a hotspot that moves along viewing direction. */
    private class Reticle extends Hotspot implements OrionSensorFusion.Listener {

        /**
         * Constructor.
         *
         * @param index The index to be set for this hotspot instance.
         */
        public Reticle(int index) {
            super(index);

            // Configure basic hotspot features.
            this.location = Vec3F.AXIS_FRONT;
            this.imageFilename = getString(R.string.asset_hotspot_reticle);
            this.scale = new Vec2F(0.10f, 0.10f);
            this.alpha = 0.90f;
        }

        @Override
        public void onDeviceOrientationChanged(QuatF orientation) {

            // Move the reticle to the current view direction; this way it appears
            // to be fixed to the video view center.
            mOrionVideoView.setTagRotation(index, orientation.conjugate());

        }

        @Override
        public void onDeviceDisplayRotationChanged(int rotationDegrees) {}

        /**
         * Make the reticle active by starting to listen view orientation changes.
         */
        protected void activate() {
            mOrionVideoView.registerOrientationChangeListener(this);
        }

        /**
         * Make the reticle inactive by stopping to listen view orientation changes.
         */
        protected void deactivate() {
            mOrionVideoView.unregisterOrientationChangeListener(this);
        }

    }

    /** Adds interactivity to the hotspot object by making it respond to gazing events. */
    private abstract class InteractiveHotspot extends Hotspot
            implements OrionSensorFusion.Listener {

        /** Sensitivity for receiving gazing focus. */
        protected float gazeInSensitivity;

        /** Sensitivity for losing gazing focus. Should be larger than gazeInSensitivity. */
        protected float gazeOutSensitivity;

        /** Flag for indicating if the hotspot is currently being gazed at. */
        private boolean mIsGazedAt = false;


        /**
         * Constructor.
         *
         * @param index The index to be set for this hotspot instance.
         */
        public InteractiveHotspot(int index) {
            super(index);
        }

        @Override
        public void onDeviceOrientationChanged(QuatF orientation) {

            // Rotate a front vector to the direction where the user is currently looking at.
            Vec3F gazeDirection = Vec3F.AXIS_FRONT.rotate(orientation.conjugate());

            // Check if the gazing vector points close enough to the hotspot location
            // using a very simple approximation.
            if (!mIsGazedAt) {
                if ((       Math.abs(location.x - gazeDirection.x) < gazeInSensitivity)
                        && (Math.abs(location.y - gazeDirection.y) < gazeInSensitivity)
                        && (Math.abs(location.z - gazeDirection.z) < gazeInSensitivity) ) {
                    mIsGazedAt = true;
                    onGotGazeFocus();
                }
            } else {
                if ((       Math.abs(location.x - gazeDirection.x) > gazeOutSensitivity)
                        || (Math.abs(location.y - gazeDirection.y) > gazeOutSensitivity)
                        || (Math.abs(location.z - gazeDirection.z) > gazeOutSensitivity) ) {
                    mIsGazedAt = false;
                    onLostGazeFocus();
                }
            }
        }

        @Override
        public void onDeviceDisplayRotationChanged(int rotationDegrees) {}

        /**
         * Check whether the hotspot is currently being gazed at, or not.
         *
         * @return true if currently gazed at, else false.
         */
        public boolean isGazedAt() {
            return mIsGazedAt;
        }

        /**
         * Make the hotspot active by starting to listen view orientation changes.
         */
        protected void activate() {
            mOrionVideoView.registerOrientationChangeListener(this);
        }

        /**
         * Make the hotspot inactive by stopping to listen view orientation changes.
         */
        protected void deactivate() {
            mOrionVideoView.unregisterOrientationChangeListener(this);
        }

        /**
         * Called when the hotspot gets gaze focus. To be overriden by subclasses.
         */
        abstract protected void onGotGazeFocus();

        /**
         * Called when the hotspot loses gaze focus. To be overriden by subclasses.
         */
        abstract protected void onLostGazeFocus();

    }

    /** Adds selection and animation capabilities to an interactive hotspot. */
    private abstract class AnimatedSelectableHotspot extends InteractiveHotspot {

        /** An animator for drawing attention to the hotspot (pulsating FX). */
        private ValueAnimator mAttentionAnimator;

        /** An animator for indicating that the hotspot will be selected soon (roll FX). */
        private ValueAnimator mPreSelectionAnimator;

        /** An animator for indicating that the hotspot has been selected (escape FX). */
        private ValueAnimator mPostSelectionAnimator;

        /** Flag for indicating whether current selection process has been canceled. */
        private boolean mSelectionCanceled = false;


        /**
         * Constructor.
         *
         * @param index The index to be set for this hotspot instance.
         */
        public AnimatedSelectableHotspot(int index) {
            super(index);
        }

        @Override
        protected void onGotGazeFocus() {
            stopAttentionAnimation();
            startPreSelectionAnimation();
        }

        @Override
        protected void onLostGazeFocus() {
            if (null != mPreSelectionAnimator && mPreSelectionAnimator.isRunning()) {
                stopPreSelectionAnimation();
                startAttentionAnimation();
            }
        }

        /**
         * Initialize the attention animation.
         *
         * @param min The minimum shrinking factor for the pulsating effect.
         * @param max The maximum enlarging factor for the pulsating effect.
         * @param periodMs The period in milliseconds for a single pulsating cycle.
         */
        protected void initAttentionAnimation(float min, float max, long periodMs) {
            mAttentionAnimator = ValueAnimator.ofFloat(min, max);
            mAttentionAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAttentionAnimator.setRepeatMode(ValueAnimator.REVERSE);
            mAttentionAnimator.setDuration(periodMs / 2);
            mAttentionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    // Create a pulsating effect by animating hotspot scale.
                    float animatedValue = (Float) animation.getAnimatedValue();
                    mOrionVideoView.setTagScale(index,
                            animatedValue * scale.x, animatedValue * scale.y);

                }

            });
        }

        /**
         * Start the attention animation, and stop all other animations (if any).
         */
        protected void startAttentionAnimation() {
            stopAllAnimations();
            if (null != mAttentionAnimator) {
                mAttentionAnimator.start();
            }
        }

        /**
         * Stop the attention animation.
         */
        protected void stopAttentionAnimation() {
            if (null != mAttentionAnimator) {
                mAttentionAnimator.cancel();
            }
        }

        /**
         * Initialize the pre-selection animation.
         *
         * @param selectionLatencyMs The amount of continuous gazing (ms) required for triggering.
         */
        protected void initPreSelectionAnimation(long selectionLatencyMs) {
            mPreSelectionAnimator = ValueAnimator.ofFloat(0, (float) (2 * Math.PI));
            mPreSelectionAnimator.setRepeatCount(0);
            mPreSelectionAnimator.setRepeatMode(ValueAnimator.RESTART);
            mPreSelectionAnimator.setDuration(selectionLatencyMs);
            mPreSelectionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    // Create a roll effect, and restore hotspot location with another rotation.
                    float animatedValue = (Float) animation.getAnimatedValue();
                    QuatF clockRotation = QuatF.fromRotationAxisZ(-animatedValue);
                    QuatF locationRotation = QuatF.fromEulerRotationZXY(
                            -location.getYaw(), location.getPitch(), 0.0f);
                    QuatF total = locationRotation.multiply(clockRotation);
                    mOrionVideoView.setTagRotation(index, total);

                }

            });
            mPreSelectionAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationCancel(Animator arg0) {

                    // The user gazed away too soon, cancel selection and remove roll effect.
                    mSelectionCanceled = true;
                    mOrionVideoView.setTagLocation(index, location);

                }

                @Override
                public void onAnimationEnd(Animator arg0) {

                    if (!mSelectionCanceled) {

                        // Make a short beep sound.
                        new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60).startTone(
                                ToneGenerator.TONE_PROP_BEEP);

                        if (null != mPostSelectionAnimator) {
                            startPostSelectionAnimation();
                        } else {
                            onSelected();
                        }

                    }

                }

                @Override
                public void onAnimationRepeat(Animator arg0) {}

                @Override
                public void onAnimationStart(Animator arg0) {
                    mSelectionCanceled = false;
                }

            });
        }

        /**
         * Start the pre-selection animation, and stop all other animations (if any).
         */
        protected void startPreSelectionAnimation() {
            stopAllAnimations();
            if (null != mPreSelectionAnimator) {
                mPreSelectionAnimator.start();
            }
        }

        /**
         * Stop the pre-selection animation.
         */
        protected void stopPreSelectionAnimation() {
            if (null != mPreSelectionAnimator) {
                mPreSelectionAnimator.cancel();
            }
        }

        /**
         * Initialize the post-selection animation.
         *
         * @param escapeDuration The duration in milliseconds to remove the hotspot from view.
         */
        protected void initPostSelectionAnimation(long escapeDuration) {
            mPostSelectionAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
            mPostSelectionAnimator.setRepeatCount(0);
            mPostSelectionAnimator.setRepeatMode(ValueAnimator.RESTART);
            mPostSelectionAnimator.setDuration(escapeDuration);
            mPostSelectionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    // Create an escape effect by simultaneously scaling to zero and fading away.
                    float animatedValue = (Float) animation.getAnimatedValue();
                    mOrionVideoView.setTagAlpha(index, animatedValue);
                    mOrionVideoView.setTagScale(index,
                            scale.x * animatedValue, scale.y * animatedValue);

                }

            });
            mPostSelectionAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationCancel(Animator arg0) {

                    // Animation was canceled, perhaps because we are leaving the activity,
                    // and should not proceed with the selection.
                    mSelectionCanceled = true;

                }

                @Override
                public void onAnimationEnd(Animator arg0) {

                    if (!mSelectionCanceled) {

                        // Trigger selection.
                        onSelected();

                    }

                }

                @Override
                public void onAnimationRepeat(Animator arg0) {}

                @Override
                public void onAnimationStart(Animator arg0) {
                    mSelectionCanceled = false;
                }

            });
        }

        /**
         * Start the post-selection animation, and stop all other animations (if any).
         */
        protected void startPostSelectionAnimation() {
            stopAllAnimations();
            if (null != mPostSelectionAnimator) {
                mPostSelectionAnimator.start();
            }
        }

        /**
         * Stop the post-selection animation.
         */
        protected void stopPostSelectionAnimation() {
            if (null != mPostSelectionAnimator) {
                mPostSelectionAnimator.cancel();
            }
        }

        /**
         * Stop ALL hotspot animations.
         */
        protected void stopAllAnimations() {
            stopAttentionAnimation();
            stopPreSelectionAnimation();
            stopPostSelectionAnimation();
        }

        /**
         * Called when the hotspot is selected. To be overriden by subclasses.
         */
        abstract protected void onSelected();

    }

}

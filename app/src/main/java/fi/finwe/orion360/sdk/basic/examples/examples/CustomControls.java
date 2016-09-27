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
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

/**
 * An example of a minimal Orion360 video player, with custom media controls.
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
public class CustomControls extends Activity {

    /** Tag for logging. */
    public static final String TAG = CustomControls.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** Custom media controller. */
    private CustomController mCustomController;

    /** Gesture detector for tapping events. */
    private GestureDetector mGestureDetector;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Initialize custom controller.
        mCustomController = new CustomController(this);
        mCustomController.setMediaPlayer(mOrionVideoView);
        mCustomController.setOrionVideoView(mOrionVideoView);
        mCustomController.setAnchorView((ViewGroup)mOrionVideoView.getParent());

        // Start playback when the player has initialized itself and buffered enough video frames.
        mOrionVideoView.setOnPreparedListener(new OrionVideoView.OnPreparedListener() {
            @Override
            public void onPrepared(OrionVideoView view) {
                mOrionVideoView.start();
                mCustomController.show();

            }
        });

        // Initialize Orion360 video view with a URI to an .mp4 video file.
        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.prepare(MainMenu.PRIVATE_EXTERNAL_FILES_PATH
                    + MainMenu.TEST_VIDEO_FILE_MQ);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Propagate all touch events from the video view to a gesture detector.
        mOrionVideoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }

        });

        // Toggle media controls by tapping the screen.
        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {

                        if (mCustomController.isShowing()) {
                            mCustomController.hide();
                        } else {
                            mCustomController.show();
                        }

                        return true;
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

        // Resume custom controls.
        mCustomController.onResume();

    }

	@Override
	public void onPause() {
        // Pause custom controls.
        mCustomController.onPause();

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
     * Class that implements custom media controls using a frame layout on top of video view.
     */
    class CustomController extends FrameLayout implements OrionVideoView.OnStatusChangeListener {

        /**
         * Tag for logging.
         */
        public final String TAG = CustomController.class.getSimpleName();

        // Message to handler: update progress.
        private static final int UPDATE_VIDEO_POSITION = 1;

        // Delay in ms for updating progress.
        private static final int UPDATE_PROGRESS_DELAY = 500;

        /**
         * Context.
         */
        private Context mContext;

        /**
         * Orion360 video view.
         */
        private OrionVideoView mOrionVideoView;

        /**
         * Media player control.
         */
        private MediaController.MediaPlayerControl mMediaPlayerControl;

        /**
         * Audio Manager.
         */
        private AudioManager mAudioManager;

        /**
         * Flag for indicating if audio is currently muted, or not.
         */
        private boolean mIsAudioMuted = false;

        /**
         * Handler for timed operations.
         */
        private PlayerControllerHandler mHandler;

        /**
         * Layout root view.
         */
        private View mRootView;

        /**
         * Anchor view group for custom controls.
         */
        private ViewGroup mAnchorViewGroup;

        /**
         * Flag for indicating if player controller is added to anchor, or not.
         */
        private boolean mIsAddedToAnchor;

        /**
         * Play/pause button.
         */
        private ImageButton mPlayPauseButton;

        /**
         * Video seek bar.
         */
        private SeekBar mSeekBar;

        /**
         * Video position label.
         */
        private TextView mPositionLabel;

        /**
         * Video duration label.
         */
        private TextView mDurationLabel;

        /**
         * Audio mute button.
         */
        private ImageButton mMuteButton;


        /**
         * Constructor.
         *
         * @param context
         */
        public CustomController(Context context) {
            super(context);

            initialize(context);
        }

        /**
         * Constructor.
         *
         * @param context
         * @param attrs
         */
        public CustomController(Context context, AttributeSet attrs) {
            super(context, attrs);

            initialize(context);
        }

        /**
         * Constructor.
         *
         * @param context
         * @param attrs
         * @param defStyle
         */
        public CustomController(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

            initialize(context);
        }

        /**
         * Initialize the controller.
         *
         * @param context
         */
        private void initialize(Context context) {

            // Context.
            mContext = context;

            // Get audio manager for mute/volume control.
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            // Initialize handler for timed operations.
            mHandler = new PlayerControllerHandler(this);

        }

        /**
         * To be called from parent activity's onResume().
         */
        public void onResume() {

            // Start updating video position.
            mHandler.sendEmptyMessage(UPDATE_VIDEO_POSITION);

            // Restore audio muting, if previously flagged.
            if (mIsAudioMuted) {
                //mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                //above: deprecated in API level 23, use this instead:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_MUTE, 0);
            }

        }

        /**
         * To be called from parent activity's onPause().
         */
        public void onPause() {

            // Stop updating video position.
            mHandler.removeMessages(UPDATE_VIDEO_POSITION);

            // Clear audio muting, but do not clear the flag so we can restore state.
            if (mIsAudioMuted) {
                //mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                //above: deprecated in API level 23, use this instead:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_UNMUTE, 0);
            }

        }

        /**
         * Set Orion Video View to listen.
         *
         * @param orionVideoView The Orion360 video view to listen.
         */
        public void setOrionVideoView(OrionVideoView orionVideoView) {
            mOrionVideoView = orionVideoView;
            if (null != mOrionVideoView) {
                mOrionVideoView.setOnStatusChangeListener(this);
            }
        }

        @Override
        public void onStatusChange(OrionVideoView view, OrionVideoView.PlayerStatus status) {
            Log.d(TAG, "onStatusChange(): new status = " + status.name());

            switch (status) {
                case INITIALIZED:
                    break;
                case STARTED:
                    updatePlayPauseButtonIcon();
                    mHandler.sendEmptyMessage(UPDATE_VIDEO_POSITION);
                    break;
                case PAUSED:
                    updatePlayPauseButtonIcon();
                    mHandler.removeMessages(UPDATE_VIDEO_POSITION);
                    break;
                case COMPLETED:
                    mMediaPlayerControl.start(); // Repeat.
                    break;
                case SEEK_COMPLETE:
                    break;
                case RELEASED:
                    break;
                case ERROR:
                    break;
                default:
                    break;
            }
        }

        /**
         * Set Media Player Control.
         *
         * @param control The media player control.
         */
        public void setMediaPlayer(MediaController.MediaPlayerControl control) {
            mMediaPlayerControl = control;
        }

        /**
         * Set anchor view where to place the custom controls.
         *
         * @param anchorView The anchor view group.
         */
        public void setAnchorView(ViewGroup anchorView) {

            // Stop listening events from current anchor view, if any.
            if (mAnchorViewGroup != null) {
                mAnchorViewGroup.removeOnLayoutChangeListener(
                        mLayoutChangeListener);
            }

            // Remove all of our child views.
            removeAllViews();

            // Adjust our size to match with the parent.
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            // (Re)initialize our views and add to view hierarchy.
            addView(createViews(), params);

            // Store handle for the new anchor view.
            mAnchorViewGroup = anchorView;

            // Start listening events from the new anchor view.
            if (mAnchorViewGroup != null) {
                mAnchorViewGroup.addOnLayoutChangeListener(mLayoutChangeListener);
            }

            // Adjust custom controls size and position.
            FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            // Add us to anchor's view hierarchy.
            mAnchorViewGroup.addView(this, params2);
            mIsAddedToAnchor = true;

            // Do not show the controls yet.
            setVisibility(View.INVISIBLE);
        }

        /**
         * Layout change listener.
         */
        private OnLayoutChangeListener mLayoutChangeListener = new OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
            }

        };

        /**
         * Create views for custom controls.
         *
         * @return root view.
         */
        private View createViews() {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mRootView = inflater.inflate(R.layout.custom_controls, null);

            // Get handle for play button, listen for clicks and set icon.
            mPlayPauseButton = (ImageButton) mRootView.findViewById(R.id.playbutton);
            mPlayPauseButton.setOnClickListener(mPlayButtonListener);
            updatePlayPauseButtonIcon();

            // Get handle for position and duration labels, and update them.
            mPositionLabel = (TextView) mRootView.findViewById(R.id.position);
            updatePositionLabel();
            mDurationLabel = (TextView) mRootView.findViewById(R.id.duration);
            updateDurationLabel();

            // Get handle for seekbar, listen to its events, and update video position.
            mSeekBar = (SeekBar) mRootView.findViewById(R.id.seek_bar);
            mSeekBar.setOnSeekBarChangeListener(mSeekbarListener);
            updateVideoPosition();

            // Get handle for audio button, listen for clicks and set icon.
            mMuteButton = (ImageButton) mRootView.findViewById(R.id.audiobutton);
            mMuteButton.setOnClickListener(mMuteButtonListener);
            updateMuteButtonIcon();

            return mRootView;
        }

        private View.OnClickListener mPlayButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Play/pause: onClick()");
                if (mMediaPlayerControl.isPlaying()) {
                    mMediaPlayerControl.pause();
                } else {
                    mMediaPlayerControl.start();
                }
            }
        };

        private void updatePlayPauseButtonIcon() {
            if (mMediaPlayerControl.isPlaying()) {
                mPlayPauseButton.setImageResource(R.drawable.pause);
            } else {
                mPlayPauseButton.setImageResource(R.drawable.play);
            }
        }

        private SeekBar.OnSeekBarChangeListener mSeekbarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Native media player is slow to seek...
                    //mMediaPlayerControl.seekTo(seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch()");
                // Temporarily stop updating progress.
                mHandler.removeMessages(UPDATE_VIDEO_POSITION);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch()");
                // Seet to the point where tracking was stopped.
                mMediaPlayerControl.seekTo(seekBar.getProgress());

                // Continue updating progress.
                mHandler.sendEmptyMessage(UPDATE_VIDEO_POSITION);
            }
        };

        private void updateVideoPosition() {
            int duration = mMediaPlayerControl.getDuration();
            int position = mMediaPlayerControl.getCurrentPosition();

            if (null != mSeekBar) {
                if (duration > 0) {
                    mSeekBar.setMax(duration);
                    mSeekBar.setProgress(position);
                }
            }
        }

        private void updatePositionLabel() {
            int position = mMediaPlayerControl.getCurrentPosition();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(position);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(position)
                    - TimeUnit.MINUTES.toSeconds(minutes);
            String formattedTime = String.format(
                    Locale.US, "%d:%02d", minutes, seconds);
            mPositionLabel.setText(formattedTime);
        }

        private void updateDurationLabel() {
            int duration = mMediaPlayerControl.getDuration();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                    - TimeUnit.MINUTES.toSeconds(minutes);
            String formattedTime = String.format(
                    Locale.US, "%d:%02d", minutes, seconds);
            mDurationLabel.setText(formattedTime);
        }

        private View.OnClickListener mMuteButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Mute on/off: onClick()");
                mIsAudioMuted = !mIsAudioMuted;
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mIsAudioMuted);
                updateMuteButtonIcon();
            }
        };

        private void updateMuteButtonIcon() {
            if (mIsAudioMuted) {
                mMuteButton.setImageResource(R.drawable.mute_on);
            } else {
                mMuteButton.setImageResource(R.drawable.mute_off);
            }
        }

        public void show() {
            setVisibility(View.VISIBLE);

            // Start updating progress.
            mHandler.sendEmptyMessage(UPDATE_VIDEO_POSITION);
        }

        public void hide() {
            Log.d(TAG, "hide()");
            setVisibility(View.INVISIBLE);

            // Stop updating progress.
            mHandler.removeMessages(UPDATE_VIDEO_POSITION);
        }

        public boolean isShowing() {
            return mIsAddedToAnchor && this.getVisibility() == View.VISIBLE;
        }

        private class PlayerControllerHandler extends Handler {
            private WeakReference<CustomController> mParent;

            public PlayerControllerHandler(CustomController parent) {
                mParent = new WeakReference<CustomController>(parent);
            }

            @Override
            public void handleMessage(Message message) {
                CustomController parent = mParent.get();
                if (parent == null) {
                    return;
                }
                switch (message.what) {
                    case UPDATE_VIDEO_POSITION:
                        // Update progress now.
                        parent.updateVideoPosition();
                        parent.updatePositionLabel();
                        parent.updateDurationLabel();

                        // Check whether progress should be updated again after a delay.
                        if (/*!parent.mTracking && parent.mShowing &&*/
                                parent.mMediaPlayerControl.isPlaying()) {
                            message = obtainMessage(UPDATE_VIDEO_POSITION);
                            sendMessageDelayed(message, UPDATE_PROGRESS_DELAY);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

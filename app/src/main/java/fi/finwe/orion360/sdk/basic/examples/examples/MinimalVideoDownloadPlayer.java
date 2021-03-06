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
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.util.Pair;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import fi.finwe.orion360.OrionVideoView;
import fi.finwe.orion360.sdk.basic.examples.MainMenu;
import fi.finwe.orion360.sdk.basic.examples.R;

import static fi.finwe.orion360.sdk.basic.examples.MainMenu.PRIVATE_EXTERNAL_FILES_PATH;
import static fi.finwe.orion360.sdk.basic.examples.MainMenu.PRIVATE_INTERNAL_FILES_PATH;

/**
 * An example of a minimal Orion360 video player, for downloading a video file before playback.
 * <p>
 * Notice that saving a copy of a video file while streaming it is not possible with Android
 * MediaPlayer as a video backend. To obtain a local copy of a video file that resides in the
 * network you need to download it separately, as shown in this example.
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
public class MinimalVideoDownloadPlayer extends Activity
        implements OrionVideoView.OnPreparedListener {

    /** Tag for logging. */
    public static final String TAG = MinimalVideoDownloadPlayer.class.getSimpleName();

    /** Orion360 video player view. */
	private OrionVideoView mOrionVideoView;

    /** Full path to a video file to be played. */
    private String mVideoPath;

    /** A class for creating a tuple from a URL and a file path. */
    private class UrlFilePair extends Pair<String, String> {
        UrlFilePair(String url, String filePath) {
            super(url, filePath);
        }
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set layout.
		setContentView(R.layout.activity_video_player);

        // Get Orion360 video view that is defined in the XML layout.
        mOrionVideoView = (OrionVideoView) findViewById(R.id.orion_video_view);

        // Listen to a callback that tells when the video is ready to be played.
        mOrionVideoView.setOnPreparedListener(this);

        // Download the video file, then play it. Notice that this link points to a
        // 4k video file, older/mid-range devices may not be able to play it!
        // In case of problems, try MainMenu.TEST_VIDEO_URI_1920x960 instead.
        downloadAndPlay(MainMenu.TEST_VIDEO_URI_3840x1920);
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
     * Downloads a video file over the network to the local file system, then plays it.
     *
     * @param videoUrl The URL to the video to be downloaded and played.
     */
    public void downloadAndPlay(String videoUrl) {

        // Save the file to external media, if it is currently mounted.
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mVideoPath = PRIVATE_EXTERNAL_FILES_PATH;
        } else {
            mVideoPath = PRIVATE_INTERNAL_FILES_PATH;
        }

        // Create a name for the video file.
        String name = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);
        mVideoPath += name;

        // Create a progress bar to be shown while downloading the file.
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.player_file_download_title));
        progress.setMessage(String.format(getString(R.string.player_file_download_message), name));
        progress.setMax(100);
        progress.setIndeterminate(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        // Create a background task for downloading the file (will take a moment).
        new DownloadFileTask(progress).execute(new UrlFilePair(videoUrl, mVideoPath));
    }

    /**
     * Background task for downloading files.
     */
    private class DownloadFileTask extends AsyncTask<UrlFilePair, Integer, Integer> {

        /** Progress dialog to be shown while working. */
        ProgressDialog mProgress;

        /**
         * Constructor.
         *
         * @param progress The progress dialog to be used.
         */
        DownloadFileTask(ProgressDialog progress) {
            mProgress = progress;
        }

        @Override
        public void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected Integer doInBackground(UrlFilePair... files) {

            // Download files over the network to the local file system,
            // if not already there.
            int downloadFileCount = 0;
            for (UrlFilePair filePair : files) {
                String downloadUrl = filePair.first;
                String outputFile = filePair.second;

                if (!new File(outputFile).exists()) {
                    DataInputStream in = null;
                    DataOutputStream out = null;

                    try {
                        URL url = new URL(downloadUrl);
                        URLConnection connection = url.openConnection();
                        int contentLength = connection.getContentLength();
                        in = new DataInputStream(url.openStream());
                        out = new DataOutputStream(new FileOutputStream(outputFile));

                        byte [] buffer = new byte[1024];
                        int read;
                        int total = 0;
                        while (( read = in.read(buffer) ) != -1 ) {
                            out.write(buffer, 0, read);
                            out.flush();
                            total += read;
                            publishProgress((int) ((total / (float) contentLength) * 100));

                            // Escape early if cancel() is called.
                            if (isCancelled()) break;
                        }
                        downloadFileCount++;

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to download " + downloadUrl + " to " + outputFile, e);
                    } finally {
                        if (null != in) {
                            try { in.close(); } catch (IOException e) { Log.e(TAG,
                                    "Failed to close input stream."); }
                        }
                        if (null != out) {
                            try { out.close(); } catch (IOException e) { Log.e(TAG,
                                    "Failed to close output stream."); }
                        }
                    }
                }
            }

            return downloadFileCount;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            mProgress.dismiss();

            // Notify downloaded files.
            if (result > 0) {
                Toast.makeText(MinimalVideoDownloadPlayer.this,
                        String.format(getString(R.string.player_file_download_completed), result),
                        Toast.LENGTH_LONG).show();
            }

            // Prepare video for playback, if the file exists.
            if (new File(mVideoPath).exists()) {

                prepare();

            }
        }
    }

    /**
     * Prepare Orion360 video view for playing a video file.
     */
    private void prepare() {

        // Notice that this call will fail if a valid Orion360 license file for the package name
        // (defined in the application's manifest file) cannot be found.
        try {
            mOrionVideoView.prepare(mVideoPath);
        } catch (OrionVideoView.LicenseVerificationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onPrepared(OrionVideoView view) {

        // Start video playback now.
        view.start();

    }
}

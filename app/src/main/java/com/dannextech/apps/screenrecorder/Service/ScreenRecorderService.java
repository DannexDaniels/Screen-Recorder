package com.dannextech.apps.screenrecorder.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.dannextech.apps.screenrecorder.Media.MediaAudioEncoder;
import com.dannextech.apps.screenrecorder.Media.MediaEncoder;
import com.dannextech.apps.screenrecorder.Media.MediaMuxerWrapper;
import com.dannextech.apps.screenrecorder.Media.MediaScreenEncoder;

import java.io.IOException;

/**
 * Created by amoh on 12/8/2017.
 */

public class ScreenRecorderService extends IntentService {


    private static final boolean DEBUG = false;
    private static final String TAG = "ScreenRecorderService";

    private static final String BASE = "com.serenegiant.service.ScreenRecorderService.";
    public static final String ACTION_START = BASE + "ACTION_START";
    public static final String ACTION_STOP = BASE + "ACTION_STOP";
    public static final String ACTION_PAUSE = BASE + "ACTION_PAUSE";
    public static final String ACTION_RESUME = BASE + "ACTION_RESUME";
    public static final String ACTION_QUERY_STATUS = BASE + "ACTION_QUERY_STATUS";
    public static final String ACTION_QUERY_STATUS_RESULT = BASE + "ACTION_QUERY_STATUS_RESULT";
    public static final String EXTRA_RESULT_CODE = BASE + "EXTRA_RESULT_CODE";
    public static final String EXTRA_QUERY_RESULT_RECORDING = BASE + "EXTRA_QUERY_RESULT_RECORDING";
    public static final String EXTRA_QUERY_RESULT_PAUSING = BASE + "EXTRA_QUERY_RESULT_PAUSING";

    private static Object sSync = new Object();
    private static MediaMuxerWrapper sMuxer;

    private MediaProjectionManager mMediaProjectionManager;

    public ScreenRecorderService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.v(TAG, "onCreate:");
        mMediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (DEBUG) Log.v(TAG, "onHandleIntent:intent=" + intent);
        final String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            startScreenRecord(intent);
            updateStatus();
        } else if (ACTION_STOP.equals(action)) {
            stopScreenRecord();
            updateStatus();
        } else if (ACTION_QUERY_STATUS.equals(action)) {
            updateStatus();
        } else if (ACTION_PAUSE.equals(action)) {
            pauseScreenRecord();
        } else if (ACTION_RESUME.equals(action)) {
            resumeScreenRecord();
        }
    }

    private void updateStatus() {
        final boolean isRecording, isPausing;
        synchronized (sSync) {
            isRecording = (sMuxer != null);
            isPausing = isRecording ? sMuxer.isPaused() : false;
        }
        final Intent result = new Intent();
        result.setAction(ACTION_QUERY_STATUS_RESULT);
        result.putExtra(EXTRA_QUERY_RESULT_RECORDING, isRecording);
        result.putExtra(EXTRA_QUERY_RESULT_PAUSING, isPausing);
        if (DEBUG) Log.v(TAG, "sendBroadcast:isRecording=" + isRecording + ",isPausing=" + isPausing);
        sendBroadcast(result);
    }

    /**
     * start screen recording as .mp4 file
     * @param intent
     */
    private void startScreenRecord(final Intent intent) {
        if (DEBUG) Log.v(TAG, "startScreenRecord:sMuxer=" + sMuxer);
        synchronized (sSync) {
            if (sMuxer == null) {
                final int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
                // get MediaProjection
                final MediaProjection projection = mMediaProjectionManager.getMediaProjection(resultCode, intent);
                if (projection != null) {
                    final DisplayMetrics metrics = getResources().getDisplayMetrics();
                    final int density = metrics.densityDpi;

                    if (DEBUG) Log.v(TAG, "startRecording:");
                    try {
                        sMuxer = new MediaMuxerWrapper(".mp4"); // if you record audio only, ".m4a" is also OK.
                        if (true) {
                            // for screen capturing
                            new MediaScreenEncoder(sMuxer, mMediaEncoderListener,
                                    projection, metrics.widthPixels, metrics.heightPixels, density);
                        }
                        if (true) {
                            // for audio capturing
                            new MediaAudioEncoder(sMuxer, mMediaEncoderListener);
                        }
                        sMuxer.prepare();
                        sMuxer.startRecording();
                    } catch (final IOException e) {
                        Log.e(TAG, "startScreenRecord:", e);
                    }
                }
            }
        }
    }

    /**
     * stop screen recording
     */
    private void stopScreenRecord() {
        if (DEBUG) Log.v(TAG, "stopScreenRecord:sMuxer=" + sMuxer);
        synchronized (sSync) {
            if (sMuxer != null) {
                sMuxer.stopRecording();
                sMuxer = null;
                // you should not wait here
            }
        }
    }

    private void pauseScreenRecord() {
        synchronized (sSync) {
            if (sMuxer != null) {
                sMuxer.pauseRecording();
            }
        }
    }

    private void resumeScreenRecord() {
        synchronized (sSync) {
            if (sMuxer != null) {
                sMuxer.resumeRecording();
            }
        }
    }

    /**
     * callback methods from encoder
     */
    private static final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
        }
    };

}

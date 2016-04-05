package com.robosoft.archana.youtubeapplication;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;
import com.robosoft.archana.youtubeapplication.Util.Auth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by archana on 1/4/16.
 */
public class UploadService extends IntentService {

    //constructor with a name for the worker thread.
    public UploadService() {
        super("YTUploadService");
    }
    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    private static long mStartTime;
    private int mUploadAttemptCount;
    private static final int MAX_RETRY = 3;
    private static final int UPLOAD_REATTEMPT_DELAY_SEC = 60;
    private static final int PROCESSING_POLL_INTERVAL_SEC = 60;
    private static final int PROCESSING_TIMEOUT_SEC = 60 * 20; // 20 minutes
    int count = 0;
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("Hello","I am in onHandleIntent");
        Uri fileUri = intent.getData();
        String chosenAccountName = intent.getStringExtra(Constants.ACCOUNT_KEY);
        Log.i("Hello","ChosenAccountName is"+chosenAccountName);
        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Lists.newArrayList(Auth.SCOPES));
        credential.setSelectedAccountName(chosenAccountName);
        credential.setBackOff(new ExponentialBackOff());
        String appName = getResources().getString(R.string.app_name);
        final YouTube youtube =
                new YouTube.Builder(transport, jsonFactory, credential).setApplicationName(
                        appName).build();
        try {
            tryUploadAndShowSelectableNotification(fileUri, youtube);
        } catch (InterruptedException e) {
            // ignore
        }
    }
    private void tryUploadAndShowSelectableNotification(final Uri fileUri, final YouTube youtube) throws InterruptedException {
        Log.i("Hello", "I am in tryUploadAndShowSelectableNotification");
        while (true) {
            Log.i("Hello","I am inside while loop of Upload Service"+"And Count is"+count);
            Log.i("Hello", "I am Service Class of tryUpload"+String.format("Uploading [%s] to YouTube", fileUri.toString()));
            String videoId = tryUpload(fileUri, youtube);
            Log.i("Hello","VideoId is"+videoId);
            Log.i("Hello","Vedeio Id is"+videoId);
            if (videoId != null) {
                Log.i("Hello", String.format("Uploaded video with ID: %s", videoId));
                tryShowSelectableNotification(videoId, youtube);
                return;
            } else {
                Log.e("Hello", String.format("Failed to upload %s", fileUri.toString()));
                if (mUploadAttemptCount++ < MAX_RETRY) {
                    Log.i("Hello", String.format("Will retry to upload the video ([%d] out of [%d] reattempts)",
                            mUploadAttemptCount, MAX_RETRY));
                    zzz(UPLOAD_REATTEMPT_DELAY_SEC * 1000);
                } else {
                    Log.e("Hello", String.format("Giving up on trying to upload %s after %d attempts",
                            fileUri.toString(), mUploadAttemptCount));
                    return;
                }
            }
        }
    }
    private static void zzz(int duration) throws InterruptedException {
        Log.i("Hello","I am in zzz method");
        Log.d("Hello", String.format("Sleeping for [%d] ms ...", duration));
        Thread.sleep(duration);
        Log.d("Hello", String.format("Sleeping for [%d] ms ... done", duration));
    }
    private static boolean timeoutExpired(long startTime, int timeoutSeconds) {
        Log.i("Hello","I am in tiemoutExpired");
        long currTime = System.currentTimeMillis();
        long elapsed = currTime - startTime;
        if (elapsed >= timeoutSeconds * 1000) {
            return true;
        } else {
            return false;
        }
    }
    private String tryUpload(Uri mFileUri, YouTube youtube) {
        Log.i("Hello", "I am in tryUpload");
        long fileSize;
        InputStream fileInputStream = null;
        String videoId = null;
        try {
            fileSize = getContentResolver().openFileDescriptor(mFileUri, "r").getStatSize();
            fileInputStream = getContentResolver().openInputStream(mFileUri);
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(mFileUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

              videoId = ResumableUpload.upload(youtube, fileInputStream, fileSize, mFileUri, cursor.getString(column_index), getApplicationContext());


        } catch (FileNotFoundException e) {
            Log.e(getApplicationContext().toString(), e.getMessage());
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                // ignore

            }
        }
        return videoId;
    }


    private void tryShowSelectableNotification(final String videoId, final YouTube youtube)
            throws InterruptedException {
        Log.i("Hello","I am in trySHowSelectableNotification");
        mStartTime = System.currentTimeMillis();
        boolean processed = false;
        while (!processed) {
            Log.i("Hello","I am in while loop(process");
            processed = ResumableUpload.checkIfProcessed(videoId, youtube);
            if (!processed) {
                // wait a while
                Log.d("Hello", String.format("Video [%s] is not processed yet, will retry after [%d] seconds",
                        videoId, PROCESSING_POLL_INTERVAL_SEC));
                if (!timeoutExpired(mStartTime, PROCESSING_TIMEOUT_SEC)) {
                    zzz(PROCESSING_POLL_INTERVAL_SEC * 1000);
                } else {
                    Log.d("Hello", String.format("Bailing out polling for processing status after [%d] seconds",
                            PROCESSING_TIMEOUT_SEC));
                    return;
                }
            } else {
               ResumableUpload.showSelectableNotification(videoId, getApplicationContext());
               return;
            }
        }
    }
}

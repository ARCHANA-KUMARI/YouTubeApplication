package com.robosoft.archana.youtubeapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class ReviewActivity extends AppCompatActivity {

    private Uri mFileUri;
    private VideoView mVideoView;
    MediaController mc;
    private Button mUploadButton;
    private String mChosenAccountName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mUploadButton = (Button)findViewById(R.id.upload_button);
      //  mVideoView = (VideoView)findViewById(R.id.videoView);
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            mUploadButton.setVisibility(View.GONE);
            setTitle(R.string.playing_the_video_in_upload_progress);
        }
        mFileUri = intent.getData();
        Log.i("Hello", "Review Activity File VIew is" + mFileUri);/*Review Activity File VIew iscontent://media/external/video/media/40*/
        loadAccount();
        reviewVideo(mFileUri);

    }

    private void reviewVideo(Uri mFileUri) {
        try {
            Log.i("Hello","Review Activity  FILE uri is"+mFileUri);/* Review Activity  FILE uri iscontent://media/external/video/media/40*/
            mVideoView = (VideoView) findViewById(R.id.videoView);
            mc = new MediaController(this);
            mVideoView.setMediaController(mc);
            mVideoView.setVideoURI(mFileUri);
            mc.show();
            mVideoView.start();
        } catch (Exception e) {
            Log.e(this.getLocalClassName(), e.toString());
        }
    }
    //TODO
    private void loadAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        mChosenAccountName = sp.getString(Constants.ACCOUNT_KEY, null);
        invalidateOptionsMenu();
    }

    public void uploadVideo(View view) {
        Log.i("Hello","I am uploadVideo Method of Review Activity");
        Log.i("Hello","mChosenAccountName is"+mChosenAccountName);//NULL JUST FOR CHECK
//        if (mChosenAccountName == null) {
//            return;
//        }
        // if a video is picked or recorded.
        Log.i("Hello","mFile uri is"+mFileUri);
        if (mFileUri != null) {

            Intent uploadIntent = new Intent(this, UploadService.class);
            uploadIntent.setData(mFileUri);
            uploadIntent.putExtra(Constants.ACCOUNT_KEY, "ms.archana57@gmail.com");
            startService(uploadIntent);
            Toast.makeText(this, R.string.youtube_upload_started,
                    Toast.LENGTH_LONG).show();
            // Go back to MainActivity after upload

            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


}

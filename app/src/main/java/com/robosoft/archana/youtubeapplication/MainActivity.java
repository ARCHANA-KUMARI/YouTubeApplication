package com.robosoft.archana.youtubeapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class MainActivity extends AppCompatActivity {


    private String mChosenAccountName;
    private Uri mFileURI = null;
    GoogleAccountCredential credential;
  //  private UploadBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadAccount();
        saveAccount();

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
    private void loadAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        mChosenAccountName = sp.getString(Constants.ACCOUNT_KEY, null);
        invalidateOptionsMenu();
    }

    private void saveAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        sp.edit().putString(Constants.ACCOUNT_KEY, mChosenAccountName).commit();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Hello", "Main Activity Intent Data is" + data);/*Main Activity Intent Data isIntent { dat=content://media/external/video/media/40 (has extras) }*/
        switch(requestCode){
            case Constants.RESULT_PICK_IMAGE_CROP:
                Log.i("Hello","I am in Result_pick_image_crop");
                if (resultCode == RESULT_OK) {
                    mFileURI = data.getData();
                    Log.i("Hello", "MainActivity mFileURI IS" + mFileURI);/* MainActivity mFileURI IScontent://media/external/video/media/40*/
                    if (mFileURI != null) {
                        Intent intent = new Intent(this, ReviewActivity.class);
                        intent.setData(mFileURI);
                        startActivity(intent);
                    }
                }
            case Constants.RESULT_VIDEO_CAP:
                Log.i("Hello","I am in result_vedio_cap case");
                if (resultCode == RESULT_OK) {
                    mFileURI = data.getData();
                    if (mFileURI != null) {
                        Intent intent = new Intent(this, ReviewActivity.class);
                        intent.setData(mFileURI);
                        startActivity(intent);
                    }
                }
                break;
            case Constants.REQUEST_AUTHORIZATION:
                Log.i("Hello","I am in Request_Authorization");
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                }
                break;
        }


    }

    private void chooseAccount() {
        Log.i("Hello","I am in chooseAccount Method");
        startActivityForResult(credential.newChooseAccountIntent(),
                Constants.REQUEST_ACCOUNT_PICKER);
    }
    public void pickFile(View view) {
        Log.i("Hello","I am in pickFIle method");
        Intent intent = new Intent(Intent.ACTION_PICK);
        Log.i("Hello","Main Activity Intent is"+intent);/*Main Activity Intent isIntent { act=android.intent.action.PICK }*/
        intent.setType("video/*");
        Log.i("Hello", "Main Activity Intent is" + intent);/* Main Activity Intent isIntent { act=android.intent.action.PICK typ=video/* }*/
        startActivityForResult(intent, Constants.RESULT_PICK_IMAGE_CROP);
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (broadcastReceiver == null)
//            broadcastReceiver = new UploadBroadcastReceiver();
//        IntentFilter intentFilter = new IntentFilter(
//                Constants.REQUEST_AUTHORIZATION_INTENT);
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                broadcastReceiver, intentFilter);
//    }
   /* private class UploadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.REQUEST_AUTHORIZATION_INTENT)) {
                Log.d("Hello", "Request auth received - executing the intent");
                Intent toRun = intent
                        .getParcelableExtra(Constants.REQUEST_AUTHORIZATION_INTENT_PARAM);
                startActivityForResult(toRun, Constants.REQUEST_AUTHORIZATION);
            }
        }
    }*/
    public void recordVideo(View view) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Workaround for Nexus 7 Android 4.3 Intent Returning Null problem
        // create a file to save the video in specific folder (this works for
        // video only)
        // mFileURI = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileURI);

        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        // start the Video Capture Intent
        startActivityForResult(intent, Constants.RESULT_VIDEO_CAP);
    }
}

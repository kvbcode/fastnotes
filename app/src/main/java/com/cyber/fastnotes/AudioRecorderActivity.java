package com.cyber.fastnotes;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;

import com.cyber.component.AudioPlayerComponent;
import com.cyber.component.AudioRecorderComponent;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class AudioRecorderActivity extends AppCompatActivity {

    private AudioPlayerComponent comAudioPlayer;
    private AudioRecorderComponent comAudioRecorder;
    private MediaPlayer mediaPlayer;

    private Uri contentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        contentUri = getIntent().getData();

        comAudioRecorder = findViewById(R.id.comAudioRecorder);
        comAudioRecorder.setTargetFile( contentUri.getPath() );

        mediaPlayer = new MediaPlayer();
        comAudioPlayer = findViewById(R.id.comAudioPlayer);
        comAudioPlayer.setMediaPlayer(mediaPlayer);
        comAudioPlayer.setAudioSource( contentUri );

        comAudioPlayer.setVisibility(View.INVISIBLE);
        comAudioRecorder.getStateObservable()
            .subscribe( state -> {
                if ( state==AudioRecorderComponent.STATE_RECORDING_END ) {
                    comAudioPlayer.prepare();
                    comAudioPlayer.setVisibility(View.VISIBLE);
                }else{
                    comAudioPlayer.setVisibility(View.INVISIBLE);
                }
            });

        lockScreenOrientation(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.menuSave:
                Intent intent = new Intent();
                intent.setData( contentUri );
                setResult(RESULT_OK, intent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void lockScreenOrientation(boolean lock){
        if (!lock) {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
            return;
        }

        int orientation = getResources().getConfiguration().orientation;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        boolean isWide = orientation == Configuration.ORIENTATION_LANDSCAPE;

        switch (rotation) {
            case Surface.ROTATION_0:
                setRequestedOrientation( isWide? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
                break;
            case Surface.ROTATION_90:
                setRequestedOrientation( isWide? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
                break;
            case Surface.ROTATION_180:
                setRequestedOrientation( isWide? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT );
                break;
            case Surface.ROTATION_270:
                setRequestedOrientation( isWide? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT );
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        comAudioPlayer.dispose();
        comAudioRecorder.dispose();
        mediaPlayer.release();
        mediaPlayer = null;

    }
}

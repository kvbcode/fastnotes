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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;

import com.cyber.component.AudioPlayerComponent;
import com.cyber.component.AudioRecorderComponent;
import com.cyber.fastnotes.model.ArticleItem;
import com.cyber.fastnotes.model.ParcelableArticleItemWrapper;
import com.cyber.fastnotes.service.IOHelper;
import com.cyber.rx.ui.ObservableTextWatcher;

import java.util.concurrent.TimeUnit;

public class AudioRecorderActivity extends AppCompatActivity {
    private static String TAG = "REC_ACTIV";

    private AudioPlayerComponent comAudioPlayer;
    private AudioRecorderComponent comAudioRecorder;
    private MediaPlayer mediaPlayer;

    private EditText editTitle;

    private ArticleItem articleItem;
    private Uri newContentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        editTitle = findViewById(R.id.editTitle);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        newContentUri = Uri.fromFile( IOHelper.createExternalFilePath( this, Environment.DIRECTORY_PODCASTS, "rec_", "m4a" ) );

        comAudioRecorder = findViewById(R.id.comAudioRecorder);
        comAudioRecorder.setTargetFile( newContentUri.getPath() );

        mediaPlayer = new MediaPlayer();
        comAudioPlayer = findViewById(R.id.comAudioPlayer);
        comAudioPlayer.setMediaPlayer(mediaPlayer);
        comAudioPlayer.setAudioSource(newContentUri);

        comAudioPlayer.setVisibility(View.INVISIBLE);
        comAudioRecorder.getStateObservable()
            .subscribe( state -> {
                if ( state==AudioRecorderComponent.STATE_RECORDING_END ) {
                    articleItem.setContentUri( newContentUri );
                    comAudioPlayer.setAudioSource( newContentUri );
                    comAudioPlayer.setVisibility(View.VISIBLE);
                }else{
                    comAudioPlayer.setVisibility(View.INVISIBLE);
                }
            });

        ObservableTextWatcher textWatcher = new ObservableTextWatcher();
        textWatcher.getOnChangedObservable()
                .debounce( App.DEBOUNCE_VALUE, TimeUnit.MILLISECONDS )
                .subscribe( str -> articleItem.setText( str ) );
        editTitle.addTextChangedListener( textWatcher );

        if (savedInstanceState==null) parseInputParams(getIntent());

        lockScreenOrientation(true);
    }

    protected void parseInputParams(Intent in){
        boolean isNew = in.getBooleanExtra(App.PARAM_IS_NEW, true);

        if (isNew) {
            articleItem = ArticleItem.fromAudio( newContentUri );
            Log.v(TAG, "new ArticleItem() " + articleItem);
        }else {
            ParcelableArticleItemWrapper itemWrapper = in.getParcelableExtra( ArticleItem.class.getSimpleName() );
            articleItem = itemWrapper.getArticleItem();
            editTitle.setText( articleItem.getText() );
            comAudioPlayer.setAudioSource( articleItem.getContentUri() );
            comAudioPlayer.setVisibility(View.VISIBLE);
            Log.v(TAG, "load ArticleItem from parcel: " + articleItem);
        }
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
                intent.putExtra( ArticleItem.class.getSimpleName(), new ParcelableArticleItemWrapper(articleItem) );
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

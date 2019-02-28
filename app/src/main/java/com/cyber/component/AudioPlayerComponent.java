package com.cyber.component;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cyber.fastnotes.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class AudioPlayerComponent extends LinearLayout {
    private static final String TAG = "AUDIOPLAYERITEM";
    private static long INFO_UPDATE_INTERVAL = 200L;

    private static final int IMG_PLAY = R.drawable.ic_play_arrow_white;
    private static final int IMG_PAUSE = R.drawable.ic_pause_white;

    private MediaPlayer mediaPlayer;
    private Disposable mediaPlayerSubDisposable;

    private SeekBar seekBar;
    private ImageButton butPlayPause;
    private TextView txtTrackInfo;

    private Uri contentUri;
    private boolean isPlayerReady = false;

    public AudioPlayerComponent(Context context) {
        super(context);
        onCreate(context);
    }

    public AudioPlayerComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
    }

    public AudioPlayerComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate(context);
    }

    private void onCreate(Context context){
        LayoutInflater.from(context).inflate( R.layout.component_audio_player, this, true );

        seekBar = findViewById(R.id.seekBar);
        butPlayPause = findViewById(R.id.butPlayPause);
        txtTrackInfo = findViewById(R.id.txtTrackInfo);

        butPlayPause.setOnClickListener( v -> {
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                setTrackInfoUpdateInterval( 0L );
            }else{
                if (!isPlayerReady) prepare();
                mediaPlayer.start();
                setTrackInfoUpdateInterval(INFO_UPDATE_INTERVAL);
            }
            updateViewState();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                updateTrackInfo(progress, mediaPlayer.getDuration());
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    private void updateViewState(){
        seekBar.setEnabled( isPlayerReady );
        butPlayPause.setImageResource( mediaPlayer.isPlaying()? IMG_PAUSE: IMG_PLAY );
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;

        mediaPlayer.setOnCompletionListener( mp -> {
            Log.v(TAG, "MediaPlayer complete");
            updateViewState();
            setTrackInfoUpdateInterval( 0L );
        });

    }

    private void setTrackInfoUpdateInterval(long msec){
        if (msec == 0){
            if (mediaPlayerSubDisposable!=null && !mediaPlayerSubDisposable.isDisposed()) mediaPlayerSubDisposable.dispose();
            return;
        }

        mediaPlayerSubDisposable = Observable.interval( msec, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(c -> updateTrackInfo(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration() ) );
    }

    private void updateTrackInfo(int pos, int max){
        if (max!=seekBar.getMax()) seekBar.setMax(max);
        seekBar.setProgress(pos);
        txtTrackInfo.setText( String.format( "%d / %d", pos/1000, max/1000 ) );
    }

    public void setAudioSource(Uri uri) {
        this.contentUri = uri;
    }

    public void prepare(){
        Log.v(TAG, "prepare for " + contentUri);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource( getContext(), this.contentUri );
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            Log.v(TAG, "MediaPlayer ready");
            isPlayerReady = true;
        }catch(IOException e){
            Log.e(TAG, "prepare() IO error: " + e.getMessage());
            isPlayerReady = false;
            txtTrackInfo.setText( "file not ready" );
        }
        setTrackInfoUpdateInterval( 0L );
        updateViewState();
    }

    public void dispose(){
        Log.v(TAG, "dispose()");
        setTrackInfoUpdateInterval(0L);
        try {
            mediaPlayer.stop();
        }catch(IllegalStateException e){
            Log.v(TAG, "dispose() error: " + e.getMessage());
        }
        mediaPlayer = null;
    }

}

package com.cyber.component;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyber.fastnotes.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class AudioRecorderComponent extends LinearLayout {
    private static final String TAG = "AUDIORECORDER";

    public static final int STATE_NOT_READY = 101;
    public static final int STATE_READY = 102;
    public static final int STATE_RECORDING_START = 103;
    public static final int STATE_RECORDING = 104;
    public static final int STATE_RECORDING_END = 105;

    private int colNotReady;
    private int colRecording;
    private int colReady;
    private int colGrey;

    private final PublishSubject<Integer> pubState = PublishSubject.create();

    private MediaRecorder recorder;
    private String filePath;
    private int state = STATE_NOT_READY;

    private long startRecordingTime;
    private SimpleDateFormat timeFormat;

    private ImageButton butStartStop;
    private TextView txtTrackInfo;

    private Disposable trackInfoUpdateDisposable;


    public AudioRecorderComponent(Context context) {
        super(context);
        onCreate(context);
    }

    public AudioRecorderComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
    }

    public AudioRecorderComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate(context);
    }


    private void onCreate(Context context){
        LayoutInflater.from(getContext()).inflate( R.layout.component_audio_recorder, this, true );

        Resources res = context.getResources();
        colGrey = res.getColor( R.color.bgGrey );
        colNotReady = res.getColor( R.color.bgOrange );
        colReady = res.getColor( R.color.bgRed );
        colRecording = res.getColor( R.color.bgGreen );

        recorder = new MediaRecorder();
        startRecordingTime = System.currentTimeMillis();
        timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        butStartStop = findViewById(R.id.butStartStop);
        txtTrackInfo = findViewById(R.id.txtTrackInfo);

        butStartStop.setOnClickListener( v -> onClick(v) );

    }

    public Observable<Integer> getStateObservable(){
        return pubState;
    }

    private void setState(int newState){
        this.state = newState;
        updateViewState();
        pubState.onNext(this.state);
    }

    private void onClick(View view){
        switch (state){
            case STATE_NOT_READY:
                prepareRecorder( this.filePath );
                break;
            case STATE_READY:
                Log.v(TAG, "start()");
                final MediaRecorder rec = recorder;
                Completable.fromAction(() -> rec.start())
                    .subscribeOn(Schedulers.io())
                    .observeOn( AndroidSchedulers.mainThread() )
                    .subscribe(() -> {
                        startRecordingTime = System.currentTimeMillis();
                        setTrackInfoUpdateInterval(200L);
                        setState( STATE_RECORDING );
                    });
                setState( STATE_RECORDING_START );
                break;
            case STATE_RECORDING:
                Log.v(TAG, "stop()");
                setTrackInfoUpdateInterval(0L);
                setState( STATE_RECORDING_END);
                recorder.stop();
                break;
            case STATE_RECORDING_END:
                prepareRecorder( filePath );
                onClick( view );
                break;
        }
    }

    public void setTargetFile(String filePath){
        this.filePath = filePath;
        prepareRecorder( filePath );
    }

    public void setTrackInfoUpdateInterval(long msec){

        if (msec==0L){
            if (trackInfoUpdateDisposable!=null && !trackInfoUpdateDisposable.isDisposed()) trackInfoUpdateDisposable.dispose();
            return;
        }

        trackInfoUpdateDisposable = Observable.interval( msec, TimeUnit.MILLISECONDS )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(v -> updateTrackInfo());

    }

    private void updateTrackInfo(){
        Date timeDelta = new Date(System.currentTimeMillis() - startRecordingTime);
        txtTrackInfo.setText( timeFormat.format( timeDelta ) );
    }

    private void updateViewState(){
        int micBgColor = colGrey;
        switch (state){
            case STATE_NOT_READY:
                micBgColor = colNotReady;
                break;
            case STATE_READY:
                micBgColor = colReady;
                break;
            case STATE_RECORDING_START:
                micBgColor = colNotReady;
                break;
            case STATE_RECORDING:
                micBgColor = colRecording;
                break;
            case STATE_RECORDING_END:
                micBgColor = colGrey;
                break;
        }
        butStartStop.setBackgroundColor( micBgColor );
    }

    private void prepareRecorder(String filePath){
        this.filePath = filePath;
        Log.v(TAG, "prepare(): " + filePath);

        try {
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setAudioChannels(1);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(32000);
            recorder.setAudioEncodingBitRate(32000);
            recorder.setOutputFile( filePath );
            recorder.prepare();
            setState( STATE_READY );
        }catch(IOException e){
            setState( STATE_NOT_READY );
            Log.e(TAG, "prepare() error: " + e.getMessage());
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void dispose(){
        try{
            if (state==STATE_RECORDING) recorder.stop();
            recorder.release();
        }catch(Exception e){
            Log.e(TAG, "dispose() error: " + e.getMessage());
        }
        setTrackInfoUpdateInterval(0L);
        recorder = null;
    }
}

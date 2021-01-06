package com.example.soundrecorder;

import android.Manifest;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;
import omrecorder.WriteAction;

public class MainActivity extends AppCompatActivity implements Response.ErrorListener, Response.Listener<String>{

    private static final String TAG = "MainActivity";
    private static String fileName = null;

    private MediaRecorder mRecorder = null;
    private boolean recordingStatus = false;

    private Button recordButton;
    Recorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordButton = findViewById(R.id.btnRecord);
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.wav";

        runTimePermission();
        setupRecorder();

    }

    private void setupRecorder() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }

    private void animateVoice(final float maxPeak) {
        recordButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }

    @NonNull
    private File file() {
        return new File(fileName);
    }



    private void runTimePermission(){
        Dexter.withContext(this).withPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).
                withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public void record(View view){
        recordingStatus = !recordingStatus;
        if(recordingStatus){
            recordButton.setText("Stop Recording");
            Log.d(TAG, "record: Start Recording");
            recorder.startRecording();
        }
        else{
            recordButton.setText("Recording");
            try {
                Log.d(TAG, "record: Stopping Recording");
                recorder.stopRecording();
            } catch (IOException e) {
                Log.e(TAG, "record: " + e );
            }
        }
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        if (mRecorder != null) {
//            mRecorder.release();
//            mRecorder = null;
//        }
//    }

//    private void startRecording() {
//        Log.d(TAG, "startRecording: Configuring");
//        mRecorder = new MediaRecorder();
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mRecorder.setOutputFile(fileName);
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//        try {
//            mRecorder.prepare();
//        } catch (IOException e) {
//            Log.e(TAG, "startRecording: Prepare Failed" );
//        }
//        Log.d(TAG, "startRecording: Starting");
//        mRecorder.start();
//    }

//    private void stopRecording() {
//        Log.d(TAG, "stopRecording: Stopping Recording");
//        mRecorder.stop();
//        mRecorder.reset();
//        mRecorder.release();
//        mRecorder = null;
//    }


    public void send(View view){
        uploadData();
    }

    private void uploadData() {
        Log.d(TAG, "uploadData: Uploading Data");
        File audioFile = new File(fileName);
        WebService.getInstance().updateProfile(MainActivity.this,
                "appl_doc",
                "appliance",
                "1",
                audioFile,
                this,
                this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "onErrorResponse: " + error );
    }

    @Override
    public void onResponse(String response) {
        //Your response here
        Log.d(TAG, "onResponse: " + response);
    }
    
}
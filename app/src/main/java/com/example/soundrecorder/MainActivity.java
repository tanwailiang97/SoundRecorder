package com.example.soundrecorder;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.List;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class MainActivity extends AppCompatActivity implements Response.ErrorListener, Response.Listener<String>{

    private static final String TAG = "MainActivity";
    private static String fileName = null;
    private String mUserName = null, mPassword = null;

    private MediaRecorder mRecorder = null;
    private boolean recordingStatus = false;

    private Button btnSend;
    private ImageButton btnRecord;
    private EditText etPostTitle,etPostDescription;
    private TextView tvSending;
    private Recorder recorder;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        mUserName = mPreferences.getString("UserName","");
        mPassword = mPreferences.getString("Password","");
        checkUserDetails();

        btnRecord = findViewById(R.id.btnRecord);
        btnSend = findViewById(R.id.btnSend);
        etPostTitle = findViewById(R.id.etPostTitle);
        etPostDescription = findViewById(R.id.etPostDescription);
        tvSending = findViewById(R.id.tvSending);
        btnSend.setBackgroundColor(0xFF6200EE);
        runTimePermission();


    }



    @Override
    protected void onDestroy() {
        if(recorder!=null){
            try {
                recorder.stopRecording();
            } catch (IOException e) {
                Log.e(TAG, "onDestroy: Stopping Recording "  + e );
            }
            recorder = null;
        }
        super.onDestroy();
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
        //btnRecord.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
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
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/" + Calendar.getInstance().getTime() + "audio.wav";
        return new File(fileName);
    }

    public void preferenceEdit(String username, String password){
        mEditor.putString("UserName",username);
        mEditor.putString("Password",password);
        mUserName = username;
        mPassword = password;
        mEditor.apply();
        Log.d(TAG, "PreferenceEdit: Name Updated");
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
        if (recorder == null){
            Log.d(TAG, "record: Setting up recorder");
            setupRecorder();
        }
        if(recordingStatus){
            btnRecord.setBackgroundResource(R.drawable.ic_baseline_stop_circle_100);
            Log.d(TAG, "record: Start Recording");
            recorder.startRecording();
        }
        else if(!recordingStatus){
            btnRecord.setBackgroundResource(R.drawable.ic_baseline_radio_button_checked_100);
            try {
                recorder.stopRecording();
                recorder = null;
                Log.d(TAG, "record: Recording Stopped");
            }catch (IOException e) {
                Log.e(TAG, "record: " + e );
            }
        }
    }

    public void send(View view){
        uploadData();
    }

    private void uploadData() {
        Log.d(TAG, "uploadData: Uploading Data");
        String username = mUserName;
        String password = mPassword;
        String title = etPostTitle.getText().toString().replaceAll(" ","%20");
        String description = etPostDescription.getText().toString().replaceAll(" ","%20");
        if(!username.isEmpty() && !title.isEmpty() && !description.isEmpty()) {
            File audioFile = new File(fileName);
            tvSending.setVisibility(View.VISIBLE);
            WebService.getInstance().createPost(MainActivity.this,
                    username,
                    password,
                    title,
                    description,
                    audioFile,
                    this,
                    this);
        }
        else if(username.isEmpty()){
            Toast.makeText(MainActivity.this,"Please Enter Username",Toast.LENGTH_SHORT).show();
        }
        else if(title.isEmpty()){
            Toast.makeText(MainActivity.this,"Please Enter a Title",Toast.LENGTH_SHORT).show();
        }
        else if(description.isEmpty()){
            Toast.makeText(MainActivity.this,"Please Enter Some Description",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "onErrorResponse: " + error );
        tvSending.setVisibility(View.GONE);
        if (error.toString().contains("TimeoutError")){
            Toast.makeText(MainActivity.this, "Error: Time out", Toast.LENGTH_LONG).show();
        }
        else{
           Toast.makeText(MainActivity.this, "Error" +
                   error.toString().replaceFirst("com.android.volley.error.",""),
                   Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response) {
        //Your response here
        Log.d(TAG, "onResponse: " + response);
        tvSending.setVisibility(View.GONE);

        if(response.equals("Incorrect Pasword")){
            Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_LONG).show();
            DialogFragment dialog = new DialogFragment();
            dialog.show(getSupportFragmentManager(), "DialogFragment");
        }
        else if(response.equals("User does not exist")) {
            Toast.makeText(MainActivity.this, "User Does not Exist", Toast.LENGTH_LONG).show();
            DialogFragment dialog = new DialogFragment();
            dialog.show(getSupportFragmentManager(), "DialogFragment");
        }
        else if(response.equals("Post Added")) {
            Toast.makeText(MainActivity.this, "Post Have been successfully uploaded", Toast.LENGTH_LONG).show();
            etPostTitle.setText("");
            etPostDescription.setText("");
        }
        else{
            Toast.makeText(MainActivity.this, "Some Error Occurred", Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserDetails() {
        if (mUserName.isEmpty() || mPassword.isEmpty()){
            DialogFragment dialog = new DialogFragment();
            dialog.show(getSupportFragmentManager(), "DialogFragment");
        }
    }

    public void userEdit(View view) {
        DialogFragment dialog = new DialogFragment();
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }
}
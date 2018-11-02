package com.example.justi.markthat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class Record extends AppCompatActivity {
    private boolean recording = false;
    private MediaRecorder recorder = null;

    String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "MarkThat";
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        final FloatingActionButton recordButton = (FloatingActionButton) findViewById(R.id.toggleRecordButton);
        recordButton.setOnClickListener(buttonListeners);
    }

    private View.OnClickListener buttonListeners = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId() /*to get clicked view id**/) {
                case R.id.toggleRecordButton:
                    if(recording){
                        stopRecording();
                        recording = false;
                    }
                    else {
                        if(hasPermissions()) {
                            startRecording();
                            recording = true;
                        }
                        else
                            requestPermissions();
                    }
                    break;
                case R.id.markButton:

                    break;
                default:
                    break;
            }
        }
    };

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        try {
            recorder.prepare();
            recorder.start();
            Toast.makeText(getApplicationContext(), "Starting recording", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        Toast.makeText(getApplicationContext(), "Stopping recording", Toast.LENGTH_SHORT).show();
        if(recorder != null) {
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            }
            catch(RuntimeException e) {
                Log.w("Error stopping recording", e.toString());
            }
        }
        else
            Toast.makeText(getApplicationContext(), "No recording found!", Toast.LENGTH_SHORT).show();
    }

    private String getFilename() {
        String name = "/" + System.currentTimeMillis() + ".mp4";
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MarkThat";
        try {
            File dir = new File(rootPath);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            String fullPath = dir.getAbsolutePath() + name;
            return (fullPath);
        }
        catch(Exception e) {
            Log.w("Creating file error", e.toString());
        }
        return null;
    }

    //Requesting run-time permissions
    private boolean hasPermissions() {
        if(this != null && permissions != null) {
            for(String permission : permissions) {
                if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    public void hideKeyboard(View view){
        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}



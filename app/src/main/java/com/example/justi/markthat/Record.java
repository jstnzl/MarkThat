package com.example.justi.markthat;

import android.Manifest;
<<<<<<< HEAD
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
=======
>>>>>>> Trying to get record to work
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class Record extends AppCompatActivity {
    private boolean recording = false;
    private MediaRecorder recorder = null;
<<<<<<< HEAD
    String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };
=======
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "MarkThat";
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };
>>>>>>> Trying to get record to work

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        final FloatingActionButton recordButton = (FloatingActionButton) findViewById(R.id.toggleRecordButton);
        recordButton.setOnClickListener(buttonListeners);
    }

<<<<<<< HEAD
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
=======
        final FloatingActionButton recordButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        recordButton.setOnClickListener(buttonListeners);
    }

    private View.OnClickListener buttonListeners = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId() /*to get clicked view id**/) {
                case R.id.floatingActionButton:
                    if(recording){
                        if(null != recorder) {
                            recording = false;
                            recorder.stop();
                            recorder.reset();
                            recorder.release();
                            recorder = null;
                        }
                    }
                    else {
                        requestAudioPermissions();
                        recording = true;
                    }
                    break;
                case R.id.floatingActionButton3:
>>>>>>> Trying to get record to work

                    break;
                default:
                    break;
            }
        }
    };

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
<<<<<<< HEAD
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
=======
        recorder.setOutputFormat(output_formats[currentFormat]);
>>>>>>> Trying to get record to work
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        try {
            recorder.prepare();
            recorder.start();
<<<<<<< HEAD
            Toast.makeText(getApplicationContext(), "Starting recording", Toast.LENGTH_SHORT).show();
=======
>>>>>>> Trying to get record to work
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
<<<<<<< HEAD
=======
        }
    }

    private String getFilename() {
//        File root = new File(Environment.getExternalStorageDirectory(), AUDIO_RECORDER_FOLDER);
//        if (!root.exists()) {
//            root.mkdirs();
//        }
        return (Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    //Requesting run-time permissions

    //Create placeholder for user's consent to record_audio permission.
    //This will be used in handling callback
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            startRecording();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    startRecording();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
>>>>>>> Trying to get record to work
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
            return (dir.getAbsolutePath() + name);
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



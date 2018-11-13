package com.example.justi.markthat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.io.File;
import java.io.IOException;

public class Record extends AppCompatActivity {
    private boolean recording = false;
    private MediaRecorder recorder = null;
    MyDB db;
    Toolbar toolbar;
    EditText title;
    EditText description;
    String fileName;

    String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //Set Title for toolbar
        getSupportActionBar().setTitle("Record");

        Stetho.initializeWithDefaults(this);
        db = new MyDB(this, null, 1);
        final FloatingActionButton recordButton = (FloatingActionButton) findViewById(R.id.toggleRecordButton);
        recordButton.setOnClickListener(buttonListeners);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
    }

    private View.OnClickListener buttonListeners = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId() /*to get clicked view id**/) {
                case R.id.toggleRecordButton:
                    if(recording){
                        stopRecording(false);
                        recording = false;
                    }
                    else {
                        if(hasPermissions()) {
                            startRecording();
                            recording = true;
                        }
                        else {
                            requestPermissions();
                            Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();
                        }
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

    private void stopRecording(boolean isBack) {
        if(recorder != null) {
            if(!isBack)
                Toast.makeText(getApplicationContext(), "Stopping recording", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Recording cancelled", Toast.LENGTH_SHORT).show();
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
                if(!isBack)
                    db.insertRecord(fileName, title.getText().toString(), description.getText().toString());
            }
            catch(RuntimeException e) {
                Log.w("Error stopping recording", e.toString());
            }
        }
    }

    private String getFilename() {
        String name = System.currentTimeMillis() + ".mp4";
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MarkThat/";
        try {
            File dir = new File(rootPath);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            fileName = name;
            return rootPath+name;
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

    @Override
    public void onBackPressed() {
        stopRecording(true);
        refreshActivity();
        super.onBackPressed();
    }

    public void refreshActivity() {
        Intent i = new Intent(this, Home.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}

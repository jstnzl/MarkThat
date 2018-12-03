package com.example.justi.markthat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import rm.com.audiowave.AudioWaveView;
import rm.com.audiowave.OnProgressListener;
//import com.facebook.stetho.Stetho;

public class EditMark extends AppCompatActivity {
    private boolean recording = false;
    private MediaRecorder recorder = null;
    MyDB db;
    Toolbar toolbar;
    EditText title;
    EditText description;
    Button saveButton;
    Button deleteButton;
    Button resetButton;
    boolean playing = false;
    private MediaPlayer mp;
    private AudioWaveView waveView;
    String filePath;
    byte[] audioData;
    int timeStamp = 0;
    int origTime = 0;
    int maxDuration = -1;
    TextView currentTime;
    FloatingActionButton playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mark);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //Set Title for toolbar
        getSupportActionBar().setTitle("Edit Mark");

        Bundle extras = getIntent().getExtras();
        // file, title, desc, folder
        String[] info = new String[3];
        if(extras != null) {
            if(extras.containsKey("MARK_INFO"))
                info = extras.getStringArray("MARK_INFO");
        }
        final String fileName = info[0];
        final String posString = info[3];
        origTime = Integer.parseInt(posString);
        timeStamp = origTime;

//        Stetho.initializeWithDefaults(this);
        db = new MyDB(this, null, 1);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        title.setText(info[1]);
        description.setText(info[2]);
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s1 = title.getText().toString();
                String s2 = description.getText().toString();
                String s3 = timeStamp+"";
                db.updateMark(fileName, posString, s1, s2, s3);
                Toast.makeText(getApplicationContext(), "Mark updated", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
        deleteButton = findViewById(R.id.delete_button);
        Button deleteButton = (Button)findViewById(R.id.delete_button);
        deleteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setCancelable(true);
                builder.setTitle("Delete this recording?");
                builder.setMessage("This action cannot be undone");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(db.deleteMark(fileName, posString)) {
                                    Toast.makeText(getApplicationContext(), "Mark deleted", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }
                                else
                                    Toast.makeText(getApplicationContext(), "Error: Please try again later", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlayback();
                timeStamp = origTime;
                mp.seekTo(timeStamp);
                updateSeekBar();
            }
        });

        playButton = (FloatingActionButton) findViewById(R.id.playButton);
        playButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing) {
                    pausePlayback();
                }
                else {
                    playing = true;
//                    mp.seekTo(timeStamp);
                    mp.start();
                    updateSeekBar();
                    playButton.setImageResource(R.drawable.stop_icon);
                    Toast.makeText(getApplicationContext(), "Playing", Toast.LENGTH_SHORT).show();
                }

            }
        });

        currentTime = (TextView)findViewById(R.id.current_time);
        waveView = (AudioWaveView) findViewById(R.id.waveView);

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MarkThat/" + fileName;
        audioData = fileToBytes();
        waveView.setRawData(audioData);
        mp = new MediaPlayer();
        try { mp.setDataSource(filePath); } catch (Exception e) {};
        try { mp.prepare(); } catch (Exception e) {};
        maxDuration = mp.getDuration();
        currentTime.setText(getFormattedDuration(timeStamp)+" / "+getFormattedDuration(maxDuration));
        mp.seekTo(timeStamp);
        updateSeekBar();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
                playButton.setImageResource(R.drawable.play_icon);
                timeStamp = 0;
                mp.seekTo(timeStamp);
                waveView.setProgress(0);
            }

        });

        waveView.setOnProgressListener(new OnProgressListener() {
            @Override
            public void onStartTracking(float v) {
                if(playing)
                    mp.pause();
                mp.seekTo(timeStamp);
            }

            @Override
            public void onStopTracking(float v) {
                mp.seekTo(timeStamp);
                if(playing)
                    mp.start();
            }

            @Override
            public void onProgressChanged(float v, boolean b) {
                float realPercent = v/(float)100.00;
                timeStamp = (int)(maxDuration * realPercent);
                currentTime.setText(getFormattedDuration(timeStamp)+" / "+getFormattedDuration(maxDuration));
                if (!playing)
                    mp.seekTo(timeStamp);
            }
        });
    }

    public void hideKeyboard(View view){
        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    @Override
    public void onBackPressed() {
        if(mp != null)
            mp.stop();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public byte[] fileToBytes() {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private String getFormattedDuration(int duration) {
        StringBuilder sb = new StringBuilder();
        int minutes = (duration/1000)/60;
        String s1 = minutes > 9 ? minutes+"" : "0"+minutes;
        int seconds = (duration/1000)%60;
        String s2 = seconds > 9 ? seconds+"" : "0"+seconds;
        String s3 = duration - minutes*1000 - seconds*1000 +"";
        s3 = s3 + "000";
        s3 = s3.substring(0,3);
        sb.append(s1+":"+s2+"."+s3);
        return sb.toString();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(playing)
                updateSeekBar();
        }
    };

    private void updateSeekBar() {
        float v = (float)mp.getCurrentPosition()/(float)maxDuration * 100;
        waveView.setProgress(v);
//        txtCurrentTime.setText(milliSecondsToTimer(mp.getCurrentPosition()));
        waveView.postDelayed(runnable, 50);
    }

    public void pausePlayback() {
        if(!playing)
            return;
        playing = false;
        mp.pause();
        timeStamp = mp.getCurrentPosition();
        playButton.setImageResource(R.drawable.play_icon);
        Toast.makeText(getApplicationContext(), "Pausing", Toast.LENGTH_SHORT).show();
    }
}

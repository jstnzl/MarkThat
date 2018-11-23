package com.example.justi.markthat;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
//import com.facebook.stetho.Stetho;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Record extends AppCompatActivity {
    private boolean recording = false;
    private MediaRecorder recorder = null;
    MyDB db;
    Toolbar toolbar;
    EditText title;
    EditText description;
    TextView markTitle;
    String fileName;
    long startTime;
    ImageView recordingIndicator;
    ObjectAnimator anim;
    ListView myListView;
    SimpleAdapter adapter;
    List<Map<String, String>> data = new ArrayList<>();
    List<List<String>> dbResults;

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
//        Stetho.initializeWithDefaults(this);

        //recording indicator
        recordingIndicator = (ImageView) findViewById(R.id.recording_ind);
        // ObjectAnimator to animate
        anim = ObjectAnimator.ofInt(recordingIndicator, "alpha", Color.WHITE, Color.RED, Color.WHITE);
        anim.setDuration(1000);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(ValueAnimator.REVERSE);
//        txt = (TextView) findViewById(R.id.txt);

        db = new MyDB(this, null, 1);
        final FloatingActionButton recordButton = (FloatingActionButton) findViewById(R.id.toggleRecordButton);
        final FloatingActionButton markButton = (FloatingActionButton) findViewById(R.id.markButton);
        recordButton.setOnClickListener(buttonListeners);
        markButton.setOnClickListener(buttonListeners);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        markTitle = findViewById(R.id.marks_title);

        myListView = (ListView)findViewById(R.id.recording_listview);
        adapter = new SimpleAdapter(this, data, R.layout.marks_list_row,
                new String[] {"Title", "Description", "Position"},
                new int[] {R.id.rowTitle, R.id.rowDesc, R.id.rowPosition });
        myListView.setAdapter(adapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(view.getContext(), EditMark.class);
                String[] info = new String[dbResults.get(position).size()];
                info = dbResults.get(position).toArray(info);
                intent.putExtra("MARK_INFO", info);
                startActivityForResult(intent, 1);
            }
        });
    }

    public View.OnClickListener buttonListeners = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId() /*to get clicked view id**/) {
                case R.id.toggleRecordButton:
                    if(recording){
                        stopRecording(false);
                    }
                    else {
                        if(hasPermissions()) {
                            startRecording();
                        }
                        else {
                            requestPermissions();
                            Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.markButton:
                    if(!recording)
                        Toast.makeText(getApplicationContext(), "Please try while recording", Toast.LENGTH_SHORT).show();
                    else {
                        markIt();
                        getData();
                        adapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    private void markIt() {
        db.insertMark(startTime+".mp4", "", "", System.currentTimeMillis()-startTime);
        Toast.makeText(getApplicationContext(), "Marked!", Toast.LENGTH_SHORT).show();
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(getFilename());
        try {
            recorder.prepare();
            recorder.start();
            recording = true;
            anim.setRepeatCount(Animation.INFINITE);
            anim.start();
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
            else {
                Toast.makeText(getApplicationContext(), "Recording cancelled", Toast.LENGTH_SHORT).show();
                db.deleteMarksForRecord(fileName);
            }
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
                recording = false;
                anim.end();
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
        startTime = Long.parseLong(name.substring(0, name.length() - 4));
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

    private void getData() {
        data.clear();
        if(fileName == null)
            return;
        markTitle.setText("Marks made for this recording");
        dbResults = db.getMarksForRecord(fileName);
        if (dbResults.size() > 0) {
            int idx = 0;
            while (idx < dbResults.size()) {
                Map<String, String> datum = new HashMap<>();
                List<String> row = dbResults.get(idx);
                // file, title, desc, position
                String pos = getFormattedDuration(Integer.parseInt(row.get(3)));
                datum.put("Title", row.get(1));
                datum.put("Description", row.get(2));
                datum.put("Position", pos);
                data.add(datum);
                idx++;
            }
        }
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                getData();
                adapter.notifyDataSetChanged();
            }
        }
    }
}

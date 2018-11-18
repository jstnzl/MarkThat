package com.example.justi.markthat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRecording extends AppCompatActivity {
    MyDB db;
    boolean playing = false;
    private MediaPlayer mp;
    private SeekBar seekBar;
    int position = 0;
    int maxDuration = -1;
    FloatingActionButton playButton;
    TextView currentTime;
    List<List<String>> dbResults;
    ListView myListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recording);

        db = new MyDB(this, null, 1);
        Stetho.initializeWithDefaults(this);

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //Set Title for toolbar
        getSupportActionBar().setTitle("View Recording");
        dbResults = db.getAllMarks();
        myListView = (ListView)findViewById(R.id.recording_listview);
        if (dbResults.size() > 0) {
            int idx = 0;
            while (idx < dbResults.size()) {
                Map<String, String> datum = new HashMap<>();
                List<String> row = dbResults.get(idx);
                String dateTime = getDateFromFile(row.get(0));
                datum.put("Title", row.get(1));
                datum.put("Description", row.get(2));
                datum.put("Date Recorded", dateTime);
                data.add(datum);
                idx++;
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.home_list_row,
                new String[] {"Title", "Description", "Date Recorded"},
                new int[] {R.id.rowTitle, R.id.rowDesc, R.id.rowDate });
        myListView.setAdapter(adapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(view.getContext(), ViewRecording.class);
                String[] info = new String[dbResults.get(position).size()];
                info = dbResults.get(position).toArray(info);
                intent.putExtra("RECORDING_INFO", info);
                startActivity(intent);
            }
        });

        //title
        setTitle("MarkThat - View Recording(s)");
        Bundle extras = getIntent().getExtras();
        // file, title, desc, folder
        String[] info = new String[4];
        if(extras != null) {
            if(extras.containsKey("RECORDING_INFO"))
                info = extras.getStringArray("RECORDING_INFO");
        }
        final String fileName = info[0];

        // Set textviews to info passed in by home
        seekBar = (SeekBar) findViewById(R.id.SeekBar);
        TextView durationText = (TextView)findViewById(R.id.duration);
        currentTime = (TextView)findViewById(R.id.current_time);
        currentTime.setText("00:00");
        TextView titleText=(TextView)findViewById(R.id.recording_title);
        TextView descText=(TextView)findViewById(R.id.recording_desc);
        TextView dateText=(TextView)findViewById(R.id.recording_date);
        titleText.setText(info[1]);
        descText.setText(info[2]);
        dateText.setText(getDateFromFile(fileName));

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MarkThat/" + fileName;
        mp = new MediaPlayer();
        try { mp.setDataSource(filePath); } catch (Exception e) {};
        try { mp.prepare(); } catch (Exception e) {};
        seekBar.setMax(mp.getDuration()*5);
        maxDuration = mp.getDuration();
        durationText.setText(getFormattedDuration(maxDuration));

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
                playButton.setImageResource(R.drawable.play_icon);
                position = 0;
                mp.seekTo(position);
                seekBar.setProgress(position);
            }

        });

        Button deleteButton = (Button)findViewById(R.id.delete_button);
        deleteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db.delete(fileName)) {
                    Toast.makeText(getApplicationContext(), "Recording deleted", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                else
                    Toast.makeText(getApplicationContext(), "Error: Please try again later", Toast.LENGTH_SHORT).show();

            }
        });

        playButton = (FloatingActionButton) findViewById(R.id.playButton);
        playButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing) {
                    playing = false;
                    mp.pause();
                    position = mp.getCurrentPosition();
                    playButton.setImageResource(R.drawable.play_icon);
                    Toast.makeText(getApplicationContext(), "Pausing", Toast.LENGTH_SHORT).show();
                }
                else {
                    playing = true;
                    mp.seekTo(position);
                    mp.start();
                    updateSeekBar();
                    playButton.setImageResource(R.drawable.stop_icon);
                    Toast.makeText(getApplicationContext(), "Playing", Toast.LENGTH_SHORT).show();
                }

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                position = i/5;
                currentTime.setText(getFormattedDuration(position));
                if(!playing)
                    mp.seekTo(position);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(playing)
                    mp.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(position);
                if(playing)
                    mp.start();
            }
        });

    }

    public String getDateFromFile(String file) {
        long timeMillis = Long.parseLong(file.substring(0, file.length()-4));
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault());
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        return dateTime.format(dateFormat);
    }

    @Override
    public void onBackPressed() {
        if(mp != null)
            mp.stop();
        refreshActivity();
        super.onBackPressed();
    }

    public void refreshActivity() {
        Intent i = new Intent(this, Home.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    private void updateSeekBar() {
        seekBar.setProgress(mp.getCurrentPosition()*5);
//        txtCurrentTime.setText(milliSecondsToTimer(mp.getCurrentPosition()));
        seekBar.postDelayed(runnable, 50);
    }

    private String getFormattedDuration(int duration) {
        StringBuilder sb = new StringBuilder();
        int minutes = (duration/1000)/60;
        String s1 = minutes > 9 ? minutes+"" : "0"+minutes;
        int seconds = (duration/1000)%60;
        String s2 = seconds > 9 ? seconds+"" : "0"+seconds;
        sb.append(s1+":"+s2);
        return sb.toString();
    }
}

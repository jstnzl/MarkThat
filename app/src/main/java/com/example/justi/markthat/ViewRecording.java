package com.example.justi.markthat;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRecording extends AppCompatActivity {
    MyDB db;
    boolean playing = false;
    private MediaPlayer mp;
    private SeekBar seekBar;
    int timeStamp = 0;
    int maxDuration = -1;
    String fileCol;
    FloatingActionButton playButton;
    TextView currentTime;
    List<List<String>> dbResults;
    ListView myListView;
    SimpleAdapter adapter;
    List<Map<String, String>> data = new ArrayList<>();
    long start = 0;
    TextView titleText;
    TextView descText;
    TextView dateText;
    TextView folderName;
    SearchView searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recording);

        db = new MyDB(this, null, 1);
//        Stetho.initializeWithDefaults(this);

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //Set Title for toolbar
        getSupportActionBar().setTitle("Info");

        Bundle extras = getIntent().getExtras();
        // file, title, desc, folder
        String[] info = new String[4];
        if(extras != null) {
            if(extras.containsKey("RECORDING_INFO"))
                info = extras.getStringArray("RECORDING_INFO");
        }
        final String[] recordingInfo = info;
        final String fileName = info[0];
        fileCol = fileName;

        // Set textviews to info passed in by home
        seekBar = (SeekBar) findViewById(R.id.SeekBar);
        TextView durationText = (TextView)findViewById(R.id.duration);
        currentTime = (TextView)findViewById(R.id.current_time);
        currentTime.setText("00:00.000");
        titleText=(TextView)findViewById(R.id.recording_title);
        descText=(TextView)findViewById(R.id.recording_desc);
        dateText=(TextView)findViewById(R.id.recording_date);
        folderName=(TextView)findViewById(R.id.folder_name);
        titleText.setText(info[1]);
        descText.setText(info[2]);
        folderName.setText("Folder: "+info[3]);
        dateText.setText(getDateFromFile(fileName));

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MarkThat/" + fileName;
        mp = new MediaPlayer();
        try { mp.setDataSource(filePath); } catch (Exception e) {};
        try { mp.prepare(); } catch (Exception e) {};
        seekBar.setMax(mp.getDuration()*5);
        maxDuration = mp.getDuration();
        durationText.setText(getFormattedDuration(maxDuration));
        myListView = (ListView)findViewById(R.id.recording_listview);

        getData();
        adapter = new SimpleAdapter(this, data, R.layout.view_recording_list_row,
                new String[] {"Title", "Description", "Position"},
                new int[] {R.id.rowTitle, R.id.rowDesc, R.id.rowPosition });
        myListView.setAdapter(adapter);
        myListView.setLongClickable(true);

        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Intent intent = new Intent(view.getContext(), EditMark.class);
                String[] info = new String[dbResults.get(position).size()];
                info = dbResults.get(position).toArray(info);
                intent.putExtra("MARK_INFO", info);
                startActivityForResult(intent, 1);
                return true;
            }
        });

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                int time = Integer.parseInt(dbResults.get(position).get(4));
                timeStamp = time;
                mp.seekTo(time);
                seekBar.setProgress(time*5);
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
                playButton.setImageResource(R.drawable.play_icon);
                timeStamp = 0;
                mp.seekTo(timeStamp);
                seekBar.setProgress(timeStamp);
            }

        });

        Button editButton = (Button)findViewById(R.id.edit_button);
        editButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditRecording.class);
                intent.putExtra("RECORDING_INFO", recordingInfo);
                startActivityForResult(intent, 1);
            }
        });


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
                                if(db.deleteRecord(fileName)) {
                                    Toast.makeText(getApplicationContext(), "Recording deleted", Toast.LENGTH_SHORT).show();
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

        playButton = (FloatingActionButton) findViewById(R.id.playButton);
        playButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing) {
                    playing = false;
                    mp.pause();
                    timeStamp = mp.getCurrentPosition();
                    playButton.setImageResource(R.drawable.play_icon);
                    Toast.makeText(getApplicationContext(), "Pausing", Toast.LENGTH_SHORT).show();
                }
                else {
                    playing = true;
                    mp.seekTo(timeStamp);
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
                timeStamp = i/5;
                currentTime.setText(getFormattedDuration(timeStamp));
                if(!playing)
                    mp.seekTo(timeStamp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(playing)
                    mp.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(timeStamp);
                if(playing)
                    mp.start();
            }
        });

        searchBar = findViewById(R.id.search_recording_button);
        searchBar.setQueryHint("Filter by title or text");
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (dbResults.size() > 0) {
                    int idx = 0;
                    data.clear();
                    while (idx < dbResults.size()) {
                        Map<String, String> datum = new HashMap<>();
                        List<String> row = dbResults.get(idx);
                        // file, title, desc, duration, position
                        String lowerTitle = row.get(1).toLowerCase();
                        String lowerDesc = row.get(2).toLowerCase();
                        if(lowerTitle.indexOf(query.toLowerCase()) > -1 || lowerDesc.indexOf(query.toLowerCase()) > -1) {
                            start = Long.parseLong(row.get(0).substring(0, row.get(0).length() - 4));
                            String pos = getFormattedDuration(Integer.parseInt(row.get(4)));
                            datum.put("Title", row.get(1));
                            datum.put("Description", row.get(2));
                            datum.put("Position", pos);
                            data.add(datum);
                        }
                        idx++;
                    }
                    adapter.notifyDataSetChanged();
                }
                return false;
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
        String s3 = duration - minutes*1000 - seconds*1000 +"";
        sb.append(s1+":"+s2+"."+s3);
        return sb.toString();
    }

    private void getData() {
        data.clear();
        dbResults = db.getMarksForRecord(fileCol);
        if (dbResults.size() > 0) {
            int idx = 0;
            while (idx < dbResults.size()) {
                Map<String, String> datum = new HashMap<>();
                List<String> row = dbResults.get(idx);
                // file, title, desc, duration, position
                start = Long.parseLong(row.get(0).substring(0, row.get(0).length()-4));
                String pos = getFormattedDuration(Integer.parseInt(row.get(4)));
                datum.put("Title", row.get(1));
                datum.put("Description", row.get(2));
//                datum.put("Duration", row.get(3));
                datum.put("Position", pos);
                data.add(datum);
                idx++;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                getData();
                adapter.notifyDataSetChanged();
            }
            if(resultCode == RESULT_FIRST_USER) {
                String[] callBack = data.getStringArrayExtra("PASS_BACK");
                titleText.setText(callBack[0]);
                descText.setText(callBack[1]);
                folderName.setText(callBack[2]);
            }
        }
    }
}

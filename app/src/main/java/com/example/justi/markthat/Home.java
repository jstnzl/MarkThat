package com.example.justi.markthat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v7.widget.Toolbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class Home extends AppCompatActivity {
    MyDB db;
    Toolbar toolbar;
    ListView myListView;
    List<Map<String, String>> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //Set Title for toolbar
        getSupportActionBar().setTitle("Home");

        db = new MyDB(this, null, 1);
        List<String> dbResults = db.getAllRecords();
        myListView = (ListView)findViewById(R.id.recording_listview);
        if (dbResults.size() > 0) {
            int idx = 0;
            while (idx < dbResults.size()) {
                Map<String, String> datum = new HashMap<>();
                String row = dbResults.get(idx);
                int commaSplit = row.indexOf(',');
                int andSplit = row.indexOf('&');
                String title = row.substring(commaSplit+1, andSplit);
                String description = row.substring(andSplit+1);
                // get our date
                String fileName = row.substring(0, commaSplit);
                String dateTime = getDateFromMillis(fileName);
                datum.put("Title", title);
                datum.put("Description", description);
                datum.put("Date Recorded", dateTime);
                data.add(datum);
                idx++;
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.home_list_row,
                new String[] {"Title", "Description", "Date Recorded"},
                new int[] {R.id.rowTitle, R.id.rowDesc, R.id.rowDate });
        myListView.setAdapter(adapter);
    }

    @Override
    public void onRestart(){
        super.onRestart();
    }

    public void goTorecord(View view) {
        Intent intent = new Intent(this, Record.class);
        startActivity(intent);
    }

    public String getDateFromMillis(String file) {
        long timeMillis = Long.parseLong(file.substring(0, file.length()-4));
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault());
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm");
        return dateTime.format(dateFormat);
    }
}

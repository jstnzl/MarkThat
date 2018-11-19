package com.example.justi.markthat;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

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
    List<List<String>> dbResults;
    SearchView searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = new MyDB(this, null, 1);
        Stetho.initializeWithDefaults(this);

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //Set Title for toolbar
        getSupportActionBar().setTitle("Home");

        searchBar = findViewById(R.id.search_recording_button);
        searchBar.setQueryHint("Search by title or description!");
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        }

        db = new MyDB(this, null, 1);
        dbResults = db.getAllRecords();
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
        else
            Toast.makeText(getApplicationContext(), "You have no recordings yet!", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onRestart(){
        super.onRestart();
    }

    public void goTorecord(View view) {
        Intent intent = new Intent(this, Record.class);
        startActivity(intent);
    }

    public String getDateFromFile(String file) {
        long timeMillis = Long.parseLong(file.substring(0, file.length()-4));
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault());
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        return dateTime.format(dateFormat);
    }
}

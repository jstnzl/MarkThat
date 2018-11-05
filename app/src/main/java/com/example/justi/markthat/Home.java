package com.example.justi.markthat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class Home extends AppCompatActivity {
    MyDB db;
    ListView myListView;
    List<Map<String, String>> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //title
        setTitle("MarkThat - Home");

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
                Log.i("time", dateTime);
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
        this.finish();
    }

    public void goTorecord(View view) {
        Intent intent = new Intent(this, Record.class);
        startActivity(intent);
    }

    public String getDateFromMillis(String file) {
        long timeMillis = Long.parseLong(file.substring(0, file.length()-4));
        Date dateTime = new Date(timeMillis);
        DateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy hh:mm");
        return dateFormat.format(dateTime);
    }
}

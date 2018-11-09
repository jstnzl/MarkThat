package com.example.justi.markthat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ViewRecording extends AppCompatActivity {
    MyDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recording);

        db = new MyDB(this, null, 1);
        Stetho.initializeWithDefaults(this);

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
        TextView titleText=(TextView)findViewById(R.id.recording_title);
        TextView descText=(TextView)findViewById(R.id.recording_desc);
        TextView dateText=(TextView)findViewById(R.id.recording_date);
        titleText.setText(info[1]);
        descText.setText(info[2]);
        dateText.setText(getDateFromFile(fileName));

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
    }

    public String getDateFromFile(String file) {
        long timeMillis = Long.parseLong(file.substring(0, file.length()-4));
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault());
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        return dateTime.format(dateFormat);
    }

    @Override
    public void onBackPressed() {
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

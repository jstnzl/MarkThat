package com.example.justi.markthat;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class Record extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        //title of page
        String recordTitle = ("MarkThat - Record");
        //set up back button
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(recordTitle);
        }

    }
    public boolean onOptionsItemSelected(MenuItem item){ //back button
        switch (item.getItemId()) {
            case android.R.id.home:
                //your intent
                Intent myIntent = new Intent(getApplicationContext(), Home.class);
                startActivityForResult(myIntent, 0);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
}

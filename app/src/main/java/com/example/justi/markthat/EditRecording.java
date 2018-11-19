package com.example.justi.markthat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

public class EditRecording extends AppCompatActivity {
    private boolean recording = false;
    private MediaRecorder recorder = null;
    MyDB db;
    Toolbar toolbar;
    EditText title;
    EditText description;
    EditText folder;
    Button saveButton;
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recording);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //Set Title for toolbar
        getSupportActionBar().setTitle("Edit Recording");

        Bundle extras = getIntent().getExtras();
        // file, title, desc, folder
        String[] info = new String[4];
        if(extras != null) {
            if(extras.containsKey("RECORDING_INFO"))
                info = extras.getStringArray("RECORDING_INFO");
        }
        final String fileName = info[0];

        Stetho.initializeWithDefaults(this);
        db = new MyDB(this, null, 1);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        folder = findViewById(R.id.folder);
        title.setText(info[1]);
        description.setText(info[2]);
        folder.setText(info[3]);
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s1 = title.getText().toString();
                String s2 = description.getText().toString();
                String s3 = folder.getText().toString();
                db.updateRecord(fileName, s1, s2, s3);
                Toast.makeText(getApplicationContext(), "Record updated", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    public void hideKeyboard(View view){
        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        String[] info = new String[3];
        info[0] = title.getText().toString();
        info[1] = description.getText().toString();
        info[2] = folder.getText().toString();
        intent.putExtra("PASS_BACK", info);
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }
}

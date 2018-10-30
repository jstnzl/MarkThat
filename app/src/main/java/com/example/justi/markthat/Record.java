package com.example.justi.markthat;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Record extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        public void toggleRecord(String fileName) {
            final MediaRecorder recorder = new MediaRecorder();
            ContentValues values = new ContentValues(3);
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setOutputFile("/sdcard/sound/" + fileName);
            try {
                recorder.prepare();
            } catch (Exception e){
                e.printStackTrace();
            }

            final ProgressDialog mProgressDialog = new ProgressDialog(Record.this);
            mProgressDialog.setTitle(R.string.lbl_recording);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mProgressDialog.dismiss();
                    recorder.stop();
                    recorder.release();
                }
            });

            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface p1) {
                    recorder.stop();
                    recorder.release();
                }
            });
            recorder.start();
            mProgressDialog.show();
        }
    }
}

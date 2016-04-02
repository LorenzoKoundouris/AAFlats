package com.example.lorenzo.aaflats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class ReportDetails extends AppCompatActivity {

    Report parceableReport;
    TextView reportContent;
    TextView reportSender;
    TextView reportTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                new android.support.v7.app.AlertDialog.Builder(view.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete?")
                        .setMessage("Are you sure you want to permanently delete this task?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }

                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle intent = getIntent().getExtras();
        parceableReport = intent.getParcelable("parceable_report");

        reportContent = (TextView) findViewById(R.id.report_content);
        reportSender = (TextView) findViewById(R.id.report_sender);
        reportTimestamp = (TextView) findViewById(R.id.report_timestamp);

        String tt = parceableReport.getContent();
        reportContent.setText(parceableReport.getContent());
        reportSender.setText(parceableReport.getSender());
        StringBuilder ts = new StringBuilder(parceableReport.getTimestamp());
        ts.insert(2, "/");
        ts.insert(5, "/");
        ts.insert(10, " ");
        ts.insert(13, ":");
        ts.insert(16, ":");
        reportTimestamp.setText(ts);
    }

}

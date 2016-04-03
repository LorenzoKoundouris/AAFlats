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
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.HashMap;
import java.util.Map;

public class ReportDetails extends AppCompatActivity {

    Firebase reportRef;

    Report parceableReport;
    TextView reportStatus;
    TextView reportContent;
    TextView reportSender;
    TextView reportTimestamp;

    boolean taskApproved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reportRef = new Firebase(getResources().getString(R.string.reports_location));

        final com.getbase.floatingactionbutton.FloatingActionButton actionA
                = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_a);
        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                actionA.setTitle("Action A clicked");
                taskApproved = true;
                changeReportStatus(taskApproved);

            }
        });

        final com.getbase.floatingactionbutton.FloatingActionButton actionB
                = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_b);
        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//                actionB.setTitle("Action B clicked");
                taskApproved = false;
                changeReportStatus(taskApproved);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle intent = getIntent().getExtras();
        parceableReport = intent.getParcelable("parceable_report");

        setTitle(parceableReport.getContent());
        reportStatus = (TextView) findViewById(R.id.report_status);
        reportContent = (TextView) findViewById(R.id.report_content);
        reportSender = (TextView) findViewById(R.id.report_sender);
        reportTimestamp = (TextView) findViewById(R.id.report_timestamp);

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

    private void changeReportStatus(boolean taskApproved) {
        Firebase changeReportStatusRef = reportRef.child(parceableReport.getReportKey());
        Map<String, Object> statusChangeMap = new HashMap<>();

        if (taskApproved) {
            parceableReport.setStatus("Approved");
            statusChangeMap.put("status", "Approved");
            reportStatus.setText("APPROVED");

        } else {
            parceableReport.setStatus("Rejected");
            statusChangeMap.put("status", "Rejected");
            reportStatus.setText("REJECTED");
        }
        changeReportStatusRef.updateChildren(statusChangeMap);
    }

}

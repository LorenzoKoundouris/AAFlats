package com.example.lorenzo.aaflats;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ComposeNew extends AppCompatActivity {

    Firebase reportRef;
    private Spinner typeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_help_black_48dp);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Get extras
        Bundle extras = getIntent().getExtras();
        int parceableComposeType = Integer.parseInt(extras.getString("composeType"));
        final Tenant parceableTenant = extras.getParcelable("parceable_tenant");
        final Flat parceableFlat = extras.getParcelable("parceable_flat");

        reportRef = new Firebase(getResources().getString(R.string.reports_location));

        // Initialise correspondence type: Report - Enquiry
        typeSpinner = (Spinner) findViewById(R.id.type_spinner);
        SpinnerAdapter typeAdapter = ArrayAdapter.createFromResource(this, R.array.report_enquiry,
                R.layout.spinner_dropdown_item2);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setSelection(parceableComposeType);

        final TextView charsTyped = (TextView) findViewById(R.id.chars_typed_textview);
        final EditText composition = (EditText) findViewById(R.id.composed_text_edittext);
        composition.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Display count of characters tenant has typed
                charsTyped.setText(Html.fromHtml(String.valueOf(composition.length()) +
                        "<font color = '#FF5722'><big><b> /250</b></big></font>"));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Send tenant report or enquiry
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.send_composition_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sending...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Report newReport = new Report();

                if (composition.length() > 0) { //ToDo: Maybe enforce additional requirements?
                    try {
                        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyHHmmss");
                        String format = s.format(new Date());
                        newReport.setTimestamp(format);
                        newReport.setContent(composition.getText().toString());
                        newReport.setType(typeSpinner.getSelectedItem().toString());
                        newReport.setSender(parceableTenant.getForename() + " " + parceableTenant.getSurname());
                        newReport.setProperty(parceableTenant.getProperty());
                        newReport.setStatus("pending");
                        newReport.setReportKey("23");

                        // Send to Firebase
                        reportRef.push().setValue(newReport);

                        // Notify all staff
                        sendNotification(newReport);

                        // Inform user of success
                        Toast toast = Toast.makeText(ComposeNew.this, newReport.getType() + " sent. SUCCESS!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        finish();
                        startActivity(new Intent(ComposeNew.this, TenantHomepage.class).putExtra("parceable_flat", parceableFlat));

                    } catch (Exception ex) {
                        Toast toast = Toast.makeText(ComposeNew.this, newReport.getType() + " NOT sent. Failure!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    // Handle attempt to send empty message
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("You have not typed anything in the message box.\"")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }


            }
        });
    }

    /**
     * Sends a Notification object to Firebase
     *
     * @param newReport is the created report or enquiry
     */
    private void sendNotification(final Report newReport) {
        final Firebase notifRef = new Firebase(getResources().getString(R.string.notifications_location));
        final Notification newNoti = new Notification();
        newNoti.setType("Report");
        Query getReportKey = reportRef.orderByChild("reportKey").equalTo("23");
        getReportKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    newNoti.setObjectID(childSnap.getKey());
                }
                notifRef.push().setValue(newNoti);

                Firebase removeTempKey = reportRef.child(newNoti.getObjectID());
                Map<String, Object> rmKeyMap = new HashMap<>();
                rmKeyMap.put("reportKey", null);
                removeTempKey.updateChildren(rmKeyMap);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
        }
        return true;
    }

    /**
     * Handle early page-leaving
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leaving page")
                .setMessage("You have not sent this message yet. Press Yes to discard or No to remain on page.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}

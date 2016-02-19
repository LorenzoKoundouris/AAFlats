package com.example.lorenzo.aaflats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Report attachedReport;
    private Spinner prioritySpinner, flatSpinner;
    private Boolean attached = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Create new task");

        Firebase.setAndroidContext(this);
        Firebase firebaseRef = new Firebase(getString(R.string.firebase_location));
        Firebase taskRef = new Firebase(getString(R.string.tasks_location));
        Firebase reportRef = new Firebase(getString(R.string.reports_location));
        Firebase propertyRef = new Firebase(getString(R.string.properties_location));
        final Firebase flatRef = new Firebase(getString(R.string.flats_location));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ArrayList<Task> mTaskList = new ArrayList<>();
        final ArrayList<String> taskTitles = new ArrayList<>();
        final ArrayList<Property> propertyList = new ArrayList<>();
        final ArrayList<String> propertyAddrLine1s = new ArrayList<>();
        final ArrayList<Flat> flatList = new ArrayList<>();
        final ArrayList<String> flatAddrLine1s = new ArrayList<>();
        final ArrayList<Report> reportList = new ArrayList<>();
        final ArrayList<String> reportTitles = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CardView ntCard = (CardView) findViewById(R.id.nt_card);
        final TextView ntCardText = (TextView) findViewById(R.id.nt_card_text_view);
        final Context context = this;

        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {
                    Task tsk = tskSnapshot.getValue(Task.class);
                    mTaskList.add(tsk);
                    taskTitles.add(tsk.getTitle());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Task: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        reportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot rptSnapshot : dataSnapshot.getChildren()) {
                    Report rpt = rptSnapshot.getValue(Report.class);
                    reportList.add(rpt);
                    if (rpt.getContent().length() > 23) {
                        reportTitles.add(rpt.getContent().substring(0, 20) + "...");
                    } else {
                        reportTitles.add(rpt.getContent());
                    }
                }

                String[] arrayRepTs = new String[reportTitles.size()];
                arrayRepTs = reportTitles.toArray(arrayRepTs);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Attach report").setItems(arrayRepTs, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //Toast.makeText(context, "You selected: ", Toast.LENGTH_LONG).show();
                        System.out.println("You attached: " + reportTitles.get(item));
                        //dialog.dismiss();

                        ntCard.setCardBackgroundColor(R.color.attach_button_focused);
                        ntCardText.setText("\"" + reportTitles.get(item) + "\"");
                        ntCardText.setTypeface(null, Typeface.ITALIC);
                        attachReport(reportList.get(item));
                    }
                });

                final AlertDialog alert = builder.create();
                ntCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.show();
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Report: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        propertyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot prtSnapshot : dataSnapshot.getChildren()) {
                    Property prt = prtSnapshot.getValue(Property.class);
                    propertyList.add(prt);
                    propertyAddrLine1s.add(prtSnapshot.getKey().substring(0, 1).toUpperCase()
                            + prtSnapshot.getKey().substring(1));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        final AutoCompleteTextView actvProperty = (AutoCompleteTextView) findViewById(R.id.nt_property_actv);
        actvProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadCorrespondingFlats(actvProperty.getText().toString().toLowerCase(), flatRef, flatList, flatAddrLine1s);
            }
        });

        actvProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Boolean isProperty = false;
                if(!hasFocus && !Objects.equals(actvProperty.getText().toString(), "")){
                    for(int i = 0; i<propertyAddrLine1s.size(); i++){
                        if(Objects.equals(propertyAddrLine1s.get(i), actvProperty.getText().toString().toLowerCase())){
                            isProperty = true;
                            break;
                        }
                    }
                    if(!isProperty){
                        new AlertDialog.Builder(context)
                                .setTitle("Wrong address")
                                .setMessage("You must enter an existing property")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        actvProperty.setText("");
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            }
        });

        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        AutoCompleteTextView actvPropText = (AutoCompleteTextView) findViewById(R.id.nt_property_actv);
        actvPropText.setAdapter(propertyAdapter);

        prioritySpinner = (Spinner) findViewById(R.id.nt_priority_spinner);
        // Create an ArrayAdapter using the string array
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, R.layout.custom_spinner);
        // Specify the layout to use when the list of choices appears
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the prioritySpinner
        prioritySpinner.setAdapter(priorityAdapter);
        prioritySpinner.setSelection(0);


    }

    private void loadCorrespondingFlats(String s, Firebase flatRef, final ArrayList<Flat> flatList, final ArrayList<String> flatAddrLine1s) {
        Query flatQuery = flatRef.orderByKey().startAt(s); //
        flatQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fltSnapshot : dataSnapshot.getChildren()) {
                    Flat flt = fltSnapshot.getValue(Flat.class);
                    flatList.add(flt);
                    String[] split = fltSnapshot.getKey().split(" - ");
                    flatAddrLine1s.add(split[1].trim().substring(0,1).toUpperCase());
//                    fltSnapshot.getKey().substring(fltSnapshot.getKey()
//                            .indexOf("flat"), fltSnapshot.getKey().length())
//                            .substring(0, 1).toUpperCase() + fltSnapshot.getKey()
//                            .substring(fltSnapshot.getKey().indexOf("flat"),
//                                    fltSnapshot.getKey().length()).substring(1)
                }
                flatSpinner = (Spinner) findViewById(R.id.nt_flat_spinner);
//        ArrayAdapter<CharSequence> flatAdapter = ArrayAdapter.createFromResource(this,
//                R.array.priorities, R.layout.custom_spinner);
                ArrayAdapter<String> flatAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_dropdown_item, flatAddrLine1s);
                flatAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                flatSpinner.setAdapter(flatAdapter);
                flatAdapter.notifyDataSetChanged();
                flatSpinner.setSelection(0);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Flat: " + "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void attachReport(Report attachedReport) {
        this.attachedReport = attachedReport;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.createtask, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leaving page")
                .setMessage("You have not saved this new task. Press Yes to discard or No to remain on page.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
            case R.id.save_new_task:
                //Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT).show();
                saveNewTask();
                break;
            case R.id.action_settings:
                startActivity(new Intent(CreateTask.this, Homepage.class));
                break;
        }
        return true;
    }
    public void saveNewTask(){

        LinearLayout llTitle = (LinearLayout) findViewById(R.id.nt_title_lnr_layout);
        LinearLayout llProperty = (LinearLayout) findViewById(R.id.nt_property_lnr_layout);
        LinearLayout llFlat = (LinearLayout) findViewById(R.id.nt_flat_lnr_layout);
        LinearLayout llNotes = (LinearLayout) findViewById(R.id.nt_notes_lnr_layout);
        LinearLayout llPriority = (LinearLayout) findViewById(R.id.nt_priority_lnr_layout);
        LinearLayout llReport = (LinearLayout) findViewById(R.id.nt_report_lnr_layout);
        LinearLayout llError = (LinearLayout) findViewById(R.id.nt_error_lnr_layout);
        EditText etTitle = (EditText) findViewById(R.id.nt_title_editview);
        AutoCompleteTextView actvProperty = (AutoCompleteTextView) findViewById(R.id.nt_property_actv);
        EditText etDescription = (EditText) findViewById(R.id.nt_notes_editview); //Notes
        TextView cardTV = (TextView) findViewById(R.id.nt_card_text_view);
        boolean validTitle = false;
        boolean validProperty = false;
        boolean validNotes = false;
        boolean validReport = false;

        llError.setVisibility(llError.VISIBLE);
        if(Objects.equals(etTitle.getText().toString(), "")){
            llTitle.setBackgroundColor(Color.parseColor("#EF9A9A"));
            etTitle.setHint(Html.fromHtml("Any meaningful " + "<b><u>" + "title" + "</u></b>"));
        }else{
            llTitle.setBackgroundColor(Color.parseColor("#eeeeee"));
            validTitle = true;
        }
        if(Objects.equals(actvProperty.getText().toString(), "")){
            llProperty.setBackgroundColor(Color.parseColor("#EF9A9A"));
            actvProperty.setHint(Html.fromHtml("<b>" + "i.e" + "</b>" + "<i>" +
                    "\"  12 Trematon Terrace\"" + "</i>"));
            llFlat.setBackgroundColor(Color.parseColor("#EF9A9A"));
        }else{
            llProperty.setBackgroundColor(Color.parseColor("#eeeeee"));
            validProperty = true;
        }
        if(Objects.equals(etDescription.getText().toString(), "")){
            llNotes.setBackgroundColor(Color.parseColor("#EF9A9A"));
            etDescription.setHint(Html.fromHtml("<b>Details about task..</b>\n<i>\"What, how, why..\"</i>"));
            etDescription.setTypeface(Typeface.DEFAULT_BOLD);
        }else{
            llNotes.setBackgroundColor(Color.parseColor("#eeeeee"));
            validNotes = true;
        }
        if(prioritySpinner.getSelectedItem() == null){
            llPriority.setBackgroundColor(Color.parseColor("#EF9A9A"));
        }else{
            llPriority.setBackgroundColor(Color.parseColor("#eeeeee"));
        }
        if(Objects.equals(cardTV.getText().toString(), "ATTACH A REPORT")){
            llReport.setBackgroundColor(Color.parseColor("#EF9A9A"));
        }else{
            llReport.setBackgroundColor(Color.parseColor("#eeeeee"));
            validReport = true;
        }

        if(validTitle && validProperty && validNotes && validReport){
            try {
                Task newTask = new Task();


                newTask.setTitle(etTitle.getText().toString().trim());


                //AutoCompleteTextView actvFlat = (AutoCompleteTextView) findViewById(R.id.nt_flat_spinner);
                newTask.setProperty(actvProperty.getText().toString().toLowerCase() + " - " +
                        flatSpinner.getSelectedItem().toString().toLowerCase());


                newTask.setDescription(etDescription.getText().toString().trim());

                //newTask.setPriority(Integer.parseInt(prioritySpinner.getSelectedItem().toString()));
                newTask.setPriority(prioritySpinner.getSelectedItem().toString().toLowerCase());

                newTask.setStatus(false);

                newTask.setReport(attachedReport.getContent().substring(0, 23));

                Firebase newTaskRef = new Firebase(getString(R.string.tasks_location));
                //newTaskRef.child(newTask.getTitle()).setValue(newTask);
                newTaskRef.push().setValue(newTask);

//                    System.out.println("Task created. SUCCESS!Title: " + newTask.getTitle() +
//                            "\n Description: " + newTask.getDescription() +
//                            "\n Property: " + newTask.getProperty() +
//                            "\n Priority: " + newTask.getPriority() +
//                            "\n Status: " + newTask.getStatus() +
//                            "\n Report: " + newTask.getReport());
                Toast toast = Toast.makeText(CreateTask.this, "Task created. SUCCESS!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                startActivity(new Intent(CreateTask.this, Homepage.class));
            } catch (Exception e) {

                Toast toast = Toast.makeText(CreateTask.this, "Task not created. FAIL", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Another interface callback
    }
}



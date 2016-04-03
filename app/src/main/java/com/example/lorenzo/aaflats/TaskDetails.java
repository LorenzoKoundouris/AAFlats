package com.example.lorenzo.aaflats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskDetails extends AppCompatActivity {

    boolean attemptEdit = false;
    boolean editsCancelled;
    boolean validTitle = true;
    boolean validProperty = true;
    boolean validDescription = true;

    EditText etTaskTitle;// = (EditText) findViewById(R.id.td_title);
    AutoCompleteTextView actvTaskProperty;// = (EditText) findViewById(R.id.et_property_actv);
    EditText etDescription;// = (EditText) findViewById(R.id.td_description);
    Button btReport;// = (Button) findViewById(R.id.bt_report);
    MenuItem miSaveEdit;// = (MenuItem) findViewById(R.id.edit_task);
    CheckBox cbTaskStatus;// = (CheckBox) findViewById(R.id.completion_check_box);
    EditText etTaskNotes;
    TextView tvTaskReportSender;// = (TextView) findViewById(R.id.tv_sender);
    TextView tvTaskReportTimestamp;// = (TextView) findViewById(R.id.tv_timestamp);
    TextView tvTaskReportContent;// = (TextView) findViewById(R.id.tv_report_content);

    ArrayList<Flat> flatList = new ArrayList<>();
    ArrayList<Report> reportList = new ArrayList<>();
    ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    ArrayList<String> reportTitles = new ArrayList<>();
    ArrayList<String> reportKeys = new ArrayList<>();
    ArrayList<String> flatNums = new ArrayList<>();
    Report associatedTaskReport;
    Report attachedReport;
    Task parceableTask = new Task();
    Spinner prioritySpinner, flatSpinner;
    Context context = this;
    String parceableTaskKey;
    String attachedReportKey;
    String[] splitAddress;

    Firebase taskRef;
    Firebase propertyRef;
    Firebase reportRef;
    Firebase flatRef;
    Firebase deleteTask;
    Query findReportQuery;


    SharedPreferences pref;
    SharedPreferences.Editor prefEditor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        pref = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefEditor = pref.edit();

        Firebase.setAndroidContext(this);

        //Clicked-on Task to be passed from Homepage
        Bundle intent = getIntent().getExtras();
        parceableTask = intent.getParcelable("parceable_task");
        parceableTaskKey = intent.getString("parceable_task_key");
        //ArrayList<Task> pTaskList = (ArrayList<Task>) intent.getParcelable("parceable_tasklist");


        //Firebase references
        reportRef = new Firebase(getString(R.string.reports_location));
        propertyRef = new Firebase(getString(R.string.properties_location));
        taskRef = new Firebase(getString(R.string.tasks_location));
        flatRef = new Firebase(getString(R.string.flats_location));
        findReportQuery = reportRef.orderByKey().equalTo(parceableTask.getReport());

        //Page components
        cbTaskStatus = (CheckBox) findViewById(R.id.completion_check_box);
        etTaskTitle = (EditText) findViewById(R.id.td_title);
        actvTaskProperty = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
        flatSpinner = (Spinner) findViewById(R.id.et_flat_spinner);
        flatSpinner.setEnabled(false);
        etDescription = (EditText) findViewById(R.id.td_description);
        prioritySpinner = (Spinner) findViewById(R.id.et_priority_spinner);
        prioritySpinner.setEnabled(false);
        btReport = (Button) findViewById(R.id.bt_report);
        etTaskNotes = (EditText) findViewById(R.id.td_notes);
        tvTaskReportSender = (TextView) findViewById(R.id.tv_sender);
        tvTaskReportTimestamp = (TextView) findViewById(R.id.tv_timestamp);
        tvTaskReportContent = (TextView) findViewById(R.id.tv_report_content);

        //Display Task details on page
        setTitle(parceableTask.getTitle());
        if (parceableTask.getStatus()) {
            cbTaskStatus.setChecked(true);
            prefEditor.putBoolean("taskStatus", true);
        } else {
            cbTaskStatus.setChecked(false);
            prefEditor.putBoolean("taskStatus", false);
        }
        etTaskTitle.setText(parceableTask.getTitle());
        splitAddress = parceableTask.getProperty().split(" - ");
        actvTaskProperty.setText(splitAddress[0].trim());
        //flat done in onDataChange
        etDescription.setText(parceableTask.getDescription());
        etTaskNotes.setText(parceableTask.getNotes());
        prefEditor.putString("taskTitle", parceableTask.getTitle());
        prefEditor.putString("taskProperty", splitAddress[0].trim());
        prefEditor.putString("taskFlat", splitAddress[1].trim());
        prefEditor.putString("taskDescription", parceableTask.getDescription());
        prefEditor.putString("taskNotes", parceableTask.getNotes());


        // Create an ArrayAdapter using the string array
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, R.layout.custom_spinner);
        // Specify the layout to use when the list of choices appears
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the prioritySpinner
        prioritySpinner.setAdapter(priorityAdapter);

        if (parceableTask.getPriority().matches("High")) {
            //high priority
            prioritySpinner.setSelection(0);
            prefEditor.putInt("taskPriority", 0);
        } else if (parceableTask.getPriority().matches("Medium")) {
            //medium priority
            prioritySpinner.setSelection(1);
            prefEditor.putInt("taskPriority", 1);
        } else {
            //low priority
            prioritySpinner.setSelection(2);
            prefEditor.putInt("taskPriority", 2);
        }
        prefEditor.commit();

        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        actvTaskProperty.setAdapter(propertyAdapter);

        propertyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot prtSnapshot : dataSnapshot.getChildren()) {
                    Property prt = prtSnapshot.getValue(Property.class);
                    propertyAddrLine1s.add(prt.getAddrline1().trim());
//                    propertyAddrLine1s.add(prtSnapshot.getKey().substring(0, 1).toUpperCase()
//                            + prtSnapshot.getKey().substring(1));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        loadCorrespondingFlats();

        //Get-Report is ok
        findReportQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report foundReport = childSnap.getValue(Report.class);
                    associatedTaskReport = foundReport;
                    prefEditor.putString("reportKey", childSnap.getKey());
                    prefEditor.commit();
                }

                try {
                    String lc = associatedTaskReport.getContent().toLowerCase().substring(0, 1).toUpperCase()
                            + associatedTaskReport.getContent().toLowerCase().substring(1);
                    if (associatedTaskReport.getContent().length() > 23) {
                        btReport.setText("\"" + lc.substring(0, 20) + "..." + "\"");
                    } else {
                        btReport.setText("\"" + lc + "\"");
                    }

                    tvTaskReportTimestamp.setText(associatedTaskReport.getTimestamp());
                    tvTaskReportSender.setText(associatedTaskReport.getSender());
                    tvTaskReportContent.setText(associatedTaskReport.getContent());

                    prefEditor.putString("btReportText", btReport.getText().toString());
                    prefEditor.putString("taskReportTimestamp", associatedTaskReport.getTimestamp());
                    prefEditor.putString("taskReportSender", associatedTaskReport.getSender());
                    prefEditor.putString("taskReportContent", associatedTaskReport.getContent());
                    prefEditor.commit();
                } catch (Exception ex) {
                    Toast toast = Toast.makeText(TaskDetails.this, "Associated task report not found", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast toast = Toast.makeText(TaskDetails.this, "Something went wrong: " + firebaseError.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        //Get all reports from Firebase to display in popup window and attach to task
        Query onlyApprovedReports = reportRef.orderByChild("status").equalTo("Approved");
        onlyApprovedReports.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                reportList.clear();
                reportKeys.clear();
                reportTitles.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report firebaseReport = childSnap.getValue(Report.class);
                    reportList.add(firebaseReport);
                    reportKeys.add(childSnap.getKey()); //list of keys of each report
                    if (firebaseReport.getContent().length() > 23) {
                        reportTitles.add(firebaseReport.getContent().substring(0, 20) + "...");
                    } else {
                        reportTitles.add(firebaseReport.getContent());
                    }
                }
                String[] arrayReportTitles = new String[reportTitles.size()];
                arrayReportTitles = reportTitles.toArray(arrayReportTitles);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setTitle("Attach report").setItems(arrayReportTitles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        System.out.println("You attached: " + reportTitles.get(item));
                        //dialog.dismiss();

                        btReport.setText("\"" + reportTitles.get(item) + "\"");
                        btReport.setTypeface(null, Typeface.ITALIC);
                        attachReport(reportList.get(item));
                        attachedReportKey = reportKeys.get(item);
                        tvTaskReportContent.setText(attachedReport.getContent());
                        tvTaskReportSender.setText(attachedReport.getSender());
//                        StringBuilder ts = new StringBuilder(attachedReport.getTimestamp());
//                        ts.insert(2, "/");
//                        ts.insert(5, "/");
//                        ts.insert(10, " ");
//                        ts.insert(13, ":");
//                        ts.insert(16, ":");
                        tvTaskReportTimestamp.setText(attachedReport.getTimestamp());
                    }
                });
                final AlertDialog alertDialog = alertBuilder.create();
                btReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        cbTaskStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Firebase changeTaskStatusRef = taskRef.child(parceableTaskKey);
                Map<String, Object> statusChangeMap = new HashMap<>();
                Map<String, Object> timestampChangeMap = new HashMap<>();
                if (isChecked) {
                    parceableTask.setStatus(true);
                    SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyHHmmss");
                    String format = s.format(new Date());
                    parceableTask.setCompletionTimestamp(format);
                    statusChangeMap.put("status", true);
                    timestampChangeMap.put("completionTimestamp", format);

                    StringBuilder ts = new StringBuilder(format);
                    ts.insert(2, "/");
                    ts.insert(5, "/");
                    ts.insert(10, " ");
                    ts.insert(13, ":");
                    ts.insert(16, ":");
                    Toast toast = Toast.makeText(TaskDetails.this, ts, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    parceableTask.setStatus(false);
                    parceableTask.setCompletionTimestamp("pending");
                    timestampChangeMap.put("completionTimestamp", "pending");
                    statusChangeMap.put("status", false);
                    //cbTaskStatus.setChecked(false);
                }
                changeTaskStatusRef.updateChildren(statusChangeMap);
                changeTaskStatusRef.updateChildren(timestampChangeMap);
                TextView completionTV = (TextView) findViewById(R.id.completion_text_view);
                completionTV.setTextColor(Color.parseColor("#FF5722"));
            }
        });

    }//END OF onCreate()

    private void loadCorrespondingFlats() {
        Query flatQuery = flatRef.orderByChild("addressLine1").equalTo(actvTaskProperty.getText().toString().trim());
        flatQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flatList.clear();
                flatNums.clear();
                for (DataSnapshot fltSnapshot : dataSnapshot.getChildren()) {
                    Flat flt = fltSnapshot.getValue(Flat.class);
                    flatList.add(flt);
                    flatNums.add(flt.getFlatNum());
//                    String[] split = fltSnapshot.getKey().split(" - ");
//                    flatNums.add(split[1].trim().substring(0, 1).toUpperCase() +
//                            split[1].substring(1).trim());
                }

                ArrayAdapter<String> flatAdapter = new ArrayAdapter<>(getBaseContext(),
                        R.layout.spinner_dropdown_item, flatNums);
                flatAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                flatSpinner.setAdapter(flatAdapter);
                flatAdapter.notifyDataSetChanged();
                for (int i = 0; i < flatNums.size(); i++) {
                    if ((splitAddress[1].trim().substring(0, 1).toUpperCase()
                            + splitAddress[1].substring(1).trim()).matches(flatSpinner.getItemAtPosition(i).toString())) {
                        flatSpinner.setSelection(i);
                        prefEditor.putInt("tFlatSpinner", i);
                        prefEditor.commit();
                        break;
                    }
                }
            }

//            if (Objects.equals(splitAddress[1].trim().substring(0, 1).toUpperCase()
//            + splitAddress[1].substring(1).trim(), flatSpinner.getItemAtPosition(i).toString()))

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast toast = Toast.makeText(TaskDetails.this, "Flat not found: " +
                        firebaseError.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    private void attachReport(Report attachedReport) {
        this.attachedReport = attachedReport;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!attemptEdit) {
            getMenuInflater().inflate(R.menu.task_details, menu);
            miSaveEdit = menu.findItem(R.id.edit_task);
        } else {
            getMenuInflater().inflate(R.menu.task_details_save, menu);
            miSaveEdit = menu.findItem(R.id.save_edited_task);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (attemptEdit && !editsCancelled || attemptEdit) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Leaving page")
                    .setMessage("You have not saved changes made to this task. Press Yes to discard or No to remain on page.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                onBackPressed();
                break;

            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;

            case R.id.delete_task:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete?")
                        .setMessage("Are you sure you want to permanently delete this task?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(TaskDetails.this, Homepage.class));
                                String delTskURL = taskRef + "/" + parceableTaskKey;
                                try {
                                    deleteTask = new Firebase(delTskURL);
                                    deleteTask.removeValue();
                                } catch (Exception ex) {
                                    Toast toast = Toast.makeText(TaskDetails.this, "Deletion failed", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }

                                Toast toast = Toast.makeText(TaskDetails.this, "Task deleted", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();

                break;

            case R.id.edit_task:
                Toast.makeText(getApplicationContext(), "Edit button clicked", Toast.LENGTH_SHORT).show();
                editTaskDetails();
                break;
            case R.id.save_edited_task:
                Toast toast = Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                saveAllChanges();
                break;
            case R.id.cancel_edit_task:
                editsCancelled = true;
                saveAllChanges();
                break;
        }
        return true;
    }

    private void saveAllChanges() {
        if (!editsCancelled) {
            try {
                parceableTask.setTitle(etTaskTitle.getText().toString());
                parceableTask.setProperty(actvTaskProperty.getText().toString() + " - " +
                        flatSpinner.getSelectedItem().toString());
                parceableTask.setDescription(etDescription.getText().toString());
                parceableTask.setNotes(etTaskNotes.getText().toString());
                parceableTask.setPriority(prioritySpinner.getSelectedItem().toString());

                if (btReport.getText().toString().matches(pref.getString("btReportText", "crashReport"))) {
                    parceableTask.setReport(pref.getString("reportKey", "crashReport"));
                } else {
                    parceableTask.setReport(attachedReportKey);
                }

//                if(Objects.equals(attachedReportKey, "")){
//                   parceableTask.setReport(pref.getString("reportKey", "crashReportKey"));
//                } else {
//                    parceableTask.setReport(attachedReportKey);
//                }

                //taskRef.child(parceableTask.getTaskKey()).setValue(parceableTask);
                taskRef.child(parceableTaskKey).setValue(parceableTask);
                Toast toast = Toast.makeText(TaskDetails.this, "Task edited! SUCCESS!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } catch (Exception ex) {
                Toast toast = Toast.makeText(TaskDetails.this, "Task not edited. FAIL", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else {
            etTaskTitle.setText(pref.getString("taskTitle", "crashTitle"));
            //String[] split = parceableTask.getProperty().split("-");
            actvTaskProperty.setText(pref.getString("taskProperty", "crashProperty"));
//            for (int i=0; i < flatNums.size(); i++){
//                if(Objects.equals(flatSpinner.getSelectedItem().toString(), flatNums.get(i))){
//                    flatSpinner.setSelection(i);
//                }
//            }
            flatSpinner.setSelection(pref.getInt("tFlatSpinner", 0));
            etDescription.setText(pref.getString("taskDescription", "crashDescription"));
            etTaskNotes.setText(pref.getString("taskNotes", "crashNotes"));
            prioritySpinner.setSelection(pref.getInt("taskPriority", 0));
            cbTaskStatus.setChecked(pref.getBoolean("taskStatus", false));
            btReport.setText(pref.getString("btReportText", "crashReport"));
            tvTaskReportSender.setText(pref.getString("taskReportSender", "crashSender"));
            tvTaskReportTimestamp.setText(pref.getString("taskReportTimestamp", "crashTimestamp"));
            tvTaskReportContent.setText(pref.getString("taskReportContent", "crashReportContent"));
        }

        etTaskTitle.setEnabled(false);
        actvTaskProperty.setEnabled(false);
        flatSpinner.setEnabled(false);
        etDescription.setEnabled(false);
        etTaskNotes.setEnabled(false);
        prioritySpinner.setEnabled(false);
        btReport.setEnabled(false);

        attemptEdit = false;
        invalidateOptionsMenu();
    }

    private void editTaskDetails() {
        editsCancelled = false;
        try {
            etTaskTitle.setEnabled(true);
            actvTaskProperty.setEnabled(true);
            flatSpinner.setEnabled(true);
            etDescription.setEnabled(true);
            etTaskNotes.setEnabled(true);
            prioritySpinner.setEnabled(true);
            btReport.setEnabled(true);

            etTaskTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etTaskTitle.getText().toString().matches(pref.getString("taskTitle", "crashTitle"))) {
                        etTaskTitle.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etTaskTitle.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });
            actvTaskProperty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (actvTaskProperty.getText().toString().matches(pref.getString("taskProperty", "crashProperty"))) {
                        actvTaskProperty.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        actvTaskProperty.setTextColor(Color.parseColor("#FF5722"));
                    }
                    loadCorrespondingFlats();
                }
            });

            etDescription.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etDescription.getText().toString().matches(pref.getString("taskDescription", "crashDescription"))) {
                        etDescription.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etDescription.setTextColor(Color.parseColor("#FF5722"));
                    }

                }
            });

            etTaskNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etTaskNotes.getText().toString().matches(pref.getString("taskNotes", "crashNotes"))) {
                        etTaskNotes.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etTaskNotes.setTextColor(Color.parseColor("#FF5722"));
                    }

                }
            });


            btReport.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (btReport.getText().toString().matches(pref.getString("btReportText", "crashbtReportText"))) {
                        btReport.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        btReport.setTextColor(Color.parseColor("#FF5722"));
                    }

                }
            });

            etTaskTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (etTaskTitle.getText().toString().matches("")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null title")
                                .setMessage("Whoops! Looks like you forgot to set a Title!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        etTaskTitle.setText(pref.getString("taskTitle", "crashTitle")); // parceableTask.getTitle()
                                        etTaskTitle.setSelectAllOnFocus(true);
                                        etTaskTitle.setSelection(etTaskTitle.length());
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        validTitle = false;
                    } else {
                        validTitle = true;
                    }
                }
            });
//
//            final Firebase flatRef = new Firebase(getString(R.string.flats_location));
//            final AutoCompleteTextView actvTaskProperty = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
//            actvTaskProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    loadCorrespondingFlats(actvTaskProperty.getText().toString().toLowerCase(), flatRef, flatList, flatNums);
//                }
//            });

            actvTaskProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    boolean isProperty = false;
                    if (!hasFocus && !actvTaskProperty.getText().toString().matches("")) {
                        for (int i = 0; i < propertyAddrLine1s.size(); i++) {
                            if (propertyAddrLine1s.get(i).matches(actvTaskProperty.getText().
                                    toString().trim())) {
                                isProperty = true;
                                break;
                            }
                        }
                        if (!isProperty) {
                            new AlertDialog.Builder(v.getContext())
                                    .setTitle("Wrong address")
                                    .setMessage("You must enter an existing property")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //String[] split = parceableTask.getProperty().split("-");
                                            actvTaskProperty.setText(pref.getString("taskProperty", "crashProperty"));//split[0].trim().substring(0, 1).toUpperCase()
                                            actvTaskProperty.requestFocus();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                        validProperty = false;
                    } else if (!hasFocus && actvTaskProperty.getText().toString().matches("")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null address")
                                .setMessage("Whoops! Looks like you forgot to set a Property!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //String[] split = parceableTask.getProperty().split("-");
                                        actvTaskProperty.setText(pref.getString("taskProperty", "crashProperty")); //split[0].trim().substring(0, 1).toUpperCase()
                                        actvTaskProperty.requestFocus();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        validProperty = false;
                    } else {
                        validProperty = true;
                    }
                }
            });

            etDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && etDescription.getText().toString().matches("")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null task description")
                                .setMessage("Whoops! Looks like you forgot to set Task Description!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        etDescription.setText(pref.getString("taskDescription", "crashDescription")); //parceableTask.getDescription()
                                        etDescription.setSelectAllOnFocus(true);
                                        etDescription.setSelection(etDescription.length());
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        validDescription = false;
                    } else {
                        validDescription = true;
                    }
                }
            });

            etTaskNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && etTaskNotes.getText().toString().matches("")) {
                        etTaskNotes.setText(pref.getString("taskNotes", "crashNotes"));
                        etTaskNotes.requestFocus();
                    }
                }
            });


            if (validTitle && validProperty && validDescription) {
                attemptEdit = true;
                invalidateOptionsMenu();
            }

        } catch (Exception ex) {
            Toast toast = Toast.makeText(TaskDetails.this, "Components not enabled.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

}

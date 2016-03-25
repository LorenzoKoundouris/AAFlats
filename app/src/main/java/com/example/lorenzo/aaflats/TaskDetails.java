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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskDetails extends AppCompatActivity {

    boolean attemptEdit = false;
    boolean editscancelled;
    boolean validTitle = true;
    boolean validProperty = true;
    boolean validNotes = true;
    EditText tdTitle;// = (EditText) findViewById(R.id.td_title);
    AutoCompleteTextView actvProperty;// = (EditText) findViewById(R.id.et_property_actv);
    EditText tdNotes;// = (EditText) findViewById(R.id.td_notes);
    Button btReport;// = (Button) findViewById(R.id.bt_report);
    MenuItem saveEdit;// = (MenuItem) findViewById(R.id.edit_task);
    CheckBox taskCompletionCheckBox;// = (CheckBox) findViewById(R.id.completion_check_box);
    final ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    final ArrayList<Report> reportList = new ArrayList<>();
    final ArrayList<Report> foundReportList = new ArrayList<>();
    final ArrayList<String> reportTitles = new ArrayList<>();
    final ArrayList<String> reportKeys = new ArrayList<>();
    final ArrayList<Flat> flatList = new ArrayList<>();
    final ArrayList<String> flatNums = new ArrayList<>();
    TextView tvSender;// = (TextView) findViewById(R.id.tv_sender);
    TextView tvTimestamp;// = (TextView) findViewById(R.id.tv_timestamp);
    TextView tvReportContent;// = (TextView) findViewById(R.id.tv_report_content);
    private Spinner prioritySpinner, flatSpinner;
    private Report attachedReport;
    final Context context = this;
    Task parceableTask = new Task();
    String parceableTaskKey;
    String attachedReportKey;

    Firebase taskRef;
    Firebase propertyRef;
    Firebase reportRef;
    Firebase flatRef;
    Firebase deleteTask;

    Query findReportQuery;

    String[] splitProp;

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
        tdTitle = (EditText) findViewById(R.id.td_title);
        actvProperty = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
        flatSpinner = (Spinner) findViewById(R.id.et_flat_spinner);
        flatSpinner.setEnabled(false);
        tdNotes = (EditText) findViewById(R.id.td_notes);
        prioritySpinner = (Spinner) findViewById(R.id.et_priority_spinner);
        prioritySpinner.setEnabled(false);
        btReport = (Button) findViewById(R.id.bt_report);
        taskCompletionCheckBox = (CheckBox) findViewById(R.id.completion_check_box);
        tvSender = (TextView) findViewById(R.id.tv_sender);
        tvTimestamp = (TextView) findViewById(R.id.tv_timestamp);
        tvReportContent = (TextView) findViewById(R.id.tv_report_content);

        //Display Task details on page
        setTitle(parceableTask.getTitle());
        if (parceableTask.getStatus()) {
            taskCompletionCheckBox.setChecked(true);
            prefEditor.putBoolean("tStatus", true);
        } else {
            taskCompletionCheckBox.setChecked(false);
            prefEditor.putBoolean("tStatus", false);
        }
        tdTitle.setText(parceableTask.getTitle());
        prefEditor.putString("tTitle", parceableTask.getTitle());
        splitProp = parceableTask.getProperty().split(" - ");
        actvProperty.setText(splitProp[0].trim());
        prefEditor.putString("tPropertyA1", actvProperty.getText().toString());
        prefEditor.putString("tFlat", splitProp[1].trim());
        //flat done in onDataChange
        tdNotes.setText(parceableTask.getDescription());
        prefEditor.putString("tNotes", parceableTask.getDescription());
        if (Objects.equals(parceableTask.getPriority().toLowerCase(), "high")) {
            //high priority
            prioritySpinner.setSelection(0);
            prefEditor.putInt("tPriority", 0);
        } else if (Objects.equals(parceableTask.getPriority().toLowerCase(), "medium")) {
            //medium priority
            prioritySpinner.setSelection(1);
            prefEditor.putInt("tPriority", 1);
        } else {
            prioritySpinner.setSelection(2);
            prefEditor.putInt("tPriority", 2);
        }
        prefEditor.commit();
        // Create an ArrayAdapter using the string array
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, R.layout.custom_spinner);
        // Specify the layout to use when the list of choices appears
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the prioritySpinner
        prioritySpinner.setAdapter(priorityAdapter);

        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        actvProperty.setAdapter(propertyAdapter);

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

        findReportQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report associatedReport = childSnap.getValue(Report.class);
                    foundReportList.add(associatedReport);
                    prefEditor.putString("rKey", childSnap.getKey());
                    prefEditor.commit();
                }

                try {
                    String lc = foundReportList.get(0).getContent().toLowerCase().
                            substring(0, 1).toUpperCase() + foundReportList.get(0).getContent().
                            toLowerCase().substring(1);
                    if (foundReportList.get(0).getContent().length() > 23) {
                        btReport.setText("\"" + lc.substring(0, 20) + "..." + "\"");
                    } else {
                        btReport.setText("\"" + lc + "\"");
                    }
                    tvTimestamp.setText(foundReportList.get(0).getTimestamp());
                    tvSender.setText(foundReportList.get(0).getSender());
                    tvReportContent.setText(foundReportList.get(0).getContent());
                    prefEditor.putString("btReportText", btReport.getText().toString());
                    prefEditor.putString("trTimestamp", tvTimestamp.getText().toString());
                    prefEditor.putString("trSender", tvSender.getText().toString());
                    prefEditor.putString("trContent", tvReportContent.getText().toString());
                    prefEditor.commit();
                } catch (Exception ex) {
                    Toast toast = Toast.makeText(TaskDetails.this, "Associated report not found", Toast.LENGTH_SHORT);
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

        reportRef.addValueEventListener(new ValueEventListener() {
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
                String[] arrayRepTs = new String[reportTitles.size()];
                arrayRepTs = reportTitles.toArray(arrayRepTs);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setTitle("Attach report").setItems(arrayRepTs, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        System.out.println("You attached: " + reportTitles.get(item));
                        //dialog.dismiss();

                        btReport.setText("\"" + reportTitles.get(item) + "\"");
                        btReport.setTypeface(null, Typeface.ITALIC);
                        attachReport(reportList.get(item));
                        attachedReportKey = reportKeys.get(item);
                        tvReportContent.setText(attachedReport.getContent());
                        tvSender.setText(attachedReport.getSender());
//                        StringBuilder ts = new StringBuilder(attachedReport.getTimestamp());
//                        ts.insert(2, "/");
//                        ts.insert(5, "/");
//                        ts.insert(10, " ");
//                        ts.insert(13, ":");
//                        ts.insert(16, ":");
                        tvTimestamp.setText(attachedReport.getTimestamp());
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

        taskCompletionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                    //taskCompletionCheckBox.setChecked(true);
                } else if (!isChecked) {
                    parceableTask.setStatus(false);
                    parceableTask.setCompletionTimestamp("pending");
                    timestampChangeMap.put("completionTimestamp", "pending");
                    statusChangeMap.put("status", false);
                    //taskCompletionCheckBox.setChecked(false);
                }
                changeTaskStatusRef.updateChildren(statusChangeMap);
                changeTaskStatusRef.updateChildren(timestampChangeMap);
                TextView completionTV = (TextView) findViewById(R.id.completion_text_view);
                completionTV.setTextColor(Color.parseColor("#FF5722"));
            }
        });

    }//END OF onCreate()

    private void loadCorrespondingFlats() {
        Query flatQuery = flatRef.orderByChild("addressLine1").equalTo(actvProperty.getText().toString());
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
                    if (Objects.equals(splitProp[1].trim().substring(0, 1).toUpperCase()
                            + splitProp[1].substring(1).trim(), flatSpinner.getItemAtPosition(i).toString())) {
                        flatSpinner.setSelection(i);
                        prefEditor.putInt("tFlatSpinner", i);
                        prefEditor.commit();
                        break;
                    }
                }
            }

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
            saveEdit = menu.findItem(R.id.edit_task);
        } else {
            getMenuInflater().inflate(R.menu.task_details_save, menu);
            saveEdit = menu.findItem(R.id.save_edited_task);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (attemptEdit && !editscancelled || attemptEdit) {
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
        } else{
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
                                try{
                                    deleteTask = new Firebase(delTskURL);
                                    deleteTask.removeValue();
                                }catch(Exception ex){
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
                editscancelled = true;
                saveAllChanges();
                break;
        }
        return true;
    }

    private void saveAllChanges() {
        if (!editscancelled) {
            try {
                parceableTask.setTitle(tdTitle.getText().toString());
                parceableTask.setProperty(actvProperty.getText().toString().toLowerCase() + " - " +
                        flatSpinner.getSelectedItem().toString().toLowerCase());
                parceableTask.setDescription(tdNotes.getText().toString());
                parceableTask.setPriority(prioritySpinner.getSelectedItem().toString().toLowerCase());

                if(Objects.equals(btReport.getText().toString(), pref.getString("btReportText", "crashReport"))){
                    parceableTask.setReport(pref.getString("rKey", "crashReport"));
                } else {
                    parceableTask.setReport(attachedReportKey);
                }

//                if(Objects.equals(attachedReportKey, "")){
//                   parceableTask.setReport(pref.getString("rKey", "crashReportKey"));
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
            tdTitle.setText(pref.getString("tTitle", "crashTitle"));
            //String[] split = parceableTask.getProperty().split("-");
            actvProperty.setText(pref.getString("tPropertyA1", "crashProperty"));
//            for (int i=0; i < flatNums.size(); i++){
//                if(Objects.equals(flatSpinner.getSelectedItem().toString(), flatNums.get(i))){
//                    flatSpinner.setSelection(i);
//                }
//            }
            flatSpinner.setSelection(pref.getInt("tFlatSpinner", 0));
            tdNotes.setText(pref.getString("tNotes", "crashNotes"));
            prioritySpinner.setSelection(pref.getInt("tPriority", 0));
            taskCompletionCheckBox.setChecked(pref.getBoolean("tStatus", false));
            btReport.setText(pref.getString("btReportText", "crashReport"));
            tvSender.setText(pref.getString("trSender", "crashSender"));
            tvTimestamp.setText(pref.getString("trTimestamp", "crashTimestamp"));
            tvReportContent.setText(pref.getString("trContent", "crashReportContent"));
        }

        tdTitle.setEnabled(false);
        actvProperty.setEnabled(false);
        flatSpinner.setEnabled(false);
        tdNotes.setEnabled(false);
        prioritySpinner.setEnabled(false);
        btReport.setEnabled(false);

        attemptEdit = false;
        invalidateOptionsMenu();
    }

    private void editTaskDetails() {
        editscancelled = false;
        try {
            tdTitle.setEnabled(true);
            actvProperty.setEnabled(true);
            flatSpinner.setEnabled(true);
            tdNotes.setEnabled(true);
            prioritySpinner.setEnabled(true);
            btReport.setEnabled(true);

            tdTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (Objects.equals(tdTitle.getText().toString(), pref.getString("tTitle", "crashTitle"))) {
                        tdTitle.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        tdTitle.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });
            actvProperty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (Objects.equals(actvProperty.getText().toString(), pref.getString("tPropertyA1", "crashProperty"))) {
                        actvProperty.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        actvProperty.setTextColor(Color.parseColor("#FF5722"));
                    }
                    loadCorrespondingFlats();
                }
            });

            tdNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (Objects.equals(tdNotes.getText().toString(), pref.getString("tNotes", "crashNotes"))) {
                        tdNotes.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        tdNotes.setTextColor(Color.parseColor("#FF5722"));
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
                    if (Objects.equals(btReport.getText().toString(), pref.getString("btReportText", "crashbtReportText"))) {
                        btReport.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        btReport.setTextColor(Color.parseColor("#FF5722"));
                    }

                }
            });

            tdTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (Objects.equals(tdTitle.getText().toString(), "")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null title")
                                .setMessage("Whoops! Looks like you forgot to set a Title!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        tdTitle.setText(pref.getString("tTitle", "crashTitle")); // parceableTask.getTitle()
                                        tdTitle.setSelectAllOnFocus(true);
                                        tdTitle.setSelection(tdTitle.length());
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
//            final AutoCompleteTextView actvProperty = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
//            actvProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    loadCorrespondingFlats(actvProperty.getText().toString().toLowerCase(), flatRef, flatList, flatNums);
//                }
//            });

            actvProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Boolean isProperty = false;
                    if (!hasFocus && !Objects.equals(actvProperty.getText().toString(), "")) {
                        for (int i = 0; i < propertyAddrLine1s.size(); i++) {
                            if (Objects.equals(propertyAddrLine1s.get(i), actvProperty.getText().
                                    toString().toLowerCase())) {
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
                                            actvProperty.setText(pref.getString("tPropertyA1", "crashProperty"));//split[0].trim().substring(0, 1).toUpperCase()
                                            actvProperty.setSelectAllOnFocus(true);
                                            actvProperty.setSelection(actvProperty.length());
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                        validProperty = false;
                    } else if (!hasFocus && Objects.equals(actvProperty.getText().toString(), "")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null address")
                                .setMessage("Whoops! Looks like you forgot to set a Property!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //String[] split = parceableTask.getProperty().split("-");
                                        actvProperty.setText(pref.getString("tPropertyA1", "crashProperty")); //split[0].trim().substring(0, 1).toUpperCase()
                                        actvProperty.setSelectAllOnFocus(true);
                                        actvProperty.setSelection(actvProperty.length());
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

            tdNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && Objects.equals(tdNotes.getText().toString(), "")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null task description")
                                .setMessage("Whoops! Looks like you forgot to set Task Notes!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        tdNotes.setText(pref.getString("tNotes", "crashNotes")); //parceableTask.getDescription()
                                        tdNotes.setSelectAllOnFocus(true);
                                        tdNotes.setSelection(tdNotes.length());
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        validNotes = false;
                    } else {
                        validNotes = true;
                    }
                }
            });

            if (validTitle && validProperty && validNotes) {
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

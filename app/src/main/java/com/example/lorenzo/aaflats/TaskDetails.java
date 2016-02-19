package com.example.lorenzo.aaflats;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
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
    MenuItem saveEdit;// = (MenuItem) findViewById(R.id.edit_save_task);
    CheckBox compCB;// = (CheckBox) findViewById(R.id.completion_check_box);
    final ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    final ArrayList<Report> reportList = new ArrayList<>();
    final ArrayList<Report> searchReportList = new ArrayList<>();
    final ArrayList<String> reportTitles = new ArrayList<>();
    final ArrayList<String> reportKeys = new ArrayList<>();
    final ArrayList<Flat> flatList = new ArrayList<>();
    final ArrayList<String> flatAddrLine1s = new ArrayList<>();
    TextView tvSender;// = (TextView) findViewById(R.id.tv_sender);
    TextView tvTimestamp;// = (TextView) findViewById(R.id.tv_timestamp);
    TextView tvReportContent;// = (TextView) findViewById(R.id.tv_report_content);
    private Spinner prioritySpinner, flatSpinner;
    private Report attachedReport;
    final Context context = this;
    Task parceableTask = new Task();
    Firebase editTaskRef;
    String attachedReportKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);
        Firebase refRep = new Firebase(getResources().getString(R.string.reports_location));

        Firebase propertyRef = new Firebase(getString(R.string.properties_location));
        editTaskRef = new Firebase(getString(R.string.tasks_location));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tdTitle = (EditText) findViewById(R.id.td_title);
        actvProperty = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
        tdNotes = (EditText) findViewById(R.id.td_notes);
        btReport = (Button) findViewById(R.id.bt_report);
        compCB = (CheckBox) findViewById(R.id.completion_check_box);
        tvSender = (TextView) findViewById(R.id.tv_sender);
        tvTimestamp = (TextView) findViewById(R.id.tv_timestamp);
        tvReportContent = (TextView) findViewById(R.id.tv_report_content);

        Bundle intent = getIntent().getExtras();
        parceableTask = intent.getParcelable("parceable_task");
        //ArrayList<Task> pTaskList = (ArrayList<Task>) intent.getParcelable("parceable_tasklist");


        if (!Objects.equals(parceableTask.getTitle(), "")) {
            setTitle(parceableTask.getTitle());
        }
        if (parceableTask.getStatus()) {
            compCB.setChecked(true);
        } else {
            compCB.setChecked(false);
        }


        propertyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot prtSnapshot : dataSnapshot.getChildren()) {
                    Property prt = prtSnapshot.getValue(Property.class);
                    //propertyList.add(prt);
                    propertyAddrLine1s.add(prtSnapshot.getKey().substring(0, 1).toUpperCase()
                            + prtSnapshot.getKey().substring(1));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        final Firebase flatRef = new Firebase(getString(R.string.flats_location));
        Query findReport = refRep.orderByKey().equalTo(parceableTask.getReport());
        findReport.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report thisReport = childSnap.getValue(Report.class);
                    searchReportList.add(thisReport);
                }
                try {
                    tdTitle.setText(parceableTask.getTitle());
                    String[] splitProp = parceableTask.getProperty().split(" - ");
                    actvProperty.setText(splitProp[0].trim());
                    loadCorrespondingFlats(actvProperty.getText().toString().toLowerCase(),
                            flatRef, flatList, flatAddrLine1s);

                    int kk = flatSpinner.getCount();
                    kk = flatSpinner.getChildCount();
                    for (int i=0; i < flatSpinner.getCount(); i++){
                        if(Objects.equals(splitProp[1], flatSpinner.getItemAtPosition(i).toString())){
                            flatSpinner.setSelection(i);
                            break;
                        }
                    }

                    tdNotes.setText(parceableTask.getDescription());
                    if(Objects.equals(parceableTask.getPriority().toLowerCase(), "high")){
                        //high priority
                        prioritySpinner.setSelection(0);
                    } else if(Objects.equals(parceableTask.getPriority().toLowerCase(), "medium")){
                        //medium priority
                        prioritySpinner.setSelection(1);
                    } else {
                        prioritySpinner.setSelection(2);
                    }
//                    tdPriority.setText(parceableTask.getPriority().substring(0, 1).toUpperCase()
//                            + parceableTask.getPriority().substring(1));
                    if (searchReportList.get(0).getContent().length() > 23) {
                        btReport.setText(searchReportList.get(0).getContent().substring(0, 20) + "...");
                    } else {
                        btReport.setText(searchReportList.get(0).getContent());
                    }
                    tvTimestamp.setText(searchReportList.get(0).getTimestamp());
                    tvSender.setText(searchReportList.get(0).getSender());
                    tvReportContent.setText(searchReportList.get(0).getContent());

                } catch (Exception ex) {
                    Toast.makeText(TaskDetails.this, "task details not loaded", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        refRep.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                reportList.clear();
                reportKeys.clear();
                reportTitles.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report associatedReport = childSnap.getValue(Report.class);
                    reportList.add(associatedReport);
                    reportKeys.add(childSnap.getKey());
                    if (associatedReport.getContent().length() > 23) {
                        reportTitles.add(associatedReport.getContent().substring(0, 20) + "...");
                    } else {
                        reportTitles.add(associatedReport.getContent());
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
                final AlertDialog alert = builder.create();
                btReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.show();
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        compCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Firebase thisTaskCheck = editTaskRef.child(parceableTask.getTaskKey());
                Map<String, Object> statusChangeMap = new HashMap<String, Object>();
                if (isChecked) {
                    parceableTask.setStatus(true);
                    statusChangeMap.put("status", "true");
                    //compCB.setChecked(true);
                } else if (!isChecked) {
                    parceableTask.setStatus(false);
                    statusChangeMap.put("status", "false");
                    //compCB.setChecked(false);
                }
                thisTaskCheck.updateChildren(statusChangeMap);
            }
        });




        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        AutoCompleteTextView actvPropText = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
        actvPropText.setAdapter(propertyAdapter);

        prioritySpinner = (Spinner) findViewById(R.id.et_priority_spinner);
        prioritySpinner.setEnabled(false);
        flatSpinner = (Spinner) findViewById(R.id.et_flat_spinner);
        flatSpinner.setEnabled(false);
        // Create an ArrayAdapter using the string array
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, R.layout.custom_spinner);
        // Specify the layout to use when the list of choices appears
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the prioritySpinner
        prioritySpinner.setAdapter(priorityAdapter);
    }

    private void loadCorrespondingFlats(String s, Firebase flatRef, final ArrayList<Flat> flatList, final ArrayList<String> flatAddrLine1s) {
        Query flatQuery = flatRef.orderByChild("addressLine1").equalTo(s);
        flatList.clear();
        flatAddrLine1s.clear();
        flatQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fltSnapshot : dataSnapshot.getChildren()) {
                    Flat flt = fltSnapshot.getValue(Flat.class);
                    flatList.add(flt);
                    String[] split = fltSnapshot.getKey().split(" - ");
                    flatAddrLine1s.add(split[1].trim().substring(0, 1).toUpperCase() + split[1].substring(1).trim());
//                    fltSnapshot.getKey().substring(fltSnapshot.getKey()
//                            .indexOf("flat"), fltSnapshot.getKey().length())
//                            .substring(0, 1).toUpperCase() + fltSnapshot.getKey()
//                            .substring(fltSnapshot.getKey().indexOf("flat"),
//                                    fltSnapshot.getKey().length()).substring(1)
                }

//        ArrayAdapter<CharSequence> flatAdapter = ArrayAdapter.createFromResource(this,
//                R.array.priorities, R.layout.custom_spinner);
                ArrayAdapter<String> flatAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_dropdown_item, flatAddrLine1s);
                flatAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                flatSpinner.setAdapter(flatAdapter);
                flatAdapter.notifyDataSetChanged();
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

        if(!attemptEdit){
            getMenuInflater().inflate(R.menu.task_details, menu);
            saveEdit = menu.findItem(R.id.edit_save_task);
        } else{
            getMenuInflater().inflate(R.menu.task_details_save, menu);
            saveEdit = menu.findItem(R.id.save_edited_task);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leaving page")
                .setMessage("You have not saved changes made to this task. Press Yes to discard or No to remain on page.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(attemptEdit && !editscancelled){}
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

            case R.id.edit_save_task:
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
        tdTitle.setEnabled(false);
        actvProperty.setEnabled(false);
        flatSpinner.setEnabled(false);
        tdNotes.setEnabled(false);
        prioritySpinner.setEnabled(false);
        btReport.setEnabled(false);
        if (!editscancelled) {
            try {
                parceableTask.setTitle(tdTitle.getText().toString().toLowerCase().substring(0, 1) + tdTitle.getText().toString().substring(1));
                parceableTask.setProperty(actvProperty.getText().toString().toLowerCase() + " - " +
                        flatSpinner.getSelectedItem().toString().toLowerCase());
                parceableTask.setDescription(tdNotes.getText().toString());
                parceableTask.setPriority(prioritySpinner.getSelectedItem().toString().toLowerCase());
                parceableTask.setReport(attachedReportKey);
                //editTaskRef.child(parceableTask.getTaskKey()).setValue(parceableTask);
                Toast toast = Toast.makeText(TaskDetails.this, "Task edited! SUCCESS!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } catch (Exception ex) {
                Toast toast = Toast.makeText(TaskDetails.this, "Task not edited. FAIL", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else{
            tdTitle.setText(parceableTask.getTitle());
            String[] split = parceableTask.getProperty().split("-");
            actvProperty.setText(split[0].trim().substring(0, 1).toUpperCase());
            for (int i=0; i < flatAddrLine1s.size(); i++){
                if(Objects.equals(flatSpinner.getSelectedItem().toString(), flatAddrLine1s.get(i))){
                    flatSpinner.setSelection(i);
                }
            }
            tdNotes.setText(parceableTask.getDescription());
           // prioritySpinner
        }
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
                    tdTitle.setTextColor(Color.parseColor("#FF5722"));
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
                    actvProperty.setTextColor(Color.parseColor("#FF5722"));
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
                    tdNotes.setTextColor(Color.parseColor("#FF5722"));
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
                    btReport.setTextColor(Color.parseColor("#FF5722"));
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
                                        tdTitle.setText(parceableTask.getTitle());
                                        tdTitle.setSelectAllOnFocus(true);
                                        tdTitle.setTextColor(getResources().getColor(R.color.black_color));
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
            final Firebase flatRef = new Firebase(getString(R.string.flats_location));
            final AutoCompleteTextView actvProperty = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
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
                                           String[] split = parceableTask.getProperty().split("-");
                                           actvProperty.setText(split[0].trim().substring(0, 1).toUpperCase() + split[0].substring(1));
                                           actvProperty.setSelectAllOnFocus(true);
                                           actvProperty.setTextColor(getResources().getColor(R.color.black_color));
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
                                       String[] split = parceableTask.getProperty().split("-");
                                       actvProperty.setText(split[0].trim().substring(0, 1).toUpperCase());
                                       actvProperty.setSelectAllOnFocus(true);
                                       actvProperty.setTextColor(getResources().getColor(R.color.black_color));
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
                                        tdNotes.setText(parceableTask.getDescription());
                                        tdNotes.setSelectAllOnFocus(true);
                                        tdNotes.setTextColor(getResources().getColor(R.color.black_color));
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

        if(validTitle && validProperty && validNotes){
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

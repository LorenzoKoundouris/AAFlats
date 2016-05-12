package com.example.lorenzo.aaflats;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskDetails extends AppCompatActivity {

    private Task edittedTask;
    private Task parceableTask = new Task();
    private Staff assignedStaff;
    private Staff edittedStaff;
    private Report associatedTaskReport;
    private Report edittedReport;

    private Report attachedReport; // do i need this?

    private TaskEditProcess mTaskDetails = null;

    private InputMethodManager inputMethodManager;


    private boolean editAttempted = false;
    private boolean editsCancelled;
    private boolean validTitle = true;
    private boolean validProperty = true;
    private boolean validDescription = true;

    private TextView completionText;
    private TextView completedBy;
    private LinearLayout mTargetDateButtonsLayout;
    private LinearLayout mTargetDateTextEditLayout;
    private CardView mReportView;
    private ImageView cancelDate;
    private Button tomorrow;
    private Button pickDateButton;
    private CheckBox cbTaskStatus;// = (CheckBox) findViewById(R.id.completion_check_box);
    private EditText mTargetDate;
    private AutoCompleteTextView mStaffAssigned;
    private EditText mTitle;// = (EditText) findViewById(R.id.td_title);
    private AutoCompleteTextView mProperty;// = (EditText) findViewById(R.id.et_property_actv);
    private Spinner mFlat;
    private EditText mDescription;// = (EditText) findViewById(R.id.td_description);
    private Spinner mPriority;
    private CardView mCard;
    private TextView mCardText;// = (Button) findViewById(R.id.td_card_btn_attach);
    private EditText mNotes;
    private TextView mReportSender;// = (TextView) findViewById(R.id.tv_sender);
    private TextView mReportTimestamp;// = (TextView) findViewById(R.id.tv_timestamp);
    private TextView mReportContent;// = (TextView) findViewById(R.id.tv_report_content);

    private ArrayList<Flat> flatList = new ArrayList<>();
    private ArrayList<Report> reportList = new ArrayList<>();
    private ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    private ArrayList<String> reportTitles = new ArrayList<>();
    private ArrayList<Staff> staffList = new ArrayList<>();
    private ArrayList<String> staffNames = new ArrayList<>();
    private ArrayList<String> flatNums = new ArrayList<>();
    private ArrayAdapter<String> flatAdapter;
    private ArrayAdapter<CharSequence> priorityAdapter;
    private Context context = this;
    private String parceableTaskKey;
    private String attachedReportKey;
    private String[] splitAddress;
    private MenuItem miSaveEdit;// = (MenuItem) findViewById(R.id.edit_task);

    private Calendar cal = Calendar.getInstance();
    private Date todayDate;
    private int year_x, month_x, day_x;
    private static final int DIALOG_ID = 0;

    Firebase staffRef;
    Firebase thisTaskRef;
    Firebase taskRef;
    Firebase propertyRef;
    Firebase reportRef;
    Firebase flatRef;
    Firebase deleteTask;
    Firebase deleteNotif;
    Firebase notifRef;
    Query findReportQuery;

    public static final String MY_PREFERENCES = "MyPreferences";
    public static final String STAFF_KEY = "StaffKey";
    public static final String FULL_NAME_KEY = "StaffFullName";
    private SharedPreferences mSharedPreferences;

    public static final String MY_TASK_NOTIFICATIONS = "MyTaskNotifications";
    private SharedPreferences mTaskNotifications;
    private SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get shared preferences
        mSharedPreferences = getApplicationContext().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        mTaskNotifications = getSharedPreferences(MY_TASK_NOTIFICATIONS, MODE_PRIVATE);
        editor = mTaskNotifications.edit();

        //Define inputMethodService to hide keyboard
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        //Clicked-on Task to be passed from Homepage
        Bundle intent = getIntent().getExtras();
        parceableTask = intent.getParcelable("parceable_task");

        editor.remove(parceableTask.getTaskKey()).commit();

//        Toast.makeText(TaskDetails.this, "tk: " + parceableTask.getTaskKey(), Toast.LENGTH_SHORT).show();
//        parceableTaskKey = intent.getString("parceable_task_key");
        //ArrayList<Task> pTaskList = (ArrayList<Task>) intent.getParcelable("parceable_tasklist");

        //Calendar components
        todayDate = Calendar.getInstance().getTime();

        //Page components
        completedBy = (TextView) findViewById(R.id.completedBy);
        cbTaskStatus = (CheckBox) findViewById(R.id.completion_check_box);
        mTargetDateButtonsLayout = (LinearLayout) findViewById(R.id.td_target_date_lnr_layout);
        mTargetDateTextEditLayout = (LinearLayout) findViewById(R.id.td_date_text_lnr_layout);
        mReportView = (CardView) findViewById(R.id.task_report_card_view);
        cancelDate = (ImageView) findViewById(R.id.cancel_date_pick);
        tomorrow = (Button) findViewById(R.id.tomorrow_button);
        pickDateButton = (Button) findViewById(R.id.td_date_picker_button);
        mTargetDate = (EditText) findViewById(R.id.td_target_date_edittext);
        mStaffAssigned = (AutoCompleteTextView) findViewById(R.id.td_staff);
        mTitle = (EditText) findViewById(R.id.td_title);
        mProperty = (AutoCompleteTextView) findViewById(R.id.et_property_actv);
        mFlat = (Spinner) findViewById(R.id.et_flat_spinner);
        mFlat.setEnabled(false);
        mDescription = (EditText) findViewById(R.id.td_description);
        mPriority = (Spinner) findViewById(R.id.et_priority_spinner);
        mPriority.setEnabled(false);
        mCard = (CardView) findViewById(R.id.td_card_btn_attach);
        mCardText = (TextView) findViewById(R.id.td_card_text_view);
        mNotes = (EditText) findViewById(R.id.td_notes);
        mReportSender = (TextView) findViewById(R.id.tv_sender);
        mReportTimestamp = (TextView) findViewById(R.id.tv_timestamp);
        mReportContent = (TextView) findViewById(R.id.tv_report_content);

        //Firebase references
        Firebase.setAndroidContext(this);
        staffRef = new Firebase(getResources().getString(R.string.staff_location));
        reportRef = new Firebase(getString(R.string.reports_location));
        propertyRef = new Firebase(getString(R.string.properties_location));
        taskRef = new Firebase(getString(R.string.tasks_location));
        flatRef = new Firebase(getString(R.string.flats_location));
        notifRef = new Firebase(getResources().getString(R.string.notifications_location));
        findReportQuery = reportRef.orderByKey().equalTo(parceableTask.getReport());

        thisTaskRef = taskRef.child(parceableTask.getTaskKey());

        //Make copy
        edittedTask = parceableTask;

        //Display Task details on page
        setTitle(edittedTask.getTitle());

        if (edittedTask.getStatus()) {
            cbTaskStatus.setChecked(true);
            completedBy.setVisibility(View.VISIBLE);
            completedBy.setText(mSharedPreferences.getString(FULL_NAME_KEY, ""));
//            editor.putBoolean("taskStatus", true);
        } else {
            cbTaskStatus.setChecked(false);
            completedBy.setVisibility(View.INVISIBLE);
            completedBy.setText("");
//            editor.putBoolean("taskStatus", false);
        }
        mTargetDate.setText(edittedTask.getTargetDate());
//        mStaffAssigned.setText(edittedTask.getAssignedStaff());
        mTitle.setText(edittedTask.getTitle());
        splitAddress = edittedTask.getProperty().split(" - ");
        mProperty.setText(splitAddress[0].trim());
        //flat done in onDataChange
        mDescription.setText(edittedTask.getDescription());
        mNotes.setText(edittedTask.getNotes());

//        editor.putString("taskTitle", parceableTask.getTitle());
//        editor.putString("taskProperty", splitAddress[0].trim());
//        editor.putString("taskFlat", splitAddress[1].trim());
//        editor.putString("taskDescription", parceableTask.getDescription());
//        editor.putString("taskNotes", parceableTask.getNotes());


        // Create an ArrayAdapter using the string array
        priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, R.layout.custom_spinner);
        // Specify the layout to use when the list of choices appears
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the mPriority
        mPriority.setAdapter(priorityAdapter);

        if (edittedTask.getPriority().matches("High")) {
            //high priority
            mPriority.setSelection(0);
//            editor.putInt("taskPriority", 0);
        } else if (edittedTask.getPriority().matches("Medium")) {
            //medium priority
            mPriority.setSelection(1);
//            editor.putInt("taskPriority", 1);
        } else {
            //low priority
            mPriority.setSelection(2);
//            editor.putInt("taskPriority", 2);
        }
//        editor.commit();

        final ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        mProperty.setAdapter(propertyAdapter);

        propertyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot prtSnapshot : dataSnapshot.getChildren()) {
                    Property prt = prtSnapshot.getValue(Property.class);
                    propertyAddrLine1s.add(prt.getAddrline1().trim());
//                    propertyAddrLine1s.add(prtSnapshot.getKey().substring(0, 1).toUpperCase()
//                            + prtSnapshot.getKey().substring(1));
                }
                propertyAdapter.notifyDataSetChanged();
                mProperty.setAdapter(propertyAdapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        final ArrayAdapter<String> staffAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, staffNames);
        mStaffAssigned.setAdapter(staffAdapter);

        mStaffAssigned.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTitle.requestFocus();
            }
        });

        staffRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Staff stf = childSnap.getValue(Staff.class);
                    stf.setStaffKey(childSnap.getKey());
                    staffList.add(stf);
                    staffNames.add(stf.getForename() + " " + stf.getSurname());
                    if (stf.getStaffKey().matches(edittedTask.getAssignedStaff())) {
                        mStaffAssigned.setText(stf.getForename() + " " + stf.getSurname());
                        assignedStaff = stf;
                    }
                }
                staffAdapter.notifyDataSetChanged();
                mStaffAssigned.setAdapter(staffAdapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        loadCorrespondingFlats();

        //Get-Report is ok
        findReportQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report foundReport = childSnap.getValue(Report.class);
                    //temp place holder
                    foundReport.setReportKey(childSnap.getKey());
                    //used in this method only
                    associatedTaskReport = foundReport;
//                    editor.putString("reportKey", );
//                    editor.commit();

                }

                edittedReport = associatedTaskReport;

                try {
                    String lc = associatedTaskReport.getContent();
                    if (associatedTaskReport.getContent().length() > 23) {
                        mCardText.setText("\"" + lc.substring(0, 20) + "...\"");
                    } else {
                        mCardText.setText("\"" + lc + "...\"");
                    }
                    mReportTimestamp.setText(associatedTaskReport.getTimestamp());
                    mReportSender.setText(associatedTaskReport.getSender());
                    mReportContent.setText(associatedTaskReport.getContent());
                    mReportView.setVisibility(View.VISIBLE);
//                    editor.putString("btReportText", mCardText.getText().toString());
//                    editor.putString("taskReportTimestamp", associatedTaskReport.getTimestamp());
//                    editor.putString("taskReportSender", associatedTaskReport.getSender());
//                    editor.putString("taskReportContent", associatedTaskReport.getContent());
//                    editor.commit();
                } catch (Exception ex) {
                    mReportView.setVisibility(View.GONE);
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
//                reportKeys.clear();
                reportTitles.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report firebaseReport = childSnap.getValue(Report.class);
                    firebaseReport.setReportKey(childSnap.getKey());
                    reportList.add(firebaseReport);
//                    reportKeys.add(childSnap.getKey()); //list of keys of each report
                    if (firebaseReport.getContent().length() > 23) {
                        reportTitles.add("\"" + firebaseReport.getContent().substring(0, 20) + "...\"");
                    } else {
                        reportTitles.add("\"" + firebaseReport.getContent() + "...\"");
                    }
                }
                String[] arrayReportTitles = new String[reportTitles.size()];
                arrayReportTitles = reportTitles.toArray(arrayReportTitles);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setTitle("Attach report").setItems(arrayReportTitles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        System.out.println("You attached: " + reportTitles.get(item));
                        //dialog.dismiss();
                        mCardText.setText(reportTitles.get(item));
                        mCardText.setTypeface(null, Typeface.ITALIC);
                        attachReport(reportList.get(item));
//                        attachedReportKey = reportKeys.get(item);
                        mReportContent.setText(edittedReport.getContent());
                        mReportSender.setText(edittedReport.getSender());
                        mReportView.setVisibility(View.VISIBLE);
//                        StringBuilder ts = new StringBuilder(attachedReport.getTimestamp());
//                        ts.insert(2, "/");
//                        ts.insert(5, "/");
//                        ts.insert(10, " ");
//                        ts.insert(13, ":");
//                        ts.insert(16, ":");
                        mReportTimestamp.setText(edittedReport.getTimestamp());
                        mReportView.requestFocus();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNeutralButton("Remove report", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCardText.setText("ATTACH A REPORT");
                        mCardText.setTypeface(null, Typeface.NORMAL);
                        mReportView.setVisibility(View.GONE);
                        edittedTask.setReport("");
                    }
                });
                final AlertDialog alertDialog = alertBuilder.create();
                mCardText.setOnClickListener(new View.OnClickListener() {
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


        Query seenNotif = notifRef.orderByChild("objectID").equalTo(parceableTask.getTaskKey());
        seenNotif.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Notification ntf = childSnap.getValue(Notification.class);
                    String delNotifURL = notifRef + "/" + childSnap.getKey();
                    deleteNotif = new Firebase(delNotifURL);
                    deleteNotif.removeValue();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        cbTaskStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<String, Object> statusChangeMap = new HashMap<>();
                Map<String, Object> timestampChangeMap = new HashMap<>();
                Map<String, Object> completedByChangeMap = new HashMap<>();

                if (isChecked) {
                    edittedTask.setStatus(true);
                    SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyHHmmss");
                    String format = s.format(new Date());
                    edittedTask.setCompletionTimestamp(format);
                    statusChangeMap.put("status", true);
                    timestampChangeMap.put("completionTimestamp", format);
                    completedByChangeMap.put("completedBy", mSharedPreferences.getString(STAFF_KEY, ""));


                    completedBy.setVisibility(View.VISIBLE);
                    completedBy.setText(mSharedPreferences.getString(FULL_NAME_KEY, ""));

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
                    edittedTask.setStatus(false);
                    edittedTask.setCompletionTimestamp("pending");
                    edittedTask.setCompletedBy(mSharedPreferences.getString(STAFF_KEY, ""));
                    timestampChangeMap.put("completionTimestamp", "pending");
                    statusChangeMap.put("status", false);
                    completedByChangeMap.put("completedBy", "");

                    completedBy.setVisibility(View.INVISIBLE);
                    completedBy.setText("");
                    //cbTaskStatus.setChecked(false);
                }
                thisTaskRef.updateChildren(statusChangeMap);
                thisTaskRef.updateChildren(timestampChangeMap);
                thisTaskRef.updateChildren(completedByChangeMap);
                TextView completionTV = (TextView) findViewById(R.id.completion_text_view);
                completionTV.setTextColor(Color.parseColor("#FF5722"));
            }
        });

        mTargetDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mTargetDateButtonsLayout.setVisibility(View.VISIBLE);
                    mTargetDateButtonsLayout.setEnabled(true);
                    mTargetDateTextEditLayout.setVisibility(View.INVISIBLE);
                    mTargetDateTextEditLayout.setEnabled(false);
                    //pickDateButton.performClick();
                }
            }
        });

        cancelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetDateButtonsLayout.setVisibility(View.INVISIBLE);
                mTargetDateButtonsLayout.setEnabled(false);
                mTargetDateTextEditLayout.setVisibility(View.VISIBLE);
                mTargetDateTextEditLayout.setEnabled(true);
            }
        });

        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        tomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    cal.setTime(formatDate.parse(mTargetDate.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
                mTargetDate.setText(formatDate.format(cal.getTime()));
                cancelDate.performClick();
            }
        });

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        completionText = (TextView) findViewById(R.id.completion_text_view);
        completionText.requestFocus();

    }//END OF onCreate()

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID) {
            DatePickerDialog da = new DatePickerDialog(this, dpickerListener,
                    year_x, month_x, day_x);
//            Calendar c = Calendar.getInstance();
////            c.add(Calendar.DATE, 0);
//            Date newDate = c.getTime();
            da.getDatePicker().setMinDate(System.currentTimeMillis());
            return da;

            //return new DatePickerDialog(this, dpickerListener, year_x, month_x, day_x);
        } else {
            return null;
        }
    }

    private DatePickerDialog.OnDateSetListener dpickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            final LinearLayout targetDateButtonsll = (LinearLayout) findViewById(R.id.nt_target_date_lnr_layout);
//            final LinearLayout targetDateTextEditll = (LinearLayout) findViewById(R.id.nt_date_text_lnr_layout);
            year_x = year;
            month_x = monthOfYear + 1;
            day_x = dayOfMonth;

            String formatter = (new DecimalFormat("00").format(day_x) + "/" +
                    new DecimalFormat("00").format(month_x) + "/" +
                    new DecimalFormat("00").format(year_x));

            mTargetDate.setText(formatter); //formatter
            cancelDate.requestFocus();

//            final Date thisDate = Calendar.getInstance().getTime();
//            final SimpleDateFormat formatt = new SimpleDateFormat("dd/MM/yyyy");

//            String chosenDat = mSharedPreferences.getString("chosenDate", formatt.format(thisDate));

//            Date chosenDate = new Date();
//            try {
//                chosenDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(formatter);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

//            if (chosenDate.before(todayDate)) {
//                mTargetDateValue.setText(formatt.format(todayDate));
//                editor.putString("chosenDate", formatter);
//                editor.commit();
//            } else {
//                editor.putString("chosenDate", formatter);
//                editor.commit();
//            }


            mTargetDateButtonsLayout.setVisibility(View.INVISIBLE);
            mTargetDateButtonsLayout.setEnabled(false);
            mTargetDateTextEditLayout.setVisibility(View.VISIBLE);
            mTargetDateTextEditLayout.setEnabled(true);
//            showDialogOnButtonClick();

        }
    };

    private void loadCorrespondingFlats() {
        Query flatQuery = flatRef.orderByChild("addressLine1").equalTo(mProperty.getText().toString().trim());
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

                flatAdapter = new ArrayAdapter<>(getBaseContext(),
                        R.layout.custom_spinner, flatNums);
                flatAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                mFlat.setAdapter(flatAdapter);
                flatAdapter.notifyDataSetChanged();
                for (int i = 0; i < flatNums.size(); i++) {
                    if (splitAddress[1].matches(mFlat.getItemAtPosition(i).toString())) {
                        mFlat.setSelection(i);
//                        editor.putInt("tFlatSpinner", i);
//                        editor.commit();
                        break;
                    }
                }
            }

//            if (Objects.equals(splitAddress[1].trim().substring(0, 1).toUpperCase()
//            + splitAddress[1].substring(1).trim(), mFlat.getItemAtPosition(i).toString()))

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
        edittedReport = attachedReport;
        edittedTask.setReport(edittedReport.getReportKey());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!editAttempted) {
            getMenuInflater().inflate(R.menu.task_details, menu);
//            miSaveEdit = menu.findItem(R.id.edit_task);
        } else {
            getMenuInflater().inflate(R.menu.task_details_save, menu);
//            miSaveEdit = menu.findItem(R.id.save_edited_task);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //for safe exit:
        //editAttempted must be false
        //editsCancelled must be true

        if (editAttempted) { //(editAttempted && !editsCancelled || editAttempted)
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
                                String delTskURL = taskRef + "/" + edittedTask.getTaskKey();
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
                Toast.makeText(getApplicationContext(), "Edit now", Toast.LENGTH_SHORT).show();
                enableComponents();
                break;
            case R.id.save_edited_task:
                attemptEdit();
                break;
            case R.id.cancel_edit_task:
                cancelAndRestore();
                break;
        }
        return true;
    }

    private void cancelAndRestore() {
        mTargetDate.setText(parceableTask.getTargetDate());
        mStaffAssigned.setText(assignedStaff.getForename() + " " + assignedStaff.getSurname());//parceableTask.getAssignedStaff()
        mTitle.setText(parceableTask.getTitle());
        mProperty.setText(splitAddress[0]);
        mFlat.setSelection(flatAdapter.getPosition(splitAddress[1]));
        mDescription.setText(parceableTask.getDescription());
        mPriority.setSelection(priorityAdapter.getPosition(parceableTask.getPriority()));
        try {
            mCardText.setText("\"" + associatedTaskReport.getContent().substring(0, 20) + "...\"");
        } catch (Exception ignored) {
            mCardText.setText("ATTACH A REPORT");
        }
        try {
            mNotes.setText(parceableTask.getNotes());
        } catch (Exception ignored) {
            mNotes.setText(null);
        }

        disableComponents();
    }

//    private void saveAllChanges() {
//        if (!editsCancelled) {
//            try {
//                parceableTask.setTitle(mTitle.getText().toString());
//                parceableTask.setProperty(mProperty.getText().toString() + " - " +
//                        mFlat.getSelectedItem().toString());
//                parceableTask.setDescription(mDescription.getText().toString());
//                parceableTask.setNotes(mNotes.getText().toString());
//                parceableTask.setPriority(mPriority.getSelectedItem().toString());
//
//                if (mCardText.getText().toString().matches(mSharedPreferences.getString("btReportText", "crashReport"))) {
//                    parceableTask.setReport(mSharedPreferences.getString("reportKey", "crashReport"));
//                } else {
//                    parceableTask.setReport(attachedReportKey);
//                }
//
////                if(Objects.equals(attachedReportKey, "")){
////                   parceableTask.setReport(pref.getString("reportKey", "crashReportKey"));
////                } else {
////                    parceableTask.setReport(attachedReportKey);
////                }
//
//                //taskRef.child(parceableTask.getTaskKey()).setValue(parceableTask);
//                taskRef.child(parceableTaskKey).setValue(parceableTask);
//                Toast toast = Toast.makeText(TaskDetails.this, "Task edited! SUCCESS!", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//            } catch (Exception ex) {
//                Toast toast = Toast.makeText(TaskDetails.this, "Task not edited. FAIL", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//            }
//        } else {
//            mTitle.setText(mSharedPreferences.getString("taskTitle", "crashTitle"));
//            //String[] split = parceableTask.getProperty().split("-");
//            mProperty.setText(mSharedPreferences.getString("taskProperty", "crashProperty"));
////            for (int i=0; i < flatNums.size(); i++){
////                if(Objects.equals(mFlat.getSelectedItem().toString(), flatNums.get(i))){
////                    mFlat.setSelection(i);
////                }
////            }
//            mFlat.setSelection(mSharedPreferences.getInt("tFlatSpinner", 0));
//            mDescription.setText(mSharedPreferences.getString("taskDescription", "crashDescription"));
//            mNotes.setText(mSharedPreferences.getString("taskNotes", "crashNotes"));
//            mPriority.setSelection(mSharedPreferences.getInt("taskPriority", 0));
//            cbTaskStatus.setChecked(mSharedPreferences.getBoolean("taskStatus", false));
//            mCardText.setText(mSharedPreferences.getString("btReportText", "crashReport"));
//            mReportSender.setText(mSharedPreferences.getString("taskReportSender", "crashSender"));
//            mReportTimestamp.setText(mSharedPreferences.getString("taskReportTimestamp", "crashTimestamp"));
//            mReportContent.setText(mSharedPreferences.getString("taskReportContent", "crashReportContent"));
//        }
//
//        mTitle.setEnabled(false);
//        mProperty.setEnabled(false);
//        mFlat.setEnabled(false);
//        mDescription.setEnabled(false);
//        mNotes.setEnabled(false);
//        mPriority.setEnabled(false);
//        mCardText.setEnabled(false);
//
//        editAttempted = false;
//        invalidateOptionsMenu();
//    }


    private void attemptEdit() {
        if (mTaskDetails != null) {
            return;
        }

        //Reset errors
        mTargetDate.setError(null);
        mStaffAssigned.setError(null);
        mTitle.setError(null);
        mProperty.setError(null);
        //Flat
        mDescription.setError(null);
        //Priority
        mCardText.setError(null);
        mNotes.setError(null);

        //Store values at the time of creation attempt
//        String targetDate = mTargetDate.getText().toString().trim();
        edittedTask.setTargetDate(mTargetDate.getText().toString().trim());
//        String staff = mStaffAssigned.getText().toString().trim();
        edittedTask.setAssignedStaff(mStaffAssigned.getText().toString().trim());// //edittedStaff.getStaffKey()
//        String title = mTitle.getText().toString().trim();
        edittedTask.setTitle(mTitle.getText().toString().trim());
//        String property = mProperty.getText().toString().trim();
        edittedTask.setProperty(mProperty.getText().toString().trim() + " - " + mFlat.getSelectedItem().toString());
//        String flat = "";
//        try {
//            flat = mFlat.getSelectedItem().toString();
//        } catch (Exception e) {
//
//        }
//        String description = mDescription.getText().toString().trim();
        edittedTask.setDescription(mDescription.getText().toString().trim());
//        String priority = mPriority.getSelectedItem().toString();
        edittedTask.setPriority(mPriority.getSelectedItem().toString());
//        String report = mReport;




//        try {
//            edittedTask.setReport(edittedReport.getReportKey());
//        } catch (Exception e) {
//            edittedTask.setReport(null);
//        }
        try {
            edittedTask.setNotes(mNotes.getText().toString().trim());
        } catch (Exception e) {
            edittedTask.setNotes(null);
        }

//        String notes = mNotes.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        //Check for a valid target date, if the user entered one
        if (TextUtils.isEmpty(edittedTask.getTargetDate())) {
            mTargetDate.setError("This field is required");
            focusView = mTargetDate;
            cancel = true;
        }

        //Check for a valid member of staff, if the user entered one
        if (TextUtils.isEmpty(edittedTask.getAssignedStaff())) {
            mStaffAssigned.setError("This field is required");
            if (focusView == null) {
                focusView = mStaffAssigned;
            }
            cancel = true;
        } else if (!isStaffValid(edittedTask.getAssignedStaff())) {
            mStaffAssigned.setError("This name is invalid");
            if (focusView == null) {
                focusView = mStaffAssigned;
            }
            cancel = true;
        }

        //Check for a valid title, if the user entered one
        if (TextUtils.isEmpty(edittedTask.getTitle())) {
            mTitle.setError("This field is required");
            if (focusView == null) {
                focusView = mTitle;
            }
            cancel = true;
        } else if (!isTitleValid(edittedTask.getTitle())) {
            mTitle.setError("This title is invalid");
            if (focusView == null) {
                focusView = mTitle;
            }
            cancel = true;
        }

        //Check for a valid property, if the user entered one
        if (TextUtils.isEmpty(edittedTask.getProperty())) {
            mProperty.setError("This field is required");
            if (focusView == null) {
                focusView = mProperty;
            }
            cancel = true;
        } else if (!isPropertyValid(edittedTask.getProperty())) {
            mProperty.setError("This property is invalid");
            focusView = mProperty;
            cancel = true;
        }

        //Check for a valid description, if the user entered one
        if (TextUtils.isEmpty(edittedTask.getDescription()) ||
                !isDescriptionValid(edittedTask.getDescription())) {
            mDescription.setError("This description is too short");
            if (focusView == null) {
                focusView = mDescription;
            }
            cancel = true;
        }

//        Check for valid report, if the user attached one
        if (TextUtils.isEmpty(edittedTask.getReport())) {
//            mCardText.setError("A valid report is required");
//            if (focusView == null) {
//                focusView = mCard;
//            }
//            cancel = true;
            edittedTask.setReport("");
        }

        // Check for valid notes, if the user attached some
        if (TextUtils.isEmpty(edittedTask.getNotes())) {
//            mCardText.setError("A valid report is required");
//            if (focusView == null) {
//                focusView = mCard;
//            }
//            cancel = true;
            edittedTask.setNotes("");
        }

//=============================================================================================
        if (cancel) {
            // There was an error; don't attempt creation and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            Toast toast = Toast.makeText(getApplicationContext(), "Saving...", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            mTaskDetails = new TaskEditProcess();
            mTaskDetails.execute((Void) null);
        }
    }

    private boolean isDescriptionValid(String description) {
        return description.length() > 4;
    }

    private boolean isPropertyValid(String property) {
        Boolean isProperty = false;
        for (int i = 0; i < propertyAddrLine1s.size(); i++) {
            if (propertyAddrLine1s.get(i).matches(mProperty.getText().toString().trim())) {
                isProperty = true;
                break;
            }
        }
        return isProperty;
    }

    private boolean isStaffValid(String staff) {
        Boolean isStaff = false;
        for (int i = 0; i < staffList.size(); i++) {
            if (staffNames.get(i).matches(mStaffAssigned.getText().toString().trim())) {
                edittedStaff = staffList.get(i);
                edittedTask.setAssignedStaff(edittedStaff.getStaffKey());
                isStaff = true;
                break;
            }
        }
        return isStaff;
    }

    private boolean isTitleValid(String title) {
        return title.length() > 4;
    }

    public class TaskEditProcess extends AsyncTask<Void, Void, Boolean> {
//        private final String mTargetDate;
//        private final String mStaff;
//        private final String mTitle;
//        private final String mProperty;
//        private final String mFlatNum;
//        private final String mDescription;
//        private final String mPriority;
//        private final String mNotes;
//        private final String mReport;
//
//        public TaskEditProcess(String targetDate, String staff, String title, String property,
//                               String flat, String description, String priority, String report,
//                               String notes) {
//            this.mTargetDate = targetDate;
//            this.mStaff = staff;
//            this.mTitle = title;
//            this.mProperty = property;
//            this.mFlatNum = flat;
//            this.mDescription = description;
//            this.mPriority = priority;
//            this.mReport = report;
//            this.mNotes = notes;
//        }


        @Override
        protected Boolean doInBackground(Void... params) {
            try {
//                String tempKey = edittedTask.getTaskKey();
//                edittedTask.setTaskKey(null);

                taskRef.child(edittedTask.getTaskKey()).setValue(edittedTask);

                //Prevent saving Firebase key as child of node
                Firebase changeKey = taskRef.child(edittedTask.getTaskKey());
                Map<String, Object> keyMap = new HashMap<>();
                keyMap.put("taskKey", null);
                changeKey.updateChildren(keyMap);

                return true;
            } catch (Exception ex) {
                Toast.makeText(TaskDetails.this, "An error occurred while trying to save new task", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            mTaskDetails = null;
            disableComponents();

            if (success) {
                Toast toast = Toast.makeText(TaskDetails.this, "Task edited! SUCCESS!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                Toast toast = Toast.makeText(TaskDetails.this, "Task not edited. FAIL", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

        @Override
        protected void onCancelled() {
            mTaskDetails = null;
        }
    }

    public void disableComponents() {
        mTargetDate.setEnabled(false);
        mStaffAssigned.setEnabled(false);
        mTitle.setEnabled(false);
        mProperty.setEnabled(false);
        mFlat.setEnabled(false);
        mDescription.setEnabled(false);
        mPriority.setEnabled(false);
        mCardText.setEnabled(false);
        mNotes.setEnabled(false);

        mTargetDate.setTextColor(getResources().getColor(R.color.grey_color));
        mStaffAssigned.setTextColor(getResources().getColor(R.color.grey_color));
        mTitle.setTextColor(getResources().getColor(R.color.grey_color));
        mProperty.setTextColor(getResources().getColor(R.color.grey_color));
        mDescription.setTextColor(getResources().getColor(R.color.grey_color));
//        mCardText.setTextColor(getResources().getColor(R.color.grey_color));
        mNotes.setTextColor(getResources().getColor(R.color.grey_color));


        editAttempted = false;
        invalidateOptionsMenu();
    }

    private void enableComponents() {
        try {
            mTargetDate.setEnabled(true);
            mStaffAssigned.setEnabled(true);
            mTitle.setEnabled(true);
            mProperty.setEnabled(true);
            mFlat.setEnabled(true);
            mDescription.setEnabled(true);
            mNotes.setEnabled(true);
            mPriority.setEnabled(true);
            mCardText.setEnabled(true);

            mTargetDate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mTargetDate.getText().toString().matches(parceableTask.getTargetDate())) {//mSharedPreferences.getString("taskTargetDate", "crashDate")
                        mTargetDate.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mTargetDate.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            mStaffAssigned.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mStaffAssigned.getText().toString().matches(parceableTask.getAssignedStaff())) {//mSharedPreferences.getString("taskStaff", "crashStaff")
                        mStaffAssigned.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mStaffAssigned.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            mTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mTitle.getText().toString().matches(parceableTask.getTitle())) {//mSharedPreferences.getString("taskTitle", "crashTitle")
                        mTitle.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mTitle.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            mProperty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mProperty.getText().toString().matches(parceableTask.getProperty())) {//mSharedPreferences.getString("taskProperty", "crashProperty")
                        mProperty.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mProperty.setTextColor(Color.parseColor("#FF5722"));
                    }
                    loadCorrespondingFlats();
                }
            });

            mDescription.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mDescription.getText().toString().matches(parceableTask.getDescription())) {//mSharedPreferences.getString("taskDescription", "crashDescription")
                        mDescription.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mDescription.setTextColor(Color.parseColor("#FF5722"));
                    }

                }
            });

            mNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mNotes.getText().toString().matches(parceableTask.getNotes())) {//mSharedPreferences.getString("taskNotes", "crashNotes")
                        mNotes.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mNotes.setTextColor(Color.parseColor("#FF5722"));
                    }

                }
            });

            mCardText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(associatedTaskReport != null){
                        if (mCardText.getText().toString().matches("\"" + associatedTaskReport.getContent().substring(0, 20) + "...\"")) {//mSharedPreferences.getString("btReportText", "crashbtReportText")
                            mCardText.setTextColor(Color.parseColor("#FFFFFF"));
                        } else {
                            mCardText.setTextColor(Color.parseColor("#FF5722"));
                        }
                    }
                }
            });

//            mTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (mTitle.getText().toString().matches("")) {
//                        new AlertDialog.Builder(v.getContext())
//                                .setTitle("Null title")
//                                .setMessage("Whoops! Looks like you forgot to set a Title!")
//                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        mTitle.setText(mSharedPreferences.getString("taskTitle", "crashTitle")); // parceableTask.getTitle()
//                                        mTitle.setSelectAllOnFocus(true);
//                                        mTitle.setSelection(mTitle.length());
//                                    }
//                                })
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                        validTitle = false;
//                    } else {
//                        validTitle = true;
//                    }
//                }
//            });

//            mProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    boolean isProperty = false;
//                    if (!hasFocus && !mProperty.getText().toString().matches("")) {
//                        for (int i = 0; i < propertyAddrLine1s.size(); i++) {
//                            if (propertyAddrLine1s.get(i).matches(mProperty.getText().
//                                    toString().trim())) {
//                                isProperty = true;
//                                break;
//                            }
//                        }
//                        if (!isProperty) {
//                            new AlertDialog.Builder(v.getContext())
//                                    .setTitle("Wrong address")
//                                    .setMessage("You must enter an existing property")
//                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            //String[] split = parceableTask.getProperty().split("-");
//                                            mProperty.setText(mSharedPreferences.getString("taskProperty", "crashProperty"));//split[0].trim().substring(0, 1).toUpperCase()
//                                            mProperty.requestFocus();
//                                        }
//                                    })
//                                    .setIcon(android.R.drawable.ic_dialog_alert)
//                                    .show();
//                        }
//                        validProperty = false;
//                    } else if (!hasFocus && mProperty.getText().toString().matches("")) {
//                        new AlertDialog.Builder(v.getContext())
//                                .setTitle("Null address")
//                                .setMessage("Whoops! Looks like you forgot to set a Property!")
//                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        //String[] split = parceableTask.getProperty().split("-");
//                                        mProperty.setText(mSharedPreferences.getString("taskProperty", "crashProperty")); //split[0].trim().substring(0, 1).toUpperCase()
//                                        mProperty.requestFocus();
//                                    }
//                                })
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                        validProperty = false;
//                    } else {
//                        validProperty = true;
//                    }
//                }
//            });

//            mDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (!hasFocus && mDescription.getText().toString().matches("")) {
//                        new AlertDialog.Builder(v.getContext())
//                                .setTitle("Null task description")
//                                .setMessage("Whoops! Looks like you forgot to set Task Description!")
//                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        mDescription.setText(mSharedPreferences.getString("taskDescription", "crashDescription")); //parceableTask.getDescription()
//                                        mDescription.setSelectAllOnFocus(true);
//                                        mDescription.setSelection(mDescription.length());
//                                    }
//                                })
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                        validDescription = false;
//                    } else {
//                        validDescription = true;
//                    }
//                }
//            });

//            mNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (!hasFocus && mNotes.getText().toString().matches("")) {
//                        mNotes.setText(mSharedPreferences.getString("taskNotes", "crashNotes"));
//                        mNotes.requestFocus();
//                    }
//                }
//            });

//            if (validTitle && validProperty && validDescription) {
            editAttempted = true;
            invalidateOptionsMenu();
//            }

        } catch (Exception ex) {
            Toast toast = Toast.makeText(TaskDetails.this, "Components not enabled.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }


}

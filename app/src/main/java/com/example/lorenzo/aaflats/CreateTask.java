package com.example.lorenzo.aaflats;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Report attachedReport;
    private Task newTask = new Task();
    private Staff loggedStaff;

    private TaskCreationProcess mTaskDetails = null;

    Firebase taskRef;
    Firebase reportRef;
    Firebase propertyRef;
    Firebase flatRef;
    Firebase staffRef;

    private LinearLayout mTargetDateButtonsLayout;
    private LinearLayout mTargetDateTextEditLayout;
    private ImageView cancelDate;
    private Button tomorrow;
    private Button pickDateButton;
    private AutoCompleteTextView mStaffAssigned;
    private EditText mTargetDateValue;
    private EditText mTitle;
    private AutoCompleteTextView mProperty;
    private Spinner mFlat;
    private EditText mDescription;
    private Spinner mPriority;
    private EditText mNotes;
    private String mReport = "";
    private CardView mCard;
    private TextView mCardText;
    private LinearLayout mErrorLayout;

    private ArrayList<Property> propertyList = new ArrayList<>();
    private ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    private ArrayList<Staff> staffList = new ArrayList<>();
    private ArrayList<String> staffNames = new ArrayList<>();
    private ArrayList<Flat> flatList = new ArrayList<>();
    private ArrayList<Report> reportList = new ArrayList<>();
    private ArrayList<String> flatNums = new ArrayList<>();
    private ArrayList<String> reportTitles = new ArrayList<>();

    private AlertDialog.Builder builder;
    private AlertDialog alert;

    public static final String MY_PREFERENCES = "MyPreferences";
    public static final String FULL_NAME_KEY = "StaffFullName";
    public static final String EMAIL_KEY = "StaffEmail";
    public static final String STAFF_KEY = "StaffKey";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    private Date todayDate;
    private SimpleDateFormat formatDate;
    private int year_x, month_x, day_x;
    static final int DIALOG_ID = 0;

    private static int uniqueID;
    private NotificationCompat.Builder notificationBuilder;

    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Create new task");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Shared Preferences
        mSharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
//        editor = mSharedPreferences.edit();

        // Define inputMethodService to hide keyboard
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Notifications
        notificationBuilder = new NotificationCompat.Builder(this);

        // Initiate components
        mTargetDateButtonsLayout = (LinearLayout) findViewById(R.id.nt_target_date_lnr_layout);
        mTargetDateTextEditLayout = (LinearLayout) findViewById(R.id.nt_date_text_lnr_layout);
        cancelDate = (ImageView) findViewById(R.id.cancel_date_pick);
        tomorrow = (Button) findViewById(R.id.tomorrow_button);
        pickDateButton = (Button) findViewById(R.id.nt_date_picker_button);
        mTargetDateValue = (EditText) findViewById(R.id.nt_target_date_value_edittext);
        mTitle = (EditText) findViewById(R.id.nt_title_editview);
        mStaffAssigned = (AutoCompleteTextView) findViewById(R.id.nt_staff_actv);
        mProperty = (AutoCompleteTextView) findViewById(R.id.nt_property_actv);
        mFlat = (Spinner) findViewById(R.id.nt_flat_spinner);
        mDescription = (EditText) findViewById(R.id.nt_description_editview);
        mPriority = (Spinner) findViewById(R.id.nt_priority_spinner);
        mNotes = (EditText) findViewById(R.id.nt_notes_editview);
        mCard = (CardView) findViewById(R.id.nt_card_btn_attach);
        mCardText = (TextView) findViewById(R.id.nt_card_text_view);
        mErrorLayout = (LinearLayout) findViewById(R.id.nt_error_lnr_layout);

        builder = new AlertDialog.Builder(this);

        // Calendar components
        todayDate = Calendar.getInstance().getTime();
        formatDate = new SimpleDateFormat("dd/MM/yyyy");

        // Firebase references
        Firebase.setAndroidContext(this);
        taskRef = new Firebase(getString(R.string.tasks_location));
        reportRef = new Firebase(getString(R.string.reports_location));
        propertyRef = new Firebase(getString(R.string.properties_location));
        flatRef = new Firebase(getString(R.string.flats_location));
        staffRef = new Firebase(getResources().getString(R.string.staff_location));


//        mTargetDateButtonsLayout.setVisibility(View.INVISIBLE);
//        mTargetDateButtonsLayout.setEnabled(false);

        mStaffAssigned.requestFocus();

        // Create an ArrayAdapter using the string array
        final ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        mProperty.setAdapter(propertyAdapter);

        mProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadCorrespondingFlats(mProperty.getText().toString().trim());
            }
        });

        final ArrayAdapter<String> staffAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_dropdown_item_1line, staffNames);
        mStaffAssigned.setAdapter(staffAdapter);

        // Create an ArrayAdapter using the string array
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priorities, R.layout.custom_spinner);
        // Specify the layout to use when the list of choices appears
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the mPriority
        mPriority.setAdapter(priorityAdapter);
        mPriority.setSelection(2);

//        taskRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                taskList.clear();
//                taskTitles.clear();
//                for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {
//                    Task tsk = tskSnapshot.getValue(Task.class);
//                    taskList.add(tsk);
//                    taskTitles.add(tsk.getTitle());
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                System.out.println("Task: " + "The read failed: " + firebaseError.getMessage());
//            }
//        });

        mCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadApprovedReports();
            }
        });

        //This fills propertyAddrLine1s for the propertyAdapter of mProperty AutoComplete
        propertyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();
                propertyAddrLine1s.clear();
                for (DataSnapshot prtSnapshot : dataSnapshot.getChildren()) {
                    Property prt = prtSnapshot.getValue(Property.class);
                    propertyList.add(prt);
                    propertyAddrLine1s.add(prt.getAddrline1());
                }
                propertyAdapter.notifyDataSetChanged();
                mProperty.setAdapter(propertyAdapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        mTargetDateValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

//        final Date todayDate = Calendar.getInstance().getTime();
//        final SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
        mTargetDateValue.setText(formatDate.format(todayDate));

        tomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                Date tomorrow = cal.getTime();
                mTargetDateValue.setText(formatDate.format(tomorrow));
                cancelDate.performClick();
            }
        });


        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });



        staffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                staffList.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Staff stf = childSnap.getValue(Staff.class);
                    stf.setStaffKey(childSnap.getKey());
                    staffList.add(stf);
                    staffNames.add(stf.getForename() + " " + stf.getSurname());
                    if (stf.getStaffKey().matches(mSharedPreferences.getString(STAFF_KEY, ""))) {
                        loggedStaff = stf;
                    }
                }
                staffAdapter.notifyDataSetChanged();
                mStaffAssigned.setAdapter(staffAdapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    } //END OF onCreate METHOD

    /**
     * Load and display only approved reports to be attached to new task
     */
    private void loadApprovedReports() {

        //This fills the alert dialog with a list of approved reports
        Query approvedReports = reportRef.orderByChild("status").equalTo("Approved");
        approvedReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reportList.clear();
                reportTitles.clear();
                for (DataSnapshot rptSnapshot : dataSnapshot.getChildren()) {
                    Report rpt = rptSnapshot.getValue(Report.class);
                    rpt.setReportKey(rptSnapshot.getKey());
                    reportList.add(rpt);
                    if (rpt.getContent().length() > 23) {
                        reportTitles.add(rpt.getContent().substring(0, 20) + "...");
                    } else {
                        reportTitles.add(rpt.getContent());
                    }
                }

                String[] arrayRepTs = new String[reportTitles.size()];
                arrayRepTs = reportTitles.toArray(arrayRepTs);
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Attach report").setItems(arrayRepTs, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //Toast.makeText(context, "You selected: ", Toast.LENGTH_LONG).show();
                        System.out.println("You attached: " + reportTitles.get(item));
                        //dialog.dismiss();

                        mCard.setCardBackgroundColor(R.color.attach_button_focused);
                        mCardText.setText("\"" + reportTitles.get(item) + "\"");
                        mCardText.setTypeface(null, Typeface.ITALIC);
                        attachReport(reportList.get(item));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alert = builder.create();
                alert.show();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Report: " + "The read failed: " + firebaseError.getMessage());
            }
        });
    }


    /**
     * Upon selecting a property, load the corresponging flats
     * @param property is the Property object selected from AutoCompleteTextView
     */
    private void loadCorrespondingFlats(String property) {
        Query flatQuery = flatRef.orderByChild("addressLine1").equalTo(property); //
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
//                    flatNums.add(split[1].trim().substring(0, 1).toUpperCase() + split[1].trim().substring(1));
                }
                Collections.sort(flatNums, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });

                ArrayAdapter<String> flatAdapter = new ArrayAdapter<>(getBaseContext(),
                        R.layout.custom_spinner, flatNums);
                flatAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                mFlat.setAdapter(flatAdapter);
                flatAdapter.notifyDataSetChanged();
                mFlat.setSelection(0);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Flat: " + "The read failed: " + firebaseError.getMessage());
            }
        });
    }

    public void showDialogOnButtonClick() {
//
//        pickDateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog(DIALOG_ID);
//            }
//        });
    }

    /**
     *
     * @param id is the datepicker being called
     * @return valid date greater than yesterday date
     */
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


    /**
     * Convert selected date and display
     */
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

            mTargetDateValue.setText(formatter); //formatter
            mTitle.requestFocus();

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

    /**
     * constructor for local variable to be assigned to global since inner method demanded a final variable
     * @param attachedReport
     */
    private void attachReport(Report attachedReport) {
        this.attachedReport = attachedReport;
        mReport = attachedReport.getReportKey();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_task, menu);
        return true;
    }

    /**
     * Handle leaving activity early
     */
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
//                sendNotification();
                attemptCreation();
                break;
            case R.id.action_settings:
                onBackPressed();
                break;
        }
        return true;
    }

    /**
     * Once Save is clicked, Check for errors in entered data
     */
    private void attemptCreation() {
        if (mTaskDetails != null) {
            return;
        }

        //Reset errors
        mTitle.setError(null);
        mStaffAssigned.setError(null);
        mProperty.setError(null);
        //Flat
        mDescription.setError(null);
        //Priority
        mNotes.setError(null);
        mCardText.setError(null);

        //Store values at the time of the creation attempt
        String targetDate = mTargetDateValue.getText().toString().trim();
        String title = mTitle.getText().toString().trim();
        String staff = mStaffAssigned.getText().toString().trim();
        String property = mProperty.getText().toString().trim();
        String flat = "";
        try {
            flat = mFlat.getSelectedItem().toString();
        } catch (Exception e) {

        }
        String description = mDescription.getText().toString().trim();
        String priority = mPriority.getSelectedItem().toString();
        String notes = mNotes.getText().toString().trim();
        String report = mReport;

        boolean cancel = false;
        View focusView = null;


        //Check for a valid target date, if the user entered one
        if (TextUtils.isEmpty(targetDate)) {
            mTargetDateValue.setError("This field is required");
            focusView = mTargetDateValue;
            cancel = true;
        }

        //Check for a valid title, if the user entered one
        if (TextUtils.isEmpty(title)) {
            mTitle.setError("This field is required");
            if (focusView == null) {
                focusView = mTitle;
            }
            cancel = true;
        } else if (!isTitleValid(title)) {
            mTitle.setError("This title is invalid");
            if (focusView == null) {
                focusView = mTitle;
            }
            cancel = true;
        }

        //Check for a valid member of staff, if the user entered one
        if (TextUtils.isEmpty(staff)) {
            mStaffAssigned.setError("This field is required");
            if (focusView == null) {
                focusView = mStaffAssigned;
            }
            cancel = true;
        } else if (!isStaffValid(staff)) {
            mStaffAssigned.setError("This name is invalid");
            if (focusView == null) {
                focusView = mStaffAssigned;
            }
            cancel = true;
        }

        //Check for a valid property, if the user entered one
        if (TextUtils.isEmpty(property)) {
            mProperty.setError("This field is required");
            if (focusView == null) {
                focusView = mProperty;
            }
            cancel = true;
        } else if (!isPropertyValid(property)) {
            mProperty.setError("This property is invalid");
            focusView = mProperty;
            cancel = true;
        }

        //Check for a valid flat, if the user entered one
//        if (!isFlatValid(flat)) {
//
//            focusView = mFlat;
//            cancel = true;
//        }

        //Check for a valid description, if the user entered one
        if (TextUtils.isEmpty(description) || !isDescriptionValid(description)) {
            mDescription.setError("This description is too short");
            if (focusView == null) {
                focusView = mDescription;
            }
            cancel = true;
        }

//        Check for valid report, if the user attached one
        if (TextUtils.isEmpty(report)) {
//            mCardText.setError("A valid report is required");
//            if (focusView == null) {
//                focusView = mCard;
//            }
//            cancel = true;
            report = "";
        }

//        Check for valid notes, if the user attached some
        if (TextUtils.isEmpty(notes)) {
//
            notes = "";
        }

        if (cancel) {
            // There was an error; don't attempt creation and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            mTaskDetails = new TaskCreationProcess(targetDate, staff, title, property, flat, description, priority, notes, report);
            mTaskDetails.execute((Void) null);
        }
    }

    /**
     * Validate property
     * @param property
     * @return
     */
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

    /**
     * Validate staff member
     * @param staff
     * @return
     */
    private boolean isStaffValid(String staff) {
        Boolean isStaff = false;
        for (int i = 0; i < staffNames.size(); i++) {
            if (staffNames.get(i).matches(mStaffAssigned.getText().toString().trim())) {
                newTask.setAssignedStaff(staffList.get(i).getStaffKey());
                isStaff = true;
                break;
            }
        }
        return isStaff;
    }

    /**
     * Validate description
     * @param description
     * @return
     */
    private boolean isDescriptionValid(String description) {
        return description.length() > 4;
    }

    /**
     * Validate title
     * @param title
     * @return
     */
    private boolean isTitleValid(String title) {
        return title.length() > 4;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Another interface callback
    }

    /**
     * since all data is valid, save new task
     */
    public class TaskCreationProcess extends AsyncTask<Void, Void, Boolean> {
        private final String mTargetDate;
        private final String mStaff;
        private final String mTitle;
        private final String mProperty;
        private final String mFlatNum;
        private final String mDescription;
        private final String mPriority;
        private final String mNotes;
        private final String mReport;

        public TaskCreationProcess(String targetDate, String staff, String title, String property,
                                   String flatNum, String description, String priority,
                                   String notes, String report) {
            this.mTargetDate = targetDate;
            this.mStaff = staff;
            this.mTitle = title;
            this.mProperty = property;
            this.mFlatNum = flatNum;
            this.mDescription = description;
            this.mPriority = priority;
            this.mNotes = notes;
            this.mReport = report;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

//                newTask.setAssignedStaff(mStaff);
                newTask.setCompletedBy("");
                newTask.setCompletionTimestamp("pending");
                newTask.setCreator(loggedStaff.getStaffKey());
                newTask.setDescription(mDescription);
                newTask.setNotes(mNotes);
                newTask.setPriority(mPriority);
                newTask.setProperty(mProperty + " - " + mFlatNum);
                newTask.setReport(mReport);
                newTask.setStatus(false);
                newTask.setTargetDate(mTargetDate);
                newTask.setTitle(mTitle);
                newTask.setTaskKey("23");

                taskRef.push().setValue(newTask);

                sendNotification(newTask);

                return true;
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            mTaskDetails = null;

            if (success) {
                startActivity(new Intent(CreateTask.this, Homepage.class).putExtra("parceable_task", newTask)); //mine
                Toast.makeText(CreateTask.this, "Success!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CreateTask.this, "An error occurred while trying to save new task", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mTaskDetails = null;
        }
    }

    /**
     * Send notification to assigned staff only
     * @param tk
     */
    private void sendNotification(final Task tk) {
//        final Context c = this;
//        new Thread(new Runnable() {
//            public void run() {
//                notificationBuilder.setAutoCancel(true);
//                notificationBuilder.setSmallIcon(R.drawable.notification_icon);
//                notificationBuilder.setTicker("New task added by " + tk.getCreator());
//                notificationBuilder.setWhen(System.currentTimeMillis());
//                notificationBuilder.setContentTitle(tk.getCreator() + " added a new task");
//                notificationBuilder.setContentText(tk.getTitle()); //newTask.getTitle()
//
////                notificationBuilder.setSound(Uri.parse("file:///sdcard/notification/notification.mp3"));
//                notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); //delay, vibrate, sleep, vibrate, sleep
//                if (tk.getPriority().matches("High")) {
//                    notificationBuilder.setLights(Color.RED, 3000, 3000);
//                } else if (tk.getPriority().matches("Medium")) {
//                    notificationBuilder.setLights(Color.YELLOW, 3000, 3000);
//                } else {
//                    notificationBuilder.setLights(Color.GREEN, 3000, 3000);
//                }
//
//
//                Random randomGen = new Random();
//                uniqueID = randomGen.nextInt(20000 - 1 + 1) + 1;
//
//
//                NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                Intent intent = new Intent(c, TaskDetails.class).putExtra("parceable_task", tk);//.putExtra("parceable_task", newTask);
//                PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                notificationBuilder.setContentIntent(pIntent);
//                mgr.notify(uniqueID, notificationBuilder.build());
//            }
//        }).start();
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyHHmmss");
        String format = s.format(new Date());

        final Firebase notifRef = new Firebase(getResources().getString(R.string.notifications_location));
        final Notification newNoti = new Notification();
        newNoti.setTimestampSent(format);
        newNoti.setType("Task");
        Query getNetTaskKey = taskRef.orderByChild("taskKey").equalTo("23");
        getNetTaskKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot chilsSnap : dataSnapshot.getChildren()) {
//                    tsk.add(chilsSnap.getValue(Task.class));
                    newNoti.setObjectID(chilsSnap.getKey());
                }
                notifRef.push().setValue(newNoti);

                Firebase removeTempKey = taskRef.child(newNoti.getObjectID());
                Map<String, Object> rmKeyMap = new HashMap<>();
                rmKeyMap.put("taskKey", null);
                removeTempKey.updateChildren(rmKeyMap);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


}



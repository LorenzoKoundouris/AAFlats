package com.example.lorenzo.aaflats;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TenantDetails extends AppCompatActivity {

    static final int DIALOG_ID = 0;
    private int year_x, month_x, day_x;

    Tenant parceableTenant = new Tenant();
    Tenant edittedTenant = new Tenant();
    ArrayList<Flat> flatList = new ArrayList<>();
    String edittedFullName;
    String fullName;

    LinearLayout parentLayout;

    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> contractDetailsCollection;
    ExpandableListView expListView;

    boolean validFullName = true;
    boolean validAddress = true;
    boolean validDob = true;
    boolean validTelephone = true;
    boolean validEmail = true;
    boolean validNotes = true;
    String tenantKey;
    String flatKey;
    ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    String[] splitter;

    EditText mFullName;
    EditText mCurrentTenant;
    CheckBox mCurrentTenantCB;
    AutoCompleteTextView mAddress;
    LinearLayout addressLayout;
    EditText mDob;
    Button btCalen;
    EditText mTelephone;
    EditText mEmail;
    EditText mNotes;
    LinearLayout contractLayout;

    boolean editsCancelled;
    boolean editAttempted = false;
    boolean staffAccess;
    MenuItem saveEdit;

    SharedPreferences pref;
    SharedPreferences.Editor prefEditor;

    private InputMethodManager inputMethodManager;

    Firebase tenantRef;
    Firebase flatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);

        //Define inputMethodService to hide keyboard
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
        flatRef = new Firebase(getResources().getString(R.string.flats_location));

        ScrollView td_scrollview = (ScrollView) findViewById(R.id.td_scrollview);
        td_scrollview.smoothScrollTo(0, 0);

//        Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
//        Query findFlat;

        Bundle intent = getIntent().getExtras();
        parceableTenant = intent.getParcelable("parceable_tenant");
        staffAccess = intent.getBoolean("staff_access");

        //copy
//        edittedTenant = parceableTenant;
        edittedTenant = new Tenant(parceableTenant);
        if (staffAccess) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        parentLayout = (LinearLayout) findViewById(R.id.parent_layout);
        mFullName = (EditText) findViewById(R.id.et_fullname_edittext);
        mCurrentTenant = (EditText) findViewById(R.id.current_tenant_edittext);
        mCurrentTenantCB = (CheckBox) findViewById(R.id.current_tenant_checkbox);
        mAddress = (AutoCompleteTextView) findViewById(R.id.et_address_actv);
        addressLayout = (LinearLayout) findViewById(R.id.address_layout);
        mDob = (EditText) findViewById(R.id.et_dob_edittext);
        btCalen = (Button) findViewById(R.id.nt_calendar_button);
        mTelephone = (EditText) findViewById(R.id.et_telephone_edittext);
        mEmail = (EditText) findViewById(R.id.et_email_edittext);
        mNotes = (EditText) findViewById(R.id.et_notes_edittext);
        contractLayout = (LinearLayout) findViewById(R.id.contract_layout);

        setTitle(edittedTenant.getForename() + "'s details");

        if (!edittedTenant.getMiddlename().matches("")) {
            mFullName.setText(edittedTenant.getSurname() + " " + edittedTenant.getForename() + " " + edittedTenant.getMiddlename());
            edittedFullName = edittedTenant.getSurname() + " " + edittedTenant.getForename() + " " + edittedTenant.getMiddlename();
        } else {
            mFullName.setText(edittedTenant.getSurname() + " " + edittedTenant.getForename());
            edittedFullName = edittedTenant.getSurname() + " " + edittedTenant.getForename();
        }

        fullName = edittedFullName;

        if (edittedTenant.isCurrentTenant()) {
            mCurrentTenantCB.setChecked(true);
            mCurrentTenant.setText("Current tenant status: Yes");
            mAddress.setText(edittedTenant.getProperty());
            loadContractDetails();
        } else {
            mCurrentTenantCB.setChecked(false);
            mCurrentTenant.setText("Current tenant status: No");
            addressLayout.setVisibility(View.GONE);
            contractLayout.setVisibility(View.GONE);
        }

//        mAddress.setText(parceableTenant.getProperty());
        mDob.setText(edittedTenant.getDob());
        mNotes.setText(edittedTenant.getNotes());
        mTelephone.setText(edittedTenant.getTelephone());
        mEmail.setText(edittedTenant.getEmail());


        //Get tenant FLAT key, propertyAddrLine1s for actv adapter and FlatList
        flatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fltSnapshot : dataSnapshot.getChildren()) {
                    Flat flt = fltSnapshot.getValue(Flat.class);
                    flt.setFlatKey(fltSnapshot.getKey());
                    flatList.add(flt);
                    propertyAddrLine1s.add(flt.getAddressLine1().trim() +
                            " - " + flt.getFlatNum().trim());
                }

                Set<String> removeDuplicates = new HashSet<>();
                removeDuplicates.addAll(propertyAddrLine1s);
                propertyAddrLine1s.clear();
                propertyAddrLine1s.addAll(removeDuplicates);
                Collections.sort(propertyAddrLine1s, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        mAddress.setAdapter(propertyAdapter);

        mAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                splitter = mAddress.getText().toString().split(" - ");
            }
        });



    } //End of OnCreate

    private void loadContractDetails() {
        groupList = new ArrayList<String>();
        groupList.add(" Contract details");

        // preparing contract details collection(child)

        String[] contractDetails = {"Start date: " + parceableTenant.getContractStart(),
                "End date: " + parceableTenant.getContractEnd(),
                "Download contract"};

        contractDetailsCollection = new LinkedHashMap<String, List<String>>();


        for (String conDet : groupList) {
            loadChild(contractDetails);
            contractDetailsCollection.put(conDet, childList);
        }

        expListView = (ExpandableListView) findViewById(R.id.contract_details_list);
        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
                this, groupList, contractDetailsCollection);
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final String selected = (String) expListAdapter.getChild(
                        groupPosition, childPosition);
                Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        });

    }

    private void loadChild(String[] items) {
        childList = new ArrayList<String>();
        for (String item : items)
            childList.add(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID) {
            DatePickerDialog da = new DatePickerDialog(this, dpickerListener,
                    year_x, month_x, day_x);

            Calendar c = Calendar.getInstance();
            Date newDate = c.getTime();

            c.add(Calendar.YEAR, -18);
            da.getDatePicker().setMaxDate(newDate.getTime());

            c.add(Calendar.YEAR, -82);
            newDate = c.getTime();
            da.getDatePicker().setMinDate(newDate.getTime());

            return da;

            //return new DatePickerDialog(this, dpickerListener, year_x, month_x, day_x);
        } else {
            return null;
        }
    }

    private DatePickerDialog.OnDateSetListener dpickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            year_x = year;
            month_x = monthOfYear + 1;
            day_x = dayOfMonth;
            String formatter = (new DecimalFormat("00").format(day_x) + "/" +
                    new DecimalFormat("00").format(month_x) + "/" +
                    new DecimalFormat("00").format(year_x));

            Calendar adultCheck = Calendar.getInstance();
            adultCheck.add(Calendar.YEAR, -18);
            final SimpleDateFormat formatt = new SimpleDateFormat("dd/MM/yyyy");
            Date maxAdultDate = adultCheck.getTime();

            Date chosenDate = new Date();
            try {
                chosenDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(formatter);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (chosenDate.before(maxAdultDate)) {
                mDob.setText(formatt.format(chosenDate));
                validDob = true;
            } else {
                mDob.setText("");
                validDob = false;
                new AlertDialog.Builder(view.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Error")
                        .setMessage("A tenant must be at least 18 years of age.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem backToAll;
        if (staffAccess) {
            if (!editAttempted) {
                getMenuInflater().inflate(R.menu.property_details, menu);
                saveEdit = menu.findItem(R.id.edit_task);
            } else {
                getMenuInflater().inflate(R.menu.task_details_save, menu);
                saveEdit = menu.findItem(R.id.save_edited_task);
            }
            backToAll = menu.findItem(R.id.action_settings);
            backToAll.setTitle("Back to All Tenants");
        } else {
            getMenuInflater().inflate(R.menu.tenant_homepage, menu);
            backToAll = menu.findItem(R.id.my_account);
            backToAll.setTitle("Back to Tenant Homepage");
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (staffAccess) {
            if (editAttempted) { //editAttempted && !editsCancelled || editAttempted
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Leaving page")
                        .setMessage("You have not saved changes made to this Tenant. Press Yes to discard or No to remain on page.")
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
        } else {
            finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
            case R.id.edit_task:
                Toast toast1 = Toast.makeText(getApplicationContext(), "Edit button clicked", Toast.LENGTH_SHORT);
                toast1.setGravity(Gravity.CENTER, 0, 0);
                toast1.show();
                enableComponents();
                break;
            case R.id.save_edited_task:
//                validateData();
                attemptEdit();
                Toast toast = Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;
            case R.id.cancel_edit_task:
//                editsCancelled = true;
//                validateData();
                cancelAndRestore();
                break;
            case R.id.action_settings:
                onBackPressed();
                break;
            case R.id.my_account:
                onBackPressed();
        }
        return true;
    }

    private void cancelAndRestore() {
        mFullName.setText(fullName);
//        if (parceableTenant.isCurrentTenant()) {
        mCurrentTenantCB.setChecked(parceableTenant.isCurrentTenant());//parceableTask.getAssignedStaff()
//        }
        mAddress.setText(parceableTenant.getProperty());
        mDob.setText(parceableTenant.getDob());
        mTelephone.setText(parceableTenant.getTelephone());
        mEmail.setText(parceableTenant.getEmail());
        mNotes.setText(parceableTenant.getNotes());

        edittedTenant = new Tenant(parceableTenant);

        parentLayout.requestFocus();

        disableComponents();
    }

    private void disableComponents() {
        mFullName.setEnabled(false);
        mFullName.setClickable(false);
        mCurrentTenantCB.setEnabled(false);
        mAddress.setEnabled(false);
        mDob.setEnabled(false);
        btCalen.setEnabled(false);
        mTelephone.setEnabled(false);
        mEmail.setEnabled(false);
        mNotes.setEnabled(false);

        mFullName.setTextColor(getResources().getColor(R.color.grey_color));
        mCurrentTenant.setTextColor(getResources().getColor(R.color.grey_color));
        mAddress.setTextColor(getResources().getColor(R.color.grey_color));
        mDob.setTextColor(getResources().getColor(R.color.grey_color));
        mTelephone.setTextColor(getResources().getColor(R.color.grey_color));
        mEmail.setTextColor(getResources().getColor(R.color.grey_color));
        mNotes.setTextColor(getResources().getColor(R.color.grey_color));

        editAttempted = false;
        invalidateOptionsMenu();
    }

    private void attemptEdit() {
        mFullName.setEnabled(true);
        mFullName.setClickable(true);
        mCurrentTenantCB.setEnabled(true);
        mAddress.setEnabled(true);
        mDob.setEnabled(true);
        btCalen.setEnabled(true);
        mTelephone.setEnabled(true);
        mEmail.setEnabled(true);
        mNotes.setEnabled(true);

        boolean cancel = false;
        View focusView = null;

        //Check for valid Address if the user entered one
        if (mCurrentTenantCB.isChecked()) {
            if (TextUtils.isEmpty(mAddress.getText().toString())) {
                mAddress.setError("This field is required.");
                cancel = true;
                focusView = mAddress;
            } else if (!isAddressValid(mAddress.getText().toString())) {
                mAddress.setError("This address is invalid.");
                cancel = true;
                focusView = mAddress;
            }
        }

        //Check for a valid date of birth, if the user entered one
        if (TextUtils.isEmpty(mDob.getText().toString())) {
            mDob.setError("This field is required.");
            cancel = true;
            focusView = mDob;
        } else if (!isDobValid(mDob.getText().toString())) {
            mDob.setError("This address is invalid.");
            cancel = true;
            focusView = mDob;
        }

        //Check for a valid email, if the user entered one
        if (!validateEmail(mEmail.getText().toString())) {
            mEmail.setError("This email is invalid");
            if (focusView == null) {
                focusView = mEmail;
            }
            cancel = true;
        }

        //Check for a valid surname, if the user entered one
        if (TextUtils.isEmpty(mTelephone.getText().toString().trim())) {
            mTelephone.setError("This field is required");
            if (focusView == null) {
                focusView = mTelephone;
            }
            cancel = true;
        } else if (!isTelValid(mTelephone.getText().toString().trim())) {
            mTelephone.setError("This telephone number is invalid");
            if (focusView == null) {
                focusView = mTelephone;
            }
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt creation and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            saveEdittedTenant();
        }
    }

    private void saveEdittedTenant() {
//        if (edittedTenant.isCurrentTenant()) {//
            for (Flat flt : flatList) {
                if (mAddress.getText().toString().matches(flt.getAddressLine1() + " - " + flt.getFlatNum())) {
                    flatKey = flt.getFlatKey();
                }
            }
            Firebase changeTenant = flatRef.child(flatKey);

            final Map<String, Object> flatHasTenantMap = new HashMap<>();

            if (mCurrentTenantCB.isChecked()) {
                edittedTenant.setCurrentTenant(true);
                flatHasTenantMap.put("tenant", edittedTenant.getTenantKey());
                changeTenant.updateChildren(flatHasTenantMap);
                edittedTenant.setProperty(mAddress.getText().toString());
            } else {
                edittedTenant.setCurrentTenant(false);
                edittedTenant.setProperty("");
                flatHasTenantMap.put("tenant", "");
                changeTenant.updateChildren(flatHasTenantMap);
//            edittedTenant.setProperty(mAddress.getText().toString());
            }
//        }

        fullName = edittedFullName;

        edittedTenant.setDob(mDob.getText().toString().trim());
        edittedTenant.setTelephone(mTelephone.getText().toString());
        edittedTenant.setNotes(mNotes.getText().toString().trim());
        edittedTenant.setEmail(mEmail.getText().toString().trim());

        tenantRef.child(edittedTenant.getTenantKey()).setValue(edittedTenant);

        parceableTenant = new Tenant(edittedTenant);
        disableComponents();

        parentLayout.requestFocus();

        Firebase changeKey = tenantRef.child(edittedTenant.getTenantKey());
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("tenantKey", null);
        changeKey.updateChildren(keyMap);

        Toast toast = Toast.makeText(TenantDetails.this, "Tenant edited! SUCCESS!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private boolean isTelValid(String trim) {
//        return android.util.Patterns.PHONE.matcher(trim).matches();
        return PhoneNumberUtils.isGlobalPhoneNumber(trim);
    }

    private boolean isDobValid(String s) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date dob = dateFormat.parse(s.toString());
            return true;
        } catch (Exception e) {
            Toast.makeText(TenantDetails.this, "Mandatory format: DD/MM/YYYY", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean isAddressValid(String s) {
        for (Flat flt : flatList) {
            if (mAddress.getText().toString().matches(flt.getAddressLine1() + " - " + flt.getFlatNum())) {
                flatKey = flt.getFlatKey();
                return true;
            }
        }
//        for (String str : propertyAddrLine1s) {
//            if (s.matches(str)) {
//                return true;
//            }
//        }
        return false;
    }


    private void enableComponents() {
        editsCancelled = false;

        try {
            mFullName.setEnabled(true);
            mFullName.setClickable(true);
            mCurrentTenantCB.setEnabled(true);
            mAddress.setEnabled(true);
            mDob.setEnabled(true);
            btCalen.setEnabled(true);
            mTelephone.setEnabled(true);
            mEmail.setEnabled(true);
            mNotes.setEnabled(true);

            //OK
            mFullName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mFullName.getText().toString().matches(fullName)) {
                        mFullName.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mFullName.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            mDob.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mDob.getText().toString().matches(edittedTenant.getDob())) {
                        mDob.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mDob.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            mTelephone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mTelephone.getText().toString().matches(edittedTenant.getTelephone())) {
                        mTelephone.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mTelephone.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            mEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mEmail.getText().toString().matches(edittedTenant.getEmail())) {
                        mEmail.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mEmail.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            mNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mNotes.getText().toString().matches(edittedTenant.getNotes())) {
                        mNotes.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        mNotes.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            btCalen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(DIALOG_ID);
                }
            });

            //OK
            mFullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        final android.support.v7.app.AlertDialog.Builder alertBuilder =
                                new android.support.v7.app.AlertDialog.Builder(v.getContext());

                        alertBuilder.setTitle("Edit name");
                        LinearLayout layout = new LinearLayout(v.getContext());
                        layout.setOrientation(LinearLayout.VERTICAL);

                        final EditText forenameET = new EditText(v.getContext());
                        forenameET.setHint("First name");
                        forenameET.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        forenameET.setText(edittedTenant.getForename());
                        layout.addView(forenameET);

                        final EditText middlenameET = new EditText(v.getContext());
                        middlenameET.setHint("Middle name");
                        middlenameET.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        if (!edittedTenant.getMiddlename().matches("")) {
                            middlenameET.setText(edittedTenant.getMiddlename());

                        }
                        layout.addView(middlenameET);

                        final EditText surnameET = new EditText(v.getContext());
                        surnameET.setHint("Last name");
                        surnameET.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        surnameET.setText(edittedTenant.getSurname());
                        layout.addView(surnameET);

                        layout.setFocusable(true);
                        layout.setFocusableInTouchMode(true);

                        alertBuilder.setView(layout);

                        alertBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                String editFullName = "";

                                if (!middlenameET.getText().toString().matches("")) {
                                    edittedFullName = surnameET.getText().toString().trim() + " " +
                                            forenameET.getText().toString().trim() + " " +
                                            middlenameET.getText().toString().trim();
                                } else {
                                    edittedFullName = surnameET.getText().toString().trim() + " " +
                                            forenameET.getText().toString().trim();
                                }

                                mFullName.setText(edittedFullName);
                                edittedTenant.setForename(forenameET.getText().toString());
                                edittedTenant.setMiddlename(middlenameET.getText().toString());
                                edittedTenant.setSurname(surnameET.getText().toString());
                                mCurrentTenantCB.requestFocus();
                            }
                        });
                        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCurrentTenantCB.requestFocus();
                            }
                        });

//                        alertBuilder.show();
                        final AlertDialog dialog = alertBuilder.create();
                        dialog.show();

                        forenameET.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (!forenameET.getText().toString().matches("") && !surnameET.getText().toString().matches("")) {
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                } else {
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                }
                            }
                        });

                        surnameET.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (!forenameET.getText().toString().matches("") && !surnameET.getText().toString().matches("")) {
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                } else {
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                }
                            }
                        });

                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                parentLayout.requestFocus();
                            }
                        });
                    }
                }
            });

            //Should be OK
            mCurrentTenantCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        addressLayout.setVisibility(View.VISIBLE);
                        edittedTenant.setCurrentTenant(true);
                        mCurrentTenant.setText("Current tenant status: Yes");

                        mAddress.requestFocus();

                        Toast toast = Toast.makeText(TenantDetails.this, "Assign a flat to " + edittedTenant.getForename(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    } else {
                        ///////edittedTenant.setCurrentTenant(false);
                        if (!edittedTenant.getProperty().matches("")) {
                            new AlertDialog.Builder(buttonView.getContext())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Warning")
                                    .setMessage("You are removing " + edittedTenant.getForename() + " from " + edittedTenant.getProperty() + ". Continue?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                            edittedTenant.setProperty("");
//                                            edittedTenant.setCurrentTenant(false);
                                            mCurrentTenant.setText("Current tenant status: No");
                                            addressLayout.setVisibility(View.GONE);
                                            mCurrentTenant.requestFocus();
                                        }

                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                            edittedTenant.setProperty(parceableTenant.getProperty());
//                                            edittedTenant.setCurrentTenant(true);
                                            mCurrentTenant.setText("Current tenant status: Yes");
                                            addressLayout.setVisibility(View.VISIBLE);
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(buttonView.getContext())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Error")
                                    .setMessage("There is no property to remove")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mCurrentTenant.requestFocus();
                                        }

                                    })
                                    .show();
                        }
                    }
                    mCurrentTenant.setTextColor(Color.parseColor("#FF5722"));
                }
            });

            editAttempted = true;
            invalidateOptionsMenu();

        } catch (Exception ex) {
            Toast toast = Toast.makeText(TenantDetails.this, "Components not enabled.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }


    }

    public final static boolean validateEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}

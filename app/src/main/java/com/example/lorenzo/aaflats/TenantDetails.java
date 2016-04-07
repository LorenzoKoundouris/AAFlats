package com.example.lorenzo.aaflats;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TenantDetails extends AppCompatActivity {

    static final int DIALOG_ID = 0;
    private int year_x, month_x, day_x;

    Tenant parceableTenant;
    ArrayList<Flat> flatList = new ArrayList<>();

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


    EditText etFullName;
    EditText etCurrentTenant;
    CheckBox etCurrentTenantCB;
    AutoCompleteTextView actvAddress;
    Button btCalen;
    EditText etDob;
    EditText etTelephone;
    EditText etEmail;
    EditText etNotes;

    boolean editsCancelled;
    boolean attemptEdit = false;
    boolean staffAccess;
    MenuItem saveEdit;

    SharedPreferences pref;
    SharedPreferences.Editor prefEditor;

    Firebase tenantRef;
    Firebase flatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
        flatRef = new Firebase(getResources().getString(R.string.flats_location));

        ScrollView td_scrollview = (ScrollView) findViewById(R.id.td_scrollview);
        td_scrollview.smoothScrollTo(0, 0);

//        Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
//        Query findFlat;

        Bundle intent = getIntent().getExtras();
        parceableTenant = intent.getParcelable("parceable_tenant");
        staffAccess = intent.getBoolean("staff_access");

        if(staffAccess){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pref = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefEditor = pref.edit();

        etFullName = (EditText) findViewById(R.id.et_fullname_edittext);
        etCurrentTenant = (EditText) findViewById(R.id.current_tenant_edittext);
        etCurrentTenantCB = (CheckBox) findViewById(R.id.current_tenant_checkbox);
        actvAddress = (AutoCompleteTextView) findViewById(R.id.et_address_actv);
        etDob = (EditText) findViewById(R.id.et_dob_edittext);
        btCalen = (Button) findViewById(R.id.nt_calendar_button);
        etTelephone = (EditText) findViewById(R.id.et_telephone_edittext);
        etEmail = (EditText) findViewById(R.id.et_email_edittext);
        etNotes = (EditText) findViewById(R.id.et_notes_edittext);

        setTitle(parceableTenant.getForename() + "'s details");
        if (!parceableTenant.getMiddlename().matches("")) {
            etFullName.setText(parceableTenant.getSurname() + " " + parceableTenant.getForename() + " " + parceableTenant.getMiddlename());
        } else {
            etFullName.setText(parceableTenant.getSurname() + " " + parceableTenant.getForename());
        }

        if (parceableTenant.isCurrentTenant()) {
            etCurrentTenantCB.setChecked(true);
            etCurrentTenant.setText("Current tenant status: Yes");
        } else {
            etCurrentTenantCB.setChecked(false);
            etCurrentTenant.setText("Current tenant status: No");
        }

        if (parceableTenant.isCurrentTenant()) {
            actvAddress.setText(parceableTenant.getProperty());
        }

        actvAddress.setText(parceableTenant.getProperty());

        etDob.setText(parceableTenant.getDob());
        etNotes.setText(parceableTenant.getNotes());
        etTelephone.setText(parceableTenant.getTelephone());
        etEmail.setText(parceableTenant.getEmail());

        prefEditor.putString("tForename", parceableTenant.getForename());
        if(!parceableTenant.getMiddlename().matches("")){
            prefEditor.putString("tMiddlename", parceableTenant.getMiddlename());
        }
        prefEditor.putString("tSurname", parceableTenant.getSurname());
        prefEditor.putString("tFullName", etFullName.getText().toString());
        prefEditor.putBoolean("tIsCurrentTenant", parceableTenant.isCurrentTenant());
        prefEditor.putString("tAddress", parceableTenant.getProperty());
        prefEditor.putString("tDob", parceableTenant.getDob());
        prefEditor.putString("tTelephone", parceableTenant.getTelephone());
        prefEditor.putString("tEmail", parceableTenant.getEmail());
        prefEditor.putString("tNotes", parceableTenant.getNotes());
        prefEditor.putString("tContractStart", parceableTenant.getContractStart());
        prefEditor.putString("tContractEnd", parceableTenant.getContractEnd());
        prefEditor.commit();

        flatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fltSnapshot : dataSnapshot.getChildren()) {
                    Flat flt = fltSnapshot.getValue(Flat.class);
                    flt.setFlatKey(fltSnapshot.getKey());
                    flatList.add(flt);
                    propertyAddrLine1s.add(flt.getAddressLine1().toLowerCase().trim() +
                            " - " + flt.getFlatNum().toLowerCase().trim());
                }

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
        actvAddress.setAdapter(propertyAdapter);

        actvAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parceableTenant.setProperty(actvAddress.getText().toString().trim());
                parceableTenant.setCurrentTenant(true);
                etCurrentTenantCB.setChecked(true);
            }
        });

        actvAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && actvAddress.getText().toString().matches("No flat assigned")) {
                    actvAddress.setText("");
                } else if (!hasFocus && actvAddress.getText().toString().matches("")) {
                    actvAddress.setText("No flat assigned");
                }
            }
        });

        final String[] splitter = pref.getString("tAddress", "crashAddress").split(" - ");
        Query getFKey = flatRef.orderByChild("addressLine1").equalTo(splitter[0].toLowerCase().trim());
        getFKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Flat flt = childSnap.getValue(Flat.class);
                    if(flt.getFlatNum().matches(splitter[1])){
                        flatKey = childSnap.getKey();
                        break;
                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        Query getTKey = tenantRef.orderByChild("surname").equalTo(pref.getString("tSurname", "crashSurname"));
        getTKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                    Tenant tnt = childSnap.getValue(Tenant.class);
                    if(tnt.getForename().matches(pref.getString("tForename", "crashForename"))){
                        tenantKey = childSnap.getKey();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        createGroupList();

        createCollection();

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

        btCalen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });


    } //End of OnCreate

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
                etDob.setText(formatt.format(chosenDate));
                validDob = true;
            } else {
                etDob.setText("");
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

    private void createGroupList() {
        groupList = new ArrayList<String>();
        groupList.add(" Contract details");
    }

    private void createCollection() {
        // preparing contract details collection(child)

        String[] contractDetails = {"Start date: " + parceableTenant.getContractStart(),
                "End date: " + parceableTenant.getContractEnd(),
                "Download contract"};

        contractDetailsCollection = new LinkedHashMap<String, List<String>>();


        for (String conDet : groupList) {
            loadChild(contractDetails);
            contractDetailsCollection.put(conDet, childList);
        }
    }

    private void loadChild(String[] laptopModels) {
        childList = new ArrayList<String>();
        for (String model : laptopModels)
            childList.add(model);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem backToAll;
        if(staffAccess){
            if (!attemptEdit) {
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
        if(staffAccess){
            if (attemptEdit && !editsCancelled || attemptEdit) {
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
        }
        else {
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
                editTenantDetails();
                break;
            case R.id.save_edited_task:
                validateData();
                Toast toast = Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;
            case R.id.cancel_edit_task:
                editsCancelled = true;
                validateData();
                break;
            case R.id.action_settings:
                onBackPressed();
                break;
            case R.id.my_account:
                onBackPressed();
        }
        return true;
    }

    private void validateData() {

        //////////////////////////////////////////////////////////////////////////////

        if (etDob.getText().toString().matches("")) {
            etDob.setBackgroundColor(Color.parseColor("#EF9A9A"));
            validDob = false;
        } else {
            validDob = true;
        }

        //////////////////////////////////////////////////////////////////////////////
        if (etEmail.getText().toString().matches("")) {
            etEmail.setBackgroundColor(Color.parseColor("#EF9A9A"));
            validEmail = false;
        } else {
            if (validateEmail(etEmail.getText().toString())) {
                validEmail = true;
            } else {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Error")
                        .setMessage("Please enter a valid email address")
                        .setPositiveButton("OK", null)
                        .show();
                validEmail = false;
                etEmail.setBackgroundColor(Color.parseColor("#EF9A9A"));
            }
        }

        //////////////////////////////////////////////////////////////////////////////

        if (etTelephone.getText().toString().matches("")) {
            etTelephone.setBackgroundColor(Color.parseColor("#EF9A9A"));
            validTelephone = false;
        } else {
            validTelephone = true;
        }

        //////////////////////////////////////////////////////////////////////////////

        String nullVitalVals = "";
        String nullVals = "";
        if (!validDob) {
            nullVitalVals += "\n- Date of Birth";
        }
        if (!validEmail) {
            nullVals += "\n- Email";
        }
        if (!validTelephone) {
            nullVals += "\n- Telephone";
        }

        if (!nullVitalVals.matches("")) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid data")
                    .setMessage("Whoops! Looks like these fields contain wrong information or none at all:" +
                            "\n\n" + nullVitalVals)
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else if (nullVitalVals.matches("") && !nullVals.matches("")) {
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Are you sure you want to create a Tenant without the following?" +
                            "\n\n" + nullVals)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveChangesToTenant();
                        }
                    })
                    .setNegativeButton("No", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            saveChangesToTenant();
        }



    }

    private void saveChangesToTenant() {

        if(!editsCancelled){
            try{
                setTitle(parceableTenant.getForename() + "'s details");
                parceableTenant.setDob(etDob.getText().toString().trim());
                parceableTenant.setTelephone(etTelephone.getText().toString().trim());
                parceableTenant.setEmail(etEmail.getText().toString().trim());
                parceableTenant.setNotes(etNotes.getText().toString().trim());

                String sss = "\n";
                sss += parceableTenant.getForename() + "\n";
                sss += parceableTenant.getMiddlename() + "\n";
                sss += parceableTenant.getSurname() + "\n";
                sss += parceableTenant.getProperty() + "\n";
                sss += parceableTenant.getDob() + "\n";
                sss += parceableTenant.getEmail() + "\n";
                sss += parceableTenant.getTelephone() + "\n";
                sss += parceableTenant.getNotes() + "\n";

                new AlertDialog.Builder(this)
                        .setTitle("tenant")
                        .setMessage(sss)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


//                tenantRef.child(tenantKey).setValue(parceableTenant);
                Toast toast = Toast.makeText(TenantDetails.this, "Tenant edited! SUCCESS!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } catch (Exception ex) {
                Toast toast = Toast.makeText(TenantDetails.this, "Tenant not edited. FAIL", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else {
            setTitle(pref.getString("tForename", "crashForename"));
            parceableTenant.setForename(pref.getString("tForename", "crashForename"));
            parceableTenant.setMiddlename(pref.getString("tMiddlename", "crashMiddlename"));
            parceableTenant.setSurname(pref.getString("tSurname", "crashSurname"));
            parceableTenant.setDob(pref.getString("tDob", "crashDob"));
            parceableTenant.setTelephone(pref.getString("tTelephone", "crashTelephone"));
            parceableTenant.setEmail(pref.getString("tEmail", "crashEmail"));
            parceableTenant.setNotes(pref.getString("tNotes", "crashNotes"));
        }

        etFullName.setEnabled(false);
        etFullName.setClickable(false);
        etCurrentTenantCB.setEnabled(false);
        actvAddress.setEnabled(false);
        etDob.setEnabled(false);
        btCalen.setEnabled(true);
        etTelephone.setEnabled(false);
        etEmail.setEnabled(false);
        etNotes.setEnabled(false);


        attemptEdit = false;
        invalidateOptionsMenu();
    }

    private void editTenantDetails() {
        editsCancelled = false;

        try {
            etFullName.setEnabled(true);
            etFullName.setClickable(true);
            etCurrentTenantCB.setEnabled(true);
            actvAddress.setEnabled(true);
            etDob.setEnabled(true);
            etTelephone.setEnabled(true);
            etEmail.setEnabled(true);
            etNotes.setEnabled(true);

            //OK
            etFullName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etFullName.getText().toString().matches(pref.getString("tFullName", "crashName"))) {
                        etFullName.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etFullName.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            etDob.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etDob.getText().toString().matches(pref.getString("tDob", "crashDob"))) {
                        etDob.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etDob.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            etTelephone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etTelephone.getText().toString().matches(pref.getString("tTelephone", "crashTelephone"))) {
                        etTelephone.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etTelephone.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            etEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etEmail.getText().toString().matches(pref.getString("tEmail", "crashEmail"))) {
                        etEmail.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etEmail.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            etNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (etNotes.getText().toString().matches(pref.getString("tNotes", "crashNotes"))) {
                        etNotes.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        etNotes.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            //OK
            etFullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                        forenameET.setText(parceableTenant.getForename());
                        layout.addView(forenameET);

                        final EditText middlenameET = new EditText(v.getContext());
                        middlenameET.setHint("Middle name");
                        middlenameET.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        if (!parceableTenant.getMiddlename().matches("")) {
                            middlenameET.setText(parceableTenant.getMiddlename());

                        }
                        layout.addView(middlenameET);

                        final EditText surnameET = new EditText(v.getContext());
                        surnameET.setHint("Last name");
                        surnameET.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        surnameET.setText(parceableTenant.getSurname());
                        layout.addView(surnameET);

                        layout.setFocusable(true);
                        layout.setFocusableInTouchMode(true);

                        alertBuilder.setView(layout);


                        alertBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String editFullName = "";

                                if (!middlenameET.getText().toString().matches("")) {
                                    editFullName = surnameET.getText().toString().trim() + " " +
                                            forenameET.getText().toString().trim() + " " +
                                            middlenameET.getText().toString().trim();
                                } else {
                                    editFullName = surnameET.getText().toString().trim() + " " +
                                            forenameET.getText().toString().trim();
                                }

                                etFullName.setText(editFullName);
                                parceableTenant.setForename(forenameET.getText().toString());
                                parceableTenant.setMiddlename(middlenameET.getText().toString());
                                parceableTenant.setSurname(surnameET.getText().toString());
                                etCurrentTenant.requestFocus();
                            }
                        });
                        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                etCurrentTenant.requestFocus();
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
                    }
                }
            });

            etCurrentTenantCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Firebase changeIsCurrentTenant = tenantRef.child(tenantKey);
                    final Map<String, Object> isCurrentTenantMap = new HashMap<>();
                    final Map<String, Object> flatHasTenantMap = new HashMap<>();
//                    String[] splitter = pref.getString("tAddress", "crashAddress").split(" - ");
//                    Query getFKey = flatRef.orderByChild("addressLine1").equalTo(splitter[0].toLowerCase().trim());
//
//                    getFKey.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            for(DataSnapshot childSnap : dataSnapshot.getChildren()){
//                                Flat flt = childSnap.getValue(Flat.class);
//                                flatKey.add(childSnap.getKey());
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(FirebaseError firebaseError) {
//
//                        }
//                    });


//                    final Firebase changeFlatHasTenant = flatRef.child(flatKey.get(0));


                    if (isChecked) {
                        actvAddress.requestFocus();
                        parceableTenant.setCurrentTenant(true);
                        isCurrentTenantMap.put("currentTenant", true);
                        if(!parceableTenant.getMiddlename().matches("")){
                            flatHasTenantMap.put("tenant", parceableTenant.getForename().trim() + " "
                                    + parceableTenant.getMiddlename().trim() + " " + parceableTenant.getSurname().trim());
                        } else {
                            flatHasTenantMap.put("tenant", parceableTenant.getForename().trim() + " "
                                    + parceableTenant.getSurname().trim());
                        }
                        etCurrentTenant.setText("Current tenant status: Yes");



                        Toast toast = Toast.makeText(TenantDetails.this, "Assign a flat to " + parceableTenant.getForename(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    } else {
                        if (!parceableTenant.getProperty().matches("")) {
                            new AlertDialog.Builder(buttonView.getContext())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Remove tenant")
                                    .setMessage("You are removing " + parceableTenant.getForename() + " from " + parceableTenant.getProperty() + ". Continue?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            parceableTenant.setProperty("");
                                            parceableTenant.setCurrentTenant(false);
                                            actvAddress.setText("No flat assigned");
                                            isCurrentTenantMap.put("currentTenant", false);
                                            flatHasTenantMap.put("tenant", "");
                                            etCurrentTenant.setText("Current tenant status: No");
                                            etCurrentTenant.requestFocus();
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        } else {
                            new AlertDialog.Builder(buttonView.getContext())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Error")
                                    .setMessage("There is no property to remove")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            etCurrentTenant.requestFocus();
                                        }

                                    })
                                    .show();
                        }
                    }
                    changeIsCurrentTenant.updateChildren(isCurrentTenantMap);
                    etCurrentTenant.setTextColor(Color.parseColor("#FF5722"));
                }
            });

            etTelephone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (etTelephone.getText().toString().matches("")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null telephone")
                                .setMessage("Whoops! Looks like you forgot to set a telephone number!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        etTelephone.setText(pref.getString("pTelephone", "crashTelephone"));
                                        etTelephone.requestFocus();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        validTelephone = false;
                    } else if(etTelephone.getText().length() < 10){
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Missing digits")
                                .setMessage("The telephone number must be 10 characters long.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        etTelephone.setText(pref.getString("pTelephone", "crashTelephone"));
                                        etTelephone.requestFocus();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        validTelephone = false;
                    } else {
                        validTelephone = true;
                    }
                }
            });


            if(actvAddress.getText().toString().matches("")){
                validAddress = false;
            } else {
                for(String str : propertyAddrLine1s){
                    if(actvAddress.getText().toString().matches(str)){
                        validAddress = true;
                        break;
                    }
                }
            }


            if(validAddress && validEmail && validTelephone && validDob && validNotes){
                attemptEdit = true;
                invalidateOptionsMenu();
            }

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

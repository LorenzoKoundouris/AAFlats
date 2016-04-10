package com.example.lorenzo.aaflats;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Locale;

public class CreateTenant extends AppCompatActivity {

    private EditText etForename, etMiddlename, etSurname, etDob, etEmail, etTel;
    private boolean isValidForename, isValidMiddlename, isValidSurname, isValidDob,
            isValidEmail, isValidTel = false;
    static final int DIALOG_ID = 0;
    private int year_x, month_x, day_x;
    private Tenant newTenant;

    Firebase propertyRef;
    Firebase flatRef;
    ArrayList<Flat> flatList = new ArrayList<>();
    ArrayList<String> flatKeys = new ArrayList<>();
    ArrayList<String> propertyFlatsAddrline1 = new ArrayList<>();

    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tenant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Create new Tenant");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Define inputMethodService to hide keyboard
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        propertyRef = new Firebase(getResources().getString(R.string.properties_location));
        flatRef = new Firebase(getResources().getString(R.string.flats_location));

        etForename = (EditText) findViewById(R.id.nt_forename_editview);
        etMiddlename = (EditText) findViewById(R.id.nt_middlename_editview);
        etSurname = (EditText) findViewById(R.id.nt_surname_editview);
        etDob = (EditText) findViewById(R.id.nt_dob_editview);
        etEmail = (EditText) findViewById(R.id.nt_email_editview);
        etTel = (EditText) findViewById(R.id.nt_telephone_editview);
        ImageView btCalen = (ImageView) findViewById(R.id.nt_calendar_button);


//        etForename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
////                if(hasFocus){
////                    etForename.setBackgroundColor(Color.parseColor("#ffffff"));
////                }
//                if (!hasFocus && etForename.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateTenant.this, "No forename ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//            }
//        });
//
//        etMiddlename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
////                if(hasFocus){
////                    etMiddlename.setBackgroundColor(Color.parseColor("#eeeeee"));
////                }
//                if (!hasFocus && etMiddlename.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateTenant.this, "No middle name ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//            }
//        });
//
//        etSurname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
////                if(hasFocus){
////                    etSurname.setBackgroundColor(Color.parseColor("#ffffff"));
////                }
//                if (!hasFocus && etSurname.getText().toString() == "") {
//                    Toast toast = Toast.makeText(CreateTenant.this, "No surname ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//            }
//        });
//
//        etDob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    //etDob.setBackgroundColor(Color.parseColor("#eeeeee"));
//                    showDialog(DIALOG_ID);
//                }
//                if (!hasFocus && etDob.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateTenant.this, "No date of birth ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//            }
//        });
//
//        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
////                if(hasFocus){
////                    etEmail.setBackgroundColor(Color.parseColor("#eeeeee"));
////                }
//                if (!hasFocus && etEmail.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateTenant.this, "No email address ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//            }
//        });
//
//        etTel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
////                if (hasFocus) {
////                    etTel.setBackgroundColor(Color.parseColor("#eeeeee"));
////                }
//                if (!hasFocus && etTel.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateTenant.this, "No telephone number ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//            }
//        });

        etDob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date dob = dateFormat.parse(s.toString());
                } catch (Exception e) {
                    if(etDob.hasFocus()){
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 2s = 2000ms
                                Toast.makeText(CreateTenant.this, "Mandatory format: DD/MM/YYYY", Toast.LENGTH_SHORT).show();
                            }
                        }, 3000);
                    }
                }

            }
        });

        btCalen = (ImageView) findViewById(R.id.nt_calendar_button);
        btCalen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });
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
                etDob.setText(formatt.format(chosenDate));
                isValidDob = true;
            } else {
                etDob.setText("");
                isValidDob = false;
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
        getMenuInflater().inflate(R.menu.create_task, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leaving page")
                .setMessage("You have not saved this new Tenant. Press Yes to discard or No to remain on page.")
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
//                saveNewTenant();
//                validateData();
                attemptCreation();
                break;
            case R.id.action_settings:
                startActivity(new Intent(CreateTenant.this, AllTenants.class));
                break;
        }
        return true;
    }

    private void attemptCreation() {
        newTenant = new Tenant();
        newTenant.setContractEnd("");
        newTenant.setContractStart("");
        newTenant.setCurrentTenant(false);
        newTenant.setDob("");
        newTenant.setEmail("");
        newTenant.setForename("");
        newTenant.setMiddlename("");
        newTenant.setNotes("");
        newTenant.setProperty("");
        newTenant.setSurname("");
        newTenant.setTelephone("");

        boolean cancel = false;
        View focusView = null;

        //Check for valid forename, if the user entered one
        if (TextUtils.isEmpty(etForename.getText().toString().trim())) {
            etForename.setError("This field is required");
            focusView = etForename;
            cancel = true;
        } else if (!isNameValid(etForename.getText().toString().trim())) {
            etForename.setError("This forename is too short");
            focusView = etForename;
            cancel = true;
        }

        //Check for a valid surname, if the user entered one
        if (TextUtils.isEmpty(etSurname.getText().toString().trim())) {
            etSurname.setError("This field is required");
            if (focusView == null) {
                focusView = etSurname;
            }
            cancel = true;
        } else if (!isNameValid(etSurname.getText().toString().trim())) {
            etSurname.setError("This surname is too short");
            if (focusView == null) {
                focusView = etSurname;
            }
            cancel = true;
        }

        //Check for a valid email, if the user entered one
        if (!validateEmail(etEmail.getText().toString())) {
            etEmail.setError("This email is invalid");
            if (focusView == null) {
                focusView = etEmail;
            }
            cancel = true;
        }

        //Check for a valid surname, if the user entered one
        if (TextUtils.isEmpty(etTel.getText().toString().trim())) {
            etTel.setError("This field is required");
            if (focusView == null) {
                focusView = etTel;
            }
            cancel = true;
        } else if (!isTelValid(etTel.getText().toString().trim())) {
            etTel.setError("This telephone number is invalid");
            if (focusView == null) {
                focusView = etTel;
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
            saveNewTenant();
        }

    }

    private boolean isTelValid(String trim) {
//        return android.util.Patterns.PHONE.matcher(trim).matches();
        return PhoneNumberUtils.isGlobalPhoneNumber(trim);
    }

    private boolean isNameValid(String trimText) {
        return trimText.length() > 2;
    }

    private void saveNewTenant() {
        try {
            newTenant.setContractEnd("");
            newTenant.setContractStart("");
            newTenant.setCurrentTenant(false);
            newTenant.setDob(etDob.getText().toString().trim());
            newTenant.setEmail(etEmail.getText().toString().toLowerCase().trim());
            newTenant.setForename(etForename.getText().toString().trim());
            newTenant.setMiddlename(etMiddlename.getText().toString().trim());
            newTenant.setNotes("");
            newTenant.setProperty("");
            newTenant.setSurname(etSurname.getText().toString().trim());
            newTenant.setTelephone(etTel.getText().toString().trim());

            Firebase tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
            tenantRef.push().setValue(newTenant);

            String initi = newTenant.getForename().substring(0, 1) + ". " + newTenant.getSurname()
                    + " has been stored as a new tenant in the system. Would you like to assign them a property?";
            final android.support.v7.app.AlertDialog.Builder alertBuilder =
                    new android.support.v7.app.AlertDialog.Builder(this);
            alertBuilder.setTitle("Success")
                    .setMessage(initi)
                    .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity(new Intent(CreateTenant.this, AllTenants.class));
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            android.support.v7.app.AlertDialog.Builder alertBuilder2 =
                                    new android.support.v7.app.AlertDialog.Builder(alertBuilder.getContext());
                            final AutoCompleteTextView actvProperty = new AutoCompleteTextView(alertBuilder.getContext());
                            actvProperty.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                            alertBuilder2.setMessage("*i.e. 12 Trematon Terrace - Tlat 1");
                            alertBuilder2.setTitle("Enter an address");
                            alertBuilder2.setView(actvProperty);
                            alertBuilder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(CreateTenant.this, AllTenants.class));
                                }
                            });

                            final ArrayList<Flat> flatList = new ArrayList<>();
                            final ArrayList<String> propertyAddrLine1s = new ArrayList<>();

                            final Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
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

                            alertBuilder2.show();

                            final ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                                    (alertBuilder.getContext(), android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
                            actvProperty.setAdapter(propertyAdapter);

                            actvProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Flat chosenflt = new Flat();
                                    for (Flat flt : flatList) {
                                        String tmp = flt.getAddressLine1() + " - " + flt.getFlatNum();
                                        if (tmp.matches(propertyAddrLine1s.get(position))) {
                                            chosenflt = flt;
                                            break;
                                        }
                                    }
                                    if (!chosenflt.getTenant().matches("")) {
                                        new AlertDialog.Builder(view.getContext())
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setTitle("Warning")
                                                .setMessage("The flat you have chosen already has a tenant. Please choose another.")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }

                                                })
                                                .show();
                                    } else {
                                        newTenant.setProperty(propertyAddrLine1s.get(position));

                                        Intent intent = new Intent(CreateTenant.this, TenantDetails.class);
                                        intent.putExtra("parceable_tenant", newTenant);

                                        startActivity(intent);
                                    }


                                }
                            });

                        }
                    }); //End of setPositiveButton

            final android.support.v7.app.AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();

        } catch (Exception e) {
            Toast toast = Toast.makeText(CreateTenant.this, "Something went wrong. Tenant NOT created.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public final static boolean validateEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}

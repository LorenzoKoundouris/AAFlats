package com.example.lorenzo.aaflats;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CreateTenant extends AppCompatActivity {

    private EditText etForename, etMiddlename, etSurname, etDob, etEmail, etTel;
    private boolean isValidForename, isValidMiddlename, isValidSurname, isValidDob,
            isValidEmail, isValidTel = false;
    static final int DIALOG_ID = 0;
    private int year_x, month_x, day_x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tenant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etForename = (EditText) findViewById(R.id.nt_forename_editview);
        etMiddlename = (EditText) findViewById(R.id.nt_middlename_editview);
        etSurname = (EditText) findViewById(R.id.nt_surname_editview);
        etDob = (EditText) findViewById(R.id.nt_dob_editview);
        etEmail = (EditText) findViewById(R.id.nt_email_editview);
        etTel = (EditText) findViewById(R.id.nt_telephone_editview);
        ImageView btCalen = (ImageView) findViewById(R.id.nt_calendar_button);


        etForename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    etForename.setBackgroundColor(Color.parseColor("#ffffff"));
//                }
                if (!hasFocus && etForename.getText().toString() == "") {
                    Toast toast = Toast.makeText(CreateTenant.this, "No forename ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        etMiddlename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    etMiddlename.setBackgroundColor(Color.parseColor("#eeeeee"));
//                }
                if (!hasFocus && etMiddlename.getText().toString() == "") {
                    Toast toast = Toast.makeText(CreateTenant.this, "No middle name ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        etSurname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    etSurname.setBackgroundColor(Color.parseColor("#ffffff"));
//                }
                if (!hasFocus && etSurname.getText().toString() == "") {
                    Toast toast = Toast.makeText(CreateTenant.this, "No surname ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        etDob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //etDob.setBackgroundColor(Color.parseColor("#eeeeee"));
                    showDialog(DIALOG_ID);
                }
                if (!hasFocus && etDob.getText().toString() == "") {
                    Toast toast = Toast.makeText(CreateTenant.this, "No date of birth ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    etEmail.setBackgroundColor(Color.parseColor("#eeeeee"));
//                }
                if (!hasFocus && etEmail.getText().toString() == "") {
                    Toast toast = Toast.makeText(CreateTenant.this, "No email address ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        etTel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    etTel.setBackgroundColor(Color.parseColor("#eeeeee"));
//                }
                if (!hasFocus && etTel.getText().toString() == "") {
                    Toast toast = Toast.makeText(CreateTenant.this, "No telephone number ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
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
        getMenuInflater().inflate(R.menu.createtask, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leaving page")
                .setMessage("You have not saved this new flat. Press Yes to discard or No to remain on page.")
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
                validateData();
                break;
            case R.id.action_settings:
                //startActivity(new Intent(CreateTenant.this, TenantDetails.class));
                break;
        }
        return true;
    }

    private void saveNewTenant() {
//        validateData();

            if (isValidForename && isValidSurname && isValidDob) {
                try {
                    Tenant newTenant = new Tenant();
                    newTenant.setForename(etForename.getText().toString().trim());
                    newTenant.setMiddlename(etMiddlename.getText().toString().trim());
                    newTenant.setSurname(etSurname.getText().toString().trim());
                    newTenant.setDob(etDob.getText().toString().trim());
                    newTenant.setEmail(etEmail.getText().toString().toLowerCase().trim());
                    newTenant.setTelephone(etTel.getText().toString().trim());
                    newTenant.setCurrentTenant(false);

                    Firebase tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
                    tenantRef.push().setValue(newTenant);

                    String initi = newTenant.getForename().substring(0, 1) + ". " + newTenant.getSurname()
                            + " has been stored as a new tenant in the system. Would you like to assign them a property?";
                    new AlertDialog.Builder(this)
                            .setTitle("Success")
                            .setMessage(initi)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                Intent intent = new Intent(CreateTenant.this, CreateFlat.class);
//                                intent.putExtra("created_property", newProperty);
//                                intent.putExtra("propertyList", propertyList);
//                                intent.putExtra("propertyAddrLine1s", propertyAddrLine1s);
//                                startActivity(intent);
                                }

                            })
                            .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                startActivity(new Intent(CreateTenant.this, AllTenants.class));
                                }
                            })
                            .show();

                } catch (Exception e) {
                    Toast toast = Toast.makeText(CreateTenant.this, "Something went wrong. Tenant NOT created.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

    }

    private void validateData() {
        if (etForename.getText().toString().matches("")) {
            etForename.setBackgroundColor(Color.parseColor("#EF9A9A"));
            isValidForename = false;
        } else {
            isValidForename = true;
        }

        ///////////////////////////////////////////////////////////////////////////////

        if (etMiddlename.getText().toString().matches("")) {
            isValidMiddlename = false;
        } else {
            isValidMiddlename = true;
        }

        //////////////////////////////////////////////////////////////////////////////

        if (etSurname.getText().toString().matches("")) {
            etSurname.setBackgroundColor(Color.parseColor("#EF9A9A"));
            isValidSurname = false;
        } else {
            isValidSurname = true;
        }
        //////////////////////////////////////////////////////////////////////////////

        if (etDob.getText().toString().matches("")) {
            etDob.setBackgroundColor(Color.parseColor("#EF9A9A"));
            isValidDob = false;
        } else {
            isValidDob = true;
        }

        //////////////////////////////////////////////////////////////////////////////
        if (etEmail.getText().toString().matches("")) {
            etEmail.setBackgroundColor(Color.parseColor("#EF9A9A"));
            isValidEmail = false;
        } else {
            if (validateEmail(etEmail.getText().toString())) {
                isValidEmail = true;
            } else {
                isValidEmail = false;
                etEmail.setBackgroundColor(Color.parseColor("#EF9A9A"));
            }
        }

        //////////////////////////////////////////////////////////////////////////////

        if (etTel.getText().toString().matches("")) {
            etTel.setBackgroundColor(Color.parseColor("#EF9A9A"));
            isValidTel = false;
        } else {
            isValidTel = true;
        }

        //////////////////////////////////////////////////////////////////////////////

        String nullVitalVals = "";
        String nullVals = "";
        if (!isValidForename) {
            nullVitalVals += "\n- Forename";
        }
        if (!isValidMiddlename) {
            nullVals += "\n- Middle name";
        }
        if (!isValidSurname) {
            nullVitalVals += "\n- Surname";
        }
        if (!isValidDob) {
            nullVitalVals += "\n- Date of Birth";
        }
        if (!isValidEmail) {
            nullVals += "\n- Email";
        }
        if (!isValidTel) {
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
                            saveNewTenant();
                        }
                    })
                    .setNegativeButton("No", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else{
            saveNewTenant();
        }

    }

    public final static boolean validateEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}

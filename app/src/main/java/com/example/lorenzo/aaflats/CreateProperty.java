package com.example.lorenzo.aaflats;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CreateProperty extends AppCompatActivity {

    private ArrayList<Property> propertyList = new ArrayList<>();
    private ArrayList<String> propertyAddrLine1s, propertyPostcodes = new ArrayList<>();
    private boolean validPostcode, validAddress, validNotes = false;
    private EditText npPostcode, npAddress, npNotes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_property);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Firebase propertyRef = new Firebase(getString(R.string.properties_location));

        npPostcode = (EditText) findViewById(R.id.np_postcode_editview);
        npAddress = (EditText) findViewById(R.id.np_address_editview);
        npNotes = (EditText) findViewById(R.id.np_notes_editview);

        propertyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();
                propertyAddrLine1s.clear();
                propertyPostcodes.clear();
                for (DataSnapshot prtSnapshot : dataSnapshot.getChildren()) {
                    Property prt = prtSnapshot.getValue(Property.class);
                    propertyList.add(prt);
                    propertyAddrLine1s.add(prt.getAddrline1());
                    propertyPostcodes.add(prt.getPostcode());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        npPostcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    npPostcode.setBackgroundColor(Color.WHITE);
                } else if(!hasFocus && Objects.equals(npPostcode.getText().toString(), "")){
                    Toast toast = Toast.makeText(CreateProperty.this, "No postcode ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });


        npAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean addressExists = false;
                if (!hasFocus && !Objects.equals(npAddress.getText().toString(), "")) {
                    for (int i = 0; i < propertyAddrLine1s.size(); i++) {
                        if (Objects.equals(propertyAddrLine1s.get(i), npAddress.getText().toString().toLowerCase().trim())) {
                            addressExists = true;
                            break;
                        }
                    }
                    if (addressExists) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Address exists")
                                .setMessage("This address belongs to an existing property record.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        npAddress.setText("");
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        validAddress = false;
                    } else {
                        validAddress = true;
                    }
                }
            }
        });

        npNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && Objects.equals(npNotes.getText().toString(), "")) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Null Notes")
                            .setMessage("Whoops! Looks like you forgot to compose some notes.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    npNotes.setText("No notes yet :( That's okay, you can add some later..");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    validNotes = true;
                } else{
                    validNotes = true;
                }
            }
        });
    }

    public boolean isValidPostcodeFormat(String pc) {
        validPostcode = false;
        String pcRegex = "^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$";
        validPostcode = pc.matches(pcRegex);
        if (!validPostcode) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid Postcode")
                    .setMessage("This is not a correct UK postcode format.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            npPostcode.setText("");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return validPostcode;
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
                .setMessage("You have not saved this new property. Press Yes to discard or No to remain on page.")
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
                saveNewProperty();
                break;
            case R.id.action_settings:
                onBackPressed();
                break;
        }
        return true;
    }

    private void saveNewProperty() {
        validateData();









        if(validPostcode && validAddress && validNotes){
            Property newProperty = new Property();
            newProperty.setAddrline1(npAddress.getText().toString().trim());
            newProperty.setPostcode(npPostcode.getText().toString().trim().toUpperCase());
            newProperty.setNotes(npNotes.getText().toString().trim());
            newProperty.setNoOfFlats("No flats yet");

            Firebase newPropertyRef = new Firebase(getString(R.string.properties_location));
            newPropertyRef.push().setValue(newProperty);

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Success")
                    .setMessage("Property saved! Would you like to add flats to " + npAddress.getText().toString() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener(){
                        @Override
                    public void onClick(DialogInterface dialog, int which){
                            startActivity(new Intent(CreateProperty.this, AllProperties.class));
                        }
                    })
                    .show();

        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Error")
                    .setMessage("All fields must contain valid data before saving.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void validateData() {

        boolean postcodeExists = false;
        validPostcode = false;
        if (!Objects.equals(npPostcode.getText().toString(), "")) {
            isValidPostcodeFormat(npPostcode.getText().toString());
            if (validPostcode) {
                for (int i = 0; i < propertyPostcodes.size(); i++) {
                    if (Objects.equals(propertyPostcodes.get(i), npPostcode.getText().toString().toUpperCase().trim())) {
                        postcodeExists = true;
                        break;
                    }
                }
                if (postcodeExists) {
                    new AlertDialog.Builder(this)
                            .setTitle("Postcode exists")
                            .setMessage("This postcode belongs to an existing property record.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    npPostcode.setText("");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    validPostcode = false;
                } else {
                    validPostcode = true;
                    npPostcode.setBackgroundColor(Color.WHITE);
                }
            }
        } else {
            npPostcode.setBackgroundColor(Color.parseColor("#EF9A9A"));
            validPostcode = false;
        }



        boolean addressExists = false;
        if (!Objects.equals(npAddress.getText().toString(), "")) {
            for (int i = 0; i < propertyAddrLine1s.size(); i++) {
                if (Objects.equals(propertyAddrLine1s.get(i), npAddress.getText().toString().toLowerCase().trim())) {
                    addressExists = true;
                    break;
                }
            }
            if (addressExists) {
                new AlertDialog.Builder(this)
                        .setTitle("Address exists")
                        .setMessage("This address belongs to an existing property record.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                npAddress.setText("");
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                validAddress = false;
            } else {
                validAddress = true;
            }
        }

    }
}

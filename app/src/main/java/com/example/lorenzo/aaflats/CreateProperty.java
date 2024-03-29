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
    private ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    private ArrayList<String> propertyPostcodes = new ArrayList<>();
    private boolean validPostcode, validAddress, validNotes = false;
    private EditText etNewPropertyPostcode, etNewPropertyAddressLine1, etNewPropertyNotes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_property);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Create new Property");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Firebase propertyRef = new Firebase(getString(R.string.properties_location));

        // Initialise components
        etNewPropertyPostcode = (EditText) findViewById(R.id.np_postcode_editview);
        etNewPropertyAddressLine1 = (EditText) findViewById(R.id.np_address_editview);
        etNewPropertyNotes = (EditText) findViewById(R.id.np_notes_editview);

        // Retrieve all properties
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

        // Warn user of missing postcode
        etNewPropertyPostcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    etNewPropertyPostcode.setBackgroundColor(Color.parseColor("#FAFAFA"));
                }
                if (!hasFocus && etNewPropertyPostcode.getText().toString().matches("")) {
                    Toast toast = Toast.makeText(CreateProperty.this, "No postcode ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        // Warn user of missing address line 1
        etNewPropertyAddressLine1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    etNewPropertyAddressLine1.setBackgroundColor(Color.parseColor("#FAFAFA"));
                }
                if (!hasFocus && etNewPropertyAddressLine1.getText().toString().matches("")) {
                    Toast toast = Toast.makeText(CreateProperty.this, "No address ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        // Warn user of missing notes
        etNewPropertyNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && etNewPropertyNotes.getText().toString().matches("")) {
                    Toast toast = Toast.makeText(CreateProperty.this, "No notes ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    /**
     * Verify is postcode is valid UK format
     * @param pc is postcode entered
     * @return boolean
     */
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
                            etNewPropertyPostcode.setText("");
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
                startActivity(new Intent(CreateProperty.this, Homepage.class));
                break;
        }
        return true;
    }

    /**
     * Since data is valid, create new property and push to Firebase
     */
    private void saveNewProperty() {
        validateData();

        if (validPostcode && validAddress && validNotes) {
            final Property newProperty = new Property();
            newProperty.setAddrline1(etNewPropertyAddressLine1.getText().toString().trim());
            newProperty.setPostcode(etNewPropertyPostcode.getText().toString().trim().toUpperCase());
            newProperty.setNotes(etNewPropertyNotes.getText().toString().trim());
            newProperty.setNoOfFlats("No flats yet");

            Firebase newPropertyRef = new Firebase(getString(R.string.properties_location));
            newPropertyRef.push().setValue(newProperty);

            // Alert user of success and prompt to add flats to new property
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Success")
                    .setMessage("Property saved! Would you like to add flats to " + etNewPropertyAddressLine1.getText().toString() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(CreateProperty.this, CreateFlat.class);
                            intent.putExtra("created_property", newProperty);
                            intent.putExtra("propertyList", propertyList);
                            intent.putExtra("propertyAddrLine1s", propertyAddrLine1s);
                            finish();
                            startActivity(intent);
                        }

                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity(new Intent(CreateProperty.this, PropertyDetails.class).putExtra("parceable_property", newProperty));
                        }
                    })
                    .show();

        }
    }

    /**
     * Verify all data entered is valid
     */
    private void validateData() {

        if (etNewPropertyPostcode.getText().toString().matches("")) {
            etNewPropertyPostcode.setBackgroundColor(Color.parseColor("#EF9A9A"));
            validPostcode = false;
        } else {
//            boolean postcodeExists = false;
            validPostcode = false;

            validPostcode = isValidPostcodeFormat(etNewPropertyPostcode.getText().toString().trim());
//            if (validPostcode) {
//                for (int i = 0; i < propertyPostcodes.size(); i++) {
//                    if (propertyPostcodes.get(i).matches(etNewPropertyPostcode.getText().toString().toUpperCase().trim())) {
//                        postcodeExists = true;
//                        break;
//                    }
//                }
//                if (postcodeExists) {
//                    new AlertDialog.Builder(this)
//                            .setTitle("Postcode exists")
//                            .setMessage("This postcode belongs to an existing property record.")
//                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    etNewPropertyPostcode.setError("Used postcode");
////                                    etNewPropertyPostcode.setText("");
//                                }
//                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .show();
//                    validPostcode = false;
//                } else {
//                    validPostcode = true;
//                    etNewPropertyPostcode.setBackgroundColor(Color.parseColor("#ffffff"));
//                }
//            }

////////////////////////////////////////////////////////////////////////////////////////////////

            if (etNewPropertyAddressLine1.getText().toString().matches("")) {
                etNewPropertyAddressLine1.setBackgroundColor(Color.parseColor("#EF9A9A"));
                etNewPropertyAddressLine1.setError("This field is required");
                validAddress = false;
            } else {
                boolean addressExists = false;
                for (int i = 0; i < propertyAddrLine1s.size(); i++) {
                    if (propertyAddrLine1s.get(i).matches(etNewPropertyAddressLine1.getText().toString().trim())) {
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
//                                    etNewPropertyAddressLine1.setText("");
                                    etNewPropertyAddressLine1.setError("Address exists");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    validAddress = false;
                } else {
                    validAddress = true;
                }
            }

////////////////////////////////////////////////////////////////////////////////////////////////
            if (etNewPropertyNotes.getText().toString().matches("")) {
                etNewPropertyNotes.setText("No notes yet. That's okay, you can add some later..");
                validNotes = true;
            }
//            else {
//                validNotes = true;
//            }
///////////////////////////////////////////////////////////////////////////////////////////////
            String nullVals = "";
            if (!validAddress) {
                nullVals += "\n- Address";
            }
            if (!validPostcode) {
                nullVals += "\n- Postcode";
            }
            if (!validNotes) {
                nullVals += "\n- Notes";
            }

            if (!nullVals.matches("")) {
                new AlertDialog.Builder(this)
                        .setTitle("Invalid data")
                        .setMessage("Whoops! Looks like these fields contain wrong information or none at all:" +
                                "\n\n" + nullVals)
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
}

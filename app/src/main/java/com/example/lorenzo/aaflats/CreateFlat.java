package com.example.lorenzo.aaflats;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreateFlat extends AppCompatActivity {

    private InputMethodManager inputMethodManager;
    Firebase tenantRef;
    private Tenant addedTenant;
    private Flat newFlat = new Flat();
    private ArrayAdapter<String> propertyAdapter;
    private ArrayList<Property> propertyList = new ArrayList<>();
    private ArrayList<Flat> flatList = new ArrayList<>();
    private ArrayList<Tenant> tenantList = new ArrayList<>();
    private ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    private ArrayList<String> flatNums = new ArrayList<>();
    private ArrayList<String> nullFields = new ArrayList<>();
    private ArrayList<String> tenantFullNames = new ArrayList<>();
    private ArrayList<String> unassignedTenants = new ArrayList<>();
    private AutoCompleteTextView actvProperty;
    private Property createdProperty;
    private EditText etFlatNum;
    private EditText etFlatNotes;
    private ImageView cancelTenantBtn;
    private CardView addTenantBtn;
    private boolean addTenant = false;
    private AutoCompleteTextView flatTenant;
    private boolean isValidAddress, isValidNotes, isValidFlatNum, isValidTenant = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Create new Flat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Define inputMethodService to hide keyboard
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Retrieve extras
        Bundle myIntent = getIntent().getExtras();
        etFlatNum = (EditText) findViewById(R.id.nf_number_edittext);
        etFlatNotes = (EditText) findViewById(R.id.nf_notes_editext);
        flatTenant = (AutoCompleteTextView) findViewById(R.id.actv_nf_tenant);

        // Initialise AutoCompleteTextView component and fill with property address line 1's
        actvProperty = (AutoCompleteTextView) findViewById(R.id.actv_recipient_property);
        propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        actvProperty.setAdapter(propertyAdapter);

        // If activity started from CreateProperty then Property object passed as extras
        if (myIntent != null) {
            createdProperty = myIntent.getParcelable("created_property");
            actvProperty.setText(createdProperty.getAddrline1());
            loadCorrespondingFlats(createdProperty.getAddrline1());
        }

        // On Property selection load corresponding flats i.e Flat 1, Flat 2, etc.
        actvProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadCorrespondingFlats(actvProperty.getText().toString());
            }
        });

        // Load all tenants that are not currently living in a flat and have moved out.
        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, unassignedTenants); //tenantFullNames
        flatTenant.setAdapter(tenantAdapter);

        // Get all flats from Firebase
        Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
        flatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flatList.clear();
                flatNums.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Flat flt = childSnapShot.getValue(Flat.class);
                    flatList.add(flt);
//                    flatNums.add(flt.getFlatNum());
//                    propertyAddrLine1s.add(flt.getAddressLine1());
                }
//                Set<String> removeDuplicates = new HashSet<>();
//                removeDuplicates.addAll(propertyAddrLine1s);
//                propertyAddrLine1s.clear();
//                propertyAddrLine1s.addAll(removeDuplicates);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Get all properties in case activity started Homepage
        Firebase propertyRef = new Firebase(getResources().getString(R.string.properties_location));
        propertyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();
                propertyAddrLine1s.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Property prt = childSnapShot.getValue(Property.class);
                    propertyAddrLine1s.add(prt.getAddrline1());
                    prt.setPropertyKey(childSnapShot.getKey());
                    propertyList.add(prt);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Retrieve all tenants from Firebase
        tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tenantList.clear();
                tenantFullNames.clear();
                unassignedTenants.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Tenant tnt = childSnapShot.getValue(Tenant.class);
                    tnt.setTenantKey(childSnapShot.getKey());
                    tenantList.add(tnt);
                    String fullName;
                    if (!tnt.getMiddlename().matches("")) {
                        fullName = tnt.getForename() + " " + tnt.getMiddlename() + " " + tnt.getSurname();
                    } else {
                        fullName = tnt.getForename() + " " + tnt.getSurname();
                    }
                    tenantFullNames.add(fullName.trim());
                    if (tnt.getProperty().matches("")) {
                        if (!tnt.getMiddlename().matches("")) {
                            fullName = tnt.getForename() + " " + tnt.getMiddlename() + " " + tnt.getSurname();
                        } else {
                            fullName = tnt.getForename() + " " + tnt.getSurname();
                        }
                        unassignedTenants.add(fullName);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        // Initialise toggle buttons when attempting to add/remove a tenant to new flat
        cancelTenantBtn = (ImageView) findViewById(R.id.cancel_tenant_iv);
        addTenantBtn = (CardView) findViewById(R.id.nf_card);
        addTenantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTenant = true;
                flatTenant.setVisibility(View.VISIBLE);
                cancelTenantBtn.setVisibility(View.VISIBLE);
                addTenantBtn.setVisibility(View.INVISIBLE);
                flatTenant.requestFocus();
            }
        });

        cancelTenantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTenant = false;
                flatTenant.setVisibility(View.INVISIBLE);
                cancelTenantBtn.setVisibility(View.INVISIBLE);
                addTenantBtn.setVisibility(View.VISIBLE);
            }
        });

    }//END onCreate()

    // Get corresponding flats from chosen property
    private void loadCorrespondingFlats(String prop) {
        Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
        Query flatsOfPropertyQuery = flatRef.orderByChild("addressLine1").equalTo(prop);
        flatsOfPropertyQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flatList.clear();
                flatNums.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Flat flt = childSnapShot.getValue(Flat.class);
                    flatList.add(flt);
                    flatNums.add(flt.getFlatNum());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_task, menu);
        return true;
    }

    // Handle leaving activity early
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
                onBackPressed();
                break;
            case R.id.save_new_task:
                attemptCreation();
                break;
            case R.id.action_settings:
                startActivity(new Intent(CreateFlat.this, Homepage.class));
                break;
        }
        return true;
    }

    /**
     * When menu item Edit is selected attempt to create new flat from inserted values
     */
    private void attemptCreation() {
        newFlat.setAddressLine1("");
        newFlat.setPostcode("");
        newFlat.setNotes("");
        newFlat.setFlatNum("");
        newFlat.setTenant("");

        // Reset errors
        actvProperty.setError(null);
        etFlatNum.setError(null);
        etFlatNotes.setError(null);
        flatTenant.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for valid address, if the user entered one
        if (TextUtils.isEmpty(actvProperty.getText().toString())) {
            actvProperty.setError("This field is required");
            cancel = true;
            focusView = actvProperty;
        } else if (!isAddressValid(actvProperty.getText().toString())) {
            actvProperty.setError("This address is invalid");
            cancel = true;
            focusView = actvProperty;
        }

        // Check for a valid flat number, if the user entered one
        if (TextUtils.isEmpty(etFlatNum.getText().toString())) {
            etFlatNum.setError("This field is required");
            cancel = true;
            if (focusView == null) {
                focusView = etFlatNum;
            }
        } else if (!isFlatNumValid("Flat " + etFlatNum.getText().toString())) {
            etFlatNum.setError("This flat number is in use");
            cancel = true;
            if (focusView == null) {
                focusView = etFlatNum;
            }

            Collections.sort(flatNums);

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Warning")
                    .setMessage("These flats already exist in " + actvProperty.getText().toString()
                            + ":\n\n" + flatNums.toString() + "\n\nPlease enter a unique number.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        // Check for valid tenant, if the user entered one
        if (TextUtils.isEmpty(flatTenant.getText().toString())) {
            // No need to add a tenant
        } else if (!isTenantValid(flatTenant.getText().toString())) {
            flatTenant.setError("This tenant does not exist in the system");
            cancel = true;
            if (focusView == null) {
                focusView = flatTenant;
            }
        } else if (!isTenantFree(flatTenant.getText().toString())) {
            flatTenant.setError("This tenant number is in use");
            cancel = true;
            if (focusView == null) {
                focusView = flatTenant;
            }
        }

        // Check for notes and add default notes if null
        if (TextUtils.isEmpty(etFlatNotes.getText().toString())) {
            etFlatNotes.setText("No notes yet. That's okay, you can add some later..");
        }

        if (cancel) {
            // There was an error; don't attempt creation and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            saveNewFlat();
        }
    }

    /**
     * Verify tenant is not currently active tenant
     * @param s is the tenant's name to be potentially assigned to new Flat
     * @return boolean if tenant is not currently living in other flat
     */
    private boolean isTenantFree(String s) {

        for (String t : unassignedTenants) {
            if (t.matches(s.trim())) {
                return true;
            }
        }

//        for (int i = 0; i < unassignedTenants.size(); i++) {
//            if (unassignedTenants.get(i).matches(s.trim())) {
//                return true;
//            }
//        }
        return false;
    }

    /**
     * Verify tenant exists as a Tenant object in system
     * @param s is the tenant name
     * @return boolean if tenant exists in system
     */
    private boolean isTenantValid(String s) {

        for (int i = 0; i < tenantFullNames.size(); i++) {
            if (tenantFullNames.get(i).matches(s.trim())) {
                addedTenant = tenantList.get(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Verify if flat num being created already exists
     * @param s is flat number to be created
     * @return if flat num exists or not
     */
    private boolean isFlatNumValid(String s) {
        for (String f : flatNums) {
            if (f.matches(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verify if property exists in system
     * @param s is address line 1 of a property
     * @return if it exists or not
     */
    private boolean isAddressValid(String s) {
        for (Property prt : propertyList) { //(String t : propertyAddrLine1s)
            if (prt.getAddrline1().matches(s)) {
                createdProperty = prt;
                return true;
            }
        }
        return false;
    }

    /**
     * Since all data entered is valid, create new flat and push to Firebase
     */
    private void saveNewFlat() {
//        validateData();
        try {
//            Property parseableProperty = new Property();
//            newFlat.setAddressLine1(createdProperty.getAddrline1());

            newFlat.setAddressLine1(actvProperty.getText().toString().trim());

//            for(Property prt : propertyList){
//                if(prt.getAddrline1().matches(newFlat.getAddressLine1())){
//                    newFlat.setPostcode(prt.getPostcode());
//                    parseableProperty = prt;
//                }
//            }

            newFlat.setPostcode(createdProperty.getPostcode());

            newFlat.setNotes(etFlatNotes.getText().toString());

            newFlat.setFlatNum("Flat " + etFlatNum.getText().toString());

            if (addTenant) {
//                newFlat.setTenant(flatTenant.getText().toString());
                newFlat.setTenant(addedTenant.getTenantKey());

                Firebase changeTenant = tenantRef.child(addedTenant.getTenantKey());
                Map<String, Object> tenantMap = new HashMap<>();
                tenantMap.put("property", (newFlat.getAddressLine1() + " - " + newFlat.getFlatNum()));
                tenantMap.put("currentTenant", true);
                tenantMap.put("tenantKey", null);
                changeTenant.updateChildren(tenantMap);

//                String fullName;
//                for (Tenant tnt : tenantList) {
//                    if (!tnt.getMiddlename().matches("")) {
//                        fullName = tnt.getForename() + " " + tnt.getMiddlename() + " " + tnt.getSurname();
//                    } else {
//                        fullName = tnt.getForename() + " " + tnt.getSurname();
//                    }
//
//                    if (fullName.matches(flatTenant.getText().toString().trim())) {
//                        changeTenant = tenantRef.child(tnt.getTenantKey());
//
//                        Map<String, Object> tenantMap = new HashMap<>();
//                        tenantMap.put("property", (newFlat.getAddressLine1() + " - " + newFlat.getFlatNum()));
//                        tenantMap.put("currentTenant", true);
//                        tenantMap.put("tenantKey", null);
//                        changeTenant.updateChildren(tenantMap);
//                        break;
//                    }
//                }


            }

            Firebase newFlatRef = new Firebase(getString(R.string.flats_location));
            newFlatRef.push().setValue(newFlat);

            // Inform user of success
            new AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage(newFlat.getFlatNum() + " has been added to " + newFlat.getAddressLine1())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(CreateFlat.this, PropertyDetails.class).putExtra("parceable_property", createdProperty));
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } catch (Exception e) {
            Toast.makeText(CreateFlat.this, "Something went wrong. Flat NOT created.", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Not used
     */
    private void validateData() {
        String nullVals = "";

        if (etFlatNum.getText().toString().matches("")) {
            etFlatNum.setBackgroundColor(Color.parseColor("#EF9A9A"));
            isValidFlatNum = false;
        } else {
            boolean flatExists = false;
            for (int i = 0; i < flatNums.size(); i++) {
                if (flatNums.get(i).matches("Flat " + etFlatNum.getText().toString().trim())) {
                    flatExists = true;
                    break;
                }
            }
            if (flatExists) {
                new AlertDialog.Builder(this)
                        .setTitle("Flat exists")
                        .setMessage(createdProperty.getAddrline1().toLowerCase() + " already has a " + " 'Flat " + etFlatNum.getText().toString().trim() + "'")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                etFlatNum.setText("");
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                isValidFlatNum = false;
            } else {
                isValidFlatNum = true;
            }
        }

////////////////////////////////////////// Validate flat number ^

        if (flatTenant.getText().toString().matches("")) {
            if (addTenant) {
                flatTenant.setBackgroundColor(Color.parseColor("#EF9A9A"));
                isValidTenant = false;
            } else {
                isValidTenant = true;
            }


        } else {
            boolean isTenant = false;
//            int j = 0;
            int i;
            for (i = 0; i < tenantFullNames.size(); i++) {
                if (tenantFullNames.get(i).matches(flatTenant.getText().toString().trim())) {
                    isTenant = true;
                    break;
                }
//                j++;
            }
            if (!isTenant) {
                new AlertDialog.Builder(this)
                        .setTitle("Missing tenant")
                        .setMessage(tenantFullNames.get(i - 1) + " was not found in records. Select one " +
                                "from the list or create a new one.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                flatTenant.setText("");
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                isValidTenant = false;
            } else if (tenantList.get(i - 1).isCurrentTenant()) {
                new AlertDialog.Builder(this)
                        .setTitle("Wrong tenant")
                        .setMessage(tenantFullNames.get(i - 1) + " is already registered to a flat.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                flatTenant.setText("");
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                isValidTenant = false;
            } else {
                isValidTenant = true;
            }

///////////////////////////////////////////// Validate tenant name ^

            if (!isValidAddress) {
                nullVals += "\n- Address";
            }
            if (!isValidTenant) {
                nullVals += "\n- Tenant";
            }
            if (!isValidFlatNum) {
                nullVals += "\n- Flat number";
            }
            if (etFlatNotes.getText().toString().matches("") || !isValidNotes) {
                etFlatNotes.setText("No notes yet. That's okay, you can add some later..");
                isValidNotes = true;
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

//    public void getProperties() {
//        Firebase propertyRef = new Firebase(getResources().getString(R.string.properties_location));
//        propertyRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                propertyList.clear();
//                propertyAddrLine1s.clear();
//                for (DataSnapshot prtSnapshot : dataSnapshot.getChildren()) {
//                    Property prt = prtSnapshot.getValue(Property.class);
//                    propertyList.add(prt);
//                    propertyAddrLine1s.add(prt.getAddrline1());
//                }
//
//                System.out.print("I FOUND THIS : " + propertyAddrLine1s.get(0).toString());
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
//            }
//        });
//    }
}

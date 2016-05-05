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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreateFlat extends AppCompatActivity {

    private Flat newFlat;

    private InputMethodManager inputMethodManager;
    private ArrayList<String> unassignedTenants = new ArrayList<>();
    private boolean nonExistentTenant = false;
    Firebase tenantRef;
    Tenant addedTenant;

    private ArrayAdapter<String> propertyAdapter;
    private ArrayList<Property> propertyList = new ArrayList<>();
    private ArrayList<Flat> flatList = new ArrayList<>();
    private ArrayList<Tenant> tenantList = new ArrayList<>();
    private ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    private ArrayList<String> flatNums = new ArrayList<>();
    private ArrayList<String> nullFields = new ArrayList<>();
    private ArrayList<String> tenantFullNames = new ArrayList<>();
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

        //Define inputMethodService to hide keyboard
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        Bundle myIntent = getIntent().getExtras();
        etFlatNum = (EditText) findViewById(R.id.nf_number_edittext);
        etFlatNotes = (EditText) findViewById(R.id.nf_notes_editext);
        flatTenant = (AutoCompleteTextView) findViewById(R.id.actv_nf_tenant);

//        getProperties();
        actvProperty = (AutoCompleteTextView) findViewById(R.id.actv_recipient_property);
        propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        actvProperty.setAdapter(propertyAdapter);

        if (myIntent != null) {
            createdProperty = myIntent.getParcelable("created_property");
//            propertyList = myIntent.getParcelableArrayList("propertyList");
//            propertyAddrLine1s = myIntent.getStringArrayList("propertyAddrLine1s");
            actvProperty.setText(createdProperty.getAddrline1());
            newFlat.setAddressLine1(createdProperty.getAddrline1());
            loadCorrespondingFlats(createdProperty.getAddrline1());
        }

        actvProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadCorrespondingFlats(actvProperty.getText().toString());
//                getTenants();
            }
        });

        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, unassignedTenants); //tenantFullNames
        flatTenant.setAdapter(tenantAdapter);

        Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
        flatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flatList.clear();
                flatNums.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Flat flt = childSnapShot.getValue(Flat.class);
                    flatList.add(flt);
                    flatNums.add(flt.getFlatNum());
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

        //Need this
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

//        actvProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                boolean isProperty = false;
//                if (!actvProperty.getText().toString().matches("")) {
//                    for (int i = 0; i < propertyAddrLine1s.size(); i++) {
//                        if (propertyAddrLine1s.get(i).matches(actvProperty.getText().toString().trim())) {
//                            isProperty = true;
//                            createdProperty = propertyList.get(i);
//                            break;
//                        }
//                    }
//                    if (!isProperty) {
//                        new AlertDialog.Builder(v.getContext())
//                                .setTitle("Wrong address")
//                                .setMessage("You must enter an existing property")
//                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        actvProperty.setText("");
//                                        actvProperty.requestFocus();
//                                    }
//                                })
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                        isValidAddress = false;
//                    } else {
//                        isValidAddress = true;
//                    }
//                }
//            }
//        });


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
////                System.out.print("I FOUND THIS : " + propertyAddrLine1s.get(0).toString());
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
//            }
//        });

//        actvProperty.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (actvProperty.getText().toString().matches("")) {
//                    etFlatNum.setEnabled(false);
//                    flatTenant.setEnabled(false);
//                    addTenantBtn.setEnabled(false);
//                    etFlatNotes.setEnabled(false);
//                } else {
//                    etFlatNum.setEnabled(true);
//                    flatTenant.setEnabled(true);
//                    addTenantBtn.setEnabled(true);
//                    etFlatNotes.setEnabled(true);
//                }
//            }
//        });

//        flatTenant.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                getTenants();
//            }
//        });

//        flatTenant.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                boolean isTenant = false;
//                if (!hasFocus && !Objects.equals(flatTenant.getText().toString(), "")) {
//                    int j = 0;
//                    for (int i = 0; i < tenantFullNames.size(); i++) {
//                        j++;
//                        if (Objects.equals(tenantFullNames.get(i), flatTenant.getText().toString().trim())) {
//                            isTenant = true;
//                            break;
//                        }
//                    }
//                    if (!isTenant) {
//                        new AlertDialog.Builder(v.getContext())
//                                .setTitle("Missing tenant")
//                                .setMessage("That tenant was not found in records. Select one " +
//                                        "from the list or create a new one.")
//                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        flatTenant.setText("");
//                                    }
//                                })
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                    } else {
//                        if (Objects.equals(tenantList.get(j).isCurrentTenant(), true)) {
//                            new AlertDialog.Builder(v.getContext())
//                                    .setTitle("Wrong tenant")
//                                    .setMessage("That tenant is already registered to a flat.")
//                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            flatTenant.setText("");
//                                        }
//                                    })
//                                    .setIcon(android.R.drawable.ic_dialog_alert)
//                                    .show();
//                        }
//
//                    }
//                } else {
//
//                    flatTenant.setBackgroundColor(Color.parseColor("#EF9A9A"));
//                    isValidTenant = false;
//                    nullFields.add("- Tenant");
//                }
//            }
//        });


//        etFlatNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus && etFlatNum.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateFlat.this, "No flat number ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//            }
//        });
//
//        etFlatNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus && etFlatNotes.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateFlat.this, "No notes ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                    etFlatNotes.setText("No notes yet. You can add some later.");
//                    isValidNotes = true;
//                }
//            }
//        });
//
//        flatTenant.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus && flatTenant.getText().toString().matches("")) {
//                    Toast toast = Toast.makeText(CreateFlat.this, "No tenant ?", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                }
//                if (hasFocus && !actvProperty.getText().toString().matches("")) {
//                    getTenants();
//                }
//            }
//        });

        cancelTenantBtn = (ImageView) findViewById(R.id.cancel_tenant_iv);
        addTenantBtn = (CardView) findViewById(R.id.nf_card);
        addTenantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTenant = true;
                flatTenant.setVisibility(View.VISIBLE);
                cancelTenantBtn.setVisibility(View.VISIBLE);
                addTenantBtn.setVisibility(View.INVISIBLE);
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
//                startActivity(new Intent(CreateFlat.this, Homepage.class));
//                finish();
                break;
            case R.id.save_new_task:
                //Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT).show();
//                saveNewFlat();
                attemptCreation();
                break;
            case R.id.action_settings:
                startActivity(new Intent(CreateFlat.this, Homepage.class));
                break;
        }
        return true;
    }

    private void attemptCreation() {
        newFlat = new Flat();
        newFlat.setAddressLine1("");
        newFlat.setPostcode("");
        newFlat.setNotes("");
        newFlat.setFlatNum("");
        newFlat.setTenant("");

        actvProperty.setError(null);
        etFlatNum.setError(null);
        etFlatNotes.setError(null);
        flatTenant.setError(null);

        boolean cancel = false;
        View focusView = null;

        //Check for valid address, if the user entered one
        if (TextUtils.isEmpty(actvProperty.getText().toString())) {
            actvProperty.setError("This field is required");
            cancel = true;
            focusView = actvProperty;
        } else if (!isAddressValid(actvProperty.getText().toString())) {
            actvProperty.setError("This address is invalid");
            cancel = true;
            focusView = actvProperty;
        }

        //Check for a valid flat number, if the user entered one
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

        //Check for valid tenant, if the user entered one
        if (TextUtils.isEmpty(flatTenant.getText().toString())) {
            //No need to add a tenant
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

        //notes
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

    private boolean isTenantValid(String s) {

        for (int i = 0; i < tenantFullNames.size(); i++) {
            if (tenantFullNames.get(i).matches(s.trim())) {
                addedTenant = tenantList.get(i);
                return true;
            }
        }
        return false;
    }

    private boolean isFlatNumValid(String s) {
        for (String f : flatNums) {
            if (f.matches(s)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAddressValid(String s) {
        for (Property prt : propertyList) { //(String t : propertyAddrLine1s)
            if (prt.getAddrline1().matches(s)) {
                createdProperty = prt;
                return true;
            }
        }
        return false;
    }


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

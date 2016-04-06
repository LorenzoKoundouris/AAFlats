package com.example.lorenzo.aaflats;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Objects;

public class CreateFlat extends AppCompatActivity {
    ArrayAdapter<String> propertyAdapter;
    ArrayList<Property> propertyList = new ArrayList<>();
    ArrayList<Flat> flatList = new ArrayList<>();
    ArrayList<Tenant> tenantList = new ArrayList<>();
    ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    ArrayList<String> flatNums = new ArrayList<>();
    ArrayList<String> nullFields = new ArrayList<>();
    ArrayList<String> tenantFullNames = new ArrayList<>();
    AutoCompleteTextView actvProperty;
    Property createdProperty;
    EditText etFlatNum;
    EditText etFlatNotes;
    ImageView cancelTenantBtn;
    CardView addTenantBtn;
    boolean addTenant = false;
    AutoCompleteTextView flatTenant;
    boolean isValidAddress, isValidNotes, isValidFlatNum, isValidTenant = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Create new Flat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
            propertyList = myIntent.getParcelableArrayList("propertyList");
            propertyAddrLine1s = myIntent.getStringArrayList("propertyAddrLine1s");
            actvProperty.setText(createdProperty.getAddrline1());
            loadCorrespondingFlats(createdProperty.getAddrline1());
        }

        actvProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadCorrespondingFlats(actvProperty.getText().toString());
                getTenants();
            }
        });


        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, tenantFullNames);
        flatTenant.setAdapter(tenantAdapter);

        actvProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean isProperty = false;
                if (!actvProperty.getText().toString().matches("")) {
                    for (int i = 0; i < propertyAddrLine1s.size(); i++) {
                        if (propertyAddrLine1s.get(i).matches(actvProperty.getText().toString().trim())) {
                            isProperty = true;
                            createdProperty = propertyList.get(i);
                            break;
                        }
                    }
                    if (!isProperty) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Wrong address")
                                .setMessage("You must enter an existing property")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        actvProperty.setText("");
                                        actvProperty.requestFocus();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        isValidAddress = false;
                    } else {
                        isValidAddress = true;
                    }
                }
            }
        });


        Firebase propertyRef = new Firebase(getResources().getString(R.string.properties_location));
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

                System.out.print("I FOUND THIS : " + propertyAddrLine1s.get(0).toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
            }
        });

        actvProperty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (actvProperty.getText().toString().matches("")) {
                    etFlatNum.setEnabled(false);
                    flatTenant.setEnabled(false);
                    addTenantBtn.setEnabled(false);
                    etFlatNotes.setEnabled(false);
                } else {
                    etFlatNum.setEnabled(true);
                    flatTenant.setEnabled(true);
                    addTenantBtn.setEnabled(true);
                    etFlatNotes.setEnabled(true);
                }
            }
        });

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


        etFlatNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && etFlatNum.getText().toString().matches("")) {
                    Toast toast = Toast.makeText(CreateFlat.this, "No flat number ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        etFlatNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && etFlatNotes.getText().toString().matches("")) {
                    Toast toast = Toast.makeText(CreateFlat.this, "No notes ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    etFlatNotes.setText("No notes yet. You can add some later.");
                    isValidNotes = true;
                }
            }
        });

        flatTenant.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && flatTenant.getText().toString().matches("")) {
                    Toast toast = Toast.makeText(CreateFlat.this, "No tenant ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if (hasFocus && !actvProperty.getText().toString().matches("")) {
                    getTenants();
                }
            }
        });

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
//                onBackPressed();
                startActivity(new Intent(CreateFlat.this, Homepage.class));
                finish();
                break;
            case R.id.save_new_task:
                //Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT).show();
                saveNewFlat();
                break;
            case R.id.action_settings:
                startActivity(new Intent(CreateFlat.this, Homepage.class));
                break;
        }
        return true;
    }

    private void saveNewFlat() {
        validateData();
        if(isValidAddress && isValidFlatNum && isValidNotes){
            try {
                Flat newFlat = new Flat();

                newFlat.setAddressLine1(createdProperty.getAddrline1());

                newFlat.setPostcode(createdProperty.getPostcode());

                newFlat.setNotes(etFlatNotes.getText().toString());

                newFlat.setFlatNum("Flat " + etFlatNum.getText().toString());

                newFlat.setTenant(flatTenant.getText().toString());

                Firebase newFlatRef = new Firebase(getString(R.string.flats_location));
                newFlatRef.push().setValue(newFlat);

                new AlertDialog.Builder(this)
                        .setTitle("Success")
                        .setMessage(newFlat.getFlatNum() + " has been added as a flat of " + newFlat.getAddressLine1())
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

    }

    private void getTenants() {
        Firebase tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
        tenantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tenantList.clear();
                tenantFullNames.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Tenant tnt = childSnapShot.getValue(Tenant.class);
                    tenantList.add(tnt);
                    String fullName;
                    if(!tnt.getMiddlename().matches("")){
                        fullName = tnt.getForename() + " " + tnt.getMiddlename() + " " + tnt.getSurname();
                    } else {
                        fullName = tnt.getForename() + " " + tnt.getSurname();
                    }
                    tenantFullNames.add(fullName.trim());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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
            if(addTenant){
                flatTenant.setBackgroundColor(Color.parseColor("#EF9A9A"));
                isValidTenant = false;
            } else{
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
            if(etFlatNotes.getText().toString().matches("") || !isValidNotes){
                etFlatNotes.setText("No notes yet. You can add some later.");
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

package com.example.lorenzo.aaflats;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
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
    ArrayList<String> propertyAddrLine1s = new ArrayList<>();
    ArrayList<String> nullFields = new ArrayList<>();
    ArrayList<Flat> flatList = new ArrayList<>();
    ArrayList<String> flatNums = new ArrayList<>();
    ArrayList<Tenant> tenantList = new ArrayList<>();
    ArrayList<String> tenantFullNames = new ArrayList<>();
    AutoCompleteTextView recipientProperty;
    Property createdProperty;
    EditText flatNum;
    EditText flatNotes;
    ImageView cancelTenantBtn;
    CardView addTenantBtn;
    boolean addTenant = false;
    AutoCompleteTextView flatTenant;
    boolean isValidPostcode, isValidAddress, isValidNotes, isValidFlatNum, isValidTenant = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flat);
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


        Bundle myIntent = getIntent().getExtras();
        flatNum = (EditText) findViewById(R.id.nf_number_edittext);
        flatNotes = (EditText) findViewById(R.id.nf_notes_editext);
        flatTenant = (AutoCompleteTextView) findViewById(R.id.actv_nf_tenant);

//        getProperties();
        recipientProperty = (AutoCompleteTextView) findViewById(R.id.actv_recipient_property);
        propertyAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
        recipientProperty.setAdapter(propertyAdapter);

        if (createdProperty != null) {
            createdProperty = myIntent.getParcelable("created_property");
            propertyList = myIntent.getParcelableArrayList("propertyList");
            propertyAddrLine1s = myIntent.getStringArrayList("propertyAddrLine1s");
            recipientProperty.setText(createdProperty.getAddrline1());
            loadCorrespondingFlats(createdProperty.getAddrline1());
        }

        recipientProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadCorrespondingFlats(recipientProperty.getText().toString());
                getTenants();
            }
        });


        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, tenantFullNames);
        flatTenant.setAdapter(tenantAdapter);

        recipientProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean isProperty = false;
                if (!Objects.equals(recipientProperty.getText().toString(), "")) {
                    for (int i = 0; i < propertyAddrLine1s.size(); i++) {
                        if (Objects.equals(propertyAddrLine1s.get(i), recipientProperty.getText().toString().toLowerCase())) {
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
                                        recipientProperty.setText("");
                                        recipientProperty.requestFocus();
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






        recipientProperty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (Objects.equals(recipientProperty.getText().toString(), "")) {
                    flatNum.setEnabled(false);
                    flatTenant.setEnabled(false);
                    addTenantBtn.setEnabled(false);
                    flatNotes.setEnabled(false);
                } else {
                    flatNum.setEnabled(true);
                    flatTenant.setEnabled(true);
                    addTenantBtn.setEnabled(true);
                    flatNotes.setEnabled(true);
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


        flatNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && Objects.equals(flatNum.getText().toString(), "")) {
                    Toast toast = Toast.makeText(CreateFlat.this, "No flat number ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        flatNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && Objects.equals(flatNotes.getText().toString(), "")) {
                    Toast toast = Toast.makeText(CreateFlat.this, "No notes ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    flatNotes.setText("No notes yet. You can add some later.");
                    isValidNotes = true;
                }
            }
        });

        flatTenant.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && Objects.equals(flatTenant.getText().toString(), "")) {
                    Toast toast = Toast.makeText(CreateFlat.this, "No tenant ?", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if (hasFocus && !Objects.equals(recipientProperty.getText().toString(), "")) {
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
        Query flatQuery = flatRef.orderByChild("addressLine1").equalTo(prop);
        flatQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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
                saveNewFlat();
                break;
            case R.id.action_settings:
                startActivity(new Intent(CreateFlat.this, PropertyDetails.class));
                break;
        }
        return true;
    }

    private void saveNewFlat() {
        validateData();
        if(isValidAddress && isValidFlatNum && isValidNotes && isValidTenant){
            try {
                Flat newFlat = new Flat();

                newFlat.setAddressLine1(createdProperty.getAddrline1());

                newFlat.setPostcode(createdProperty.getPostcode());

                newFlat.setNotes(flatNotes.getText().toString());

                newFlat.setFlatNum("Flat " + flatNum.getText().toString());

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
                    if(tnt.getMiddlename() != null || !Objects.equals(tnt.getMiddlename(), "")){
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

        if (flatNum.getText().toString() == "") {
            flatNum.setBackgroundColor(Color.parseColor("#EF9A9A"));
            isValidFlatNum = false;
        } else {
            boolean flatExists = false;
            for (int i = 0; i < flatNums.size(); i++) {
                if (flatNums.get(i) == "Flat " + flatNum.getText().toString().trim()) {
                    flatExists = true;
                    break;
                }
            }
            if (flatExists) {
                new AlertDialog.Builder(this)
                        .setTitle("Flat exists")
                        .setMessage(createdProperty.getAddrline1().toLowerCase() + " already has a " + " 'Flat " + flatNum.getText().toString().trim() + "'")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                flatNum.setText("");
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

        if (flatTenant.getText().toString() == "") {
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
                if (tenantFullNames.get(i) == flatTenant.getText().toString().trim()) {
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
            if(Objects.equals(flatNotes.getText().toString(), "") || !isValidNotes){
                flatNotes.setText("No notes yet. You can add some later.");
                isValidNotes = true;
            }

            if (!Objects.equals(nullVals, "")) {
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

package com.example.lorenzo.aaflats;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class PropertyDetails extends AppCompatActivity {

    boolean attemptEdit = false;
    boolean editsCancelled;
    boolean validPostcode = true;
    boolean validAddress = true;
    boolean validNotes = true;
    MenuItem saveEdit;
    Property parceableProperty;
    String parceablePropertyKey;

    EditText propertyPostcode;
    EditText propertyAddrline1;
    TextView propertyFlats;
    EditText propertyNotes;

    SharedPreferences pref;
    SharedPreferences.Editor prefEditor;

    CardView flatListCardView;

    Firebase propertyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(PropertyDetails.this, MapProperty.class);
                intent.putExtra("findProperty", parceableProperty.getAddrline1());
                startActivity(intent);
            }
        });

        propertyRef = new Firebase(getResources().getString(R.string.properties_location));
        pref = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefEditor = pref.edit();

        Bundle intent = getIntent().getExtras();
        parceableProperty = intent.getParcelable("parceable_property");
        parceablePropertyKey = intent.getString("parceable_property_key");

        final ArrayList<Flat> flatList = new ArrayList<>();
        final ArrayList<String> flatKeys = new ArrayList<>();
        final ArrayList<String> flatNums = new ArrayList<>();


        propertyPostcode = (EditText) findViewById(R.id.property_details_postcode);
        propertyAddrline1 = (EditText) findViewById(R.id.property_details_addrline1);
        propertyFlats = (TextView) findViewById(R.id.property_details_flats);
        propertyNotes = (EditText) findViewById(R.id.property_details_notes);
        flatListCardView = (CardView) findViewById(R.id.flatrecycler_card_view);

        propertyPostcode.setText(parceableProperty.getPostcode().toUpperCase());
        prefEditor.putString("pPostcode", parceableProperty.getPostcode().toUpperCase());
        propertyAddrline1.setText(parceableProperty.getAddrline1());
        prefEditor.putString("pAddress", parceableProperty.getAddrline1());
        propertyFlats.setText("(" + parceableProperty.getNoOfFlats() + ")");
        propertyNotes.setText(parceableProperty.getNotes());
        prefEditor.putString("pNotes", parceableProperty.getNotes());
        prefEditor.commit();
        setTitle(parceableProperty.getAddrline1());

        final RecyclerView flatRecyclerView = (RecyclerView) findViewById(R.id.flat_recycler_view);
        flatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Firebase flatsRef = new Firebase(getResources().getString(R.string.flats_location));
        Query flatQuery = flatsRef.orderByChild("addressLine1").equalTo(parceableProperty.getAddrline1());

        flatQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flatList.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Flat flt = childSnapShot.getValue(Flat.class);
                    flatList.add(flt);
                    flatKeys.add(childSnapShot.getKey());
                    flatNums.add(flt.getFlatNum().trim().substring(0, 1).toUpperCase() +
                            flt.getFlatNum().substring(1).trim());
//                    String[] split = childSnapShot.getKey().split(" - ");
//                    flatNums.add(split[1].trim().substring(0, 1).toUpperCase() +
//                            split[1].substring(1).trim());
                }
                flatRecyclerView.setAdapter(new FlatAdapter(flatList, flatKeys, flatNums, parceableProperty, parceablePropertyKey));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void saveAllChanges() {
        if (!editsCancelled) {
            try {
                setTitle(propertyAddrline1.getText().toString());
                parceableProperty.setPostcode(propertyPostcode.getText().toString());
                parceableProperty.setAddrline1(propertyAddrline1.getText().toString());
                parceableProperty.setNotes(propertyNotes.getText().toString());

                propertyRef.child(parceablePropertyKey).setValue(parceableProperty);
                Toast toast = Toast.makeText(PropertyDetails.this, "Property edited! SUCCESS!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } catch (Exception ex) {
                Toast toast = Toast.makeText(PropertyDetails.this, "Property not edited. FAIL", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else {
            setTitle(pref.getString("pKey", "crashAddrline1"));
            propertyPostcode.setText(pref.getString("pPostcode", "crashPostcode"));
            propertyAddrline1.setText(pref.getString("pKey", "crashAddrline1"));
            propertyNotes.setText(pref.getString("pNotes", "crashNotes"));
        }
        propertyPostcode.setEnabled(false);
        propertyAddrline1.setEnabled(false);
        propertyNotes.setEnabled(false);
        flatListCardView.setVisibility(View.VISIBLE);

        attemptEdit = false;
        invalidateOptionsMenu();
    }

    private void editPropertyDetails() {
        editsCancelled = false;

        try {
            propertyPostcode.setEnabled(true);
            propertyAddrline1.setEnabled(true);
            propertyNotes.setEnabled(true);
            flatListCardView.setVisibility(View.INVISIBLE);

            propertyPostcode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (Objects.equals(propertyPostcode.getText().toString(), pref.getString("pPostcode", "crashPostcode"))) {
                        propertyPostcode.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        propertyPostcode.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            propertyAddrline1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (Objects.equals(propertyAddrline1.getText().toString(), pref.getString("pKey", "crashAddrline1"))) {
                        propertyAddrline1.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        propertyAddrline1.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            propertyNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (Objects.equals(propertyNotes.getText().toString(), pref.getString("pNotes", "crashNotes"))) {
                        propertyNotes.setTextColor(getResources().getColor(R.color.black_color));
                    } else {
                        propertyNotes.setTextColor(Color.parseColor("#FF5722"));
                    }
                }
            });

            propertyPostcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (Objects.equals(propertyPostcode.getText().toString(), "")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null postcode")
                                .setMessage("Whoops! Looks like you forgot to set a Postcode!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        propertyPostcode.setText(pref.getString("pPostcode", "crashPostcode"));
                                        propertyPostcode.setSelectAllOnFocus(true);
                                        propertyPostcode.setSelection(propertyPostcode.length());
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        validPostcode = false;
                    } else {
                        isValidPostcodeFormat(propertyPostcode.getText().toString());
                    }
                }
            });

            propertyAddrline1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (Objects.equals(propertyAddrline1.getText().toString(), "")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null Address")
                                .setMessage("Whoops! Looks like you forgot to set a Address!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        propertyAddrline1.setText(pref.getString("pKey", "crashAddrline1"));
                                        propertyAddrline1.setSelectAllOnFocus(true);
                                        propertyAddrline1.setSelection(propertyAddrline1.length());
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        validAddress = false;
                    } else {
                        validAddress = true;
                    }
                }
            });

            propertyNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (Objects.equals(propertyNotes.getText().toString(), "")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Null Notes")
                                .setMessage("Whoops! Looks like you forgot to set a Notes!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        propertyNotes.setText(pref.getString("pNotes", "crashNotes"));
                                        propertyNotes.setSelectAllOnFocus(true);
                                        propertyNotes.setSelection(propertyNotes.length());
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        validNotes = false;
                    } else {
                        validNotes = true;
                    }
                }
            });
            if (validPostcode && validAddress && validNotes) {
                attemptEdit = true;
                invalidateOptionsMenu();
            }
        } catch (Exception ex) {
            Toast toast = Toast.makeText(PropertyDetails.this, "Components not enabled.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
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
                            propertyPostcode.setText(pref.getString("pPostcode", "crashPostcode"));
                            propertyPostcode.setSelectAllOnFocus(true);
                            propertyPostcode.setSelection(propertyPostcode.length());
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return validPostcode;
    }

    @Override
    public void onBackPressed() {
        if (attemptEdit && !editsCancelled || attemptEdit) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Leaving page")
                    .setMessage("You have not saved changes made to this property. Press Yes to discard or No to remain on page.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        } else{
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!attemptEdit) {
            getMenuInflater().inflate(R.menu.property_details, menu);
            saveEdit = menu.findItem(R.id.edit_task);
        } else {
            getMenuInflater().inflate(R.menu.task_details_save, menu);
            saveEdit = menu.findItem(R.id.save_edited_task);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                onBackPressed();
                break;

            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;

            case R.id.edit_task:
                Toast toast1 = Toast.makeText(getApplicationContext(), "Edit button clicked", Toast.LENGTH_SHORT);
                toast1.setGravity(Gravity.CENTER, 0, 0);
                toast1.show();
                editPropertyDetails();
                break;
            case R.id.save_edited_task:
                Toast toast = Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                saveAllChanges();
                break;
            case R.id.cancel_edit_task:
                editsCancelled = true;
                saveAllChanges();
                break;
        }
        return true;
    }
}

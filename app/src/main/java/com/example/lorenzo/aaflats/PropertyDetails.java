package com.example.lorenzo.aaflats;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class PropertyDetails extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Property parceableProperty;
    String parceablePropertyKey;

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
                startActivity(new Intent(PropertyDetails.this, MapProperty.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle intent = getIntent().getExtras();
        parceableProperty = intent.getParcelable("parceable_property");
        parceablePropertyKey = intent.getString("parceable_property_key");

        final ArrayList<Flat> flatList = new ArrayList<>();
        final ArrayList<String> flatKeys = new ArrayList<>();
        final ArrayList<String> flatNums = new ArrayList<>();

        setTitle(parceablePropertyKey);
        EditText propertyPostcode = (EditText) findViewById(R.id.property_details_postcode);
        EditText propertyAddrline1 = (EditText) findViewById(R.id.property_details_addrline1);
        EditText propertyFlats = (EditText) findViewById(R.id.property_details_flats);
        EditText propertyNotes = (EditText) findViewById(R.id.property_details_notes);

        propertyPostcode.setText(parceableProperty.getPostcode().toUpperCase());
        propertyAddrline1.setText(parceablePropertyKey);
        propertyFlats.setText("(" + parceableProperty.getNoOfFlats() + ")");
        propertyNotes.setText(parceableProperty.getNotes());

        final RecyclerView flatRecyclerView = (RecyclerView) findViewById(R.id.flat_recycler_view);
        flatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Firebase flatsRef = new Firebase(getResources().getString(R.string.flats_location));
        Query flatQuery = flatsRef.orderByChild("addressLine1").equalTo(parceablePropertyKey);

        flatQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flatList.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Flat flt = childSnapShot.getValue(Flat.class);
                    flatList.add(flt);
                    flatKeys.add(childSnapShot.getKey());
                    String[] split = childSnapShot.getKey().split(" - ");
                    flatNums.add(split[1].trim().substring(0, 1).toUpperCase() +
                            split[1].substring(1).trim());
                }
                flatRecyclerView.setAdapter(new FlatAdapter(flatList, flatKeys, flatNums, parceableProperty, parceablePropertyKey));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        if (!attemptEdit) {
            getMenuInflater().inflate(R.menu.property_details, menu);
//            saveEdit = menu.findItem(R.id.edit_task);
//        } else {
//            getMenuInflater().inflate(R.menu.task_details_save, menu);
//            saveEdit = menu.findItem(R.id.save_edited_task);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

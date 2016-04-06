package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class TenantHomepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<Tenant> tenantList = new ArrayList<>();
    AutoCompleteTextView actvLogout;
    View myHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle intent = getIntent().getExtras();
        Flat parceableFlat = intent.getParcelable("parceable_flat");

        Firebase tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
        final String tenantPropertyFlat = parceableFlat.getAddressLine1() + " - " + parceableFlat.getFlatNum();
        Query getFirebaseTenant = tenantRef.orderByChild("property").equalTo(tenantPropertyFlat);
        getFirebaseTenant.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                    Tenant tnt = childSnap.getValue(Tenant.class);
                    tenantList.add(tnt);
                }
                displayTenantDetails();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        myHeader = navigationView.getHeaderView(0);
        actvLogout = (AutoCompleteTextView) myHeader.findViewById(R.id.login_tenant_name);



        TextView composeReport = (TextView) findViewById(R.id.compose_report_textview);
        composeReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "0"));
            }
        });

        TextView composeEnquiry = (TextView) findViewById(R.id.compose_enquiry_textview);
        composeEnquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "1"));
            }
        });

    }

    private void displayTenantDetails() {

        ArrayList<String> logoutName = new ArrayList<>();

        try{
            logoutName.add(tenantList.get(0).getForename());
            logoutName.add("Logout");
        }catch(Exception ex){
            Toast.makeText(TenantHomepage.this, "Could not load tenant details", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> logoutAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, logoutName);
        actvLogout.setAdapter(logoutAdapter);
        actvLogout.setSelection(0);

//        TextView tenantName = (TextView) myHeader.findViewById(R.id.login_tenant_name);
        TextView tenantAddress = (TextView) myHeader.findViewById(R.id.login_tenant_address);
        TextView greetingText = (TextView) myHeader.findViewById(R.id.greeting_text);
        try{
            tenantAddress.setText(tenantList.get(0).getProperty());
            greetingText.setText("Welcome back, " + tenantList.get(0).getForename());
        } catch(Exception ex){
            Toast.makeText(TenantHomepage.this, "Could not load tenant details", Toast.LENGTH_SHORT).show();
        }
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
        getMenuInflater().inflate(R.menu.tenant_homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.my_account) {
            //ToDO: Tenant myAccount as well as navDrawer
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
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_my_account) {

        } else if (id == R.id.nav_compose_report) {
            startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "0"));
        } else if (id == R.id.nav_compose_enquiry) {
            startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "1"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

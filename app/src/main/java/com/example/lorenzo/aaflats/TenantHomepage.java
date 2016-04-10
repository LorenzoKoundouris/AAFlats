package com.example.lorenzo.aaflats;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import android.widget.Spinner;
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

    ArrayList<String> logoutName = new ArrayList<>();
    ArrayAdapter<String> logoutAdapter;
    ArrayList<Tenant> tenantList = new ArrayList<>();
    Spinner actvLogout;
    View myHeader;
    TextView logoutText;
    TextView tenantAddress;

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

        navigationView.getMenu().getItem(0).setChecked(true);

        Bundle intent = getIntent().getExtras();
        Flat parceableFlat;
        Firebase.setAndroidContext(this);
        Firebase tenantRef = new Firebase(getResources().getString(R.string.tenants_location));
        String tenantPropertyFlat = "";
        try {
            parceableFlat = intent.getParcelable("parceable_flat");
            tenantPropertyFlat = parceableFlat.getAddressLine1() + " - " + parceableFlat.getFlatNum();
        } catch (Exception ex) {

        }

        Query getFirebaseTenant = tenantRef.orderByChild("property").equalTo(tenantPropertyFlat);
        getFirebaseTenant.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
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
        actvLogout = (Spinner) myHeader.findViewById(R.id.login_tenant_name);
        logoutText = (TextView) myHeader.findViewById(R.id.logout_text);
        tenantAddress = (TextView) myHeader.findViewById(R.id.login_tenant_address);

        TextView composeReport = (TextView) findViewById(R.id.compose_report_textview);
        composeReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "0").putExtra("parceable_tenant", tenantList.get(0)));
            }
        });

        TextView composeEnquiry = (TextView) findViewById(R.id.compose_enquiry_textview);
        composeEnquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "1").putExtra("parceable_tenant", tenantList.get(0)));
            }
        });

        actvLogout.setOnTouchListener(Spinner_OnTouch);

        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Sign out")
                        .setMessage("Are you sure you wish to sign out?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(TenantHomepage.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });

    }

    private View.OnTouchListener Spinner_OnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                if(logoutText.getVisibility() == View.GONE){
                    logoutText.setVisibility(View.VISIBLE);
                    tenantAddress.setVisibility(View.INVISIBLE);
                } else {
                    logoutText.setVisibility(View.GONE);
                    tenantAddress.setVisibility(View.VISIBLE);
                }


            }
            return true;
        }
    };
//
//    private static View.OnKeyListener Spinner_OnKey = new View.OnKeyListener() {
//        public boolean onKey(View v, int keyCode, KeyEvent event) {
//            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//                Toast.makeText(v.getContext(), "boobies key", Toast.LENGTH_SHORT).show();
//                return true;
//            } else {
//                return false;
//            }
//        }
//    };


    private void displayTenantDetails() {


        try {
            logoutName.add(tenantList.get(0).getForename() + " " + tenantList.get(0).getSurname());
//            logoutName.add("Lorenzo");
        } catch (Exception ex) {
            Toast.makeText(TenantHomepage.this, "Could not load tenant details", Toast.LENGTH_SHORT).show();
        }

        logoutAdapter = new ArrayAdapter<>
                (this, R.layout.custom_spinner_transparent, logoutName);

        logoutAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item2);
        actvLogout.setAdapter(logoutAdapter);

        TextView greetingText = (TextView) findViewById(R.id.greeting_text);
        try {

            tenantAddress.setText(tenantList.get(0).getProperty());
//            tenantAddress.setText("12 Trematon Terrace - Flat 1");
            greetingText.setText("Welcome back, " + tenantList.get(0).getForename());
        } catch (Exception ex) {
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
            startActivity(new Intent(TenantHomepage.this, TenantDetails.class).putExtra("parceable_tenant", tenantList.get(0)).putExtra("staff_access", false));
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
        } else if (id == R.id.nav_my_account) {
            startActivity(new Intent(TenantHomepage.this, TenantDetails.class).putExtra("parceable_tenant", tenantList.get(0)).putExtra("staff_access", false));
        } else if (id == R.id.nav_compose_report) {
            startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "0").putExtra("parceable_tenant", tenantList.get(0)));
        } else if (id == R.id.nav_compose_enquiry) {
            startActivity(new Intent(TenantHomepage.this, ComposeNew.class).putExtra("composeType", "1").putExtra("parceable_tenant", tenantList.get(0)));
        } else if(id == R.id.nav_contact_details){
            new AlertDialog.Builder(this)
                    .setTitle("A&A Flats")
                    .setMessage("Mon - Fri 08:00 - 17:00\nTelephone: 07450236875\nMobile: 07450236875\nEmail: aaflats.agency@support.com")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

package com.example.lorenzo.aaflats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AllTenants extends AppCompatActivity {

    private RecyclerView tenantRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tenants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllTenants.this, CreateTenant.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        final ArrayList<Tenant> tenantList = new ArrayList<>();

        tenantRecyclerView = (RecyclerView) findViewById(R.id.tenants_recycler_view);
        tenantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Firebase tenantsRef = new Firebase(getResources().getString(R.string.tenants_location));

        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tenantList.clear();

                for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                    Tenant tnt = childSnap.getValue(Tenant.class);
                    tenantList.add(tnt);
                }
                Collections.sort(tenantList, new Comparator<Tenant>() {
                    @Override
                    public int compare(Tenant lhs, Tenant rhs) {
                        return lhs.getSurname().compareTo(rhs.getSurname());
                    }
                });

                sendContext(tenantList);

                tenantRecyclerView.setVisibility(View.VISIBLE);
                ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar3);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void sendContext(ArrayList<Tenant> tenantList) {
        tenantRecyclerView.setAdapter(new TenantAdapter(tenantList, this));
    }
}

package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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

    private boolean notFirstLoad = false;
    private RecyclerView tenantRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tenants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("All tenants");

        setupRecyclerview();

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

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_alltenants);
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_1);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 2s = 2000ms
                        setRecyclerAdapterContents(tenantList);
                    }
                }, 2000);

            }
        });


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

                setRecyclerAdapterContents(tenantList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setupRecyclerview() {
        tenantRecyclerView = (RecyclerView) findViewById(R.id.tenants_recycler_view);
        tenantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setRecyclerAdapterContents(ArrayList<Tenant> tenantList) {
        tenantRecyclerView.setAdapter(new TenantAdapter(tenantList, this));
        if (!notFirstLoad) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 2s = 2000ms
                    tenantRecyclerView.setVisibility(View.VISIBLE);
                    ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_alltenants);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }
        notFirstLoad = true;
        refreshLayout.setRefreshing(false);
    }
}

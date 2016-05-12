package com.example.lorenzo.aaflats;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private ArrayList<Tenant> searchQuery = new ArrayList<>();
    private ArrayList<Tenant> tenantList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tenants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);

        setTitle("All tenants");

        // Define recycler-view component
        setupRecyclerview();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllTenants.this, CreateTenant.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialise recycler-view component
        tenantRecyclerView = (RecyclerView) findViewById(R.id.tenants_recycler_view);
        tenantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialise manual refresh component
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
                        // Delay filling recycler-view to demo manual refresh for 2s = 2000ms
                        setRecyclerAdapterContents(tenantList);
                    }
                }, 2000);

            }
        });

        // Get all tenants and sort them alphabetically by surname
        Firebase tenantsRef = new Firebase(getResources().getString(R.string.tenants_location));
        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tenantList.clear();
                for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                    Tenant tnt = childSnap.getValue(Tenant.class);
                    tnt.setTenantKey(childSnap.getKey());
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
    } // End of onCreate

    /**
     * Initialise recycler-view
     */
    private void setupRecyclerview() {
        tenantRecyclerView = (RecyclerView) findViewById(R.id.tenants_recycler_view);
        tenantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Load recycler-view with the extracted list of Properties
     *
     * @param tenantList list of Tenant objects extracted from Firebase
     */
    private void setRecyclerAdapterContents(ArrayList<Tenant> tenantList) {
        tenantRecyclerView.setAdapter(new TenantAdapter(tenantList, this));
        if (!notFirstLoad) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // During first app launch, demo refresh animation
                    tenantRecyclerView.setVisibility(View.VISIBLE);
                    ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_alltenants);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }
        notFirstLoad = true;
        refreshLayout.setRefreshing(false);
    }

    /**
     * Search through Tenants by full name ((name surname) or (surname name))
     * or Postcode or email address or telephone number
     *
     * @param menu is the toolbar menu containing the search icon
     * @return inflated menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView(); //(SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Query tenants
                searchQuery.clear();
                boolean exactMatch = false;
                for (int i = 0; i < tenantList.size(); i++) {
                    String fullName = tenantList.get(i).getForename() + " " + tenantList.get(i).getSurname();
                    String revName = tenantList.get(i).getSurname() + " " + tenantList.get(i).getForename();
                    if (query.toLowerCase().trim().matches(fullName.toLowerCase()) || query.toLowerCase().trim().matches(revName.toLowerCase()) ||
                            query.toLowerCase().trim().matches(tenantList.get(i).getEmail().toLowerCase())) {
                        exactMatch = true;
                        startActivity(new Intent(AllTenants.this, TenantDetails.class)
                                .putExtra("parceable_tenant", tenantList.get(i)).putExtra("staff_access", true));
                        break;
                    } else if (fullName.contains(query.trim()) || tenantList.get(i).getProperty().toLowerCase().contains(query.toLowerCase().trim()) ||
                            tenantList.get(i).getEmail().toLowerCase().contains(query.toLowerCase().trim())
                            || tenantList.get(i).getTelephone().toLowerCase().contains(query.toLowerCase().trim())) {
                        searchQuery.add(tenantList.get(i));
                    }
                }
                if (!exactMatch) {
                    setRecyclerAdapterContents(searchQuery);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Text has changed, apply filtering
                loadResults(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Re-populate the recycler-view with the search results only
     *
     * @param newText is the search query entered by the user
     */
    private void loadResults(String newText) {
        searchQuery.clear();
        for (int i = 0; i < tenantList.size(); i++) {
            String fullName = tenantList.get(i).getForename() + " " + tenantList.get(i).getSurname();
            if (fullName.toLowerCase().contains(newText.toLowerCase().trim()) ||
                    tenantList.get(i).getProperty().toLowerCase().contains(newText.toLowerCase().trim()) ||
                    tenantList.get(i).getEmail().toLowerCase().contains(newText.toLowerCase().trim()) ||
                    tenantList.get(i).getTelephone().toLowerCase().contains(newText.toLowerCase().trim())) {
                searchQuery.add(tenantList.get(i));
            }
        }
        setRecyclerAdapterContents(searchQuery);
    }


}

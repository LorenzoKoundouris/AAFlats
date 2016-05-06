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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AllProperties extends AppCompatActivity {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView propertyRecyclerView;
    private boolean notFirstLoad = false;
    private ArrayList<Property> searchQuery = new ArrayList<>();
    private ArrayList<Property> propertyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_properties);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllProperties.this, CreateProperty.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupRecyclerview();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_allproperties);
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
                        setRecyclerAdapterContents(propertyList);
                    }
                }, 2000);

            }
        });

//        propertyRecyclerView = (RecyclerView) findViewById(R.id.properties_recycler_view);
//        propertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Firebase propertiesRef = new Firebase(getResources().getString(R.string.properties_location));

        propertiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Property prt = childSnap.getValue(Property.class);
                    prt.setPropertyKey(childSnap.getKey());
                    propertyList.add(prt);
                }
                for (int i = 0; i < propertyList.size(); i++) {
                    String[] splitter = propertyList.get(i).getAddrline1().split(" ");
                    if (splitter[0].length() == 1) {
                        splitter[0] = "0" + splitter[0];
                    }
                    propertyList.get(i).setAddrline1(splitter[1] + " " + splitter[2] + " " + splitter[0]);
                }
                Collections.sort(propertyList, new Comparator<Property>() {
                    @Override
                    public int compare(Property lhs, Property rhs) {
                        return lhs.getAddrline1().compareTo(rhs.getAddrline1());
                    }
                });
                for (int i = 0; i < propertyList.size(); i++) {
                    String[] splitter = propertyList.get(i).getAddrline1().split(" ");
                    if (splitter[2].substring(0, 1).matches("0")) {
                        splitter[2] = splitter[2].substring(1);
                    }
                    propertyList.get(i).setAddrline1(splitter[2] + " " + splitter[0] + " " + splitter[1]);
                }
//                searchQuery = propertyList;
                setRecyclerAdapterContents(propertyList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }//End of onCreate

    private void setupRecyclerview() {
        propertyRecyclerView = (RecyclerView) findViewById(R.id.properties_recycler_view);
        propertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setRecyclerAdapterContents(ArrayList<Property> propertyList) {
        propertyRecyclerView.setAdapter(new PropertyAdapter(propertyList));
        if (!notFirstLoad) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 2s = 2000ms
                    propertyRecyclerView.setVisibility(View.VISIBLE);
                    ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_allproperties);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }
        notFirstLoad = true;
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Perform final search
                searchQuery.clear();
                boolean exactMatch = false;
                for (int i = 0; i < propertyList.size(); i++) {
                    if (query.toLowerCase().trim().matches(propertyList.get(i).getAddrline1().toLowerCase()) ||
                            query.toLowerCase().trim().matches(propertyList.get(i).getPostcode().toLowerCase())) {
                        exactMatch = true;
                        startActivity(new Intent(AllProperties.this, PropertyDetails.class)
                                .putExtra("parceable_property", propertyList.get(i)));
                        break;
                    } else if (propertyList.get(i).getAddrline1().toLowerCase().contains(query.toLowerCase().trim()) ||
                            propertyList.get(i).getPostcode().toLowerCase().contains(query.toLowerCase().trim())) {
                        searchQuery.add(propertyList.get(i));
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


//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.search_view, menu);
//
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//
//        SearchManager searchManager = (SearchManager) AllProperties.this.getSystemService(Context.SEARCH_SERVICE);
//
//        SearchView searchView = null;
//        if (searchItem != null) {
//            searchView = (SearchView) searchItem.getActionView();
//        }
//        if (searchView != null) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(AllProperties.this.getComponentName()));
//        }
        return super.onCreateOptionsMenu(menu);
    }

    private void loadResults(String newText) {
        searchQuery.clear();
        for (int i = 0; i < propertyList.size(); i++) {
            if (propertyList.get(i).getAddrline1().toLowerCase().contains(newText.toLowerCase().trim()) ||
                    propertyList.get(i).getPostcode().toLowerCase().contains(newText.toLowerCase().trim())) {
                searchQuery.add(propertyList.get(i));
            }
        }
        setRecyclerAdapterContents(searchQuery);
    }

}

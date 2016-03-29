package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_properties);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupRecyclerview();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllProperties.this, CreateProperty.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final ArrayList<Property> propertyList = new ArrayList<>();
        final ArrayList<String> propertyKeys = new ArrayList<>();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_allproperties);
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_1);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRecyclerAdapterContents(propertyList);
            }
        });

        propertyRecyclerView = (RecyclerView) findViewById(R.id.properties_recycler_view);
        propertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Firebase propertiesRef = new Firebase(getResources().getString(R.string.properties_location));

        propertiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Property prt = childSnap.getValue(Property.class);
                    propertyList.add(prt);
                }
                for (int i = 0; i < propertyList.size(); i++) {
                    String[] splitter = propertyList.get(i).getAddrline1().split(" ");
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
                    propertyList.get(i).setAddrline1(splitter[2] + " " + splitter[0] + " " + splitter[1]);
                }
                setRecyclerAdapterContents(propertyList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setupRecyclerview() {
        propertyRecyclerView = (RecyclerView) findViewById(R.id.properties_recycler_view);
        propertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setRecyclerAdapterContents(ArrayList<Property> propertyList) {
        propertyRecyclerView.setAdapter(new PropertyAdapter(propertyList));
        if (!notFirstLoad) {
            propertyRecyclerView.setVisibility(View.VISIBLE);
            ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_allproperties);
            mProgressBar.setVisibility(View.INVISIBLE);
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

}

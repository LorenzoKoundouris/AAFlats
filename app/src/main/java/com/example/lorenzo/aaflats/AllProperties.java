package com.example.lorenzo.aaflats;

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

public class AllProperties extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_properties);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllProperties.this, CreateProperty.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final RecyclerView propertyRecyclerView;
        final ArrayList<Property> propertyList = new ArrayList<>();
        final ArrayList<String> propertyKeys = new ArrayList<>();

        propertyRecyclerView = (RecyclerView) findViewById(R.id.properties_recycler_view);
        propertyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String propertiesRefString = getResources().getString(R.string.properties_location);
        Firebase propertiesRef = new Firebase(propertiesRefString);

        propertiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();
                propertyKeys.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Property prt = childSnap.getValue(Property.class);
                    propertyList.add(prt);
                    propertyKeys.add(childSnap.getKey());
                }
                propertyRecyclerView.setAdapter(new PropertyAdapter(propertyList, propertyKeys));
                propertyRecyclerView.setVisibility(View.VISIBLE);
                ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

}
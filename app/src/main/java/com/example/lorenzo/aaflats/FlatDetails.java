package com.example.lorenzo.aaflats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FlatDetails extends AppCompatActivity {
    private Property parceableProperty;
    private String parceablePropertyKey;
    private String parceableFlatKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flat_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Bundle intent = getIntent().getExtras();
        final Flat parceableFlat = intent.getParcelable("parceable_flat");
//        parceableFlatKey = intent.getString("parceable_flat_key");
        parceableProperty = intent.getParcelable("parceable_property");
        parceablePropertyKey = intent.getString("parceable_property_key");

//        Firebase flatsRef = new Firebase(getResources().getString(R.string.flats_location));
//        Query flatQuery = flatsRef.orderByChild("addressLine1").equalTo(parceableProperty.getAddrline1());
//        final ArrayList<Flat> flatList = new ArrayList<>();
//        flatQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                //flatList.clear();
//                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
//                    Flat flt = childSnapShot.getValue(Flat.class);
//                    flatList.add(flt);
//                    assert parceableFlat != null;
//                    if (flt.getFlatNum().matches(parceableFlat.getFlatNum())) {
//                        parceableFlatKey = childSnapShot.getKey();
//                        break;
//                    }
//                }
//                ttestt = flatList.get(0).getFlatNum();
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });

        assert parceableFlat != null;
        setTitle(parceableFlat.getAddressLine1() + " - " + parceableFlat.getFlatNum());

        final TextView flatAddrline1 = (TextView) findViewById(R.id.flat_details_addrline1);
        flatAddrline1.setText(parceableFlat.getAddressLine1());

        TextView flatPostcode = (TextView) findViewById(R.id.flat_details_postcode);
        flatPostcode.setText(parceableFlat.getPostcode());

        TextView flatTenant = (TextView) findViewById(R.id.flat_details_tenant);
        flatTenant.setText(parceableFlat.getTenant());

        TextView flatNotes = (TextView) findViewById(R.id.flat_details_notes);
        flatNotes.setText(parceableFlat.getNotes());

        final ArrayList<Task> flatPendingTasks = new ArrayList<>();
        final ArrayList<Task> flatCompletedTasks = new ArrayList<>();
        final ArrayList<String> flatPendingTasksKeys = new ArrayList<>();
        final ArrayList<String> flatCompletedTasksKeys = new ArrayList<>();
        Firebase taskRef = new Firebase(getString(R.string.tasks_location));
        //Query tasksOfThisFlatQ = taskRef.orderByChild("property").equalTo(parceableFlatKey);// .orderByChild("status").equalTo("false");
        Query tasksOfThisFlatQ = taskRef.orderByChild("property").equalTo(parceableProperty.getAddrline1() + " - " + parceableFlat.getFlatNum().toLowerCase());
        tasksOfThisFlatQ.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                flatPendingTasks.clear();
//                flatCompletedTasks.clear();
//                flatPendingTasksKeys.clear();
//                flatCompletedTasksKeys.clear();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    Task tsk = childSnapShot.getValue(Task.class);
                    if (!tsk.getStatus()) {
                        flatPendingTasks.add(tsk);
                        flatPendingTasksKeys.add(childSnapShot.getKey());
                    } else {
                        flatCompletedTasks.add(tsk);
                        flatCompletedTasksKeys.add(childSnapShot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        RecyclerView fltDtPdgTskRecyclerView = (RecyclerView) findViewById(R.id.flt_det_pdg_tsk_recyclerview);
        RecyclerView fltDtCompTskRecyclerView = (RecyclerView) findViewById(R.id.flt_det_comp_tsk_recyclerview);
        fltDtPdgTskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fltDtCompTskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fltDtPdgTskRecyclerView.setAdapter(new FlatPendingTasksAdapter(flatPendingTasks, flatPendingTasksKeys));
        fltDtCompTskRecyclerView.setAdapter(new FlatCompletedTasksAdapter(flatCompletedTasks, flatCompletedTasksKeys));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PropertyDetails.class);
        intent.putExtra("parceable_property", parceableProperty);
        intent.putExtra("parceable_property_key", parceablePropertyKey);
        System.out.println(parceableProperty.getAddrline1() + "  -  " + parceablePropertyKey);
        this.startActivity(intent);
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

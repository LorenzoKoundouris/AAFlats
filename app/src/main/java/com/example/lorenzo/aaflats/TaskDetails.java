package com.example.lorenzo.aaflats;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class TaskDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle intent = getIntent().getExtras();
        final Task parceableTask = (Task) intent.getParcelable("parceable_task");
        //ArrayList<Task> pTaskList = (ArrayList<Task>) intent.getParcelable("parceable_tasklist");

        final TextView tv = (TextView) findViewById(R.id.tv23);
        setTitle(parceableTask.getTitle());

        Firebase ref = new Firebase(getResources().getString(R.string.reports_location));
        Query refQ = ref.orderByKey().equalTo(parceableTask.getReport());


//        refQ.addListenerForSingleValueEvent(new ValueEventListener() {
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot childSnap : dataSnapshot.getChildren()) {
//                    Report associatedReport = childSnap.getValue(Report.class);
//                    //Report associatedReport = dataSnapshot.getValue(Report.class);
//                    tv.setText(associatedReport.getContent());
//                    //tv.setText(parceableTask.getReport());
//                    System.out.println("PRINT THIS CHILD: " + childSnap.getValue());
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });

        refQ.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report associatedReport = childSnap.getValue(Report.class);
                    //Report associatedReport = dataSnapshot.getValue(Report.class);
                    tv.setText(associatedReport.getContent());
                    //tv.setText(parceableTask.getReport());
                    System.out.println("PRINT THIS CHILD: " + childSnap.getValue());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

}

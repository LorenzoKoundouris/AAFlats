package com.example.lorenzo.aaflats;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class CreateTask extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("New task");

        Firebase.setAndroidContext(this);
        final ArrayList<Task> mTaskList = new ArrayList<Task>();
        Firebase taskRef = new Firebase(getString(R.string.tasks_location));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Bundle intent = getIntent().getExtras();
//        Task parceableTask = (Task) intent.getParcelable("parceable_task");
        //ArrayList<Task> pTaskList = new ArrayList<>();
        final ArrayList<String> taskTitles = new ArrayList<>();

//        for(Task tsk : mTaskList){
//            taskTitles.add(tsk.getTitle());
//        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_dropdown_item_1line, taskTitles);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.nt_title_editview);
        textView.setAdapter(adapter);

        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {
                    System.out.println("THERE ARE " + dataSnapshot.getChildrenCount()
                            + " tasks - " + dataSnapshot.getValue());
                    Task tsk = tskSnapshot.getValue(Task.class);
                    System.out.println("onData Title---: " + tsk.getTitle());
                    System.out.println("onData Description--- : " + tsk.getDescription());
                    mTaskList.add(tsk);
                    System.out.println("ARRAY CONTENTS---: " + mTaskList); //It has tasks here
                    taskTitles.add(tsk.getTitle());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Task: " + "The read failed: " + firebaseError.getMessage());
            }
        });
        taskRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Task tsk = dataSnapshot.getValue(Task.class);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mTaskList.clear();
                Task newTask = dataSnapshot.getValue(Task.class);
                //taskArrayList.add(newTask);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.createtask, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leaving page")
                .setMessage("You have not saved this new task. Press Yes to discard or No to remain on page.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
            case R.id.save_new_task:
                Toast.makeText(getApplicationContext(), "Save button clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}



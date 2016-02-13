package com.example.lorenzo.aaflats;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class TaskDetails extends AppCompatActivity {

    public static String taskTitle = "";
    public static Task viewTask = new Task();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            //viewTask = extras.getParcelable("pTask");
            taskTitle = extras.getString("taskTitle");
            //viewTask = extras.getParcelable("pTask");
        }
        if (viewTask == null) {
            throw new IllegalArgumentException("Activity cannot find extras (>.<)");
        }
        TextView tv = (TextView) findViewById(R.id.tv23);
        tv.setText(taskTitle);
        setTitle(taskTitle);
    }

}

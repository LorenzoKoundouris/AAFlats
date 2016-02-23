package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class Homepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String SAVED_ADAPTER_ITEMS = "SAVED_ADAPTER_ITEMS";
    private final static String SAVED_ADAPTER_KEYS = "SAVED_ADAPTER_KEYS";
    private Query mQuery;
    private TaskAdapter mTaskAdapter;
    private ArrayList<Task> mAdapterItems;
    private ArrayList<String> mAdapterKeys;
    private RecyclerView taskRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupFirebase();
        setupRecyclerview();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.home_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CreateTask.class);
                view.getContext().startActivity(intent);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        //refreshLayout.setColorSchemeColors(android.R.color.holo_green_dark, android.R.color.holo_green_light, android.R.color.holo_blue_dark, android.R.color.holo_blue_bright, android.R.color.holo_blue_light, android.R.color.holo_orange_dark, android.R.color.holo_orange_light, android.R.color.holo_purple);
        refreshLayout.setColorSchemeResources(R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
//        refreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                refreshLayout.setRefreshing(true);
//            }
//        });
    }


    private void setupRecyclerview() {
        taskRecyclerView = (RecyclerView) findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyHHmmss");
//        String format = s.format(new Date());
//        System.out.println("TIMESTAMP: " + format);
    }

    private void setupFirebase() {
        Firebase.setAndroidContext(this);
        String tasksLocation = getResources().getString(R.string.tasks_location);
        final ArrayList<Task> mTaskList = new ArrayList<Task>();
        final ArrayList<String> taskKeys = new ArrayList<>();
        mQuery = new Firebase(tasksLocation);

        mQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot tskSnapshot : snapshot.getChildren()) {
//                    Task tsk = tskSnapshot.getValue(Task.class);
//                    System.out.println("Task: " + tsk.getTitle() + ": " + tsk.getDescription());
////                    mAdapterItems.add(tsk);
//                }
//                taskRecyclerView.setAdapter(new TaskAdapter(mQuery, Task.class));
                mTaskList.clear();
                taskKeys.clear();
                System.out.println("PRINT OUT OF FOR-LOOP+++++++++++++++++++++");
                int i=0;
                for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {
//                    System.out.println("There are " + dataSnapshot.getChildrenCount()
//                            + " tasks - " + dataSnapshot.getValue());
                    Task tsk = tskSnapshot.getValue(Task.class);
                    System.out.println("PRINT INSIDE OF FOR-LOOP: "+tskSnapshot.getValue());
                    //System.out.println("onData Title: " + tsk.getTitle());
                    //System.out.println("onData Description : " + tsk.getDescription());
                    mTaskList.add(tsk);
                    //mTaskList.get(i).setTaskKey(tskSnapshot.getKey());
                    taskKeys.add(tskSnapshot.getKey());
                    //System.out.println("taskArrayList contents: " + mTaskList); //It has tasks here

                    // specify an adapter (see also next example)

                    i++;
                }
                taskRecyclerView.setAdapter(new TaskAdapter(taskKeys, mTaskList)); //, Task.class
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //System.out.println("Task: " + "The read failed: " + firebaseError.getMessage());
            }
        });
        mQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {

                Task tsk = dataSnapshot.getValue(Task.class);
                //System.out.println("PRINT THIS CHILD: " + dataSnapshot.getValue() +"$$$$$$$$$$$$$$$$$$$");
                // specify an adapter (see also next example)
                //mRecyclerView.setAdapter(new TaskAdapter(mQuery, taskArrayList)); //, Task.class
                //}


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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_inbox) {
            // Handle the camera action
        } else if (id == R.id.nav_today) {

        } else if (id == R.id.nav_next7) {

        } else if (id == R.id.nav_filter) {

        } else if (id == R.id.nav_properties) {
            startActivity(new Intent(Homepage.this, AllProperties.class));
        } else if (id == R.id.nav_something) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

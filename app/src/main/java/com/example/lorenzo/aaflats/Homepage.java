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
import android.widget.ProgressBar;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Homepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Firebase taskRef = new Firebase(getResources().getString(R.string.tasks_location));
    private ArrayList<Task> highPT = new ArrayList<>();
    private ArrayList<Task> mediumPT = new ArrayList<>();
    private ArrayList<Task> lowPT = new ArrayList<>();
    private ArrayList<Task> prioritisedTasks= new ArrayList<>();
    final ArrayList<Task> mTaskList = new ArrayList<>();
    final ArrayList<String> taskKeys = new ArrayList<>();
    private RecyclerView taskRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private boolean notFirstLoad = false;
    private boolean filterTasks = false;

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

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_homepage);
        //refreshLayout.setColorSchemeColors(android.R.color.holo_green_dark, android.R.color.holo_green_light, android.R.color.holo_blue_dark, android.R.color.holo_blue_bright, android.R.color.holo_blue_light, android.R.color.holo_orange_dark, android.R.color.holo_orange_light, android.R.color.holo_purple);
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_1);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRecyclerAdapterContents(taskKeys, mTaskList);
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

        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTaskList.clear();
                taskKeys.clear();

                for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {
                    Task tsk = tskSnapshot.getValue(Task.class);
                    mTaskList.add(tsk);
                    taskKeys.add(tskSnapshot.getKey());
                    if(tsk.getPriority().matches("high")){
                        highPT.add(tsk);
                    } else if(tsk.getPriority().matches("medium")){
                        mediumPT.add(tsk);
                    } else if(tsk.getPriority().matches("low")){
                        lowPT.add(tsk);
                    }
                }

                setRecyclerAdapterContents(taskKeys, mTaskList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //System.out.println("Task: " + "The read failed: " + firebaseError.getMessage());
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

    public void setRecyclerAdapterContents(ArrayList<String> taskKeys, ArrayList<Task> mTaskList) {
        taskRecyclerView.setAdapter(new TaskAdapter(taskKeys, mTaskList)); //, Task.class
        if(!notFirstLoad){
            taskRecyclerView.setVisibility(View.VISIBLE);
            ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_homepage);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        notFirstLoad = true;
        refreshLayout.setRefreshing(false);
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
        } else if (id == R.id.filter_tasks){
            filterTasks = true;
        } else if (id == R.id.sort_tasks){
            prioritiseTaskss();
        }

        return super.onOptionsItemSelected(item);
    }

    private void prioritiseTaskss() {
        prioritisedTasks.clear();
        Collections.sort(highPT, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                return lhs.getProperty().compareTo(rhs.getProperty());
            }
        });
        Collections.sort(mediumPT, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                return lhs.getProperty().compareTo(rhs.getProperty());
            }
        });
        Collections.sort(lowPT, new Comparator<Task>() {
            @Override
            public int compare(Task lhs, Task rhs) {
                return lhs.getProperty().compareTo(rhs.getProperty());
            }
        });
        prioritisedTasks.addAll(highPT);
        prioritisedTasks.addAll(mediumPT);
        prioritisedTasks.addAll(lowPT);
//        setRecyclerAdapterContents(prioritisedTasks);
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
            startActivity(new Intent(Homepage.this, TenantHomepage.class));
        } else if (id == R.id.nav_today) {
            startActivity(new Intent(Homepage.this, CreateFlat.class));
        } else if (id == R.id.nav_next7) {

        } else if (id == R.id.nav_filter) {

        } else if (id == R.id.nav_properties) {
            startActivity(new Intent(Homepage.this, AllProperties.class));
        } else if (id == R.id.nav_tenants) {
            startActivity(new Intent(Homepage.this, AllTenants.class));
        } else if (id == R.id.nav_reports) {

        } else if (id == R.id.nav_map) {

        } else if (id == R.id.nav_chat) {

        } else if (id == R.id.nav_add_tenant) {
            startActivity(new Intent(Homepage.this, CreateTenant.class));
        } else if (id == R.id.nav_add_flat) {
            startActivity(new Intent(Homepage.this, CreateFlat.class));
        } else if (id == R.id.nav_add_property) {
            startActivity(new Intent(Homepage.this, CreateProperty.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

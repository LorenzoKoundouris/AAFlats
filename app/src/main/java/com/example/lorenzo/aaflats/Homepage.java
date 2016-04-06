package com.example.lorenzo.aaflats;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


public class Homepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static int REQUEST_CAMERA = 0;

    private ArrayList<Task> highPT = new ArrayList<>();
    private ArrayList<Task> mediumPT = new ArrayList<>();
    private ArrayList<Task> lowPT = new ArrayList<>();
    private ArrayList<Task> onlyTodayTasks = new ArrayList<>();
    private ArrayList<Task> onlyTomorrowTasks = new ArrayList<>();
    private ArrayList<Task> onlyNext7Tasks = new ArrayList<>();
    private ArrayList<Task> pendingFT = new ArrayList<>();
    private ArrayList<Task> prioritisedTasks = new ArrayList<>();
    private ArrayList<Task> mTaskList = new ArrayList<>();
    private ArrayList<String> taskKeys = new ArrayList<>();
    private RecyclerView taskRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private boolean notFirstLoad = false;
    private boolean showToday = true;
    private boolean showTomorrow = false;
    private boolean showNext7 = false;
    private boolean showPendingOnly = false;
    private boolean prioritiseAll = false;
    private String todaysDate = "";
    private String tempDateHolder;
    private ArrayList<String> next7Dates = new ArrayList<>();

    private NavigationView navigationView;
    private MenuItem filterByMenuItem;
    private TextView dateTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        todaysDate = df.format(c.getTime());
        tempDateHolder = df.format(c.getTime());

        for (int i = 0; i < 8; i++) {
            next7Dates.add(tempDateHolder);
            c.add(Calendar.DAY_OF_MONTH, 1);
            tempDateHolder = df.format(c.getTime());
        }

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

        filterByMenuItem = new MenuItem() {
            public int getItemId() {
                return R.id.filter_tasks;
            }

            @Override
            public int getGroupId() {
                return 0;
            }

            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public MenuItem setTitle(CharSequence title) {
                return null;
            }

            @Override
            public MenuItem setTitle(int title) {
                return null;
            }

            @Override
            public CharSequence getTitle() {
                return null;
            }

            @Override
            public MenuItem setTitleCondensed(CharSequence title) {
                return null;
            }

            @Override
            public CharSequence getTitleCondensed() {
                return null;
            }

            @Override
            public MenuItem setIcon(Drawable icon) {
                return null;
            }

            @Override
            public MenuItem setIcon(int iconRes) {
                return null;
            }

            @Override
            public Drawable getIcon() {
                return null;
            }

            @Override
            public MenuItem setIntent(Intent intent) {
                return null;
            }

            @Override
            public Intent getIntent() {
                return null;
            }

            @Override
            public MenuItem setShortcut(char numericChar, char alphaChar) {
                return null;
            }

            @Override
            public MenuItem setNumericShortcut(char numericChar) {
                return null;
            }

            @Override
            public char getNumericShortcut() {
                return 0;
            }

            @Override
            public MenuItem setAlphabeticShortcut(char alphaChar) {
                return null;
            }

            @Override
            public char getAlphabeticShortcut() {
                return 0;
            }

            @Override
            public MenuItem setCheckable(boolean checkable) {
                return null;
            }

            @Override
            public boolean isCheckable() {
                return false;
            }

            @Override
            public MenuItem setChecked(boolean checked) {
                return null;
            }

            @Override
            public boolean isChecked() {
                return false;
            }

            @Override
            public MenuItem setVisible(boolean visible) {
                return null;
            }

            @Override
            public boolean isVisible() {
                return false;
            }

            @Override
            public MenuItem setEnabled(boolean enabled) {
                return null;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public boolean hasSubMenu() {
                return false;
            }

            @Override
            public SubMenu getSubMenu() {
                return null;
            }

            @Override
            public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
                return null;
            }

            @Override
            public ContextMenu.ContextMenuInfo getMenuInfo() {
                return null;
            }

            @Override
            public void setShowAsAction(int actionEnum) {

            }

            @Override
            public MenuItem setShowAsActionFlags(int actionEnum) {
                return null;
            }

            @Override
            public MenuItem setActionView(View view) {
                return null;
            }

            @Override
            public MenuItem setActionView(int resId) {
                return null;
            }

            @Override
            public View getActionView() {
                return null;
            }

            @Override
            public MenuItem setActionProvider(ActionProvider actionProvider) {
                return null;
            }

            @Override
            public ActionProvider getActionProvider() {
                return null;
            }

            @Override
            public boolean expandActionView() {
                return false;
            }

            @Override
            public boolean collapseActionView() {
                return false;
            }

            @Override
            public boolean isActionViewExpanded() {
                return false;
            }

            @Override
            public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
                return null;
            }
        };

        dateTasks = (TextView) findViewById(R.id.text_view_today);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(1).setChecked(true);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_homepage);
        //refreshLayout.setColorSchemeColors(android.R.color.holo_green_dark, android.R.color.holo_green_light, android.R.color.holo_blue_dark, android.R.color.holo_blue_bright, android.R.color.holo_blue_light, android.R.color.holo_orange_dark, android.R.color.holo_orange_light, android.R.color.holo_purple);
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
                        setRecyclerAdapterContents();
                    }
                }, 2000);
            }
        });
//        refreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                refreshLayout.setRefreshing(true);
//            }
//        });


        Bundle intent = getIntent().getExtras();
        Staff staffLoggedIn = intent.getParcelable("parceable_staff");
        View myHeader = navigationView.getHeaderView(0);
        TextView staffName = (TextView) myHeader.findViewById(R.id.staff_name);
        TextView staffEmail = (TextView) myHeader.findViewById(R.id.staff_email);
        try{
            staffName.setText(staffLoggedIn.getForename() + " " + staffLoggedIn.getSurname());
            staffEmail.setText(staffLoggedIn.getUsername());
        } catch(Exception ex){
            Toast.makeText(Homepage.this, "Could not load staff details", Toast.LENGTH_SHORT).show();
        }



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
        Firebase taskRef = new Firebase(getResources().getString(R.string.tasks_location));
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTaskList.clear();
                taskKeys.clear();
                highPT.clear();
                mediumPT.clear();
                lowPT.clear();
                pendingFT.clear();
                onlyTodayTasks.clear();
                onlyNext7Tasks.clear();

                System.out.println(todaysDate);
                for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {
                    Task tsk = tskSnapshot.getValue(Task.class);
                    mTaskList.add(tsk);
                    tsk.setTaskKey(tskSnapshot.getKey());
                    taskKeys.add(tskSnapshot.getKey());
//                    if (tsk.getPriority().matches("High")) {
//                        highPT.add(tsk);
//                    } else if (tsk.getPriority().matches("Medium")) {
//                        mediumPT.add(tsk);
//                    } else if (tsk.getPriority().matches("Low")) {
//                        lowPT.add(tsk);
//                    }
//                    if (!tsk.getStatus()) {
//                        pendingFT.add(tsk);
//                    }
                    if (tsk.getTargetDate().matches(todaysDate)) {
                        onlyTodayTasks.add(tsk);
                    }
                    for (int i = 0; i < 8; i++) {
                        if (tsk.getTargetDate().matches(next7Dates.get(i))) {
                            onlyNext7Tasks.add(tsk);

                        }
                    }
                }
                setRecyclerAdapterContents();
//                    setRecyclerAdapterContents(mTaskList);
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

    public void setRecyclerAdapterContents() {

        pendingFT.clear();
        ArrayList<Task> justPrioritised = new ArrayList<>();
        ArrayList<Task> pendingAndPrioritised = new ArrayList<>();
        Toast toast;
        highPT.clear();
        mediumPT.clear();
        lowPT.clear();

        if (showToday) {
            for (int i = 0; i < onlyTodayTasks.size(); i++) {
                if (onlyTodayTasks.get(i).getPriority().matches("High")) {
                    highPT.add(onlyTodayTasks.get(i));
                }
            }
            Collections.sort(highPT, new Comparator<Task>() {
                @Override
                public int compare(Task lhs, Task rhs) {
                    return lhs.getProperty().compareTo(rhs.getProperty());
                }
            });
            for (int i = 0; i < onlyTodayTasks.size(); i++) {
                if (onlyTodayTasks.get(i).getPriority().matches("Medium")) {
                    mediumPT.add(onlyTodayTasks.get(i));
                }
            }
            Collections.sort(mediumPT, new Comparator<Task>() {
                @Override
                public int compare(Task lhs, Task rhs) {
                    return lhs.getProperty().compareTo(rhs.getProperty());
                }
            });
            for (int i = 0; i < onlyTodayTasks.size(); i++) {
                if (onlyTodayTasks.get(i).getPriority().matches("Low")) {
                    lowPT.add(onlyTodayTasks.get(i));
                }
            }
            Collections.sort(lowPT, new Comparator<Task>() {
                @Override
                public int compare(Task lhs, Task rhs) {
                    return lhs.getProperty().compareTo(rhs.getProperty());
                }
            });

            justPrioritised.addAll(highPT);
            justPrioritised.addAll(mediumPT);
            justPrioritised.addAll(lowPT);

            if (showPendingOnly && prioritiseAll) {

                pendingAndPrioritised.addAll(justPrioritised);

                for (Task tsk : justPrioritised) {
                    if (tsk.getStatus()) {
                        pendingAndPrioritised.remove(tsk);
                    }
                }

                //Show today's prioritised pending tasks
                toast = Toast.makeText(Homepage.this, "Showing today's prioritised tasks that are pending ", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(pendingAndPrioritised));

            } else if (showPendingOnly) {
                for (Task tsk : onlyTodayTasks) {
                    if (!tsk.getStatus()) {
                        pendingFT.add(tsk);
                    }
                }
                //Show today's pending tasks
                toast = Toast.makeText(Homepage.this, "Showing today's tasks that are pending ", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(pendingFT));

            } else if (prioritiseAll) {
                //Show today's prioritised tasks
                toast = Toast.makeText(Homepage.this, "Showing today's prioritised tasks", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(justPrioritised));

            } else {
                //Show today's tasks
                toast = Toast.makeText(Homepage.this, "Showing today's tasks", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(onlyTodayTasks));
            }

        } else if (showNext7) {

            for (int i = 0; i < onlyNext7Tasks.size(); i++) {
                if (onlyNext7Tasks.get(i).getPriority().matches("High")) {
                    highPT.add(onlyNext7Tasks.get(i));
                }
            }
            Collections.sort(highPT, new Comparator<Task>() {
                @Override
                public int compare(Task lhs, Task rhs) {
                    return lhs.getProperty().compareTo(rhs.getProperty());
                }
            });
            for (int i = 0; i < onlyNext7Tasks.size(); i++) {
                if (onlyNext7Tasks.get(i).getPriority().matches("Medium")) {
                    mediumPT.add(onlyNext7Tasks.get(i));
                }
            }
            Collections.sort(mediumPT, new Comparator<Task>() {
                @Override
                public int compare(Task lhs, Task rhs) {
                    return lhs.getProperty().compareTo(rhs.getProperty());
                }
            });
            for (int i = 0; i < onlyNext7Tasks.size(); i++) {
                if (onlyNext7Tasks.get(i).getPriority().matches("Low")) {
                    lowPT.add(onlyNext7Tasks.get(i));
                }
            }
            Collections.sort(lowPT, new Comparator<Task>() {
                @Override
                public int compare(Task lhs, Task rhs) {
                    return lhs.getProperty().compareTo(rhs.getProperty());
                }
            });

            justPrioritised.addAll(highPT);
            justPrioritised.addAll(mediumPT);
            justPrioritised.addAll(lowPT);

            if (showPendingOnly && prioritiseAll) {

                pendingAndPrioritised.addAll(justPrioritised);

                for (Task tsk : justPrioritised) {
                    if (tsk.getStatus()) {
                        pendingAndPrioritised.remove(tsk);
                    }
                }

                //Show next 7 days prioritised pending tasks
                toast = Toast.makeText(Homepage.this, "Showing next 7 days prioritised tasks that are pending ", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(pendingAndPrioritised));

            } else if (showPendingOnly) {
                for (Task tsk : onlyNext7Tasks) {
                    if (!tsk.getStatus()) {
                        pendingFT.add(tsk);
                    }
                }
                //Show next 7 days pending tasks
                toast = Toast.makeText(Homepage.this, "Showing next 7 days tasks that are pending", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(pendingFT));

            } else if (prioritiseAll) {
                //Show next 7 days prioritised tasks
                toast = Toast.makeText(Homepage.this, "Showing next 7 days prioritised tasks", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(justPrioritised));

            } else {
                //Show next 7 days tasks
                toast = Toast.makeText(Homepage.this, "Showing next 7 days tasks", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                taskRecyclerView.setAdapter(new TaskAdapter(onlyNext7Tasks));
            }
        } else {
            taskRecyclerView.setAdapter(new TaskAdapter(mTaskList));
        }

//        onlyTodayTasks.clear();
//        onlyNext7Tasks.clear();

//        taskRecyclerView.setAdapter(new TaskAdapter(mTaskList)); //, Task.class
        if (!notFirstLoad) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 2s = 2000ms
                    taskRecyclerView.setVisibility(View.VISIBLE);
                    ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_homepage);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 2000);

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
        if (id == R.id.filter_tasks) {

            final String[] arrayFilters = new String[]{"Address", "Pending only"};

            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle("Filter by..")
                    .setItems(arrayFilters, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            System.out.println("You filtered by: " + arrayFilters[item]);
                            if (arrayFilters[item].matches("Pending only")) {
                                showPendingOnly = true;
//                                ArrayList<Task> tempPrioritise = new ArrayList<>();
//                                for (int i = 0; i < pendingFT.size(); i++) {
//                                    if (pendingFT.get(i).getPriority().matches("High")) {
//                                        tempPrioritise.add(pendingFT.get(i));
//                                    }
//                                }
//                                for (int i = 0; i < pendingFT.size(); i++) {
//                                    if (pendingFT.get(i).getPriority().matches("Medium")) {
//                                        tempPrioritise.add(pendingFT.get(i));
//                                    }
//                                }
//                                for (int i = 0; i < pendingFT.size(); i++) {
//                                    if (pendingFT.get(i).getPriority().matches("Low")) {
//                                        tempPrioritise.add(pendingFT.get(i));
//                                    }
//                                }
//                                Collections.sort(pendingFT, new Comparator<Task>() {
//                                    @Override
//                                    public int compare(Task lhs, Task rhs) {
//                                        return lhs.getPriority().compareTo(rhs.getPriority());
//                                    }
//                                });
                                setRecyclerAdapterContents();

                            } else if (arrayFilters[item].matches("Address")) {

                                final AutoCompleteTextView actvProperty = new AutoCompleteTextView(alertBuilder.getContext());
                                alertBuilder.setMessage("*i.e. 12 Trematon Terrace - Flat 1");
                                alertBuilder.setTitle("Enter an address");

                                alertBuilder.setView(actvProperty);

                                final ArrayList<Flat> flatList = new ArrayList<Flat>();
                                final ArrayList<String> propertyAddrLine1s = new ArrayList<>();
                                final ArrayList<Property> foundProperties = new ArrayList<>();

                                final Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
                                flatRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot fltSnapshot : dataSnapshot.getChildren()) {
                                            Flat flt = fltSnapshot.getValue(Flat.class);
                                            flt.setFlatKey(fltSnapshot.getKey());
                                            flatList.add(flt);
                                            propertyAddrLine1s.add(flt.getAddressLine1().trim() +
                                                    " - " + flt.getFlatNum().trim());
                                        }

                                        Collections.sort(propertyAddrLine1s, new Comparator<String>() {
                                            @Override
                                            public int compare(String lhs, String rhs) {
                                                return lhs.compareTo(rhs);
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                        System.out.println("Property: " + "The read failed: " + firebaseError.getMessage());
                                    }
                                });


                                ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>
                                        (alertBuilder.getContext(), android.R.layout.simple_dropdown_item_1line, propertyAddrLine1s);
                                actvProperty.setAdapter(propertyAdapter);

// SO FAR ALL ABOVE WORKS ----^
                                Firebase propertyRef = new Firebase(getResources().getString(R.string.properties_location));

//                        Query getPropertyObject = propertyRef.orderByChild("addrline1").
//                                equalTo(propertyEntered[0].toLowerCase().trim());

                                final ArrayList<Flat> tempFlat = new ArrayList<>();
                                propertyRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                            Property prt = childSnap.getValue(Property.class);
                                            prt.setPropertyKey(childSnap.getKey());
                                            foundProperties.add(prt);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {

                                    }
                                });

                                alertBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        final String[] propertyEntered = actvProperty.getText().toString().split(" - ");

                                        for (Property pt : foundProperties) {
                                            if (pt.getAddrline1().matches(propertyEntered[0])) {
                                                foundProperties.clear();
                                                foundProperties.add(pt);
                                                break;
                                            }
                                        }


                                        for (Flat ft : flatList) {
                                            if (ft.getAddressLine1().matches(propertyEntered[0]) &&
                                                    ft.getFlatNum().matches(propertyEntered[1].substring(0, 1)
                                                            .toUpperCase() + propertyEntered[1].substring(1))) {
                                                tempFlat.add(ft);
                                                break;
                                            }
                                        }

//                                Query getFlatObject = flatRef.orderByChild("addressLine1").equalTo(propertyEntered[0].toLowerCase().trim());
//                                getFlatObject.addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
//                                            Flat flt = childSnap.getValue(Flat.class);
//                                            if (flt.getFlatNum().matches(propertyEntered[1].trim().substring(0,1).toUpperCase())) {
//                                                tempFlat.add(flt);
//                                                flt.setFlatKey(childSnap.getKey());
//                                            }
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(FirebaseError firebaseError) {
//
//                                    }
//                                });

                                        String YouEditTextValue = actvProperty.getText().toString();
                                        System.out.println(YouEditTextValue);

                                        Intent intent = new Intent(Homepage.this, FlatDetails.class);
                                        intent.putExtra("parceable_flat", tempFlat.get(0));
                                        intent.putExtra("parceable_property", foundProperties.get(0));
                                        intent.putExtra("parceable_property_key", foundProperties.get(0).getPropertyKey());
                                        startActivity(intent);

                                    }
                                });

                                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // what ever you want to do with No option.
                                    }
                                });

                                alertBuilder.show();
                            }

                        }
                    });

            final AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();

        } else if (id == R.id.sort_tasks) {
            prioritiseAll = true;
            setRecyclerAdapterContents();
        } else if (id == R.id.scan_qr) {
            //Check if device is running Android Marshmallow. Permissions changed after Lollipop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Check if camera permission is granted
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(Homepage.this, ScanQR.class).putExtra("fromHome", true));
                } else {
                    //Camera permission not granted

                    //Provide context to the user to justify permission
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(Homepage.this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                    }
                    //Request permission
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                }
            } else {
                startActivity(new Intent(Homepage.this, ScanQR.class).putExtra("fromHome", true));
            }
        }
        return super.onOptionsItemSelected(item);
    }

//    private void prioritiseTaskss() {
//        prioritisedTasks.clear();
//        Collections.sort(highPT, new Comparator<Task>() {
//            @Override
//            public int compare(Task lhs, Task rhs) {
//                return lhs.getProperty().compareTo(rhs.getProperty());
//            }
//        });
//        Collections.sort(mediumPT, new Comparator<Task>() {
//            @Override
//            public int compare(Task lhs, Task rhs) {
//                return lhs.getProperty().compareTo(rhs.getProperty());
//            }
//        });
//        Collections.sort(lowPT, new Comparator<Task>() {
//            @Override
//            public int compare(Task lhs, Task rhs) {
//                return lhs.getProperty().compareTo(rhs.getProperty());
//            }
//        });
//        prioritisedTasks.addAll(highPT);
//        prioritisedTasks.addAll(mediumPT);
//        prioritisedTasks.addAll(lowPT);
////        setRecyclerAdapterContents(prioritisedTasks);
//        taskRecyclerView.setAdapter(new TaskAdapter(prioritisedTasks));
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(Homepage.this, ScanQR.class).putExtra("fromHome", true));
            }
        }
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
            startActivity(new Intent(Homepage.this, Inbox.class));
        } else if (id == R.id.nav_today) {
            dateTasks.setText("Today");
            showToday = true;
            showNext7 = false;
            showPendingOnly = false;
            prioritiseAll = false;
            setRecyclerAdapterContents();
            navigationView.getMenu().getItem(1).setChecked(true);
        } else if (id == R.id.nav_next7) {
            dateTasks.setText("Next 7 days");
            showToday = false;
            showNext7 = true;
            showPendingOnly = false;
            prioritiseAll = false;
            setRecyclerAdapterContents();
            navigationView.getMenu().getItem(2).setChecked(true);
        } else if (id == R.id.nav_filter) {
            onOptionsItemSelected(filterByMenuItem);
        } else if (id == R.id.nav_properties) {
            startActivity(new Intent(Homepage.this, AllProperties.class));
        } else if (id == R.id.nav_tenants) {
            startActivity(new Intent(Homepage.this, AllTenants.class));
        } else if (id == R.id.nav_reports) {
            startActivity(new Intent(Homepage.this, AllReports.class));
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(Homepage.this, MapProperty.class));
        } else if (id == R.id.nav_chat) {
            startActivity(new Intent(Homepage.this, TenantHomepage.class));
//            startActivity(new Intent(Homepage.this, LoginActivity.class));
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

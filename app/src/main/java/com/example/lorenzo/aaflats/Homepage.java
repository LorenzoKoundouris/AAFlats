package com.example.lorenzo.aaflats;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.SubMenu;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;


public class Homepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static int REQUEST_CAMERA = 0;

    CoordinatorLayout snackbarCoordinatorLayout;

    private ArrayList<Task> highPT = new ArrayList<>();
    private ArrayList<Task> mediumPT = new ArrayList<>();
    private ArrayList<Task> lowPT = new ArrayList<>();
    private ArrayList<Task> onlyTodayTasks = new ArrayList<>();
    private ArrayList<Task> onlyMyTasks = new ArrayList<>();
    private ArrayList<Task> onlyNext7Tasks = new ArrayList<>();
    private ArrayList<Task> pendingFT = new ArrayList<>();
    private ArrayList<Task> prioritisedTasks = new ArrayList<>();
    private ArrayList<Task> mTaskList = new ArrayList<>();
    private ArrayList<String> taskKeys = new ArrayList<>();
    private RecyclerView taskRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private boolean notifyFromNowOn = false;
    private boolean notFirstLoad = false;
    private boolean showToday = true;
    private boolean allTasks = false;
    private boolean showNext7 = false;
    private boolean showPendingOnly = false;
    private boolean prioritiseAll = false;
    private String todaysDate = "";
    private String tempDateHolder;
    private ArrayList<String> next7Dates = new ArrayList<>();

    private NavigationView navigationView;
    private MenuItem filterByMenuItem;
    private TextView dateTasks;
    private TextView logoutText;
    private TextView staffEmail;

    private static int uniqueID; // =25
    private NotificationCompat.Builder notificationBuilder;

    private Staff staffLoggedIn;

    public static final String MY_PREFERENCES = "MyPreferences";
    public static final String FULL_NAME_KEY = "StaffFullName";
    public static final String EMAIL_KEY = "StaffEmail";
    public static final String STAFF_KEY = "StaffKey";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    private Target viewTarget;

    Context context;

    Firebase taskRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Firebase.setAndroidContext(this);
        taskRef = new Firebase(getResources().getString(R.string.tasks_location));

        //Get Shared Preferences
        mSharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = mSharedPreferences.edit();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(2).setChecked(true);


        if (!mSharedPreferences.getBoolean("showcaseview-ed_home", false)) {
            Target homeTarget = new Target() {
                @Override
                public Point getPoint() {
                    // Get approximate position of home icon's center
                    int actionBarSize = toolbar.getHeight();
                    int x = actionBarSize / 2;
                    int y = actionBarSize / 2;
                    return new Point(x, y);
                }
            };
            new ShowcaseView.Builder(this)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setTarget(homeTarget) //new ActionViewTarget(this, ActionViewTarget.Type.HOME
                    .setContentTitle("Start here")
                    .setContentText("Navigate through the app by selecting the desired page.")
                    .hideOnTouchOutside()
                    .build();
            editor.putBoolean("showcaseview-ed_home", true).apply();
        }

        viewTarget = new Target() {
            @Override
            public Point getPoint() {
                return new ViewTarget(toolbar.findViewById(R.id.scan_qr)).getPoint();
            }
        };

        snackbarCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.snackbarCoordinatorLayout);

        context = this;

        notificationBuilder = new NotificationCompat.Builder(this);


        new Thread(new Runnable() {
            public void run() {
                Calendar c = Calendar.getInstance();
//                System.out.println("Current time => " + c.getTime());

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                todaysDate = df.format(c.getTime());
                tempDateHolder = df.format(c.getTime());

                for (int i = 0; i < 8; i++) {
                    next7Dates.add(tempDateHolder);
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    tempDateHolder = df.format(c.getTime());
                }
            }
        }).start();

        Bundle intent = getIntent().getExtras();
        staffLoggedIn = intent.getParcelable("parceable_staff");

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
                        editor.putBoolean("showcaseview-ed", false).apply();
                        editor.putBoolean("showcaseview-ed_home", false).apply();
                        invalidateOptionsMenu();

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


        View myHeader = navigationView.getHeaderView(0);
        Spinner staffName = (Spinner) myHeader.findViewById(R.id.staff_name);
        staffEmail = (TextView) myHeader.findViewById(R.id.staff_email);
        logoutText = (TextView) myHeader.findViewById(R.id.logout_text);
        ArrayList<String> logoutName = new ArrayList<>();
        try {
            logoutName.add(mSharedPreferences.getString(FULL_NAME_KEY, ""));//staffLoggedIn.getForename() + " " + staffLoggedIn.getSurname()
            ArrayAdapter<String> logoutAdapter = new ArrayAdapter<>
                    (this, R.layout.custom_spinner_transparent, logoutName);
            logoutAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item2);
            staffName.setAdapter(logoutAdapter);
            staffEmail.setText(mSharedPreferences.getString(EMAIL_KEY, "")); //staffLoggedIn.getUsername()
        } catch (Exception ex) {
            Toast.makeText(Homepage.this, "Could not load staff details", Toast.LENGTH_SHORT).show();
        }

        staffName.setOnTouchListener(Spinner_OnTouch);

        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Sign out")
                        .setMessage("Are you sure you wish to sign out?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences settings =
                                        v.getContext().getSharedPreferences("MyPreferences",
                                                Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = settings.edit();
                                /////////////////////////////////////////////////////
                                Map<String, ?> usedAccounts = settings.getAll();
                                final ArrayList<String> aa = new ArrayList<>();
                                for (Map.Entry<String, ?> tEntry : usedAccounts.entrySet()) {
                                    aa.add(tEntry.getValue().toString());
                                }
                                System.out.println(aa.toString());
                                ////////////////////////////////////////
                                editor.putBoolean("logout", true).commit();

                                /////////////////////////////////////////////////////
                                Map<String, ?> usedAccounts2 = settings.getAll();
                                final ArrayList<String> aa2 = new ArrayList<>();
                                for (Map.Entry<String, ?> tEntry2 : usedAccounts2.entrySet()) {
                                    aa2.add(tEntry2.getValue().toString());
                                }
                                System.out.println(aa2.toString());
                                ////////////////////////////////////////

//                                settings.edit().clear().commit();


                                stopService(new Intent(v.getContext(), MyService.class));
                                startService(new Intent(v.getContext(), MyService.class));

                                startActivity(new Intent(Homepage.this, SplashActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });


        stopService(new Intent(Homepage.this, MyService.class));
        sendBroadcast(new Intent("RestartServiceNow"));

//        manualUpdate();


    }

    private void manualUpdate() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//         Do something after 2s = 2000ms
                Task tUpdater = new Task();
                tUpdater.setTaskKey("foo");
                tUpdater.setAssignedStaff("bar");
                taskRef.push().setValue(tUpdater);
                Query getUpdTsk = taskRef.orderByChild("taskKey").equalTo("foo");
                getUpdTsk.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            Firebase removeUpdater;
                            String delUpdURL = taskRef + "/" + childSnap.getKey();
                            removeUpdater = new Firebase(delUpdURL);
                            removeUpdater.removeValue();
                        }

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });


//                stopService(new Intent(Homepage.this, MyService.class));
//                startService(new Intent(Homepage.this, MyService.class));
            }
        }, 4000);
    }

    private void receiveNtf(final Task tsk) {
        final Context c = this;
        new Thread(new Runnable() {
            public void run() {
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setSmallIcon(R.drawable.notification_icon);
                notificationBuilder.setTicker("New task added by " + tsk.getCreator());
                notificationBuilder.setWhen(System.currentTimeMillis());
                notificationBuilder.setContentTitle(tsk.getCreator() + " added a new task");
                notificationBuilder.setContentText(tsk.getTitle()); //newTask.getTitle()

//                notificationBuilder.setSound(Uri.parse("file:///sdcard/notification/notification.mp3"));
                notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); //delay, vibrate, sleep, vibrate, sleep
                if (tsk.getPriority().matches("High")) {
                    notificationBuilder.setLights(Color.RED, 3000, 3000);
                } else if (tsk.getPriority().matches("Medium")) {
                    notificationBuilder.setLights(Color.YELLOW, 3000, 3000);
                } else {
                    notificationBuilder.setLights(Color.GREEN, 3000, 3000);
                }


                Random randomGen = new Random();
                uniqueID = randomGen.nextInt(20000 - 1 + 1) + 1;


                NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Intent intent = new Intent(c, TaskDetails.class).putExtra("parceable_task", tsk);//.putExtra("parceable_task", newTask);
                PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(pIntent);
//                mgr.notify(uniqueID, notificationBuilder.build());
            }
        }).start();
    }

    private View.OnTouchListener Spinner_OnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (logoutText.getVisibility() == View.GONE) {
                    logoutText.setVisibility(View.VISIBLE);
                    staffEmail.setVisibility(View.INVISIBLE);
                } else {
                    logoutText.setVisibility(View.GONE);
                    staffEmail.setVisibility(View.VISIBLE);
                }
            }
            return true;
        }
    };

    private void setupRecyclerview() {
        taskRecyclerView = (RecyclerView) findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyHHmmss");
//        String format = s.format(new Date());
//        System.out.println("TIMESTAMP: " + format);
    }

    private void setupFirebase() {
        new Thread(new Runnable() {
            public void run() {
                Query getLoggedStaff = taskRef.orderByChild("assignedStaff").equalTo(staffLoggedIn.getStaffKey());
                getLoggedStaff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mTaskList.clear();
//                taskKeys.clear();
                        highPT.clear();
                        mediumPT.clear();
                        lowPT.clear();
                        pendingFT.clear();
                        onlyTodayTasks.clear();
                        onlyNext7Tasks.clear();

//                        System.out.println(todaysDate);
                        for (DataSnapshot tskSnapshot : dataSnapshot.getChildren()) {
                            Task tsk = tskSnapshot.getValue(Task.class);
                            tsk.setTaskKey(tskSnapshot.getKey());
                            mTaskList.add(tsk);
//                    taskKeys.add(tskSnapshot.getKey());
//
                            if (tsk.getTargetDate().matches(todaysDate)) {
                                onlyTodayTasks.add(tsk);
                            }
                            for (int i = 0; i < 8; i++) {
                                if (tsk.getTargetDate().matches(next7Dates.get(i))) {
                                    onlyNext7Tasks.add(tsk);
                                }
                            }
                        }
                        Collections.sort(mTaskList, new Comparator<Task>() {
                            @Override
                            public int compare(Task lhs, Task rhs) {
                                return rhs.getTaskKey().compareTo(lhs.getTaskKey());
                            }
                        });
                        setRecyclerAdapterContents();
//                    setRecyclerAdapterContents(mTaskList);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        //System.out.println("Task: " + "The read failed: " + firebaseError.getMessage());
                    }
                });


                final ArrayList<Task> newTaskAddedList = new ArrayList<>();
                taskRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        newTaskAddedList.clear();
//                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                        Task tsk = dataSnapshot.getValue(Task.class);
                        newTaskAddedList.add(tsk);
//                }
                        if (staffLoggedIn.getStaffKey().matches(tsk.getAssignedStaff())) {
//                            receiveNtf(newTaskAddedList.get(0));
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        mTaskList.clear();
                        newTaskAddedList.clear();
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
        }).start();
//        notifyFromNowOn = true;
    }

    public void setRecyclerAdapterContents() {
        new Thread(new Runnable() {
            public void run() {

                pendingFT.clear();
                final ArrayList<Task> justPrioritised = new ArrayList<>();
                final ArrayList<Task> pendingAndPrioritised = new ArrayList<>();
                Toast toast;
                highPT.clear();
                mediumPT.clear();
                lowPT.clear();

                if (showToday) {
                    for (int i = 0; i < onlyTodayTasks.size(); i++) {
//                if(onlyTodayTasks.get(i).getPriority().matches("High") && showMyTasks &&
//                        !onlyTodayTasks.get(i).getAssignedStaff().matches(staffLoggedIn.getStaffKey())){
//                    highPT.add(onlyTodayTasks.get(i));
//                } else
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
//                if(onlyTodayTasks.get(i).getPriority().matches("Medium") && showMyTasks &&
//                        !onlyTodayTasks.get(i).getAssignedStaff().matches(staffLoggedIn.getStaffKey())){
//                    mediumPT.add(onlyTodayTasks.get(i));
//                } else
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
//                if(onlyTodayTasks.get(i).getPriority().matches("Low") && showMyTasks &&
//                        !onlyTodayTasks.get(i).getAssignedStaff().matches(staffLoggedIn.getStaffKey())){
//                    lowPT.add(onlyTodayTasks.get(i));
//                } else
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
//                    if(tsk.getStatus() && showMyTasks && !tsk.getAssignedStaff().matches(staffLoggedIn.getStaffKey())){
//                        pendingAndPrioritised.remove(tsk);
//                    } else
                            if (tsk.getStatus()) {
                                pendingAndPrioritised.remove(tsk);
                            }
                        }

                        //Show today's prioritised pending tasks
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskRecyclerView.setAdapter(new TaskAdapter(pendingAndPrioritised));
                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing today's prioritised tasks that are pending", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        });
//                        toast = Toast.makeText(Homepage.this, "Showing today's prioritised tasks that are pending ", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();

                    } else if (showPendingOnly) {
                        for (Task tsk : onlyTodayTasks) {
//                    if(!tsk.getStatus() && showMyTasks && !tsk.getAssignedStaff().matches(staffLoggedIn.getStaffKey())){
//                        pendingFT.add(tsk);
//                    } else
                            if (!tsk.getStatus()) {
                                pendingFT.add(tsk);
                            }
                        }
                        //Show today's pending tasks

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskRecyclerView.setAdapter(new TaskAdapter(pendingFT));
                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing today's tasks that are pending", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        });

//                        toast = Toast.makeText(Homepage.this, "Showing today's tasks that are pending ", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();

                    } else if (prioritiseAll) {
                        //Show today's prioritised tasks
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                toast = Toast.makeText(Homepage.this, "Showing today's prioritised tasks", Toast.LENGTH_SHORT);
//                                toast.setGravity(Gravity.CENTER, 0, 0);
//                                toast.show();
                                taskRecyclerView.setAdapter(new TaskAdapter(justPrioritised));
                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing today's prioritised tasks", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        });


                    } else {
                        //Show today's tasks
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskRecyclerView.setAdapter(new TaskAdapter(onlyTodayTasks));

                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing today's tasks", Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        });
//                        toast = Toast.makeText(Homepage.this, "Showing today's tasks", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();

//                if(showMyTasks){
//                    for(int i = 0; i < onlyTodayTasks.size(); i++){
//                        if(!onlyTodayTasks.get(i).getAssignedStaff().matches(staffLoggedIn.getStaffKey())){
//                            onlyTodayTasks.remove(i);
//                            taskRecyclerView.setAdapter(new TaskAdapter(onlyTodayTasks));
//                        }
//                    }
//                } else {
//                }
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskRecyclerView.setAdapter(new TaskAdapter(pendingAndPrioritised));
                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing next 7 days prioritised tasks that are pending", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        });
//                        toast = Toast.makeText(Homepage.this, "Showing next 7 days prioritised tasks that are pending ", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();

                    } else if (showPendingOnly) {
                        for (Task tsk : onlyNext7Tasks) {
                            if (!tsk.getStatus()) {
                                pendingFT.add(tsk);
                            }
                        }
                        //Show next 7 days pending tasks
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskRecyclerView.setAdapter(new TaskAdapter(pendingFT));
                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing next 7 days tasks that are pending", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        });
//                        toast = Toast.makeText(Homepage.this, "Showing next 7 days tasks that are pending", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();

                    } else if (prioritiseAll) {
                        //Show next 7 days prioritised tasks
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskRecyclerView.setAdapter(new TaskAdapter(justPrioritised));
                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing next 7 days prioritised tasks", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        });
//                        toast = Toast.makeText(Homepage.this, "Showing next 7 days prioritised tasks", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();

                    } else {
                        //Show next 7 days tasks
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                taskRecyclerView.setAdapter(new TaskAdapter(onlyNext7Tasks));
                                Snackbar snackbar = Snackbar
                                        .make(snackbarCoordinatorLayout, "Showing next 7 days tasks", Snackbar.LENGTH_SHORT);

                                snackbar.show();
                            }
                        });
//                        toast = Toast.makeText(Homepage.this, "Showing next 7 days tasks", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            taskRecyclerView.setAdapter(new TaskAdapter(mTaskList));
                            Snackbar snackbar = Snackbar
                                    .make(snackbarCoordinatorLayout, "Showing newest first", Snackbar.LENGTH_LONG);

                            snackbar.show();
                        }
                    });
//                    toast = Toast.makeText(Homepage.this, "Showing newest first", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
                }

//        onlyTodayTasks.clear();
//        onlyNext7Tasks.clear();

//        taskRecyclerView.setAdapter(new TaskAdapter(mTaskList)); //, Task.class
                if (!notFirstLoad) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                    });


                }
                notFirstLoad = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//stuff that updates ui
                        refreshLayout.setRefreshing(false);

                    }
                });
            }
        }).start();

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
//                                        System.out.println(YouEditTextValue);

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();

        if (id == R.id.nav_inbox) {
            startActivity(new Intent(Homepage.this, Inbox.class));
        } else if (id == R.id.nav_newest_first) {
            dateTasks.setText("Newest first");
            allTasks = true;
            showToday = false;
            showNext7 = false;
            showPendingOnly = false;
            prioritiseAll = false;
            setRecyclerAdapterContents();
            navigationView.getMenu().getItem(1).setChecked(true);
        } else if (id == R.id.nav_today) {
            dateTasks.setText("Today");
            showToday = true;
            allTasks = false;
            showNext7 = false;
            showPendingOnly = false;
            prioritiseAll = false;
            setRecyclerAdapterContents();
            navigationView.getMenu().getItem(2).setChecked(true);
        } else if (id == R.id.nav_next7) {
            dateTasks.setText("Next 7 days");
            showNext7 = true;
            allTasks = false;
            showToday = false;
            showPendingOnly = false;
            prioritiseAll = false;
            setRecyclerAdapterContents();
            navigationView.getMenu().getItem(3).setChecked(true);
        } else if (id == R.id.nav_filter) {
            onOptionsItemSelected(filterByMenuItem);
        } else if (id == R.id.nav_properties) {
            if (!mSharedPreferences.getBoolean("showcaseview-ed", false)) {
                drawer.closeDrawer(GravityCompat.START);
                new ShowcaseView.Builder(this)
                        .setStyle(R.style.CustomShowcaseTheme)
                        .setTarget(viewTarget) //new ActionViewTarget(this, ActionViewTarget.Type.HOME
                        .setContentTitle("QR Scan")
                        .setContentText("Find a Flat quickly by scanning its QR code")
                        .hideOnTouchOutside()
                        .build();

                editor.putBoolean("showcaseview-ed", true).apply();
            } else {
                startActivity(new Intent(Homepage.this, AllProperties.class));
            }

        } else if (id == R.id.nav_tenants) {
            startActivity(new Intent(Homepage.this, AllTenants.class));
        } else if (id == R.id.nav_reports) {
            startActivity(new Intent(Homepage.this, AllReports.class));
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(Homepage.this, MapProperty.class));
        } else if (id == R.id.nav_chat) {
            drawer.closeDrawer(GravityCompat.START);
            new AlertDialog.Builder(Homepage.this)
                    .setTitle("Update coming soon")
                    .setMessage("Send and receive messages with our chat service!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

//            startActivity(new Intent(Homepage.this, TutorialActivity.class));
//            startActivity(new Intent(Homepage.this, LoginActivity.class));
        } else if (id == R.id.nav_add_tenant) {
            startActivity(new Intent(Homepage.this, CreateTenant.class));
        } else if (id == R.id.nav_add_flat) {
            startActivity(new Intent(Homepage.this, CreateFlat.class));
        } else if (id == R.id.nav_add_property) {
            startActivity(new Intent(Homepage.this, CreateProperty.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

package com.example.lorenzo.aaflats;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class MyService extends Service {

    private static int uniqueID; // =25
    private NotificationCompat.Builder notificationBuilder;

    public static final String MY_PREFERENCES = "MyPreferences";
    public static final String STAFF_KEY = "StaffKey";
    public static final String EMAIL_KEY = "StaffEmail";
    public static final String PASSWORD_KEY = "StaffPassword";
    private SharedPreferences mSharedPreferences;

    public static final String MY_TASK_NOTIFICATIONS = "MyTaskNotifications";
    private SharedPreferences mTaskNotifications;
    private SharedPreferences.Editor taskEditor;

    public static final String MY_REPORT_NOTIFICATIONS = "MyReportNotifications";
    private SharedPreferences mReportNotifications;
    private SharedPreferences.Editor reportEditor;

    private Notification notif = new Notification();
    private ArrayList<Notification> taskNotifList = new ArrayList<>();
    private ArrayList<Notification> reportNotifList = new ArrayList<>();
    private Task newTask = new Task();
    private Report newReport = new Report();

    Firebase taskRef;
    Firebase reportRef;
    Query getTaskQ;
    Query getReportQ;

    @Override
    public void onCreate() {

        Firebase.setAndroidContext(this);
        //Get Shared Preferences
        mSharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);

        mTaskNotifications = getSharedPreferences(MY_TASK_NOTIFICATIONS, MODE_PRIVATE);
        taskEditor = mTaskNotifications.edit();
        taskEditor.putBoolean("editor_activated", true).commit();

        mReportNotifications = getSharedPreferences(MY_REPORT_NOTIFICATIONS, MODE_PRIVATE);
        reportEditor = mReportNotifications.edit();
        reportEditor.putBoolean("editor_activated", true).commit();

        notificationBuilder = new NotificationCompat.Builder(this);

        taskRef = new Firebase(getResources().getString(R.string.tasks_location));
        reportRef = new Firebase(getResources().getString(R.string.reports_location));

        System.out.println("Started MY SERVICE");

        boolean logout = mSharedPreferences.getBoolean("logout", true);
        String loggedUsn = mSharedPreferences.getString(EMAIL_KEY, "");

        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (!logout) {
            Toast.makeText(MyService.this, "A&A Flats running in background.\nSigned-in: " + mSharedPreferences.getString(EMAIL_KEY, ""),
                    Toast.LENGTH_SHORT).show();

            notificationBuilder.setSmallIcon(R.drawable.notification_icon);//notification_icon
            notificationBuilder.setTicker("A&A Flats still running");
            notificationBuilder.setWhen(System.currentTimeMillis());
            notificationBuilder.setContentTitle("A&A Flats running in background");
            notificationBuilder.setContentText(loggedUsn); //newTask.getTitle()
            uniqueID = 23;

            Intent intent = new Intent(this, SplashActivity.class);//.putExtra("parceable_task", newTask);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pIntent);
            mgr.notify(uniqueID, notificationBuilder.build());

            enableNotificationListener();
        } else {
            mgr.cancel(23);
            Toast.makeText(MyService.this, "notif removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableNotificationListener() {

//        Toast.makeText(MyService.this, "enableNotificationListener running", Toast.LENGTH_SHORT).show();
        final Firebase notifRef = new Firebase(getResources().getString(R.string.notifications_location));
//        Query getUnseenNotifs = notifRef.orderByChild("seen").equalTo(false);
//        Query getTaskNotifs = notifRef.orderByChild("type").equalTo("Task");
        notifRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskNotifList.clear();
                reportNotifList.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    notif = childSnap.getValue(Notification.class);

                    if(notif.getType().matches("Task")){
                        taskNotifList.add(notif);
                    } else if(notif.getType().matches("Report")){
                        reportNotifList.add(notif);
                    }
                }
                if (!taskNotifList.isEmpty()) { //!TextUtils.isEmpty(notif.getObjectID())
//                    getTaskDetails(notif.getObjectID());
                    for (Notification nt : taskNotifList) {
                        getTaskDetails(nt.getObjectID());
                    }
                }
                if(!reportNotifList.isEmpty()){
                    for (Notification nt : reportNotifList) {
                        getReportDetails(nt.getObjectID());
                    }
                }

//                for(Notification nt : taskNotifList){
//                    getTaskDetails(nt.getObjectID());
//                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
//        notifRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
//                    Notification notif = childSnap.getValue(Notification.class);
//                }
//                getTaskDetails();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });

    }

    private void getReportDetails(String oID) {

        final Map<String, ?> notifiedReports = mReportNotifications.getAll();
        final ArrayList<String> rr = new ArrayList<>();
        for(Map.Entry<String, ?> rEntry : notifiedReports.entrySet()){
            rr.add(rEntry.getValue().toString());
        }

        getReportQ = reportRef.orderByKey().equalTo(oID);
        getReportQ.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean notified = false;

                for (DataSnapshot childsnap : dataSnapshot.getChildren()) {
                    newReport = childsnap.getValue(Report.class);
                    newReport.setReportKey(childsnap.getKey());
                    for (String rEntry : rr) {
                        if (newReport.getReportKey().matches(rEntry)) {
                            notified = true;
                            break;
                        }
                    }
                    if (!notified) {
                        displayReportNotification(newReport);
                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void displayReportNotification(final Report newReport) {
        final Context c = this;
        new Thread(new Runnable() {
            public void run() {
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setSmallIcon(R.drawable.ic_send_white_48dp);//notification_icon
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon3);
                notificationBuilder.setLargeIcon(bm);
                notificationBuilder.setTicker("New report received by " + newReport.getSender());
                notificationBuilder.setWhen(System.currentTimeMillis());
                notificationBuilder.setContentTitle("New " + newReport.getType());
                notificationBuilder.setContentText("From " + newReport.getSender()); //newTask.getTitle()

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder.setSound(alarmSound);//file:///sdcard/notification/notification.mp3
                //Uri.parse("android.resource://" + getPackageName() + "ringtone/my_notif.mp3"
                notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); //delay, vibrate, sleep, vibrate, sleep

                notificationBuilder.setLights(Color.BLUE, 3000, 3000);


                Random randomGen = new Random();
                uniqueID = randomGen.nextInt(20000 - 10001 + 1) + 10001;

//                taskEditor.putString(Integer.toString(uniqueID), Integer.toString(uniqueID));
//                taskEditor.commit();

                NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Intent intent = new Intent(c, ReportDetails.class).putExtra("parceable_report", newReport);//.putExtra("parceable_task", newTask);
                PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(pIntent);
                mgr.notify(uniqueID, notificationBuilder.build());

                reportEditor.putString(newReport.getReportKey(), newReport.getReportKey()).commit();

            }
        }).start();
    }

    private void getTaskDetails(String tID) {

        final Map<String, ?> notifiedTasks = mTaskNotifications.getAll();
        final ArrayList<String> tt = new ArrayList<>();
        for (Map.Entry<String, ?> tEntry : notifiedTasks.entrySet()) {
            tt.add(tEntry.getValue().toString());
        }

//        Toast.makeText(MyService.this, "getTaskDetails running - " + notif.getObjectID(), Toast.LENGTH_SHORT).show();

//        Firebase taskRef = new Firebase(getResources().getString(R.string.tasks_location));//"https://aaflats.firebaseio.com/tasks"
//        Query getTaskQ = taskRef.orderByKey().equalTo("-KG3rOIDSrUmo-8XjmGp"); //notif.getObjectID()
        getTaskQ = taskRef.orderByKey().equalTo(tID);
        getTaskQ.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean notified = false;

                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    newTask = childSnap.getValue(Task.class);
                    newTask.setTaskKey(childSnap.getKey());
                    if (newTask.getAssignedStaff().matches(mSharedPreferences.getString(STAFF_KEY, ""))) {
//                        Toast.makeText(MyService.this, "Tasks for " + mSharedPreferences.getString(STAFF_KEY, ""),
//                                Toast.LENGTH_SHORT).show();

                        for (String tEntry : tt) {
                            if (newTask.getTaskKey().matches(tEntry)) {
                                notified = true;
                                break;
                            }
//                            else {
//                                notified = false;
//                                break;
//                            }
//                            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                        }

                        if (!notified) {
                            displayTaskNotification(newTask);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void displayTaskNotification(final Task tsk) {

//        Toast.makeText(MyService.this, "displayTaskNotification running - " + newTask.getTitle(), Toast.LENGTH_SHORT).show();

        final Context c = this;
        new Thread(new Runnable() {
            public void run() {
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setSmallIcon(R.drawable.ic_build_plus_48dp);//notification_icon
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon3);
                notificationBuilder.setLargeIcon(bm);
                notificationBuilder.setTicker("New task added by " + tsk.getCreator());
                notificationBuilder.setWhen(System.currentTimeMillis());
                notificationBuilder.setContentTitle(tsk.getCreator() + " added a new task");
                notificationBuilder.setContentText(tsk.getTitle()); //newTask.getTitle()

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder.setSound(alarmSound);//file:///sdcard/notification/notification.mp3
                //Uri.parse("android.resource://" + getPackageName() + "ringtone/my_notif.mp3"
                notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); //delay, vibrate, sleep, vibrate, sleep
                if (tsk.getPriority().matches("High")) {
                    notificationBuilder.setLights(Color.RED, 3000, 3000);
                } else if (tsk.getPriority().matches("Medium")) {
                    notificationBuilder.setLights(Color.YELLOW, 3000, 3000);
                } else {
                    notificationBuilder.setLights(Color.GREEN, 3000, 3000);
                }


                Random randomGen = new Random();
                uniqueID = randomGen.nextInt(10000 - 1 + 1) + 1;

//                taskEditor.putString(Integer.toString(uniqueID), Integer.toString(uniqueID));
//                taskEditor.commit();

                NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Intent intent = new Intent(c, TaskDetails.class).putExtra("parceable_task", tsk);//.putExtra("parceable_task", newTask);
                PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(pIntent);
                mgr.notify(uniqueID, notificationBuilder.build());

                taskEditor.putString(tsk.getTaskKey(), tsk.getTaskKey()).commit();

            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

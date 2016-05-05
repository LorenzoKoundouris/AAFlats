package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private ArrayList<Staff> staffList = new ArrayList<>();
    SharedPreferences mpr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mpr = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor pre = mpr.edit();
//        pre.clear().apply();
        Bundle intent = getIntent().getExtras();

        super.onCreate(savedInstanceState);
        if(intent==null && !mpr.getBoolean("tutorial_viewed", false)){
            startActivity(new Intent(SplashActivity.this, TutorialActivity.class));
            finish();
        } else {
            pre.putBoolean("tutorial_viewed", true).apply();
            Firebase.setAndroidContext(this);
            readyGo();
        }


//        SharedPreferences mnt = getSharedPreferences("MyNotifications", MODE_PRIVATE);
//        SharedPreferences.Editor nte = mnt.edit();
//        nte.clear().apply();
//

//        final Map<String, ?> ntfs = mnt.getAll();
//        final ArrayList<String> nt = new ArrayList<>();
//        for (Map.Entry<String, ?> tEntry : ntfs.entrySet()) {
//            nt.add(tEntry.getValue().toString());
//        }




//        System.out.println("Notifications: \t" + nt.toString());


    }


    public void readyGo(){

        final Map<String, ?> prefs = mpr.getAll();
        final ArrayList<String> pr = new ArrayList<>();
        for (Map.Entry<String, ?> tEntry : prefs.entrySet()) {
            pr.add(tEntry.getValue().toString());
        }
        System.out.println("MyPreferences: \t" + pr.toString());

        Intent startServiceIntent = new Intent(this, MyService.class);
        startServiceIntent.setAction("com.example.lorenzo.aaflats.action.startforeground");
        this.startService(startServiceIntent);

        final Firebase staffRef = new Firebase(getResources().getString(R.string.staff_location));
//        Query verifyCredentials = staffRef.orderByChild("username").equalTo(mEmailView);
        staffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Staff stf = childSnap.getValue(Staff.class);
                    stf.setStaffKey(childSnap.getKey());
                    staffList.add(stf);
                }
                startActivity(new Intent(SplashActivity.this, LoginActivity.class).putExtra("parceable_staff_list", staffList));
                overridePendingTransition(R.anim.login_animation, R.anim.splash_animation);
                finish();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}

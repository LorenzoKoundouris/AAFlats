package com.example.lorenzo.aaflats;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScanQR extends AppCompatActivity implements ZBarScannerView.ResultHandler{

    private ZBarScannerView mScannerView;
    ArrayList<Flat> flatList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);
        setContentView(mScannerView);
//        setContentView(R.layout.activity_scan_qr);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
        flatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnap : dataSnapshot.getChildren() ){
                    Flat flt = childSnap.getValue(Flat.class);
                    flatList.add(flt);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        // Do something with the result here
//        Log.v(TAG, result.getContents()); // Prints scan results
//        Log.v(TAG, result.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

//        Toast.makeText(ScanQR.this, "Res.getCon: " + result.getContents(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(ScanQR.this, "Res.getBarFor: " + result.getBarcodeFormat(), Toast.LENGTH_SHORT).show();

        boolean foundFlat = false;

        for(int i = 0; i<flatList.size(); i++){
            if((flatList.get(i).getAddressLine1() + " - "+ flatList.get(i).getFlatNum())
                    .matches(result.getContents())){
                Flat tempFlat = flatList.get(i);
                startActivity(new Intent(ScanQR.this, FlatDetails.class).putExtra("parceable_flat", tempFlat));
                foundFlat = true;
            }
        }

        if(!foundFlat){
            Toast.makeText(ScanQR.this, "No flats found", Toast.LENGTH_SHORT).show();
        }

        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }
}

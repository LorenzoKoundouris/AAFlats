package com.example.lorenzo.aaflats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScanQR extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;
    private ArrayList<Flat> flatList = new ArrayList<>();
    private ArrayList<Flat> occupiedFlatList = new ArrayList<>();

    private boolean fromHome;
    private int attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);
        setContentView(mScannerView);
//        setContentView(R.layout.activity_scan_qr);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle intent = getIntent().getExtras();
        fromHome = intent.getBoolean("fromHome");

        Firebase flatRef = new Firebase(getResources().getString(R.string.flats_location));
        flatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Flat flt = childSnap.getValue(Flat.class);
                    flatList.add(flt);
                    if (!flt.getTenant().matches("")) {
                        occupiedFlatList.add(flt);
                    }
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

//        mScannerView.stopCamera();

        boolean foundFlat = false;
        Flat tempF;
        String tempS;
        if(fromHome){
            for (int i = 0; i < flatList.size(); i++) {
                tempF = flatList.get(i);
                tempS = tempF.getAddressLine1() + " - " + tempF.getFlatNum();
                if (tempS.matches(result.getContents()) && fromHome) {
                    mScannerView.stopCamera();
                    startActivity(new Intent(ScanQR.this, FlatDetails.class).putExtra("parceable_flat", tempF));
                    foundFlat = true;
                    break;
                }
            }
        } else {
            for (int j = 0; j < occupiedFlatList.size(); j++) {
                tempF = occupiedFlatList.get(j);
                tempS = tempF.getAddressLine1() + " - " + tempF.getFlatNum();
                if (tempS.matches(result.getContents())) {
                    mScannerView.stopCamera();
                    startActivity(new Intent(ScanQR.this, TenantHomepage.class).putExtra("parceable_flat", tempF));
                    foundFlat = true;
                    break;
                }
            }
        }


        if (!foundFlat) {
            attempts++;
            if (attempts > 3) {
                mScannerView.stopCamera();
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("There seems to be a problem with this QR. Would you like to try again?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                attempts = 0;
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (fromHome) {
                                    startActivity(new Intent(ScanQR.this, Homepage.class));
                                } else {
                                    startActivity(new Intent(ScanQR.this, LoginActivity.class));
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        }

        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onBackPressed() {

        if (fromHome) {
            startActivity(new Intent(ScanQR.this, Homepage.class));
            finish();
        } else {
            startActivity(new Intent(ScanQR.this, LoginActivity.class));
            finish();
        }
    }
}

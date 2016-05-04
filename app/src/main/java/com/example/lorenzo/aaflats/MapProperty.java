package com.example.lorenzo.aaflats;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapProperty extends FragmentActivity implements OnMapReadyCallback {

    private ArrayList<Marker> markers = new ArrayList<>();
    private GoogleMap mMap;
    private Bundle intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_property);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        intent = getIntent().getExtras();
//        final ArrayList<String> locaddr = new ArrayList<>();

        mMap = googleMap;

//        new Thread(new Runnable() {
//            public void run() {
        if (intent != null) {
//                    locaddr.add(intent.getString("findProperty"));
            showMarker(intent.getString("findProperty"));//locaddr.get(0)
            oneScreenToFitAll();
        } else {
            Firebase propertyRef = new Firebase(getResources().getString(R.string.properties_location));
            propertyRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    markers.clear();
                    for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                        Property prt = childSnap.getValue(Property.class);
//                        locaddr.add(prt.getAddrline1() + ", Plymouth, UK");
                        showMarker(prt.getAddrline1());
                    }
                    oneScreenToFitAll();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
//            }
//        }).start();


//        for (int i = 0; i < locaddr.size(); i++) {
//            mMap = googleMap;
//            List<Address> addressList = null;
////            ArrayList<Address> addressList = new ArrayList<Address>();
//            Geocoder geocoder = new Geocoder(getApplicationContext());
//            Address resultAddr;
//
//            try {
//                addressList = geocoder.getFromLocationName(locaddr.get(i), 1);//addressList = geocoder..
//                resultAddr = addressList.get(0);//i
//                LatLng latLang = new LatLng(resultAddr.getLatitude(), resultAddr.getLongitude());
//                MarkerOptions myMarker = new MarkerOptions().position(latLang).title(locaddr.get(i));
//                myMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_house_icon));
//                mMap.addMarker(myMarker).showInfoWindow();
//
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLang));
//                float zoomLevel = (float) 16.0; //This goes up to 21
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, zoomLevel));
//            } catch (IOException e) {
//                Toast.makeText(MapProperty.this, "An error occurred #23", Toast.LENGTH_SHORT).show();
//            }
//
//        }


//        // Add a marker in Sydney and move the camera
//        //LatLng trematonMarker = new LatLng(50.382281, -4.135601);
//        mMap.addMarker(new MarkerOptions().position(trematonMarker).title("Marker in Mutley"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLng(trematonMarker));
//        float zoomLevel = (float) 16.0; //This goes up to 21
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(trematonMarker, zoomLevel));
    }

    private void oneScreenToFitAll() {
        if (intent == null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50); //2nd parameter is pixels of padding from edges of map
            mMap.animateCamera(cu);
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(markers.get(0).getPosition()));
            float zoomLevel = (float) 16.0; //This goes up to 21
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), zoomLevel));
        }
    }

    private void showMarker(final String prtAddr) {

        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(prtAddr + ", Plymouth, UK", 1);
            if (addressList == null) {
                return;
            }
            Address resultAddr = addressList.get(0);
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(resultAddr.getLatitude(), resultAddr.getLongitude())).title(prtAddr).icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_house_icon)));

            marker.showInfoWindow();
            markers.add(marker);
        } catch (Exception ex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MapProperty.this, "An error occurred with: " + prtAddr, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}

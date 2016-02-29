package com.example.lorenzo.aaflats;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapProperty extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

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
    public void onMapReady(GoogleMap googleMap) {
        Bundle intent = getIntent().getExtras();
        String locaddr = intent.getString("findProperty");
        String locaddrPlym = locaddr  + ", plymouth";

        mMap = googleMap;
        List<Address> addressList= null;
        Geocoder geocoder = new Geocoder(this);

        try{
            addressList = geocoder.getFromLocationName(locaddrPlym, 1);
        } catch(IOException e){
            e.printStackTrace();
        }

        Address resultAddr = addressList.get(0);
        LatLng latLang = new LatLng(resultAddr.getLatitude(), resultAddr.getLongitude());
        MarkerOptions myMarker = new MarkerOptions().position(latLang).title(locaddr);
        myMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_house_icon));
        mMap.addMarker(myMarker);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLang));
        float zoomLevel = (float) 16.0; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, zoomLevel));
//        // Add a marker in Sydney and move the camera
//        //LatLng trematonMarker = new LatLng(50.382281, -4.135601);
//        mMap.addMarker(new MarkerOptions().position(trematonMarker).title("Marker in Mutley"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLng(trematonMarker));
//        float zoomLevel = (float) 16.0; //This goes up to 21
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(trematonMarker, zoomLevel));
    }
}

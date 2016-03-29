package com.example.lorenzo.aaflats;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TenantDetails extends AppCompatActivity {

    Tenant parceableTenant;

    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> contractDetailsCollection;
    ExpandableListView expListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle intent = getIntent().getExtras();
        parceableTenant = intent.getParcelable("parceable_tenant");

        createGroupList();

        createCollection();

        expListView = (ExpandableListView) findViewById(R.id.laptop_list);
        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
                this, groupList, contractDetailsCollection);
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final String selected = (String) expListAdapter.getChild(
                        groupPosition, childPosition);
                Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        });

    } //End of OnCreate

    private void createGroupList() {
        groupList = new ArrayList<String>();
        groupList.add(" Contract");
    }

    private void createCollection() {
        // preparing contract details collection(child)

        String[] contractDetails = {"Start date: ", "End date: ", "Download contract"};

        contractDetailsCollection = new LinkedHashMap<String, List<String>>();


        for (String conDet : groupList) {
            loadChild(contractDetails);
            contractDetailsCollection.put(conDet, childList);
        }
    }

    private void loadChild(String[] laptopModels) {
        childList = new ArrayList<String>();
        for (String model : laptopModels)
            childList.add(model);
    }
}

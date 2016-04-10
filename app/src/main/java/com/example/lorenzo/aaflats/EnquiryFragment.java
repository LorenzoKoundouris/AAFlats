package com.example.lorenzo.aaflats;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Lorenzo on 03/04/2016.
 */
public class EnquiryFragment extends Fragment {

    ArrayList<Report> enquiryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View v = inflater.inflate(R.layout.enquiry_fragment, null);

        //RecyclerCiew
        final RecyclerView enquiryRV = (RecyclerView) v.findViewById(R.id.inboxEnquiryRecyclerView);
        enquiryRV.setLayoutManager(new LinearLayoutManager(v.getContext()));

        Firebase reportRef = new Firebase(getResources().getString(R.string.reports_location));
        Query onlyEnquiries = reportRef.orderByChild("type").equalTo("Enquiry");
        onlyEnquiries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report rpt = childSnap.getValue(Report.class);
                    enquiryList.add(rpt);
                }

                Collections.sort(enquiryList, new Comparator<Report>() {
                    @Override
                    public int compare(Report lhs, Report rhs) {
                        return lhs.getTimestamp().compareTo(rhs.getTimestamp());
                    }
                });

                enquiryRV.setAdapter(new ReportAdapter(enquiryList));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        return v;
    }

    //Set title for fragment
    @Override
    public String toString(){
        return "Enquiry";
    }

}

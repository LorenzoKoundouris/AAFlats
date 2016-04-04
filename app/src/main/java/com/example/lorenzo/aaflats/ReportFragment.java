package com.example.lorenzo.aaflats;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
public class ReportFragment extends Fragment {

    ArrayList<Report> reportList = new ArrayList<>();
    ArrayList<String> shorterContents = new ArrayList<>();
    public RecyclerView reportRV;
    @Nullable
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View v = inflater.inflate(R.layout.report_fragment, null);
        
        //RecyclerCiew
        reportRV = (RecyclerView) v.findViewById(R.id.inboxReportRecyclerView);
        reportRV.setLayoutManager(new LinearLayoutManager(v.getContext()));

        Firebase reportRef = new Firebase(getResources().getString(R.string.reports_location));
        Query onlyReports = reportRef.orderByChild("type").equalTo("Report");
        onlyReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report rpt = childSnap.getValue(Report.class);
                    reportList.add(rpt);
                }

                Collections.sort(reportList, new Comparator<Report>() {
                    @Override
                    public int compare(Report lhs, Report rhs) {
                        return lhs.getTimestamp().compareTo(rhs.getTimestamp());
                    }
                });

                for (int i = 0; i < reportList.size(); i++) {
                    if (reportList.get(i).getContent().length() > 23) {
                        shorterContents.add(reportList.get(i).getContent().substring(0, 20) + "...");
                    } else {
                        shorterContents.add(reportList.get(i).getContent());
                    }
                }

                reportRV.setAdapter(new ReportAdapter(reportList, shorterContents));
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
        return "Report";
    }
}

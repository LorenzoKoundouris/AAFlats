package com.example.lorenzo.aaflats;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AllReports extends AppCompatActivity {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView reportRecyclerView;
    private boolean notFirstLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reports);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //ToDo: go to Inbox
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("All reports");

        final ArrayList<Report> reportList = new ArrayList<>();
        final ArrayList<String> shorterContents = new ArrayList<>();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_allreports);
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_1);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRecyclerAdapterContents(reportList, shorterContents);
            }
        });


        Firebase reportRef = new Firebase(getResources().getString(R.string.reports_location));
        Query reportsOnly = reportRef.orderByChild("type").equalTo("report");
        reportsOnly.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reportList.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Report rpt = childSnap.getValue(Report.class);
                    rpt.setReportKey(childSnap.getKey());
                    reportList.add(rpt);
                }

                Collections.sort(reportList, new Comparator<Report>() {
                    @Override
                    public int compare(Report lhs, Report rhs) {
                        return lhs.getTimestamp().compareTo(rhs.getTimestamp());
                    }
                });

                for (int i = 0; i < reportList.size(); i++) {
                    if(reportList.get(i).getContent().length() > 23){
                        shorterContents.add(reportList.get(i).getContent().substring(0, 20) + "...");
                    } else {
                        shorterContents.add(reportList.get(i).getContent());
                    }
                }

                setRecyclerAdapterContents(reportList, shorterContents);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        setupRecyclerview();
    }//End of onCreate


    private void setupRecyclerview(){
        reportRecyclerView = (RecyclerView) findViewById(R.id.reports_recycler_view);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    private void setRecyclerAdapterContents(ArrayList<Report> reportList, ArrayList<String> shorterContents){
        reportRecyclerView.setAdapter(new ReportAdapter(reportList, shorterContents));
        if(!notFirstLoad){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 2s = 2000ms
                    reportRecyclerView.setVisibility(View.VISIBLE);
                    ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_allreports);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 0000);
        }
        notFirstLoad = true;
        refreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Toast.makeText(getApplicationContext(), "Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
        }
        return true;
    }

}

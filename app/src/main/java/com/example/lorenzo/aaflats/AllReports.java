package com.example.lorenzo.aaflats;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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
    private ArrayList<Report> searchQuery = new ArrayList<>();

    private ArrayList<Report> reportList = new ArrayList<>();

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
                startActivity(new Intent(AllReports.this, Inbox.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("All reports");


        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_allreports);
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_1);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 2s = 2000ms
                        setRecyclerAdapterContents(reportList);
                    }
                }, 2000);
            }
        });


        Firebase reportRef = new Firebase(getResources().getString(R.string.reports_location));
        Query reportsOnly = reportRef.orderByChild("type").equalTo("Report");
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


                setRecyclerAdapterContents(reportList);
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


    private void setRecyclerAdapterContents(ArrayList<Report> reportList){
        reportRecyclerView.setAdapter(new ReportAdapter(reportList));
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
            }, 2000);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Perform final search
                searchQuery.clear();
                boolean exactMatch = false;
                for (int i = 0; i < reportList.size(); i++) {
                    if (query.matches(reportList.get(i).getReportKey())) {
                        exactMatch = true;
                        startActivity(new Intent(AllReports.this, ReportDetails.class)
                                .putExtra("parceable_report", reportList.get(i)));
                        break;
                    } else if (reportList.get(i).getReportKey().contains(query) || reportList.get(i).getProperty().toLowerCase().contains(query.toLowerCase()) ||
                            reportList.get(i).getSender().toLowerCase().contains(query.toLowerCase())
                            || reportList.get(i).getContent().toLowerCase().contains(query.toLowerCase())) {
                        searchQuery.add(reportList.get(i));
                    }
                }
                if (!exactMatch) {
                    setRecyclerAdapterContents(searchQuery);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Text has changed, apply filtering
                loadResults(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void loadResults(String newText) {
        searchQuery.clear();
        for (int i = 0; i < reportList.size(); i++) {
            if (reportList.get(i).getReportKey().contains(newText) || reportList.get(i).getProperty().toLowerCase().contains(newText.toLowerCase()) ||
                    reportList.get(i).getSender().toLowerCase().contains(newText.toLowerCase()) ||
                    reportList.get(i).getContent().toLowerCase().contains(newText.toLowerCase())) {
                searchQuery.add(reportList.get(i));
            }
        }
        setRecyclerAdapterContents(searchQuery);
    }



}

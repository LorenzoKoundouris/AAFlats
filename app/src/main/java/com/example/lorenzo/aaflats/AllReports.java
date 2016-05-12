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

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivity(new Intent(AllReports.this, Inbox.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("All reports");

        // Initialise manual refresh component
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
                        // Delay filling recycler-view to demo manual refresh for 2s = 2000ms
                        setRecyclerAdapterContents(reportList);
                    }
                }, 2000);
            }
        });

        //Get report-type tenant correspondence only
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
                        return getTMDate(rhs.getTimestamp()).compareTo(getTMDate(lhs.getTimestamp()));
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

    /**
     * Convert timestamp attribute to Date object format
     * ddMMyyyyHHmmss -> dd/MM/yyyy
     * @param timestamp is an attribute of Report object signifying
     * millisecond moment it was sent by tenant
     * @return Date variable
     */
    private Date getTMDate(String timestamp) {
        DateFormat dFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH);
        Date tsDate = null;//timestamp.getTime()
        try {
            tsDate = dFormat.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tsDate;
    }

    /**
     * Initialise recycler-view
     */
    private void setupRecyclerview() {
        reportRecyclerView = (RecyclerView) findViewById(R.id.reports_recycler_view);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Load recycler-view with the extracted list of Reports
     *
     * @param reportList list of Report objects extracted from Firebase
     */
    private void setRecyclerAdapterContents(ArrayList<Report> reportList) {
        reportRecyclerView.setAdapter(new ReportAdapter(reportList));
        if (!notFirstLoad) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // During first app launch, demo refresh animation
                    reportRecyclerView.setVisibility(View.VISIBLE);
                    ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_allreports);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }
        notFirstLoad = true;
        refreshLayout.setRefreshing(false);
    }

    /**
     * Handle menu item selection
     * @param item is a menu item in toolbar menu that has been selected by user
     * @return true
     */
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

    /**
     * Search through Reports by report-key or sender or content
     * @param menu inflated toolbar menu
     * @return inflated menu
     */
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
                //Query reports
                searchQuery.clear();
                boolean exactMatch = false;
                for (int i = 0; i < reportList.size(); i++) {
                    if (query.trim().matches(reportList.get(i).getReportKey())) {
                        exactMatch = true;
                        startActivity(new Intent(AllReports.this, ReportDetails.class)
                                .putExtra("parceable_report", reportList.get(i)));
                        break;
                    } else if (reportList.get(i).getReportKey().contains(query.trim()) || reportList.get(i).getProperty().toLowerCase().contains(query.toLowerCase().trim()) ||
                            reportList.get(i).getSender().toLowerCase().contains(query.toLowerCase())
                            || reportList.get(i).getContent().toLowerCase().contains(query.toLowerCase().trim())) {
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

    /**
     * Re-populate the recycler-view with the search results only
     * @param newText is the search query entered by the user
     */
    private void loadResults(String newText) {
        searchQuery.clear();
        for (int i = 0; i < reportList.size(); i++) {
            if (reportList.get(i).getReportKey().contains(newText.trim()) ||
                    reportList.get(i).getProperty().toLowerCase().contains(newText.toLowerCase().trim()) ||
                    reportList.get(i).getSender().toLowerCase().contains(newText.toLowerCase().trim()) ||
                    reportList.get(i).getContent().toLowerCase().contains(newText.toLowerCase().trim())) {
                searchQuery.add(reportList.get(i));
            }
        }
        setRecyclerAdapterContents(searchQuery);
    }


}

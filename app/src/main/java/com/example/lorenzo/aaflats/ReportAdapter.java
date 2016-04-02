package com.example.lorenzo.aaflats;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 02/04/2016.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportViewHolder> {

    public static Report mReport = new Report();
    private ArrayList<Report> reportList;
    private ArrayList<String> shorterContents;

    public ReportAdapter(ArrayList<Report> reportList, ArrayList<String> shorterContents){
        this.reportList = reportList;
        this.shorterContents = shorterContents;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View reportView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.report_item, viewGroup, false);
        return new ReportViewHolder(reportView, reportList);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder reportViewHolder, int position) {
        mReport = reportList.get(position);
        reportViewHolder.reportContent.setText(shorterContents.get(position));
        reportViewHolder.reportSender.setText(mReport.getSender());
        StringBuilder ts = new StringBuilder(mReport.getTimestamp());
        ts.insert(2, "/");
        ts.insert(5, "/");
        ts.insert(10, " ");
        ts.insert(13, ":");
        ts.insert(16, ":");
        reportViewHolder.reportTimestamp.setText(ts);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }
}

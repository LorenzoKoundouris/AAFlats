package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 02/04/2016.
 */
public class ReportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected CardView cardView;
    protected TextView reportContent;
    protected TextView reportSender;
    protected TextView reportTimestamp;
    protected ArrayList<Report> reportList;


    public ReportViewHolder(View itemView, ArrayList<Report> reportList) {
        super(itemView);
        cardView = (CardView) itemView.findViewById(R.id.report_card_view);
        reportContent = (TextView) itemView.findViewById(R.id.report_content);
        reportSender = (TextView) itemView.findViewById(R.id.report_sender);
        reportTimestamp = (TextView) itemView.findViewById(R.id.report_timestamp);

        this.reportList = reportList;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Report pReport = reportList.get(getAdapterPosition());
        Intent intent = new Intent(v.getContext(), ReportDetails.class);
        intent.putExtra("parceable_report", pReport);
        v.getContext().startActivity(intent);
    }
}

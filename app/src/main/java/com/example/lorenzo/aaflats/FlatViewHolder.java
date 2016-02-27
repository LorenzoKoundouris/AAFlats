package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 25/02/2016.
 */
public class FlatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    //    protected TextView flatAddrline1;
    protected TextView flatTenant;
    //    protected TextView flatNotes;
//    protected TextView flatPostcode;
//    protected TextView flatPendingTask;
    protected ArrayList<Flat> flatList;
    protected ArrayList<String> flatKeys;
    protected ArrayList<String> flatNums;

    public FlatViewHolder(View flatView, ArrayList<Flat> flatList, ArrayList<String> flatKeys, ArrayList<String> flatNums) {
        super(flatView);
        flatTenant = (TextView) flatView.findViewById(R.id.flat_tenant_textview);
        this.flatList = flatList;
        this.flatKeys = flatKeys;
        this.flatNums = flatNums;
        flatView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Flat pFlat = flatList.get(getAdapterPosition());
        String pFlatKey = flatKeys.get(getAdapterPosition());
        Intent intent = new Intent(v.getContext(), FlatDetails.class);
        intent.putExtra("parceable_flat", pFlat);
        intent.putExtra("parceable_flat_key", pFlatKey);
        v.getContext().startActivity(intent);
    }
}

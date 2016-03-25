package com.example.lorenzo.aaflats;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 25/02/2016.
 */
public class FlatAdapter extends RecyclerView.Adapter<FlatViewHolder>{
    public static Flat mFlat = new Flat();
    private ArrayList<Flat> flatList = new ArrayList<>();
    private ArrayList<String> flatKeys = new ArrayList<>();
    private ArrayList<String> flatNums = new ArrayList<>();
    private Property parceableProperty;
    private String parceablePropertyKey;

    public FlatAdapter(ArrayList<Flat> flatList, ArrayList<String> flatKeys, ArrayList<String> flatNums, Property parceableProperty, String parceablePropertyKey) {
        this.flatList = flatList;
        this.flatNums = flatNums;
        this.flatKeys = flatKeys;
        this.parceableProperty = parceableProperty;
        this.parceablePropertyKey = parceablePropertyKey;
    }

    @Override
    public FlatViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View flatView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flat_item, viewGroup, false);
        return new FlatViewHolder(flatView, flatList, flatKeys, flatNums, parceableProperty, parceablePropertyKey);
    }

    @Override
    public void onBindViewHolder(FlatViewHolder flatViewHolder, int position) {
        mFlat = flatList.get(position);
//        flatViewHolder.flatTenant.setText(flatNums.get(position) + " - " + mFlat.getTenant());
        flatViewHolder.flatTenant.setText(flatList.get(position).getFlatNum() + " - " + mFlat.getTenant());
    }

    @Override
    public int getItemCount() {
        return flatList.size();
    }
}

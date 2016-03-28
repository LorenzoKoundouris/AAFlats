package com.example.lorenzo.aaflats;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 23/02/2016.
 */
public class PropertyAdapter extends RecyclerView.Adapter<PropertyViewHolder>{

    public static Property mProperty = new Property();
    private ArrayList<Property> propertyList;


    public PropertyAdapter(ArrayList<Property> propertyList){
        this.propertyList = propertyList;
    }

    @Override
    public PropertyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View propertyView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.property_item, viewGroup, false);
        return new PropertyViewHolder(propertyView, propertyList);
    }

    @Override
    public void onBindViewHolder(PropertyViewHolder propertyViewHolder, int position) {
        mProperty = propertyList.get(position);
        propertyViewHolder.propertyAddrline1.setText(mProperty.getAddrline1());
        propertyViewHolder.propertyPostcode.setText(mProperty.getPostcode().toUpperCase());

        String notesLengthCheck = mProperty.getNotes();
        if (notesLengthCheck.length() > 23) {
            notesLengthCheck = notesLengthCheck.substring(0, 23);
            propertyViewHolder.propertyNotes.setText(notesLengthCheck + "..");
        } else {
            propertyViewHolder.propertyNotes.setText(mProperty.getNotes());
        }
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }
}

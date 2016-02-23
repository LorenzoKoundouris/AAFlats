package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 23/02/2016.
 */
public class PropertyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    protected CardView cardView;
    protected TextView propertyAddrline1;
    protected TextView propertyFlats;
    protected TextView propertyPostcode;
    protected ArrayList<Property> propertyList;
    private ArrayList<String> propertyKeys;

    public PropertyViewHolder(View itemView, ArrayList<Property> propertyList, ArrayList<String> propertyKeys) {
        super(itemView);
        cardView = (CardView) itemView.findViewById(R.id.property_card_view);
        propertyAddrline1 = (TextView) itemView.findViewById(R.id.property_addrline1);
        propertyPostcode = (TextView) itemView.findViewById(R.id.property_postcode);
        propertyFlats = (TextView) itemView.findViewById(R.id.property_flats);

        this.propertyList = propertyList;
        this.propertyKeys = propertyKeys;
    }

    @Override
    public void onClick(View v)
    {
        Property pProperty = propertyList.get(getAdapterPosition());
        String pPropertyKey = propertyKeys.get(getAdapterPosition());
        Intent intent = new Intent(v.getContext(), PropertyDetails.class);
        intent.putExtra("parceable_property", pProperty);
        intent.putExtra("parceable_property_key", pPropertyKey);
        v.getContext().startActivity(intent);


    }
}

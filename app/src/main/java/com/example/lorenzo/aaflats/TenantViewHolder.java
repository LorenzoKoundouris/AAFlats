package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 27/03/2016.
 */
public class TenantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected CardView cardView;
    protected TextView tenantForename;
    protected TextView tenantMiddlename;
    protected TextView tenantSurname;
    protected TextView tenantAddress;
    protected Button tenantContact;
    protected ArrayList<Tenant> tenantList;

    public TenantViewHolder(View tenantView, ArrayList<Tenant> tenantList) {
        super(tenantView);

        cardView = (CardView) tenantView.findViewById(R.id.tenant_card_view);
        tenantForename = (TextView) tenantView.findViewById(R.id.tenant_forename);
        tenantMiddlename = (TextView) tenantView.findViewById(R.id.tenant_middlename);
        tenantSurname = (TextView) tenantView.findViewById(R.id.tenant_surname);
        tenantAddress = (TextView) tenantView.findViewById(R.id.tenant_address);
        tenantContact = (Button) tenantView.findViewById(R.id.contact_button);

        this.tenantList = tenantList;
        tenantView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        Tenant tTenant = tenantList.get(getAdapterPosition());
        Intent intent = new Intent(v.getContext(), TenantDetails.class);
        intent.putExtra("parceable_tenant", tTenant);
        v.getContext().startActivity(intent);
        Toast.makeText(v.getContext(), tTenant.getSurname(), Toast.LENGTH_SHORT).show();
    }
}

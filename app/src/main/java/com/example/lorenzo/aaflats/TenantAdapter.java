package com.example.lorenzo.aaflats;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 27/03/2016.
 */
public class TenantAdapter extends RecyclerView.Adapter<TenantViewHolder> {

    static int REQUEST_CALL = 0;

    private Context context;
    public static Tenant mTenant = new Tenant();
    private ArrayList<Tenant> tenantList;

    public TenantAdapter(ArrayList<Tenant> tenantList, Context context) {
        this.tenantList = tenantList;
        this.context = context;

    }

    @Override
    public TenantViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View tenantView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tenant_item, viewGroup, false);
        return new TenantViewHolder(tenantView, tenantList);
    }

    @Override
    public void onBindViewHolder(TenantViewHolder tenantViewHolder, final int position) {
        mTenant = tenantList.get(position);
        tenantViewHolder.tenantSurname.setText(mTenant.getSurname());
        if (!mTenant.getMiddlename().matches("")) {
            tenantViewHolder.tenantMiddlename.setText(mTenant.getMiddlename().substring(0, 1) + ".");
        }

        tenantViewHolder.tenantAddress.setText(mTenant.getProperty());

        int charLength = mTenant.getSurname().length() + mTenant.getForename().length() + 2;
        if (charLength > 18 && mTenant.getForename().length() > 7) {
            tenantViewHolder.tenantForename.setText(mTenant.getForename().substring(0,7) + ".");
        } else {
            tenantViewHolder.tenantForename.setText(mTenant.getForename());
        }

        tenantViewHolder.tenantContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(v.getContext(), "Contact details", Toast.LENGTH_SHORT).show();
                String tenantContStr = "";
                if(!tenantList.get(position).getEmail().matches("")){
                    tenantContStr += "\nEmail: " + tenantList.get(position).getEmail();
                }
                if(!tenantList.get(position).getEmail().matches("")){
                    tenantContStr += "\n\nTel.: " + tenantList.get(position).getTelephone();
                }

                new AlertDialog.Builder(v.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Contact details for " + tenantList.get(position).getForename())
                        .setMessage(tenantContStr)
                        .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(v.getContext(), "Email", Toast.LENGTH_SHORT).show();
                            }

                        })
                        .setNegativeButton("Call", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                call(position);
                                Toast.makeText(v.getContext(), "Call", Toast.LENGTH_SHORT).show();
                            }

                        })
                        .show();
//==================================================================================================
//                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//                builder.setMessage("title")
//                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        })
//                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                AlertDialog alert = builder.create();
//                alert.show();
//                ((Button)alert.findViewById(android.R.id.button1)).setBackgroundResource(R.drawable.ic_call_black_24dp);
//                ((Button)alert.findViewById(android.R.id.button2)).setBackgroundResource(R.drawable.ic_call_black_18dp);
// =================================================================================================


//                AlertDialog.Builder builderSingle = new AlertDialog.Builder(v.getContext());
//                builderSingle.setIcon(R.drawable.ic_account_circle_black_48dp);
//                builderSingle.setTitle("Select One Name:-");
//
//                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                        v.getContext(),
//                        android.R.layout.select_dialog_singlechoice);
//                arrayAdapter.add("Hardik");
//                arrayAdapter.add("Archit");
//                arrayAdapter.add("Jignesh");
//                arrayAdapter.add("Umang");
//                arrayAdapter.add("Gatti");
//
//                builderSingle.setNegativeButton(
//                        "cancel",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                builderSingle.setAdapter(
//                        arrayAdapter,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                String strName = arrayAdapter.getItem(which);
//                                AlertDialog.Builder builderInner = new AlertDialog.Builder(
//                                        v.getContext());
//                                builderInner.setMessage(strName);
//                                builderInner.setTitle("Your Selected Item is");
//                                builderInner.setPositiveButton(
//                                        "Ok",
//                                        new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(
//                                                    DialogInterface dialog,
//                                                    int which) {
//                                                dialog.dismiss();
//                                            }
//                                        });
//                                builderInner.show();
//                            }
//                        });
//                builderSingle.show();

// =================================================================================================

            }
        });
    }

    private void call(int position) {

//        Intent in=new Intent(Intent.ACTION_CALL, Uri.parse(mTenant.getTelephone()));
        Intent in = new Intent(Intent.ACTION_CALL);
        in.setData(Uri.parse("tel:" + tenantList.get(position).getTelephone()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                try{
                    context.startActivity(in);
                }
                catch (android.content.ActivityNotFoundException ex){
                    Toast.makeText(context,"Activity not found",Toast.LENGTH_SHORT).show();
                }
            }else{
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                call(position);
            }
        } else {
            try{
                context.startActivity(in);
            }
            catch (android.content.ActivityNotFoundException ex){
                Toast.makeText(context,"Activity not found",Toast.LENGTH_SHORT).show();
            }
        }
////        Intent in=new Intent(Intent.ACTION_CALL, Uri.parse(mTenant.getTelephone()));
//        Intent in = new Intent(Intent.ACTION_CALL);
//        in.setData(Uri.parse("tel:" + tenantList.get(position).getTelephone()));

    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }
}

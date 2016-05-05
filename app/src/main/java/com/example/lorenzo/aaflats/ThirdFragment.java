package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Lorenzo on 04/05/2016.
 */
public class ThirdFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view3 = inflater.inflate(R.layout.tut_third_fragment, container, false);

        Button done3Btn = (Button) view3.findViewById(R.id.done3);
        done3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SplashActivity.class).putExtra("tutorial_viewed", true));
                getActivity().finish();
            }
        });
        return view3;
    }

    public static ThirdFragment newInstance(String page){
        ThirdFragment frag = new ThirdFragment();
        Bundle args = new Bundle();
        args.putString("tut_page", page);
        frag.setArguments(args);
        return frag;
    }
}

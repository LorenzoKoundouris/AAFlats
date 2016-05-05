package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Lorenzo on 04/05/2016.
 */
public class SecondFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view2 = inflater.inflate(R.layout.tut_second_fragment, container, false);
        final ViewPager pager = ((TutorialActivity) getActivity()).getPager();

        Button next2Btn = (Button) view2.findViewById(R.id.next2);
        next2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(2);
            }
        });
        Button skipBtn = (Button) view2.findViewById(R.id.skip2);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SplashActivity.class).putExtra("tutorial_viewed", true));
                getActivity().finish();
            }
        });
        return view2;
    }

    public static SecondFragment newInstance(String page) {
        SecondFragment frag = new SecondFragment();
        Bundle args = new Bundle();
        args.putString("tut_page", page);
        frag.setArguments(args);
        return frag;
    }
}

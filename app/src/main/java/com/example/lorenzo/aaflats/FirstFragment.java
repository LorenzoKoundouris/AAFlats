package com.example.lorenzo.aaflats;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Lorenzo on 04/05/2016.
 */
public class FirstFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view1 = inflater.inflate(R.layout.tut_first_fragment, container, false);

        final ViewPager pager = ((TutorialActivity) getActivity()).getPager();

        Button nextBtn = (Button) view1.findViewById(R.id.next1);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(1);
            }
        });

        Button skipBtn = (Button) view1.findViewById(R.id.skip1);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SplashActivity.class).putExtra("tutorial_viewed", true));
                getActivity().finish();
            }
        });

        return view1;
    }

    public static FirstFragment newInstance(String page) {
        FirstFragment frag = new FirstFragment();
        Bundle args = new Bundle();
        args.putString("tut_page", page);
        frag.setArguments(args);
        return frag;
    }
}

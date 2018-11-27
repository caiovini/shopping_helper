package com.example.caio.shoppinghelper.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.activities.MainActivity;
import com.example.caio.shoppinghelper.adapter.TabAdapter;
import com.example.caio.shoppinghelper.helper.SlidingTabLayout;

public class HomeFragment extends Fragment {

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_home, container, false);

        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.stl_tabs);
        viewPager = (ViewPager) view.findViewById(R.id.vp_page);

        //Configure sliding tabs
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(getActivity(),R.color.white));


        //Configure web pager
        TabAdapter tabAdapter = new TabAdapter(getChildFragmentManager());
        viewPager.setAdapter(tabAdapter);
        viewPager.setSoundEffectsEnabled(false);
        slidingTabLayout.setViewPager(viewPager);

        ((MainActivity) getActivity()).setOnTabChanged(new MainActivity.ChangeTab(){
            @Override
            public void onTabSelected(int direction) {



                if (direction == 1) {
                    if (viewPager.getCurrentItem() != 0) {

                        viewPager.requestFocus();
                        viewPager.refreshDrawableState();
                        viewPager.arrowScroll(View.FOCUS_LEFT);
                    }
                } else

                    viewPager.requestFocus();
                viewPager.refreshDrawableState();
                viewPager.arrowScroll(direction);
            }
        });

        return view;
    }

}


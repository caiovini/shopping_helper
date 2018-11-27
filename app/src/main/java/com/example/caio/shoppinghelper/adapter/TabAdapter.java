package com.example.caio.shoppinghelper.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.example.caio.shoppinghelper.fragments.DescriptionFragment;
import com.example.caio.shoppinghelper.fragments.ProductsFragment;


public class TabAdapter extends FragmentStatePagerAdapter {

    private String[] tabTitles = {"PRODUCTS" , "NUTRIMENTS"};

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;


        switch (position){

            case 0:
                fragment = new ProductsFragment();
                break;
            case 1:
                fragment = new DescriptionFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}

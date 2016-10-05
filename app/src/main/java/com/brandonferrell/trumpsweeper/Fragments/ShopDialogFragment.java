package com.brandonferrell.trumpsweeper.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.brandonferrell.trumpsweeper.activities.StartActivity;
import com.brandonferrell.trumpsweeper.util.NonSwipeableViewPager;
import com.brandonferrell.trumpsweeper.R;

/**
 * Created by Brandon on 4/16/2016.
 */
public class ShopDialogFragment extends DialogFragment {

    NonSwipeableViewPager shopPager;
    ImageButton shopPrev, shopNext;
    Button shopBuy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shop, container, false);

        // Remove Title Bar
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        shopBuy = (Button) rootView.findViewById(R.id.shop_buy);
        final Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/PressStart2P.ttf");
        shopBuy.setTypeface(font);

        shopPager = (NonSwipeableViewPager) rootView.findViewById(R.id.shopPager);
        shopPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));

        shopPrev = (ImageButton) rootView.findViewById(R.id.shop_prev);
        shopNext = (ImageButton) rootView.findViewById(R.id.shop_next);

        shopPrev.setEnabled(false);
        shopPrev.setColorFilter(Color.parseColor("#AA000000"));

        shopPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();

                shopPager.setCurrentItem(shopPager.getCurrentItem() - 1);

                if(shopPager.getCurrentItem() == 0) {
                    shopPrev.setEnabled(false);
                    shopPrev.setColorFilter(Color.parseColor("#AA000000"));
                }

                shopNext.setEnabled(true);
                shopNext.setColorFilter(Color.parseColor("#00000000"));

                shopBuy.setEnabled(true);

            }
        });

        shopNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();

                shopPager.setCurrentItem(shopPager.getCurrentItem() + 1);

                if(shopPager.getCurrentItem() == shopPager.getAdapter().getCount() - 1) {
                    shopNext.setEnabled(false);
                    shopNext.setColorFilter(Color.parseColor("#77000000"));

                    shopBuy.setEnabled(false);
                }

                shopPrev.setEnabled(true);
                shopPrev.setColorFilter(Color.parseColor("#00000000"));

            }
        });

        shopBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //StartActivity.bp.purchase(getActivity(), "android.test.purchased");

                switch(shopPager.getCurrentItem()) {
                    case 0:
                        StartActivity.bp.purchase(getActivity(), getResources().getString(R.string.billing_ads));
                        break;
                    case 1:
                        StartActivity.bp.purchase(getActivity(), getResources().getString(R.string.billing_china));
                        break;
                }
            }
        });

        return rootView;
    }

    public void vibrate() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(25);
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }


        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ShopItemFragment.newInstance("No Ads", .99f, "Destroy the liberal media and get the most out of your experience");
                case 1:
                    return ShopItemFragment.newInstance("Variable Chai-na", .99f, "China is a rising threat. Customize the % chance of a Chai-na and shut them down");
                case 2:
                    return ShopItemFragment.newInstance("Themes", 0f, "Coming soon if there is popular demand");
                default:
                    return null;
            }
        }

    }
}

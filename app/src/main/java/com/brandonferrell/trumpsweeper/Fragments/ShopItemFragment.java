package com.brandonferrell.trumpsweeper.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.R;

import java.text.NumberFormat;

/**
 * Created by Brandon on 5/2/2016.
 */
public class ShopItemFragment extends Fragment {

    String title, description;
    float price;

    TextView shopTitle, shopPrice, shopDesc;

    public static ShopItemFragment newInstance(String title, float price, String description) {
        Bundle args = new Bundle();

        args.putString("TITLE", title);
        args.putFloat("PRICE", price);
        args.putString("DESCRIPTION", description);

        ShopItemFragment fragment = new ShopItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = getArguments().getString("TITLE");
        price = getArguments().getFloat("PRICE");
        description = getArguments().getString("DESCRIPTION");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shop_item, container, false);

        shopTitle = (TextView) rootView.findViewById(R.id.shop_item_title);
        shopPrice = (TextView) rootView.findViewById(R.id.shop_item_price);
        shopDesc = (TextView) rootView.findViewById(R.id.shop_item_desc);

        String moneyString;

        if(price != 0) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            moneyString = formatter.format(price);
        }
        else
            moneyString = "";


        shopTitle.setText(title);
        shopPrice.setText(moneyString);
        shopDesc.setText(description);

        return rootView;
    }
}

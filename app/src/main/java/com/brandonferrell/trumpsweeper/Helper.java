package com.brandonferrell.trumpsweeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.brandonferrell.trumpsweeper.R;

/**
 * Created by Brandon on 3/28/2016.
 */
public class Helper {

    Context mContext;

    public Helper() { }

    public Helper(Context context) { mContext = context; }

    public int getMineResource(boolean explode) {
        int id = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        switch (prefs.getString("theme_preference", "default")) {
            case "default":
                if(explode)
                    id = R.drawable.trump_mine_explode;
                else
                    id = R.drawable.trump_mine;
                break;
            case "vanilla":
                if(explode)
                    id = R.drawable.mine_explode;
                else
                    id = R.drawable.mine;
                break;
        }

        return id;
    }

    public int getTileResource(boolean pressed) {
        int id = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        switch (prefs.getString("theme_preference", "default")) {
            case "default":
                if(pressed)
                    id = R.drawable.trump_btn_pressed;
                else
                    id = R.drawable.trump_btn_unpressed;
                break;
            case "vanilla":
                if(pressed)
                    id = R.drawable.btn_pressed;
                else
                    id = R.drawable.btn_unpressed;
                break;
        }

        return id;
    }

    public int getFlagResource(boolean button) {
        int id = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        switch (prefs.getString("theme_preference", "default")) {
            case "default":
                if(button)
                    id = R.drawable.btn_flag;
                else
                    id = R.drawable.flag;
                break;
            case "vanilla":
                if(button)
                    id = R.drawable.btn_flag;
                else
                    id = R.drawable.flag;
                break;
        }

        return id;
    }

    public int getCellResource(int mineCount) {
        int id = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        switch (prefs.getString("theme_preference", "default")) {
            case "default":
                switch (mineCount) {
                    case 0:
                        id = R.drawable.btn_pressed;
                        break;
                    case 1:
                        id = R.drawable.cell_1;
                        break;
                    case 2:
                        id = R.drawable.cell_2;
                        break;
                    case 3:
                        id = R.drawable.cell_3;
                        break;
                    case 4:
                        id = R.drawable.cell_4;
                        break;
                    case 5:
                        id = R.drawable.cell_5;
                        break;
                    case 6:
                        id = R.drawable.cell_6;
                        break;
                    case 7:
                        id = R.drawable.cell_7;
                        break;
                    case 8:
                        id = R.drawable.cell_8;
                        break;
                }
                break;
            case "vanilla":
                switch (mineCount) {
                    case 0:
                        id = R.drawable.btn_pressed;
                        break;
                    case 1:
                        id = R.drawable.cell_1;
                        break;
                    case 2:
                        id = R.drawable.cell_2;
                        break;
                    case 3:
                        id = R.drawable.cell_3;
                        break;
                    case 4:
                        id = R.drawable.cell_4;
                        break;
                    case 5:
                        id = R.drawable.cell_5;
                        break;
                    case 6:
                        id = R.drawable.cell_6;
                        break;
                    case 7:
                        id = R.drawable.cell_7;
                        break;
                    case 8:
                        id = R.drawable.cell_8;
                        break;
                }
                break;
        }

        return id;
    }
}

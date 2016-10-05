package com.brandonferrell.trumpsweeper.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.R;


public class GameToolbarFragment extends Fragment {

    public static TextView tv_minecount, tv_timecount;
    Typeface typeFace;
    public static ImageButton btnToupee;
    Vibrator vibr;
    public static Chronometer cr;
    Fragment gameFrag;
    int flagCount;
    long mLastStopTime;
    public SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public void incFlagCount() {
        flagCount++;
        setNewMineCount();
    }

    public void decFlagCount() {
        flagCount--;
        setNewMineCount();
    }

    public void setNewMineCount() {
        if(gameFrag != null && gameFrag instanceof SweeperImageButtonGridFragment)
            tv_minecount.setText(flagCount + "/" + ((SweeperImageButtonGridFragment) gameFrag).getMineCount(((SweeperImageButtonGridFragment) gameFrag).getSize()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_game_toolbar, container, false);
        vibr = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        gameFrag = (SweeperImageButtonGridFragment) getFragmentManager().findFragmentByTag("GRID");

        cr = (Chronometer) v.findViewById(R.id.timeCount);
        cr.setBase(SystemClock.elapsedRealtime());
        cr.start();

        tv_minecount = (TextView) v.findViewById(R.id.game_trumps_swept);
        tv_timecount = (TextView) v.findViewById(R.id.timeCount);
        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font_LED.ttf");

        tv_minecount.setTypeface(typeFace);
        tv_timecount.setTypeface(typeFace);

        btnToupee = (ImageButton) v.findViewById(R.id.toupee_button);
        btnToupee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnToupee.setSelected(!btnToupee.isSelected());

                if(btnToupee.isSelected())
                    btnToupee.setImageResource(R.drawable.toupee_button_on);
                else
                    btnToupee.setImageResource(R.drawable.toupee_button_off);

                vibr.vibrate(30);
            }
        });

        flagCount = 0;

        if(gameFrag instanceof SweeperImageButtonGridFragment)
            tv_minecount.setText(flagCount + "/" + ((SweeperImageButtonGridFragment) gameFrag).getMineCount(((SweeperImageButtonGridFragment) gameFrag).getSize()));

       /* btnSmiley = (ImageButton) v.findViewById(R.id.smiley_button);
        btnSmiley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vibrate 30 ms
                vibr.vibrate(30);


            }
        });*/

        /*btnFlag = (ImageButton) v.findViewById(R.id.btn_flag);

        btnShovel = (ImageButton) v.findViewById(R.id.btn_shovel);
        btnShovel.setPressed(true);
        btnShovel.setEnabled(false);

        btnFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibr.vibrate(20);
                btnFlag.setPressed(true);
                btnFlag.setEnabled(false);
                btnShovel.setPressed(false);
                btnShovel.setEnabled(true);
            }
        });

        btnShovel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibr.vibrate(20);
                btnShovel.setPressed(true);
                btnShovel.setEnabled(false);
                btnFlag.setPressed(false);
                btnFlag.setEnabled(true);
            }
        });*/

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = prefs.edit();

        if(prefs.getBoolean("tutorial_preference", true)) {
            TutorialInterface tutorialInterface = (TutorialInterface) getActivity();
            tutorialInterface.showTutorial();
        }

        return v;

    }

    public void restart() {
        // New Grid
        // Get the size of the old grid and make the new one the same size
        Fragment oldFragment = getFragmentManager().findFragmentByTag("GRID");
        Fragment gameFragment = new SweeperImageButtonGridFragment();

        // Set all the buttons in grid to be usable and reset
        if (oldFragment instanceof SweeperImageButtonGridFragment) {
            Bundle bundle = new Bundle();
            bundle.putInt("SIZE", ((SweeperImageButtonGridFragment) oldFragment).getSize());
            gameFragment.setArguments(bundle);
        }

        // Create new grid
        FragmentChangeListener fc = (FragmentChangeListener) getActivity();
        fc.replaceGameFragment(gameFragment);

        // Reset chronometer
        cr.setBase(SystemClock.elapsedRealtime());

        // Reset flag count
        flagCount = 0;

        // Set the counter to 0
        if(gameFrag instanceof SweeperImageButtonGridFragment)
            tv_minecount.setText(flagCount + "/" + ((SweeperImageButtonGridFragment) gameFrag).getMineCount(((SweeperImageButtonGridFragment) gameFrag).getSize()));


    }

    public interface FragmentChangeListener
    {
        void replaceToolbarFragment(Fragment fragment);
        void replaceGameFragment(Fragment fragment);
    }

    private void chronoStart()
    {
        // on first start
        if (mLastStopTime == 0)
            cr.setBase(SystemClock.elapsedRealtime());
            // on resume after pause
        else
        {
            long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
            cr.setBase(cr.getBase() + intervalOnPause);
        }

        cr.start();
    }

    private void chronoPause()
    {
        cr.stop();

        mLastStopTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onPause() {
        super.onPause();
        chronoPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        chronoStart();
    }

    public interface TutorialInterface {
        void showTutorial();
    }
}

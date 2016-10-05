package com.brandonferrell.trumpsweeper.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.activities.GameActivity;
import com.brandonferrell.trumpsweeper.R;

/**
 * Created by Brandon on 4/2/2016.
 */
public class TutorialDialogFragment extends DialogFragment implements DialogInterface.OnDismissListener {

    Button tut_interface, tut_advanced, tut_exit;
    TextView tut_main_text;
    LinearLayout tut_advanced_text;
    public SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tutorial_dialog, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = prefs.edit();

        tut_interface = (Button) rootView.findViewById(R.id.tut_interface);
        tut_advanced = (Button) rootView.findViewById(R.id.tut_advanced);
        tut_exit = (Button) rootView.findViewById(R.id.tut_exit);
        tut_main_text = (TextView) rootView.findViewById(R.id.tut_main_text);
        tut_advanced_text = (LinearLayout) rootView.findViewById(R.id.tut_advanced_text);

        tut_interface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                ((GameActivity)getActivity()).showLayout();
                dismiss();
            }
        });

        tut_advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();

                ObjectAnimator fadeOut;
                ObjectAnimator fadeIn;

                if(tut_advanced_text.getAlpha() == 0f) {
                    fadeOut = ObjectAnimator.ofFloat(tut_main_text, "alpha",  1f, 0f);
                    fadeOut.setDuration(500);
                    fadeIn = ObjectAnimator.ofFloat(tut_advanced_text, "alpha", 0f, 1f);
                    fadeIn.setDuration(500);
                }
                else {
                    fadeOut = ObjectAnimator.ofFloat(tut_advanced_text, "alpha",  1f, 0f);
                    fadeOut.setDuration(500);
                    fadeIn = ObjectAnimator.ofFloat(tut_main_text, "alpha", 0f, 1f);
                    fadeIn.setDuration(500);
                }

                final AnimatorSet mAnimationSet = new AnimatorSet();

                mAnimationSet.play(fadeIn).after(fadeOut);

                mAnimationSet.start();
            }
        });

        tut_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                dismiss();
            }
        });

        // Remove Title Bar
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return rootView;

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GameToolbarFragment.cr.setBase(SystemClock.elapsedRealtime());
        editor.putBoolean("tutorial_preference", false).commit();
    }

    public void vibrate() {
        Vibrator v = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(25);
    }
}

package com.example.brandon.rubecube.Fragments;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.rubecube.R;


public class GameEndOverlay extends Fragment {
    private static final String WIN = "WIN";

    private boolean win;

    private MediaPlayer mp;
    private ImageView trumpHead;
    private TextView header, dollar1, dollar2;
    private ObjectAnimator animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        win = getArguments().getBoolean(WIN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_game_end_overlay, container, false);

        header = (TextView) v.findViewById(R.id.tv_game_end_overlay_header);
        header.setText(getHeader());

        dollar1 = (TextView) v.findViewById(R.id.dollar_1);
        dollar2 = (TextView) v.findViewById(R.id.dollar_2);

        trumpHead = (ImageView) v.findViewById(R.id.trump_head);
        Drawable d = ContextCompat.getDrawable(getActivity(), getTrumpImage(this.win));
        trumpHead.setImageDrawable(d);

        animation = ObjectAnimator.ofFloat(dollar1, "scaleX", -1f);
        animation.setDuration(1000);
        animation.setRepeatMode(ObjectAnimator.REVERSE);
        animation.setRepeatCount(ObjectAnimator.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();

        animation = ObjectAnimator.ofFloat(dollar2, "scaleX", -1f);
        animation.setDuration(1000);
        animation.setRepeatMode(ObjectAnimator.REVERSE);
        animation.setRepeatCount(ObjectAnimator.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();

        // Create a new MediaPlayer to play this sound
        mp = MediaPlayer.create(getActivity(), getTrumpClipId(this.win));
        mp.start();


        return v;
    }

    private int getTrumpClipId(boolean win) {
        int id = 0;
        int rand;

        rand = (int) (Math.random() * 6);

        if(win) {
            switch (rand) {
                case 0:
                    id = R.raw.win_not_nice_person;
                    break;
                case 1:
                    id = R.raw.win_special_guy;
                    break;
                case 2:
                    id = R.raw.win_not_nice_person;
                    break;
                case 3:
                    id = R.raw.win_special_guy;
                    break;
                case 4:
                    id = R.raw.win_not_nice_person;
                    break;
                case 5:
                    id = R.raw.win_special_guy;
                    break;
            }
        } else {
            switch (rand) {
                case 0:
                    id = R.raw.lose_not_very_good;
                    break;
                case 1:
                    id = R.raw.lose_wall_10_feet_higher;
                    break;
                case 2:
                    id = R.raw.lose_we_have_losers;
                    break;
                case 3:
                    id = R.raw.lose_beaten_up_badly;
                    break;
                case 4:
                    id = R.raw.lose_congrats_fired;
                    break;
                case 5:
                    id = R.raw.lose_sit_down;
                    break;
            }
        }

        return id;
    }

    /**
     * Get random header from string-array resource "headers"
     * @return
     */
    private String getHeader() {
        int index = 0;

        String[] headers = getActivity().getResources().getStringArray(R.array.headers);

        index = (int) (Math.random() * headers.length);

        return headers[index];
    }

    private int getTrumpImage(boolean win) {
        int id = 0;
        int rand = (int) (Math.random() * 3);

        if(win) {
            switch (rand) {
                case 0:
                    id = R.drawable.win_trump_pointing;
                    break;
                case 1:
                    id = R.drawable.win_trump_red;
                    break;
                case 2:
                    id = R.drawable.win_trump_weird_face;
                    break;
            }
        } else {
            switch (rand) {
                case 0:
                    id = R.drawable.lose_trump_circle;
                    break;
                case 1:
                    id = R.drawable.win_trump_weird_face;
                    break;
                case 2:
                    id = R.drawable.win_trump_weird_face;
                    break;
            }
        }

        return id;
    }

}

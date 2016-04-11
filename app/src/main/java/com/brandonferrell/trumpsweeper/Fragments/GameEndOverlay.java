package com.brandonferrell.trumpsweeper.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.Activities.GameActivity;
import com.brandonferrell.trumpsweeper.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Player;


public class GameEndOverlay extends Fragment {
    private static final String WIN = "WIN";
    private static final String SIZE = "SIZE";
    private static final String TIME = "TIME";
    private static final String TOUPEES = "TOUPEES";

    private boolean win;
    private int size;
    private double time;
    private int toupees;

    private Player mPlayer;
    private GoogleApiClient mGoogleApiClient;

    private MediaPlayer mp;
    private ImageView trumpHead;
    private ImageView youStatus;
    private ObjectAnimator animation;
    private OnClipPlayedListener onClipPlayedListener;
    private AchieveInterface achieveInterface;
    private LinearLayout statsLayout, highHolder;
    private GameEndOverlay.ProfileInterface profileInterface;
    private TextView endText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        win = getArguments().getBoolean(WIN);
        size = getArguments().getInt(SIZE);
        time = getArguments().getDouble(TIME);
        toupees = getArguments().getInt(TOUPEES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_game_end_overlay, container, false);

        mPlayer = ((GameActivity) getActivity()).getPlayer();
        mGoogleApiClient = ((GameActivity) getActivity()).getGoogleApiClient();

        onClipPlayedListener = (OnClipPlayedListener) getActivity();
        achieveInterface = (AchieveInterface) getActivity();

        youStatus = (ImageView) v.findViewById(R.id.you_status);

        if(win)
            youStatus.setImageDrawable(getResources().getDrawable(R.drawable.you_win));
        else
            youStatus.setImageDrawable(getResources().getDrawable(R.drawable.you_lose));

        statsLayout = (LinearLayout) v.findViewById(R.id.stats_layout);
        endText = (TextView) v.findViewById(R.id.end_game_text);
        endText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/PressStart2P.ttf"));

        if(mPlayer == null) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) statsLayout.getLayoutParams();
            params.topMargin = 0;

            endText.setText(R.string.no_player_game_end);
            Button signIn = (Button) v.findViewById(R.id.end_game_sign_in);
            signIn.setVisibility(View.VISIBLE);
            profileInterface = (ProfileInterface) getActivity();
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileInterface.endGameSignIn();
                }
            });
        }
        else if(!win) {
            endText.setText("You're terrible.. Just terrible");
        }
        else if(mGoogleApiClient.isConnected()){
            // Add stats
            // Check/add achievements
            achieveInterface.handleAchievementsLeaders(win, size, time, toupees);

            endText.setText("New High Scores!");
            highHolder = (LinearLayout) v.findViewById(R.id.highHolder);

            TextView tv = new TextView(getActivity());
            tv.setText("Visit the leaderboards to see your new ranks");
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(13f);

            highHolder.addView(tv);

            // Display stats
            /*for(newHigh high : newHighHolder.newHighs) {
                LinearLayout ll = new LinearLayout(getActivity());
                TextView title = new TextView(getActivity());
                title.setText(high.getName());
                TextView score = new TextView(getActivity());

                if(high.getCount() > 0) {
                    score.setText("" + high.getCount());
                }
                else {
                    score.setText("" + high.getTime());
                }

                ll.addView(title);
                ll.addView(score);
                highHolder.addView(ll);
            }*/
        }

        trumpHead = (ImageView) v.findViewById(R.id.trump_head);
        Drawable d = ContextCompat.getDrawable(getActivity(), getTrumpImage(this.win));
        trumpHead.setImageDrawable(d);


        trumpHead.post(new Runnable() {
                           @Override
                           public void run() {
                               trumpHead.setVisibility(View.VISIBLE);

                               animation = ObjectAnimator.ofFloat(trumpHead, "translationX", -trumpHead.getWidth(), trumpHead.getWidth());
                               animation.setDuration(2000);
                               animation.setInterpolator(new DecelerateInterpolator());
                               animation.start();

                               AnimatorSet animatorSet1;
                               AnimatorSet animatorSet2;

                               ObjectAnimator animationIn = ObjectAnimator.ofFloat(trumpHead, "translationX", -trumpHead.getWidth(), 0);
                               animationIn.setDuration(1500);

                               ObjectAnimator animationWait = ObjectAnimator.ofFloat(trumpHead, "translationX", 0, trumpHead.getWidth());
                               animationWait.setDuration(1000);

                               ObjectAnimator animationOut = ObjectAnimator.ofFloat(trumpHead, "alpha", 1, 1);
                               animationOut.setDuration(1500);

                               ObjectAnimator animationStatsAlpha = ObjectAnimator.ofFloat(statsLayout, "alpha", 0, 1);
                               animationStatsAlpha.setDuration(1500);

                               animatorSet1 = new AnimatorSet();
                               animatorSet1.play(animationIn).before(animationWait);
                               animatorSet1.play(animationOut).after(animationWait);
                               animatorSet1.play(animationStatsAlpha).with(animationWait);
                               animatorSet1.start();

                               ObjectAnimator animationFall = ObjectAnimator.ofFloat(youStatus, "translationY", 0, 150);
                               animationFall.setDuration(3000);

                               ObjectAnimator animationRise = ObjectAnimator.ofFloat(youStatus, "translationY", 150, 100);
                               animationFall.setDuration(500);

                               ObjectAnimator animationAlpha = ObjectAnimator.ofFloat(youStatus, "alpha", 0, 1);
                               animationAlpha.setDuration(500);

                               animatorSet2 = new AnimatorSet();
                               animatorSet2.play(animationFall).with(animationAlpha);
                               animatorSet2.play(animationRise).after(animationFall);
                               animatorSet2.start();

                           }
                       }
        );

        // Get trump clip id that is not the last one played
        int trumpId = 0;
        do {
            trumpId = getTrumpClipId(this.win);
        } while(trumpId == ((GameActivity) getActivity()).getLastClipId());

        // Set the last one played to this one
        ((GameActivity) getActivity()).setLastClipId(trumpId);

        // Play clip
        onClipPlayedListener.playTrumpClip(trumpId);


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
    /*private String getHeader() {
        int index = 0;

        String[] headers = getActivity().getResources().getStringArray(R.array.headers);

        index = (int) (Math.random() * headers.length);

        return headers[index];
    }*/

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

    public void setPlayer(Player player) { mPlayer = player; }

    public interface OnClipPlayedListener {
        void playTrumpClip(int id);
    }

    public interface ProfileInterface {
        void endGameSignIn();
    }

    public interface AchieveInterface {
        void handleAchievementsLeaders(boolean win, int size, double time, int toupees);
    }

}
